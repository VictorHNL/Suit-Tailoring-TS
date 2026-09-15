package br.com.suittailoring.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductTest {

  @Test
  void shouldChangeNameWhenNewNameIsValid() {
    Product product = new Product("Blazer Classic", "Blazer de alfaiataria");

    product.changeName("Blazer Classic Black");

    assertEquals("Blazer Classic Black", product.getName());
  }

  @Test
  void shouldRejectBlankName() {
    Product product = new Product("Blazer Classic", "Blazer de alfaiataria");

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> product.changeName(" "));

    assertEquals("O nome do produto não pode ficar vazio", exception.getMessage());
  }
}
