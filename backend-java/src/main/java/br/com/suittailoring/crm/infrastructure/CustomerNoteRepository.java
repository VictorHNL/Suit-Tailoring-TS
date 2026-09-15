package br.com.suittailoring.crm.infrastructure;

import br.com.suittailoring.crm.domain.CustomerNote;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerNoteRepository extends JpaRepository<CustomerNote, UUID> {
  Page<CustomerNote> findByCustomerId(UUID customerId, Pageable pageable);
}
