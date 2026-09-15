# Executar, testar e operar

## 1. VS Code e terminal

Abra a pasta Suit Tailoring TS no VS Code. Use Terminal > Novo Terminal e execute:

```powershell
cd "C:\Users\victorhugonunes\Documents\ChatGPT\Suit Tailoring TS\backend-java"
java -version
javac -version
.\mvnw.cmd -v
```

Os comandos devem mostrar Java 25. O arquivo se chama mvnw.cmd; não existe uma pasta mvnw para esse comando.

## 2. Banco PostgreSQL

Use um banco exclusivo para esta aplicação. Não aponte os testes para seu banco de desenvolvimento ou produção.

Caso ainda não exista usuário/banco, abra o SQL Shell (psql), conecte como administrador e execute:

```sql
CREATE ROLE suit_tailoring LOGIN;
\password suit_tailoring
CREATE DATABASE suit_tailoring OWNER suit_tailoring;
```

O comando \password solicita a senha de forma interativa. Guarde-a localmente. Se já existir um banco apropriado, apenas configure seu nome e usuário.

## 3. Configuração local

Dentro de backend-java:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
code .env
```

Preencha DB_URL, DB_USER e DB_PASSWORD. A URL segue jdbc:postgresql://localhost:5432/suit_tailoring.

O Spring Boot não lê .env automaticamente. O script run-local.ps1 carrega esse arquivo para o processo; Docker Compose e um deploy têm seus próprios mecanismos.

| Variável | Utilidade |
|---|---|
| DB_URL | Endereço JDBC do banco |
| DB_USER | Usuário da aplicação |
| DB_PASSWORD | Senha; não versionar |
| COOKIE_SECURE | true em HTTPS; dev usa false |
| MEDIA_DIRECTORY | Diretório persistente de mídia |
| SPRING_PROFILES_ACTIVE | dev, worker ou bootstrap, conforme necessidade |
| APP_JOBS_ENABLED | Habilita expiração; desligue na API se usar worker |
| OWNER_EMAIL / OWNER_PASSWORD | Somente criação inicial do dono |

## 4. Criar o proprietário uma vez

No mesmo terminal:

```powershell
$env:OWNER_EMAIL = Read-Host "E-mail do proprietário"
$env:OWNER_PASSWORD = [System.Net.NetworkCredential]::new("", (Read-Host "Senha (12+ caracteres)" -AsSecureString)).Password
.\scripts\run-local.ps1 -Profile "dev,bootstrap"
```

Espere a aplicação iniciar. O perfil bootstrap cria o dono e recusa sobrescrever uma conta existente. Encerre com Ctrl+C e retire as credenciais do ambiente:

```powershell
Remove-Item Env:\OWNER_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:\OWNER_EMAIL -ErrorAction SilentlyContinue
.\scripts\run-local.ps1 -Profile "dev"
```

O limite é de 72 bytes UTF-8 para senha BCrypt. Caracteres acentuados podem ocupar mais de um byte.

## 5. Iniciar API e worker

Somente API local:

```powershell
.\scripts\run-local.ps1 -Profile "dev"
```

Abra http://localhost:8080/actuator/health. O resultado esperado é status UP. A aplicação não possui uma página inicial visual; ela oferece APIs.

Para executar a expiração no worker separado, adicione APP_JOBS_ENABLED=false ao .env da API ou defina essa variável antes de iniciar. Em um segundo terminal execute:

```powershell
cd "C:\Users\victorhugonunes\Documents\ChatGPT\Suit Tailoring TS\backend-java"
.\scripts\run-local.ps1 -Profile "worker"
```

O script força APP_JOBS_ENABLED=true ao iniciar o perfil worker. Assim, o mesmo .env pode desligar os jobs da API e o segundo terminal continua executando as tarefas. O Docker Compose também configura os dois processos separadamente.

O worker consulta solicitações fiscais, respeita intervalo crescente e limita tentativas automáticas a cinco. Sem fornecedor, mantém PENDING_PROVIDER. A tentativa manual pode ser acionada pela administração.

## 6. Testes

Testes rápidos com H2 em memória, sem usar PostgreSQL:

```powershell
.\mvnw.cmd test
```

Um bloco específico:

```powershell
.\mvnw.cmd test "-Dtest=ProductTest"
.\mvnw.cmd test "-Dtest=CheckoutIntegrationTest"
```

Teste real no PostgreSQL 17 instalado no Windows:

```powershell
.\scripts\test-postgres.ps1
```

O script cria um cluster novo dentro de target, inicia apenas em 127.0.0.1:55433, executa testes e encerra o processo em finally. Não utiliza o serviço PostgreSQL existente. O cluster usa trust somente nessa instância temporária local, sem dados reais. Logs permanecem em target/postgres-test-*.

Se o PostgreSQL estiver em outro caminho:

```powershell
.\scripts\test-postgres.ps1 -PostgresBin "C:\Program Files\PostgreSQL\17\bin" -Port 55434
```

Leia target/surefire-reports para os resultados. BUILD SUCCESS significa que aquela execução terminou corretamente; não substitui homologação do produto.

## 7. Gerar executável

```powershell
.\mvnw.cmd verify
java -jar target/backend-java-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Ao executar o JAR diretamente, configure DB_URL/DB_USER/DB_PASSWORD no ambiente: o comando java não carrega .env.

## 8. Docker Compose opcional

Requer Docker disponível. Na raiz do repositório:

```powershell
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", (Read-Host "Senha do banco local Docker" -AsSecureString)).Password
docker compose up --build
```

São três containers: db, api e worker. O banco não publica uma porta para o host; a API publica apenas em loopback. O compose usa perfil dev e serve para desenvolvimento. O Dockerfile executa testes no build.

Para o bootstrap nesse banco Docker, em outro terminal com a mesma configuração:

```powershell
$env:OWNER_EMAIL = Read-Host "E-mail do proprietário"
$env:OWNER_PASSWORD = [System.Net.NetworkCredential]::new("", (Read-Host "Senha do proprietário" -AsSecureString)).Password
docker compose run --rm -e OWNER_EMAIL -e OWNER_PASSWORD -e SPRING_PROFILES_ACTIVE=bootstrap api
```

Quando o dono for criado, encerre esse container com Ctrl+C. O processo normal da API continua atendendo.

Não use docker compose down -v para uma instalação com dados a preservar: isso remove os volumes.

O compose/Dockerfile são fornecidos como configuração de desenvolvimento. A validação local principal desta entrega usa Maven e PostgreSQL instalado; não presume Docker instalado.

## 9. Frontend

Recomenda-se proxy da mesma origem: o navegador chama /api no domínio da loja, e o proxy encaminha para Java. Isso preserva cookies e CSRF sem liberar CORS para qualquer origem.

Em toda requisição de escrita:

1. Obtenha o token em /api/auth/csrf ou /api/admin/auth/csrf.
2. Guarde o cookie recebido.
3. Envie o token no cabeçalho informado (normalmente X-CSRF-TOKEN).
4. Use credentials: "include" no fetch.

Após login/logout, obtenha um token atualizado. O frontend preserva o token do carrinho durante a navegação para login e retorna ao checkout.

## 10. Antes de produção

Homologue pagamento/fiscal/frete reais; desligue dev e bootstrap; use HTTPS, COOKIE_SECURE=true, secrets do ambiente, usuário de banco com permissões limitadas e backup restaurável. Defina monitoramento do worker, retenção de dados, recuperação de senha, e-mail e rate limiting no gateway.

O GitHub Actions foi adicionado para executar Maven com PostgreSQL a cada push/PR relevante. Sua execução remota só ocorrerá quando os commits forem enviados.
