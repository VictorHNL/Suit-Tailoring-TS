package br.com.suittailoring.catalog.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductTest {

    @Test
    void shouldChangeNameWhenNewNameIsValid() {
        Product product = new Product(
            "Blazer Classic",
            "Blazer de alfaiataria" 
            );

            product.changeName("Blazer Classic Black");

            assertEquals("Blazer Classic Black", product.getName());
    }
}