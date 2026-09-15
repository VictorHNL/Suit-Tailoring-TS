package br.com.suittailoring.accounts.infrastructure;

import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableMethodSecurity
@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
public class SecurityConfig {
  public static final String STAFF_CONTEXT = "STAFF_SECURITY_CONTEXT";

  @Bean
  UserDetailsService users(AccountRepository accounts) {
    return email ->
        accounts
            .findByEmail(email.strip().toLowerCase(java.util.Locale.ROOT))
            .map(
                a ->
                    User.withUsername(a.getEmail())
                        .password(a.getPasswordHash())
                        .roles(a.getRole().name())
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
  }

  @Bean
  @Order(1)
  SecurityFilterChain staff(HttpSecurity http) throws Exception {
    var repository = new HttpSessionSecurityContextRepository();
    repository.setSpringSecurityContextKey(STAFF_CONTEXT);
    http.securityMatcher("/api/admin/**")
        .securityContext(c -> c.securityContextRepository(repository))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/admin/auth/csrf", "/api/admin/auth/login")
                    .permitAll()
                    .anyRequest()
                    .hasAnyRole("OWNER", "CATALOG", "FINANCE"));
    common(http);
    return http.build();
  }

  @Bean
  @Order(2)
  SecurityFilterChain customer(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        a ->
            a.requestMatchers(
                    "/api/auth/csrf",
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/catalog/**",
                    "/api/cart/**",
                    "/api/media/**",
                    "/actuator/health",
                    "/error")
                .permitAll()
                .anyRequest()
                .hasRole("CUSTOMER"));
    common(http);
    return http.build();
  }

  private void common(HttpSecurity http) throws Exception {
    http.requestCache(c -> c.disable())
        .formLogin(c -> c.disable())
        .httpBasic(c -> c.disable())
        .logout(c -> c.disable())
        .exceptionHandling(
            c ->
                c.authenticationEntryPoint((req, res, e) -> res.sendError(401))
                    .accessDeniedHandler((req, res, e) -> res.sendError(403)));
    // CSRF remains enabled. Each frontend obtains a token from its auth/csrf endpoint.
  }
}
