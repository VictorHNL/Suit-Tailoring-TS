package br.com.suittailoring.operations.api;
import br.com.suittailoring.accounts.api.AuthController.Profile;
import br.com.suittailoring.accounts.domain.Account;
import br.com.suittailoring.accounts.infrastructure.AccountRepository;
import br.com.suittailoring.catalog.infrastructure.VariantRepository;
import br.com.suittailoring.orders.infrastructure.OrderRepository;
import br.com.suittailoring.orders.domain.PurchaseOrder;
import br.com.suittailoring.operations.domain.AuditEvent;
import br.com.suittailoring.operations.infrastructure.AuditRepository;
import br.com.suittailoring.operations.application.AuditService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('OWNER')")
public class AdministrationController {
    public record Staff(@NotBlank @Size(max=150) String name,@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(min=12,max=72) String password,@NotNull Account.Role role){}
    private final AccountRepository accounts;private final VariantRepository variants;private final OrderRepository orders;
    private final AuditRepository events;private final AuditService audit;private final PasswordEncoder encoder;
    public AdministrationController(AccountRepository a,VariantRepository v,OrderRepository o,AuditRepository e,AuditService log,PasswordEncoder p){accounts=a;variants=v;orders=o;events=e;audit=log;encoder=p;}
    @GetMapping("/dashboard") public Map<String,Long> dashboard(){return Map.of("orders",orders.count(),"awaitingPayment",orders.countByStatus(PurchaseOrder.Status.AWAITING_PAYMENT),"lowStockVariants",variants.countByStockLessThanEqual(5));}
    @GetMapping("/users") public Page<Profile> users(@RequestParam(defaultValue="0")int page){return accounts.findAll(PageRequest.of(Math.max(0,page),20,Sort.by("name"))).map(Profile::of);}
    @PostMapping("/users") @Transactional public Profile staff(@Valid @RequestBody Staff i){
        if(i.role()!=Account.Role.CATALOG && i.role()!=Account.Role.FINANCE)throw new IllegalArgumentException("Equipe aceita perfis CATALOG ou FINANCE");
        if(i.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException("Senha deve ter no máximo 72 bytes");
        var account=accounts.save(new Account(i.name(),i.email(),encoder.encode(i.password()),i.role()));audit.record("STAFF_CREATED",account.getId());return Profile.of(account);
    }
    @GetMapping("/audit") public Page<AuditEvent> audit(@RequestParam(defaultValue="0")int page){return events.findAll(PageRequest.of(Math.max(0,page),20,Sort.by("createdAt").descending()));}
}
