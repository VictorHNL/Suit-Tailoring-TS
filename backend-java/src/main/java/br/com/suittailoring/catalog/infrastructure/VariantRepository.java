package br.com.suittailoring.catalog.infrastructure;

import br.com.suittailoring.catalog.domain.model.Variant;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface VariantRepository extends JpaRepository<Variant, UUID> {
  List<Variant> findByProductIdOrderBySku(UUID productId);

  long countByStockLessThanEqual(int stock);

  @Query("select distinct v.product.id from Variant v where v.id in :ids")
  List<UUID> productIds(List<UUID> ids);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select v from Variant v where v.id=:id")
  Optional<Variant> lock(UUID id);
}
