package br.com.suittailoring.finance.infrastructure;

import br.com.suittailoring.finance.domain.LedgerEntry;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface LedgerRepository extends JpaRepository<LedgerEntry, UUID> {
  @Query("select coalesce(sum(e.amount),0) from LedgerEntry e where e.kind=:kind")
  BigDecimal sum(LedgerEntry.Kind kind);

  long countByOrderIdAndKind(UUID orderId, LedgerEntry.Kind kind);
}
