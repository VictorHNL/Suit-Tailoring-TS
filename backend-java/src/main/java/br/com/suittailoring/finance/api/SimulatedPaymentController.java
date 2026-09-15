package br.com.suittailoring.finance.api;

import br.com.suittailoring.finance.application.SimulatedPaymentService;
import br.com.suittailoring.orders.api.OrderView;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("dev")
@RequestMapping("/api/admin/dev/orders")
@PreAuthorize("hasRole('OWNER')")
public class SimulatedPaymentController {
  private final SimulatedPaymentService payments;

  public SimulatedPaymentController(SimulatedPaymentService payments) {
    this.payments = payments;
  }

  @PostMapping("/{id}/pay")
  public OrderView pay(@PathVariable UUID id) {
    return payments.pay(id);
  }

  @PostMapping("/{id}/refund")
  public OrderView refund(@PathVariable UUID id) {
    return payments.refund(id);
  }
}
