package br.com.suittailoring.catalog.api;

import br.com.suittailoring.catalog.api.CatalogDtos.*;
import br.com.suittailoring.catalog.application.CatalogService;
import br.com.suittailoring.catalog.domain.model.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/products")
public class CatalogController {
  private final CatalogService catalog;

  public CatalogController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  public Page<View> list(
      @RequestParam(required = false) Product.Audience audience,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page) {
    return catalog.list(audience, category, search, page);
  }

  @GetMapping("/{id}")
  public View detail(@PathVariable UUID id) {
    return catalog.detail(id, false);
  }
}
