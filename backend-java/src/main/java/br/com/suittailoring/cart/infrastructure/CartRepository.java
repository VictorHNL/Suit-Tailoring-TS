package br.com.suittailoring.cart.infrastructure;
import br.com.suittailoring.cart.domain.Cart;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface CartRepository extends JpaRepository<Cart,UUID>{
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select c from Cart c where c.id=:id")
    Optional<Cart> lock(UUID id);
}
