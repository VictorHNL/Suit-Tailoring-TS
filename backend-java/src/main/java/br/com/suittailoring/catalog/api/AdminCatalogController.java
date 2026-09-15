package br.com.suittailoring.catalog.api;

import br.com.suittailoring.catalog.api.CatalogDtos.*;
import br.com.suittailoring.catalog.application.CatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('OWNER','CATALOG')")
public class AdminCatalogController {
  public record Stock(@Min(0) int quantity) {}

  private final CatalogService catalog;

  public AdminCatalogController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping("/products")
  public Page<View> list(@RequestParam(defaultValue = "0") int page) {
    return catalog.adminList(page);
  }

  @GetMapping("/products/{id}")
  public View detail(@PathVariable UUID id) {
    return catalog.detail(id, true);
  }

  @PostMapping("/products")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public View create(@Valid @RequestBody Input i) {
    return catalog.create(i);
  }

  @PutMapping("/products/{id}")
  public View update(@PathVariable UUID id, @Valid @RequestBody Input i) {
    return catalog.update(id, i);
  }

  @PostMapping("/products/{id}/publish")
  public View publish(@PathVariable UUID id) {
    return catalog.publish(id);
  }

  @DeleteMapping("/products/{id}")
  public void archive(@PathVariable UUID id) {
    catalog.archive(id);
  }

  @PostMapping("/products/{id}/clone")
  public View cloneProduct(@PathVariable UUID id) {
    return catalog.cloneProduct(id);
  }

  @PostMapping("/products/{id}/variants")
  public VariantView variant(@PathVariable UUID id, @Valid @RequestBody VariantInput i) {
    return catalog.addVariant(id, i);
  }

  @PutMapping("/variants/{id}/stock")
  public VariantView stock(@PathVariable UUID id, @Valid @RequestBody Stock i) {
    return catalog.stock(id, i.quantity());
  }

  @PutMapping("/variants/{id}")
  public VariantView updateVariant(@PathVariable UUID id, @Valid @RequestBody VariantInput i) {
    return catalog.updateVariant(id, i);
  }
}
