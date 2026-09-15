package br.com.suittailoring.operations.infrastructure;

import br.com.suittailoring.operations.domain.AuditEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditEvent, UUID> {}
