package br.com.suittailoring;

import static org.junit.jupiter.api.Assertions.*;

import br.com.suittailoring.accounts.domain.*;
import br.com.suittailoring.accounts.infrastructure.*;
import br.com.suittailoring.cart.application.CartService;
import br.com.suittailoring.catalog.domain.model.*;
import br.com.suittailoring.catalog.infrastructure.*;
import br.com.suittailoring.orders.application.OrderService;
import br.com.suittailoring.orders.infrastructure.OrderRepository;
import br.com.suittailoring.shared.BusinessException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class CheckoutIntegrationTest {
  @Autowired AccountRepository accounts;
  @Autowired AddressRepository addresses;
  @Autowired ProductRepository products;
  @Autowired VariantRepository variants;
  @Autowired CartService carts;
  @Autowired OrderService orders;
  @Autowired OrderRepository orderRepository;
  @Autowired PlatformTransactionManager transactionManager;
  UUID customer, address, variant;

  @BeforeEach
  void prepare() {
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            tx -> {
              var account =
                  accounts.save(
                      new Account(
                          "Teste",
                          UUID.randomUUID() + "@example.com",
                          "unused",
                          Account.Role.CUSTOMER));
              customer = account.getId();
              address =
                  addresses
                      .save(new Address(customer, "Rua 1", "São Paulo", "SP", "01001000"))
                      .getId();
              Product p = new Product("Blazer", "Descrição");
              p.update(
                  "Blazer",
                  "Descrição",
                  UUID.randomUUID().toString(),
                  "Blazer",
                  null,
                  null,
                  Product.Audience.UNISEX,
                  Product.BodyPart.TORSO,
                  new BigDecimal("100.00"),
                  null,
                  List.of("https://example.com/photo.jpg"),
                  null);
              p.publish();
              products.save(p);
              variant =
                  variants
                      .save(new Variant(p, UUID.randomUUID().toString(), "M", "Preto", 1))
                      .getId();
            });
  }

  UUID cart() {
    UUID id = carts.create().token();
    carts.set(id, variant, 1, null);
    return id;
  }

  @Test
  void checkoutIsIdempotentAndCancellationRestocksOnlyOnce() {
    UUID token = cart();
    var first = orders.checkout(customer, token, address, "retry-1");
    var second = orders.checkout(customer, token, address, "retry-1");
    assertEquals(first.id(), second.id());
    assertEquals(new BigDecimal("100.00"), first.total());
    assertEquals(0, variants.findById(variant).orElseThrow().getStock());
    orders.cancel(first.id(), customer);
    orders.cancel(first.id(), customer);
    assertEquals(1, variants.findById(variant).orElseThrow().getStock());
  }

  @Test
  void anotherCustomerCannotReadOrder() {
    var order = orders.checkout(customer, cart(), address, "private-1");
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> orders.detail(order.id(), UUID.randomUUID()));
  }

  @Test
  void failedCheckoutRollsBackCartAndStock() {
    UUID token = cart();
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(tx -> variants.lock(variant).orElseThrow().setStock(0));
    assertThrows(
        BusinessException.class, () -> orders.checkout(customer, token, address, "no-stock"));
    assertFalse(carts.get(token, null).consumed());
    assertTrue(orderRepository.findByCustomerIdAndIdempotencyKey(customer, "no-stock").isEmpty());
  }

  @Test
  void concurrentCheckoutsCannotOversell() throws Exception {
    UUID a = cart(), b = cart();
    CountDownLatch start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var one = executor.submit(() -> buy(a, start));
      var two = executor.submit(() -> buy(b, start));
      start.countDown();
      assertEquals(1, one.get(20, TimeUnit.SECONDS) + two.get(20, TimeUnit.SECONDS));
      assertEquals(0, variants.findById(variant).orElseThrow().getStock());
    }
  }

  int buy(UUID token, CountDownLatch start) throws InterruptedException {
    start.await();
    try {
      orders.checkout(customer, token, address, token.toString());
      return 1;
    } catch (BusinessException expected) {
      return 0;
    }
  }

  @Test
  void unisexIsVisibleInBothFilters() {
    assertTrue(
        products
                .search(
                    Product.Status.PUBLISHED,
                    Product.Audience.WOMEN,
                    null,
                    "Blazer",
                    org.springframework.data.domain.PageRequest.of(0, 100))
                .getTotalElements()
            > 0);
    assertTrue(
        products
                .search(
                    Product.Status.PUBLISHED,
                    Product.Audience.MEN,
                    null,
                    "Blazer",
                    org.springframework.data.domain.PageRequest.of(0, 100))
                .getTotalElements()
            > 0);
  }
}
