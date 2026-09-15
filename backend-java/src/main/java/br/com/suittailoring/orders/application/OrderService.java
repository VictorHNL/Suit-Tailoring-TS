package br.com.suittailoring.orders.application;

import br.com.suittailoring.accounts.infrastructure.AddressRepository;
import br.com.suittailoring.cart.infrastructure.CartRepository;
import br.com.suittailoring.catalog.domain.model.*;
import br.com.suittailoring.catalog.infrastructure.*;
import br.com.suittailoring.operations.application.AuditService;
import br.com.suittailoring.orders.api.OrderView;
import br.com.suittailoring.orders.domain.*;
import br.com.suittailoring.orders.infrastructure.OrderRepository;
import br.com.suittailoring.shared.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
  private final OrderRepository orders;
  private final CartRepository carts;
  private final VariantRepository variants;
  private final ProductRepository products;
  private final AddressRepository addresses;
  private final AuditService audit;

  public OrderService(
      OrderRepository o,
      CartRepository c,
      VariantRepository v,
      ProductRepository p,
      AddressRepository a,
      AuditService log) {
    orders = o;
    carts = c;
    variants = v;
    products = p;
    addresses = a;
    audit = log;
  }

  public OrderView checkout(UUID customer, UUID cartId, UUID addressId, String key) {
    var cart = carts.lock(cartId).orElseThrow(EntityNotFoundException::new);
    cart.checkAccess(customer);
    var previous = orders.findByCustomerIdAndIdempotencyKey(customer, key);
    if (previous.isPresent()) {
      var o = previous.get();
      if (!o.getCartId().equals(cartId) || !o.getAddressId().equals(addressId))
        throw new BusinessException("Chave de idempotência já utilizada com outros dados");
      return OrderView.of(o);
    }
    var address =
        addresses
            .findByIdAndCustomerId(addressId, customer)
            .orElseThrow(EntityNotFoundException::new);
    cart.consume(customer);
    // All checkouts lock product rows first, then variant rows in UUID order.
    // This also serializes checkout against product archival/price edits.
    List<UUID> variantIds = cart.getItems().keySet().stream().sorted().toList();
    variants.productIds(variantIds).stream()
        .sorted()
        .forEach(id -> products.lock(id).orElseThrow(EntityNotFoundException::new));
    List<OrderLine> lines = new ArrayList<>();
    for (UUID id : variantIds) {
      Variant v = variants.lock(id).orElseThrow(EntityNotFoundException::new);
      if (v.getProduct().getStatus() != Product.Status.PUBLISHED)
        throw new BusinessException("Produto indisponível");
      int quantity = cart.getItems().get(id);
      v.reserve(quantity);
      lines.add(
          new OrderLine(
              id, v.getProduct().getName(), v.getSku(), quantity, v.getProduct().sellingPrice()));
    }
    var order =
        orders.save(new PurchaseOrder(customer, cartId, addressId, key, address.snapshot(), lines));
    audit.record("ORDER_CREATED", order.getId());
    return OrderView.of(order);
  }

  @Transactional(readOnly = true)
  public Page<OrderView> list(UUID customer, int page) {
    var pageable = PageRequest.of(Math.max(0, page), 20, Sort.by("createdAt").descending());
    return (customer == null
            ? orders.findAll(pageable)
            : orders.findByCustomerId(customer, pageable))
        .map(OrderView::of);
  }

  @Transactional(readOnly = true)
  public OrderView detail(UUID id, UUID customer) {
    var o = orders.findById(id).orElseThrow(EntityNotFoundException::new);
    authorize(o, customer);
    return OrderView.of(o);
  }

  public OrderView cancel(UUID id, UUID customer) {
    var o = orders.lock(id).orElseThrow(EntityNotFoundException::new);
    authorize(o, customer);
    if (o.getStatus() == PurchaseOrder.Status.CANCELLED) return OrderView.of(o);
    o.cancel();
    release(o);
    audit.record("ORDER_CANCELLED", id);
    return OrderView.of(o);
  }

  public OrderView ship(UUID id, String tracking) {
    var o = orders.lock(id).orElseThrow(EntityNotFoundException::new);
    o.ship(tracking);
    audit.record("ORDER_SHIPPED", id);
    return OrderView.of(o);
  }

  public OrderView deliver(UUID id) {
    var o = orders.lock(id).orElseThrow(EntityNotFoundException::new);
    o.deliver();
    audit.record("ORDER_DELIVERED", id);
    return OrderView.of(o);
  }

  public void expire(UUID id) {
    var o = orders.lock(id).orElseThrow(EntityNotFoundException::new);
    if (o.getStatus() == PurchaseOrder.Status.AWAITING_PAYMENT
        && !o.getExpiresAt().isAfter(Instant.now())) {
      o.cancel();
      release(o);
      audit.record("ORDER_EXPIRED", id);
    }
  }

  public void release(PurchaseOrder o) {
    o.getLines().stream()
        .sorted(Comparator.comparing(OrderLine::getVariantId))
        .forEach(
            l ->
                variants
                    .lock(l.getVariantId())
                    .orElseThrow(EntityNotFoundException::new)
                    .release(l.getQuantity()));
  }

  private void authorize(PurchaseOrder o, UUID customer) {
    if (customer != null && !o.getCustomerId().equals(customer))
      throw new AccessDeniedException("Pedido de outro cliente");
  }
}
