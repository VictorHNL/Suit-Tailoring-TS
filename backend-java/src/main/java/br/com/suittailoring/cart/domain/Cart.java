package br.com.suittailoring.cart.domain;

import br.com.suittailoring.shared.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "carts")
public class Cart {
  @Id private UUID id = UUID.randomUUID();
  private UUID customerId;

  @Column(nullable = false)
  private boolean consumed;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @ElementCollection
  @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
  @MapKeyColumn(name = "variant_id")
  @Column(name = "quantity", nullable = false)
  private Map<UUID, Integer> items = new HashMap<>();

  public Cart() {}

  public UUID getId() {
    return id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public boolean isConsumed() {
    return consumed;
  }

  public Map<UUID, Integer> getItems() {
    return Map.copyOf(items);
  }

  public void checkAccess(UUID customer) {
    if (customerId != null && !customerId.equals(customer))
      throw new org.springframework.security.access.AccessDeniedException(
          "Carrinho pertence a outro cliente");
  }

  public void setItem(UUID variant, int quantity) {
    if (consumed) throw new BusinessException("Carrinho já finalizado");
    if (quantity < 0 || quantity > 99)
      throw new BusinessException("Quantidade deve estar entre 0 e 99");
    if (quantity == 0) {
      items.remove(variant);
      return;
    }
    if (!items.containsKey(variant) && items.size() >= 50)
      throw new BusinessException("Limite de 50 variantes por carrinho");
    items.put(variant, quantity);
  }

  public void consume(UUID customer) {
    if (consumed || items.isEmpty()) throw new BusinessException("Carrinho vazio ou já finalizado");
    checkAccess(customer);
    customerId = customer;
    consumed = true;
  }
}
