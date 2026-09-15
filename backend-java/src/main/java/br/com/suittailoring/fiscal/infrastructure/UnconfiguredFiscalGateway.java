package br.com.suittailoring.fiscal.infrastructure;
import br.com.suittailoring.fiscal.application.FiscalGateway;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component
public class UnconfiguredFiscalGateway implements FiscalGateway {
    public String issue(UUID invoiceId,UUID orderId){throw new UnsupportedOperationException("Provedor fiscal não configurado");}
}
