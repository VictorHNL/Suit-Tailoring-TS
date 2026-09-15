package br.com.suittailoring.accounts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.suittailoring.accounts.application.AccountService;
import br.com.suittailoring.accounts.domain.Account;
import br.com.suittailoring.accounts.infrastructure.AccountRepository;
import br.com.suittailoring.shared.BusinessException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
  @Mock AccountRepository repository;
  @Mock PasswordEncoder encoder;
  @InjectMocks AccountService service;

  @Test
  void registrationHashesPasswordAndNeverGrantsOwner() {
    when(repository.findByEmail("client@example.com")).thenReturn(Optional.empty());
    when(encoder.encode("strong-password")).thenReturn("hashed");
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
    Account account = service.register("Cliente", "CLIENT@example.com", "strong-password");
    assertEquals(Account.Role.CUSTOMER, account.getRole());
    assertEquals("hashed", account.getPasswordHash());
    assertEquals("client@example.com", account.getEmail());
    verify(repository).save(account);
  }

  @Test
  void duplicateDoesNotWrite() {
    when(repository.findByEmail("client@example.com"))
        .thenReturn(
            Optional.of(
                new Account("Cliente", "client@example.com", "hash", Account.Role.CUSTOMER)));
    assertThrows(
        BusinessException.class,
        () -> service.register("Cliente", "client@example.com", "password"));
    verify(repository, never()).save(any());
  }
}
