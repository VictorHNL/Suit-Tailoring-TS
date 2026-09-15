package br.com.suittailoring.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "ledger_entries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "kind"}))
public class LedgerEntry {
  public enum Kind {
    PAYMENT,
    REFUND,
    EXPENSE
  }

  @Id private UUID id = UUID.randomUUID();
  private UUID orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Kind kind;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 500)
  private String description;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  protected LedgerEntry() {}

  public LedgerEntry(UUID orderId, Kind kind, BigDecimal amount, String description) {
    if (amount.signum() <= 0) throw new IllegalArgumentException("Valor deve ser positivo");
    this.orderId = orderId;
    this.kind = kind;
    this.amount = amount;
    this.description = description;
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public Kind getKind() {
    return kind;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
