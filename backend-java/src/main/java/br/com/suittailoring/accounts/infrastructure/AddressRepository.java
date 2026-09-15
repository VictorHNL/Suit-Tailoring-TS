package br.com.suittailoring.accounts.infrastructure;

import br.com.suittailoring.accounts.domain.Address;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, UUID> {
  List<Address> findByCustomerId(UUID customerId);

  Optional<Address> findByIdAndCustomerId(UUID id, UUID customerId);
}
