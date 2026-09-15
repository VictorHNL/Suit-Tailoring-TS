package br.com.suittailoring;
import br.com.suittailoring.fiscal.application.*;
import br.com.suittailoring.fiscal.domain.Invoice;
import br.com.suittailoring.fiscal.infrastructure.InvoiceRepository;
import br.com.suittailoring.operations.application.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class FiscalServiceTest {
    @Mock InvoiceRepository invoices;
    @Mock FiscalGateway gateway;
    @Mock AuditService audit;
    @InjectMocks FiscalService fiscal;
    @Test void missingProviderNeverPretendsToIssueAnInvoice(){
        var invoice=new Invoice(UUID.randomUUID());
        when(invoices.lock(invoice.getId())).thenReturn(Optional.of(invoice));
        when(gateway.issue(invoice.getId(),invoice.getOrderId())).thenThrow(new UnsupportedOperationException());
        var result=fiscal.retry(invoice.getId());
        assertEquals(Invoice.Status.PENDING_PROVIDER,result.getStatus());
        assertNull(result.getDocumentUrl());
        assertEquals(1,result.getAttempts());
        verify(audit).record("INVOICE_ATTEMPTED",invoice.getId());
    }
}
