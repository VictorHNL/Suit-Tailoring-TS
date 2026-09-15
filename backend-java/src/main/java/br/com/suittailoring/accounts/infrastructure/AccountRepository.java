package br.com.suittailoring.accounts.infrastructure;

import br.com.suittailoring.accounts.domain.Account;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
  Optional<Account> findByEmail(String email);

  org.springframework.data.domain.Page<Account> findByRole(
      Account.Role role, org.springframework.data.domain.Pageable pageable);
}
