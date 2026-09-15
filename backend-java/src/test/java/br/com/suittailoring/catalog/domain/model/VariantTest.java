package br.com.suittailoring.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import br.com.suittailoring.shared.BusinessException;
import org.junit.jupiter.api.Test;

class VariantTest {
  @Test
  void reserveAndReleaseStock() {
    Variant v = new Variant(new Product("Blazer", "Descrição"), "SKU-1", "M", "Preto", 3);
    v.reserve(2);
    assertEquals(1, v.getStock());
    v.release(2);
    assertEquals(3, v.getStock());
  }

  @Test
  void insufficientStockDoesNotMutate() {
    Variant v = new Variant(new Product("Blazer", "Descrição"), "SKU-1", "M", "Preto", 1);
    assertThrows(BusinessException.class, () -> v.reserve(2));
    assertEquals(1, v.getStock());
    assertThrows(BusinessException.class, () -> v.reserve(0));
  }

  @Test
  void constructorRejectsBlankProductName() {
    assertThrows(IllegalArgumentException.class, () -> new Product(" ", "Descrição"));
  }
}
