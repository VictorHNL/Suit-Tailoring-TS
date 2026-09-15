package br.com.suittailoring.crm.api;
import br.com.suittailoring.accounts.api.AuthController.Profile;
import br.com.suittailoring.accounts.domain.Account;
import br.com.suittailoring.accounts.infrastructure.AccountRepository;
import br.com.suittailoring.crm.domain.CustomerNote;
import br.com.suittailoring.crm.infrastructure.CustomerNoteRepository;
import br.com.suittailoring.orders.application.OrderService;
import br.com.suittailoring.orders.api.OrderView;
import br.com.suittailoring.operations.application.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.UUID;
@RestController @RequestMapping("/api/admin/crm/customers") @PreAuthorize("hasRole('OWNER')")
public class CrmController {
    public record Note(@NotBlank @Size(max=2000) String content){}
    private final AccountRepository accounts;private final CustomerNoteRepository notes;private final OrderService orders;private final AuditService audit;
    public CrmController(AccountRepository a,CustomerNoteRepository n,OrderService o,AuditService log){accounts=a;notes=n;orders=o;audit=log;}
    @GetMapping public Page<Profile> customers(@RequestParam(defaultValue="0")int page){return accounts.findByRole(Account.Role.CUSTOMER,PageRequest.of(Math.max(0,page),20,Sort.by("name"))).map(Profile::of);}
    @GetMapping("/{id}/orders") public Page<OrderView> orders(@PathVariable UUID id,@RequestParam(defaultValue="0")int page){customer(id);return orders.list(id,page);}
    @GetMapping("/{id}/notes") public Page<CustomerNote> notes(@PathVariable UUID id,@RequestParam(defaultValue="0")int page){customer(id);return notes.findByCustomerId(id,PageRequest.of(Math.max(0,page),20,Sort.by("createdAt").descending()));}
    @PostMapping("/{id}/notes") @Transactional public CustomerNote note(@PathVariable UUID id,@Valid @RequestBody Note input,Principal p){
        customer(id);var note=notes.save(new CustomerNote(id,input.content(),p.getName()));audit.record("CRM_NOTE_CREATED",id);return note;
    }
    private void customer(UUID id){accounts.findById(id).filter(a->a.getRole()==Account.Role.CUSTOMER).orElseThrow(jakarta.persistence.EntityNotFoundException::new);}
}
