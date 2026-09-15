package br.com.suittailoring.accounts.api;

import br.com.suittailoring.accounts.application.AccountService;
import br.com.suittailoring.accounts.domain.Account;
import br.com.suittailoring.accounts.infrastructure.SecurityConfig;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
  public record Register(
      @NotBlank @Size(max = 150) String name,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 12, max = 72) String password) {}

  public record Login(@NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}

  public record Profile(UUID id, String name, String email, Account.Role role) {
    public static Profile of(Account a) {
      return new Profile(a.getId(), a.getName(), a.getEmail(), a.getRole());
    }
  }

  private final AccountService accounts;
  private final PasswordEncoder encoder;

  public AuthController(AccountService accounts, PasswordEncoder encoder) {
    this.accounts = accounts;
    this.encoder = encoder;
  }

  @GetMapping({"/api/auth/csrf", "/api/admin/auth/csrf"})
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
  }

  @PostMapping("/api/auth/register")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Profile register(@Valid @RequestBody Register input) {
    if (input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalArgumentException("Senha deve ter no máximo 72 bytes UTF-8");
    return Profile.of(accounts.register(input.name(), input.email(), input.password()));
  }

  @PostMapping({"/api/auth/login", "/api/admin/auth/login"})
  public Profile login(
      @Valid @RequestBody Login input, HttpServletRequest request, HttpServletResponse response) {
    Account account;
    try {
      account = accounts.current(input.email().strip().toLowerCase(Locale.ROOT));
    } catch (jakarta.persistence.EntityNotFoundException e) {
      encoder.matches(input.password(), encoder.encode("dummy-password"));
      throw new BadCredentialsException("Credenciais inválidas");
    }
    if (!encoder.matches(input.password(), account.getPasswordHash()))
      throw new BadCredentialsException("Credenciais inválidas");
    boolean staff = request.getRequestURI().startsWith("/api/admin/");
    if (staff == (account.getRole() == Account.Role.CUSTOMER))
      throw new BadCredentialsException("Use a entrada correspondente ao seu perfil");
    request.getSession();
    request.changeSessionId();
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            account.getEmail(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()))));
    var repository = new HttpSessionSecurityContextRepository();
    if (staff) repository.setSpringSecurityContextKey(SecurityConfig.STAFF_CONTEXT);
    repository.saveContext(context, request, response);
    SecurityContextHolder.setContext(context);
    return Profile.of(account);
  }

  @GetMapping({"/api/auth/me", "/api/admin/auth/me"})
  public Profile me(Authentication auth) {
    return Profile.of(accounts.current(auth.getName()));
  }

  @PostMapping({"/api/auth/logout", "/api/admin/auth/logout"})
  public void logout(HttpServletRequest request) {
    var session = request.getSession(false);
    if (session != null)
      session.removeAttribute(
          request.getRequestURI().startsWith("/api/admin/")
              ? SecurityConfig.STAFF_CONTEXT
              : HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
    SecurityContextHolder.clearContext();
  }
}
