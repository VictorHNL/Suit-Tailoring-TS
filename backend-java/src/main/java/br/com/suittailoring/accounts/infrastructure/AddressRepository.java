package br.com.suittailoring.accounts.infrastructure;
import br.com.suittailoring.accounts.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AddressRepository extends JpaRepository<Address,UUID> {
    List<Address> findByCustomerId(UUID customerId);
    Optional<Address> findByIdAndCustomerId(UUID id,UUID customerId);
}

