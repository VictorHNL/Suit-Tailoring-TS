package br.com.suittailoring.catalog.infrastructure;
import br.com.suittailoring.catalog.domain.model.Variant;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface VariantRepository extends JpaRepository<Variant,UUID> {
    List<Variant> findByProductIdOrderBySku(UUID productId);
    long countByStockLessThanEqual(int stock);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select v from Variant v where v.id=:id")
    Optional<Variant> lock(UUID id);
}

