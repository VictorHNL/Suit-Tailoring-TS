package br.com.suittailoring.fiscal.application;

import br.com.suittailoring.fiscal.domain.Invoice;
import br.com.suittailoring.fiscal.infrastructure.InvoiceRepository;
import br.com.suittailoring.operations.application.AuditService;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalService {
  private final InvoiceRepository invoices;
  private final FiscalGateway gateway;
  private final AuditService audit;

  public FiscalService(InvoiceRepository i, FiscalGateway g, AuditService a) {
    invoices = i;
    gateway = g;
    audit = a;
  }

  @Transactional
  public Invoice retry(UUID id) {
    return process(id, false);
  }

  @Transactional
  public Invoice retryScheduled(UUID id) {
    return process(id, true);
  }

  private Invoice process(UUID id, boolean scheduled) {
    var invoice = invoices.lock(id).orElseThrow(EntityNotFoundException::new);
    if (invoice.getStatus() != Invoice.Status.PENDING_PROVIDER) return invoice;
    if (scheduled
        && (invoice.getAttempts() >= 5
            || invoice.getNextAttemptAt().isAfter(java.time.Instant.now()))) return invoice;
    try {
      invoice.issue(gateway.issue(invoice.getId(), invoice.getOrderId()));
    } catch (UnsupportedOperationException e) {
      invoice.unavailable();
    }
    audit.record("INVOICE_ATTEMPTED", id);
    return invoice;
  }
}
