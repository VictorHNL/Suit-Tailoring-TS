package br.com.suittailoring.operations.application;
import br.com.suittailoring.operations.domain.AuditEvent;
import br.com.suittailoring.operations.infrastructure.AuditRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service
public class AuditService {
    private final AuditRepository events;
    public AuditService(AuditRepository events){this.events=events;}
    public void record(String action,UUID id){
        var auth=SecurityContextHolder.getContext().getAuthentication();
        events.save(new AuditEvent(auth==null?"system":auth.getName(),action,id));
    }
}
