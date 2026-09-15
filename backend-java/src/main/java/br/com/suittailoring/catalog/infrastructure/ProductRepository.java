package br.com.suittailoring.catalog.infrastructure;
import br.com.suittailoring.catalog.domain.model.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;
public interface ProductRepository extends JpaRepository<Product,UUID> {
    @Query("select p from Product p where p.status = :status and (:audience is null or p.audience = :audience or p.audience = br.com.suittailoring.catalog.domain.model.Product$Audience.UNISEX) and (:category is null or lower(p.category) = lower(:category)) and (:search is null or lower(p.name) like lower(concat('%', :search, '%')))")
    Page<Product> search(Product.Status status,Product.Audience audience,String category,String search,Pageable pageable);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id")
    Optional<Product> lock(UUID id);
}

