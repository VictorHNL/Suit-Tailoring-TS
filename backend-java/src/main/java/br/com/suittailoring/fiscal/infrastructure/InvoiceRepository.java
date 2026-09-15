package br.com.suittailoring.fiscal.infrastructure;
import br.com.suittailoring.fiscal.domain.Invoice;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice,UUID>{
    Optional<Invoice> findByOrderId(UUID orderId);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select i from Invoice i where i.id=:id")
    Optional<Invoice> lock(UUID id);
}
