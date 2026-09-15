package br.com.suittailoring.accounts.application;

import br.com.suittailoring.accounts.domain.Account;
import br.com.suittailoring.accounts.infrastructure.AccountRepository;
import br.com.suittailoring.shared.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
  private final AccountRepository accounts;
  private final PasswordEncoder encoder;

  public AccountService(AccountRepository accounts, PasswordEncoder encoder) {
    this.accounts = accounts;
    this.encoder = encoder;
  }

  @Transactional
  public Account register(String name, String email, String password) {
    String normalized = email.strip().toLowerCase(java.util.Locale.ROOT);
    if (accounts.findByEmail(normalized).isPresent())
      throw new BusinessException("Não foi possível cadastrar esta conta");
    return accounts.save(
        new Account(name, normalized, encoder.encode(password), Account.Role.CUSTOMER));
  }

  public Account current(String email) {
    return accounts
        .findByEmail(email)
        .orElseThrow(jakarta.persistence.EntityNotFoundException::new);
  }
}
