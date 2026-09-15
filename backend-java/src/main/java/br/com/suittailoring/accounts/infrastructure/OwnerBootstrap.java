package br.com.suittailoring.accounts.infrastructure;
import br.com.suittailoring.accounts.domain.Account;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
@Component @Profile("bootstrap")
public class OwnerBootstrap implements ApplicationRunner {
    private final AccountRepository accounts; private final PasswordEncoder encoder; private final Environment env;
    public OwnerBootstrap(AccountRepository a, PasswordEncoder p, Environment e) { accounts=a;encoder=p;env=e; }
    public void run(ApplicationArguments args) {
        String email=env.getRequiredProperty("OWNER_EMAIL").strip().toLowerCase(java.util.Locale.ROOT);
        String password=env.getRequiredProperty("OWNER_PASSWORD");
        if(password.length()<12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException("OWNER_PASSWORD deve ter 12+ caracteres e até 72 bytes");
        if(accounts.findByEmail(email).isPresent()) throw new IllegalStateException("Conta já existe; bootstrap não altera permissões");
        accounts.save(new Account("Proprietário",email,encoder.encode(password),Account.Role.OWNER));
    }
}

