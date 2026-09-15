package br.com.suittailoring.fiscal.infrastructure;

import br.com.suittailoring.fiscal.domain.Invoice;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  Optional<Invoice> findByOrderId(UUID orderId);

  List<Invoice> findTop100ByStatusAndAttemptsLessThanAndNextAttemptAtBefore(
      Invoice.Status status, int attempts, java.time.Instant now);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Invoice i where i.id=:id")
  Optional<Invoice> lock(UUID id);
}
