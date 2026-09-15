package br.com.suittailoring.accounts.api;

import br.com.suittailoring.accounts.application.AccountService;
import br.com.suittailoring.accounts.domain.Address;
import br.com.suittailoring.accounts.infrastructure.AddressRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
  public record Input(
      @NotBlank @Size(max = 500) String street,
      @NotBlank @Size(max = 150) String city,
      @NotNull @Pattern(regexp = "[A-Z]{2}") String state,
      @NotNull @Pattern(regexp = "[0-9]{8}") String postalCode) {}

  private final AddressRepository addresses;
  private final AccountService accounts;

  public AddressController(AddressRepository a, AccountService u) {
    addresses = a;
    accounts = u;
  }

  @GetMapping
  public List<Address> list(Principal p) {
    return addresses.findByCustomerId(accounts.current(p.getName()).getId());
  }

  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Address create(@Valid @RequestBody Input i, Principal p) {
    return addresses.save(
        new Address(
            accounts.current(p.getName()).getId(),
            i.street(),
            i.city(),
            i.state(),
            i.postalCode()));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id, Principal p) {
    addresses.delete(
        addresses
            .findByIdAndCustomerId(id, accounts.current(p.getName()).getId())
            .orElseThrow(jakarta.persistence.EntityNotFoundException::new));
  }

  @PutMapping("/{id}")
  @org.springframework.transaction.annotation.Transactional
  public Address update(@PathVariable UUID id, @Valid @RequestBody Input i, Principal p) {
    var address =
        addresses
            .findByIdAndCustomerId(id, accounts.current(p.getName()).getId())
            .orElseThrow(jakarta.persistence.EntityNotFoundException::new);
    address.update(i.street(), i.city(), i.state(), i.postalCode());
    return address;
  }
}
