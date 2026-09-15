package br.com.suittailoring.operations.infrastructure;
import br.com.suittailoring.operations.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface AuditRepository extends JpaRepository<AuditEvent,UUID>{}
