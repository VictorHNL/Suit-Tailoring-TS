package br.com.suittailoring;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.suittailoring.accounts.domain.Account;
import br.com.suittailoring.accounts.infrastructure.AccountRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired AccountRepository accounts;
  @Autowired PasswordEncoder encoder;

  @Test
  void customerSessionCannotBecomeAnOwnerSession() throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    accounts.save(
        new Account("Cliente", email, encoder.encode("password-12345"), Account.Role.CUSTOMER));
    var result =
        mvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"email\":\"" + email + "\",\"password\":\"password-12345\"}"))
            .andExpect(status().isOk())
            .andReturn();
    var session = (MockHttpSession) result.getRequest().getSession(false);
    mvc.perform(get("/api/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("CUSTOMER"));
    mvc.perform(get("/api/admin/products").session(session)).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/admin/auth/login")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"password\":\"password-12345\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void ownerSessionIsSeparateFromCustomerSession() throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    accounts.save(new Account("Dono", email, encoder.encode("password-12345"), Account.Role.OWNER));
    var result =
        mvc.perform(
                post("/api/admin/auth/login")
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"email\":\"" + email + "\",\"password\":\"password-12345\"}"))
            .andExpect(status().isOk())
            .andReturn();
    var session = (MockHttpSession) result.getRequest().getSession(false);
    mvc.perform(get("/api/admin/dashboard").session(session)).andExpect(status().isOk());
    mvc.perform(get("/api/orders").session(session)).andExpect(status().isUnauthorized());
  }
}
