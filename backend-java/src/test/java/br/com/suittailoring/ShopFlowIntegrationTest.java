package br.com.suittailoring;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.suittailoring.accounts.domain.Account;
import br.com.suittailoring.accounts.infrastructure.AccountRepository;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ShopFlowIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired AccountRepository accounts;
  @Autowired PasswordEncoder encoder;

  @Test
  void ownerPublishesAndCustomerBuysThroughHttp() throws Exception {
    var owner = login(Account.Role.OWNER);
    var customer = login(Account.Role.CUSTOMER);
    String product =
        id(
            mvc.perform(
                    post("/api/admin/products")
                        .session(owner)
                        .with(csrf())
                        .contentType("application/json")
                        .content(
                            """
                            {"name":"Blazer HTTP","description":"Teste completo","slug":"http-%s","category":"Blazers","audience":"UNISEX","bodyPart":"TORSO","price":120.00,"images":["https://example.com/photo.jpg"]}
                            """
                                .formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String variant =
        id(
            mvc.perform(
                    post("/api/admin/products/" + product + "/variants")
                        .session(owner)
                        .with(csrf())
                        .contentType("application/json")
                        .content(
                            "{\"sku\":\""
                                + UUID.randomUUID()
                                + "\",\"size\":\"M\",\"color\":\"Preto\",\"stock\":1}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    mvc.perform(post("/api/admin/products/" + product + "/publish").session(owner).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PUBLISHED"));
    String cart =
        JsonPath.read(
            mvc.perform(post("/api/cart").with(csrf()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            "$.token");
    mvc.perform(
            put("/api/cart/items/" + variant)
                .header("X-Cart-Token", cart)
                .with(csrf())
                .contentType("application/json")
                .content("{\"quantity\":1}"))
        .andExpect(status().isOk());
    String address =
        id(
            mvc.perform(
                    post("/api/addresses")
                        .session(customer)
                        .with(csrf())
                        .contentType("application/json")
                        .content(
                            """
                            {"street":"Rua 1","city":"São Paulo","state":"SP","postalCode":"01001000"}
                            """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String order =
        id(
            mvc.perform(
                    post("/api/orders")
                        .session(customer)
                        .with(csrf())
                        .header("X-Cart-Token", cart)
                        .contentType("application/json")
                        .content(
                            "{\"addressId\":\"" + address + "\",\"idempotencyKey\":\"http-buy\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(120.0))
                .andReturn()
                .getResponse()
                .getContentAsString());
    mvc.perform(post("/api/admin/dev/orders/" + order + "/pay").session(owner).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PAID"));
    mvc.perform(post("/api/admin/products/" + product + "/clone").session(owner).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.variants[0].stock").value(0));
    mvc.perform(delete("/api/admin/products/" + product).session(owner).with(csrf()))
        .andExpect(status().isOk());
    mvc.perform(get("/api/catalog/products/" + product)).andExpect(status().isNotFound());
    mvc.perform(get("/api/orders/" + order).session(customer))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lines[0].productName").value("Blazer HTTP"));
  }

  String id(String body) {
    return JsonPath.read(body, "$.id");
  }

  MockHttpSession login(Account.Role role) throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    accounts.save(new Account("Teste HTTP", email, encoder.encode("password-12345"), role));
    String path = role == Account.Role.OWNER ? "/api/admin/auth/login" : "/api/auth/login";
    return (MockHttpSession)
        mvc.perform(
                post(path)
                    .with(csrf())
                    .contentType("application/json")
                    .content("{\"email\":\"" + email + "\",\"password\":\"password-12345\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);
  }
}
