package br.com.suittailoring;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
@SpringBootTest @AutoConfigureMockMvc
class ApiSecurityTest {
    @Autowired MockMvc mvc;
    @Test void publicCatalogIsAccessible() throws Exception {mvc.perform(get("/api/catalog/products")).andExpect(status().isOk());}
    @Test void anonymousCannotAccessAdministration() throws Exception {mvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());}
    @Test void customerCannotAccessAdministration() throws Exception {mvc.perform(get("/api/admin/products").with(user("customer").roles("CUSTOMER"))).andExpect(status().isForbidden());}
    @Test void financeCannotEditCatalog() throws Exception {mvc.perform(post("/api/admin/products").with(user("finance").roles("FINANCE")).with(csrf()).contentType("application/json").content("{}")).andExpect(status().is4xxClientError());}
    @Test void csrfIsMandatory() throws Exception {mvc.perform(post("/api/auth/register").contentType("application/json").content("{}")).andExpect(status().isForbidden());}
}
