package br.com.suittailoring.operations.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false)
  private String actor;

  @Column(nullable = false)
  private String action;

  @Column(nullable = false)
  private UUID targetId;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  protected AuditEvent() {}

  public AuditEvent(String actor, String action, UUID targetId) {
    this.actor = actor;
    this.action = action;
    this.targetId = targetId;
  }

  public UUID getId() {
    return id;
  }

  public String getActor() {
    return actor;
  }

  public String getAction() {
    return action;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
