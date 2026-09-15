package br.com.suittailoring.orders.infrastructure;

import br.com.suittailoring.orders.domain.PurchaseOrder;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface OrderRepository extends JpaRepository<PurchaseOrder, UUID> {
  Optional<PurchaseOrder> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);

  Page<PurchaseOrder> findByCustomerId(UUID customerId, Pageable pageable);

  List<PurchaseOrder> findTop100ByStatusAndExpiresAtBefore(
      PurchaseOrder.Status status, Instant now);

  long countByStatus(PurchaseOrder.Status status);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from PurchaseOrder o where o.id=:id")
  Optional<PurchaseOrder> lock(UUID id);
}
