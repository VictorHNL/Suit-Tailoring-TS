package br.com.suittailoring.accounts.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "addresses")
public class Address {
  @Id private UUID id = UUID.randomUUID();

  @Column(nullable = false)
  private UUID customerId;

  @Column(nullable = false, length = 500)
  private String street;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false, length = 2)
  private String state;

  @Column(nullable = false, length = 8)
  private String postalCode;

  protected Address() {}

  public Address(UUID customerId, String street, String city, String state, String postalCode) {
    this.customerId = customerId;
    this.street = street;
    this.city = city;
    this.state = state;
    this.postalCode = postalCode;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void update(String street, String city, String state, String postalCode) {
    this.street = street;
    this.city = city;
    this.state = state;
    this.postalCode = postalCode;
  }

  public String getStreet() {
    return street;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String snapshot() {
    return street + ", " + city + "/" + state + " CEP " + postalCode;
  }
}
