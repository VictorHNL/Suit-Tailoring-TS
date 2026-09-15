package br.com.suittailoring.fiscal.domain;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity @Table(name="invoices")
public class Invoice {
    public enum Status { PENDING_PROVIDER, ISSUED, CANCELLED }
    @Id private UUID id=UUID.randomUUID();
    @Column(nullable=false,unique=true) private UUID orderId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.PENDING_PROVIDER;
    @Column(nullable=false) private int attempts;
    @Column(length=500) private String lastError;
    @Column(length=1000) private String documentUrl;
    @Column(nullable=false) private Instant createdAt=Instant.now();
    protected Invoice(){}
    public Invoice(UUID orderId){this.orderId=orderId;}
    public void unavailable(){attempts++;lastError="Provedor fiscal não configurado. Nenhuma nota foi emitida.";}
    public void issue(String url){if(status!=Status.PENDING_PROVIDER)throw new IllegalStateException("Solicitação fiscal já concluída");attempts++;documentUrl=url;status=Status.ISSUED;lastError=null;}
    public void cancelPending(){if(status==Status.PENDING_PROVIDER)status=Status.CANCELLED;}
    public UUID getId(){return id;}public UUID getOrderId(){return orderId;}public Status getStatus(){return status;}
    public int getAttempts(){return attempts;}public String getLastError(){return lastError;}public String getDocumentUrl(){return documentUrl;}public Instant getCreatedAt(){return createdAt;}
}
