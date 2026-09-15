package br.com.suittailoring.finance.api;
import br.com.suittailoring.finance.domain.LedgerEntry;
import br.com.suittailoring.finance.infrastructure.LedgerRepository;
import br.com.suittailoring.fiscal.infrastructure.InvoiceRepository;
import br.com.suittailoring.fiscal.domain.Invoice;
import br.com.suittailoring.fiscal.application.FiscalService;
import br.com.suittailoring.operations.application.AuditService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
@RestController @RequestMapping("/api/admin") @PreAuthorize("hasAnyRole('OWNER','FINANCE')")
public class FinanceController {
    public record Expense(@NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal amount,@NotBlank @Size(max=500) String description){}
    public record Summary(BigDecimal received,BigDecimal refunded,BigDecimal expenses,BigDecimal balance,String currency){}
    private final LedgerRepository ledger;private final InvoiceRepository invoices;private final FiscalService fiscal;private final AuditService audit;
    public FinanceController(LedgerRepository l,InvoiceRepository i,FiscalService f,AuditService a){ledger=l;invoices=i;fiscal=f;audit=a;}
    @GetMapping("/finance/summary") public Summary summary(){
        var received=ledger.sum(LedgerEntry.Kind.PAYMENT);var refunded=ledger.sum(LedgerEntry.Kind.REFUND);var expenses=ledger.sum(LedgerEntry.Kind.EXPENSE);
        return new Summary(received,refunded,expenses,received.subtract(refunded).subtract(expenses),"BRL");
    }
    @GetMapping("/finance/entries") public Page<LedgerEntry> entries(@RequestParam(defaultValue="0") int page){return ledger.findAll(page(page));}
    @PostMapping("/finance/expenses") @Transactional public LedgerEntry expense(@Valid @RequestBody Expense input){
        var entry=ledger.save(new LedgerEntry(null,LedgerEntry.Kind.EXPENSE,input.amount(),input.description()));audit.record("EXPENSE_CREATED",entry.getId());return entry;
    }
    @GetMapping("/invoices") public Page<Invoice> invoices(@RequestParam(defaultValue="0") int page){return invoices.findAll(page(page));}
    @PostMapping("/invoices/{id}/retry") public Invoice retry(@PathVariable UUID id){return fiscal.retry(id);}
    private Pageable page(int number){return PageRequest.of(Math.max(0,number),20,Sort.by("createdAt").descending());}
}
