package br.com.suittailoring.crm.domain;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity @Table(name="customer_notes")
public class CustomerNote {
    @Id private UUID id=UUID.randomUUID();
    @Column(nullable=false) private UUID customerId;
    @Column(nullable=false,length=2000) private String content;
    @Column(nullable=false) private String author;
    @Column(nullable=false) private Instant createdAt=Instant.now();
    protected CustomerNote(){}
    public CustomerNote(UUID customerId,String content,String author){this.customerId=customerId;this.content=content;this.author=author;}
    public UUID getId(){return id;}public UUID getCustomerId(){return customerId;}public String getContent(){return content;}
    public String getAuthor(){return author;}public Instant getCreatedAt(){return createdAt;}
}
