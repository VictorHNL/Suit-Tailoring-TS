package br.com.suittailoring;

import static org.junit.jupiter.api.Assertions.*;

import br.com.suittailoring.accounts.domain.*;
import br.com.suittailoring.accounts.infrastructure.*;
import br.com.suittailoring.cart.application.CartService;
import br.com.suittailoring.catalog.domain.model.*;
import br.com.suittailoring.catalog.infrastructure.*;
import br.com.suittailoring.finance.application.SimulatedPaymentService;
import br.com.suittailoring.finance.domain.LedgerEntry;
import br.com.suittailoring.finance.infrastructure.LedgerRepository;
import br.com.suittailoring.fiscal.domain.Invoice;
import br.com.suittailoring.fiscal.infrastructure.InvoiceRepository;
import br.com.suittailoring.orders.application.OrderService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class PaymentIntegrationTest {
  @Autowired AccountRepository accounts;
  @Autowired AddressRepository addresses;
  @Autowired ProductRepository products;
  @Autowired VariantRepository variants;
  @Autowired CartService carts;
  @Autowired OrderService orders;
  @Autowired SimulatedPaymentService payments;
  @Autowired LedgerRepository ledger;
  @Autowired InvoiceRepository invoices;

  @Test
  void paymentAndRefundAreIdempotentAndPreserveLedger() {
    var user =
        accounts.save(
            new Account(
                "Cliente", UUID.randomUUID() + "@example.com", "unused", Account.Role.CUSTOMER));
    var address = addresses.save(new Address(user.getId(), "Rua 1", "São Paulo", "SP", "01001000"));
    var p = new Product("Camisa", "Descrição");
    p.update(
        "Camisa",
        "Descrição",
        UUID.randomUUID().toString(),
        "Camisas",
        null,
        null,
        Product.Audience.UNISEX,
        Product.BodyPart.TORSO,
        new BigDecimal("150.00"),
        null,
        List.of("https://example.com/photo.jpg"),
        null);
    p.publish();
    products.save(p);
    var variant = variants.save(new Variant(p, UUID.randomUUID().toString(), "M", "Branca", 2));
    var cart = carts.create();
    carts.set(cart.token(), variant.getId(), 1, null);
    var order = orders.checkout(user.getId(), cart.token(), address.getId(), "payment-test");
    payments.pay(order.id());
    payments.pay(order.id());
    assertEquals(1, ledger.countByOrderIdAndKind(order.id(), LedgerEntry.Kind.PAYMENT));
    assertEquals(
        Invoice.Status.PENDING_PROVIDER,
        invoices.findByOrderId(order.id()).orElseThrow().getStatus());
    payments.refund(order.id());
    payments.refund(order.id());
    assertEquals(1, ledger.countByOrderIdAndKind(order.id(), LedgerEntry.Kind.REFUND));
    assertEquals(2, variant.getStock());
    assertEquals(
        Invoice.Status.CANCELLED, invoices.findByOrderId(order.id()).orElseThrow().getStatus());
  }
}
