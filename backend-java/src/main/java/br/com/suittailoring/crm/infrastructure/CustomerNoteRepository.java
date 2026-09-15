package br.com.suittailoring.crm.infrastructure;
import br.com.suittailoring.crm.domain.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.UUID;
public interface CustomerNoteRepository extends JpaRepository<CustomerNote,UUID>{
    Page<CustomerNote> findByCustomerId(UUID customerId,Pageable pageable);
}
