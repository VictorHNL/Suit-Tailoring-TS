package br.com.suittailoring.accounts.infrastructure;

import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;

@Configuration
public class PasswordConfiguration {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
