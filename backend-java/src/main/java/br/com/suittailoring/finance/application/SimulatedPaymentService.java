package br.com.suittailoring.finance.application;
import br.com.suittailoring.orders.domain.PurchaseOrder;
import br.com.suittailoring.orders.api.OrderView;
import br.com.suittailoring.orders.infrastructure.OrderRepository;
import br.com.suittailoring.orders.application.OrderService;
import br.com.suittailoring.finance.domain.LedgerEntry;
import br.com.suittailoring.finance.infrastructure.LedgerRepository;
import br.com.suittailoring.fiscal.domain.Invoice;
import br.com.suittailoring.fiscal.infrastructure.InvoiceRepository;
import br.com.suittailoring.operations.application.AuditService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
@Service @Profile("dev") @Transactional
public class SimulatedPaymentService {
    private final OrderRepository orders;private final OrderService service;private final LedgerRepository ledger;
    private final InvoiceRepository invoices;private final AuditService audit;
    public SimulatedPaymentService(OrderRepository o,OrderService s,LedgerRepository l,InvoiceRepository i,AuditService a){orders=o;service=s;ledger=l;invoices=i;audit=a;}
    public OrderView pay(UUID id){
        var o=orders.lock(id).orElseThrow(EntityNotFoundException::new);
        if(o.getStatus()==PurchaseOrder.Status.PAID || o.getStatus()==PurchaseOrder.Status.SHIPPED || o.getStatus()==PurchaseOrder.Status.DELIVERED)return OrderView.of(o);
        o.pay();ledger.save(new LedgerEntry(id,LedgerEntry.Kind.PAYMENT,o.getTotal(),"Pagamento SIMULADO"));
        invoices.save(new Invoice(id));audit.record("PAYMENT_SIMULATED",id);return OrderView.of(o);
    }
    public OrderView refund(UUID id){
        var o=orders.lock(id).orElseThrow(EntityNotFoundException::new);
        if(o.getStatus()==PurchaseOrder.Status.REFUNDED)return OrderView.of(o);
        o.refund();service.release(o);ledger.save(new LedgerEntry(id,LedgerEntry.Kind.REFUND,o.getTotal(),"Estorno SIMULADO"));
        invoices.findByOrderId(id).ifPresent(Invoice::cancelPending);
        audit.record("REFUND_SIMULATED",id);return OrderView.of(o);
    }
}
