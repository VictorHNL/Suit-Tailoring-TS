package br.com.suittailoring.catalog.infrastructure;

import br.com.suittailoring.catalog.domain.model.Product;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ProductRepository
    extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
  default Page<Product> search(
      Product.Status status,
      Product.Audience audience,
      String category,
      String search,
      Pageable pageable) {
    org.springframework.data.jpa.domain.Specification<Product> specification =
        (root, query, cb) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          predicates.add(cb.equal(root.get("status"), status));
          if (audience != null)
            predicates.add(
                cb.or(
                    cb.equal(root.get("audience"), audience),
                    cb.equal(root.get("audience"), Product.Audience.UNISEX)));
          if (category != null && !category.isBlank())
            predicates.add(
                cb.equal(cb.lower(root.get("category")), category.toLowerCase(Locale.ROOT)));
          if (search != null && !search.isBlank())
            predicates.add(
                cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase(Locale.ROOT) + "%"));
          return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return findAll(specification, pageable);
  }

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id=:id")
  Optional<Product> lock(UUID id);
}
