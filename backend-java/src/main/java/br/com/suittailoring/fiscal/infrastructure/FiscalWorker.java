package br.com.suittailoring.fiscal.infrastructure;

import br.com.suittailoring.fiscal.application.FiscalService;
import br.com.suittailoring.fiscal.domain.Invoice;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Profile("worker")
@EnableScheduling
public class FiscalWorker {
  private final InvoiceRepository invoices;
  private final FiscalService fiscal;

  public FiscalWorker(InvoiceRepository i, FiscalService f) {
    invoices = i;
    fiscal = f;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 10000)
  public void process() {
    for (var invoice :
        invoices.findTop100ByStatusAndAttemptsLessThanAndNextAttemptAtBefore(
            Invoice.Status.PENDING_PROVIDER, 5, Instant.now())) {
      try {
        fiscal.retryScheduled(invoice.getId());
      } catch (RuntimeException e) {
        org.slf4j.LoggerFactory.getLogger(getClass())
            .warn("Falha ao processar solicitação fiscal {}", invoice.getId());
      }
    }
  }
}
