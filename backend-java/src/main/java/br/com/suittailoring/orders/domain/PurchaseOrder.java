package br.com.suittailoring.orders.domain;

import br.com.suittailoring.shared.BusinessException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(
    name = "purchase_orders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "idempotency_key"}))
public class PurchaseOrder {
  public enum Status {
    AWAITING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
  }

  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false)
  private UUID customerId;

  @Column(nullable = false)
  private UUID cartId;

  @Column(nullable = false)
  private UUID addressId;

  @Column(nullable = false, length = 80)
  private String idempotencyKey;

  @Column(nullable = false, length = 1000)
  private String shippingAddress;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal total;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status = Status.AWAITING_PAYMENT;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant expiresAt = Instant.now().plusSeconds(1800);

  private String trackingCode;

  @ElementCollection
  @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
  @OrderColumn(name = "position")
  private List<OrderLine> lines = new ArrayList<>();

  protected PurchaseOrder() {}

  public PurchaseOrder(
      UUID customerId,
      UUID cartId,
      UUID addressId,
      String key,
      String address,
      List<OrderLine> lines) {
    this.customerId = customerId;
    this.cartId = cartId;
    this.addressId = addressId;
    this.idempotencyKey = key;
    this.shippingAddress = address;
    this.lines.addAll(lines);
    this.total = lines.stream().map(OrderLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public void pay() {
    require(Status.AWAITING_PAYMENT);
    if (!expiresAt.isAfter(Instant.now())) throw new BusinessException("Pedido expirado");
    status = Status.PAID;
  }

  public void cancel() {
    require(Status.AWAITING_PAYMENT);
    status = Status.CANCELLED;
  }

  public void refund() {
    require(Status.PAID);
    status = Status.REFUNDED;
  }

  public void ship(String tracking) {
    require(Status.PAID);
    if (tracking == null || tracking.isBlank())
      throw new BusinessException("Informe o rastreamento");
    trackingCode = tracking;
    status = Status.SHIPPED;
  }

  public void deliver() {
    require(Status.SHIPPED);
    status = Status.DELIVERED;
  }

  private void require(Status expected) {
    if (status != expected) throw new BusinessException("Transição inválida a partir de " + status);
  }

  public UUID getId() {
    return id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public UUID getCartId() {
    return cartId;
  }

  public UUID getAddressId() {
    return addressId;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public Status getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public String getTrackingCode() {
    return trackingCode;
  }

  public List<OrderLine> getLines() {
    return List.copyOf(lines);
  }
}
