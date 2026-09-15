package br.com.suittailoring.cart.api;

import br.com.suittailoring.accounts.application.AccountService;
import br.com.suittailoring.cart.application.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
  public record Quantity(@Min(0) @Max(99) int quantity) {}

  private final CartService carts;
  private final AccountService accounts;

  public CartController(CartService c, AccountService a) {
    carts = c;
    accounts = a;
  }

  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public CartService.View create() {
    return carts.create();
  }

  @GetMapping
  public CartService.View get(@RequestHeader("X-Cart-Token") UUID token, Principal p) {
    return carts.get(token, customer(p));
  }

  @PutMapping("/items/{variant}")
  public CartService.View set(
      @RequestHeader("X-Cart-Token") UUID token,
      @PathVariable UUID variant,
      @Valid @RequestBody Quantity q,
      Principal p) {
    return carts.set(token, variant, q.quantity(), customer(p));
  }

  @DeleteMapping("/items/{variant}")
  public CartService.View remove(
      @RequestHeader("X-Cart-Token") UUID token, @PathVariable UUID variant, Principal p) {
    return carts.set(token, variant, 0, customer(p));
  }

  private UUID customer(Principal p) {
    return p == null ? null : accounts.current(p.getName()).getId();
  }
}
