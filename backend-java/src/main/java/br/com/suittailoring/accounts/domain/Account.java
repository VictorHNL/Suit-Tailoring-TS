package br.com.suittailoring.accounts.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
  public enum Role {
    CUSTOMER,
    CATALOG,
    FINANCE,
    OWNER
  }

  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  protected Account() {}

  public Account(String name, String email, String passwordHash, Role role) {
    this.name = name;
    this.email = email.strip().toLowerCase(java.util.Locale.ROOT);
    this.passwordHash = passwordHash;
    this.role = role;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public void changeRole(Role role) {
    this.role = role;
  }
}
