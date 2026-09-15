package br.com.suittailoring.orders.api;

import br.com.suittailoring.orders.application.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('OWNER')")
public class AdminOrderController {
  public record Tracking(@NotBlank @Size(max = 150) String trackingCode) {}

  private final OrderService orders;

  public AdminOrderController(OrderService orders) {
    this.orders = orders;
  }

  @GetMapping
  public Page<OrderView> list(@RequestParam(defaultValue = "0") int page) {
    return orders.list(null, page);
  }

  @GetMapping("/{id}")
  public OrderView detail(@PathVariable UUID id) {
    return orders.detail(id, null);
  }

  @PostMapping("/{id}/cancel")
  public OrderView cancel(@PathVariable UUID id) {
    return orders.cancel(id, null);
  }

  @PostMapping("/{id}/ship")
  public OrderView ship(@PathVariable UUID id, @Valid @RequestBody Tracking tracking) {
    return orders.ship(id, tracking.trackingCode());
  }

  @PostMapping("/{id}/deliver")
  public OrderView deliver(@PathVariable UUID id) {
    return orders.deliver(id);
  }
}
