package br.com.suittailoring.orders.api;

import br.com.suittailoring.accounts.application.AccountService;
import br.com.suittailoring.orders.application.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  public record Checkout(
      @NotNull UUID addressId, @NotBlank @Size(max = 80) String idempotencyKey) {}

  private final OrderService orders;
  private final AccountService accounts;

  public OrderController(OrderService o, AccountService a) {
    orders = o;
    accounts = a;
  }

  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public OrderView checkout(
      @RequestHeader("X-Cart-Token") UUID token, @Valid @RequestBody Checkout i, Principal p) {
    return orders.checkout(
        accounts.current(p.getName()).getId(), token, i.addressId(), i.idempotencyKey());
  }

  @GetMapping
  public Page<OrderView> list(@RequestParam(defaultValue = "0") int page, Principal p) {
    return orders.list(accounts.current(p.getName()).getId(), page);
  }

  @GetMapping("/{id}")
  public OrderView detail(@PathVariable UUID id, Principal p) {
    return orders.detail(id, accounts.current(p.getName()).getId());
  }

  @PostMapping("/{id}/cancel")
  public OrderView cancel(@PathVariable UUID id, Principal p) {
    return orders.cancel(id, accounts.current(p.getName()).getId());
  }
}
