# Trilha de estudos — do Java básico ao backend

Links consultados em 15/09/2026. Priorize exercícios no próprio projeto. Não é necessário comprar todos os cursos; os guias oficiais já permitem estudar os conceitos usados.

## 1. Java e POO

**Opção estruturada em português:** [Java COMPLETO — Programação Orientada a Objetos + Projetos, Nélio Alves](https://www.udemy.com/course/java-curso-completo/). Curso pago; consulte a ementa e as aulas de demonstração antes de comprar.

**Opção gratuita oficial:** [Classes and Objects — Dev.java](https://dev.java/learn/classes-objects/).

Estude variáveis, tipos, condições, laços, métodos, construtores, this, private/public, getters, exceções, enum, List, Map, UUID e BigDecimal.

No projeto: Product, Variant, PurchaseOrder e OrderLine.

Exercícios:

1. Explique cada campo de Product com suas próprias palavras.
2. Crie um teste para nome null, vazio e com espaços.
3. Explique por que reserve(2) deve lançar exceção quando stock vale 1.
4. Compare composição (pedido com linhas) com herança; justifique a escolha usada.
5. Explique por que usamos BigDecimal para dinheiro.

Você está pronto para avançar quando conseguir mudar uma regra e escrever um teste sem copiar o código inteiro.

## 2. Maven e organização de projetos

**Guia oficial:** [Maven — Getting Started](https://maven.apache.org/guides/getting-started/).

Estude pom.xml, dependências, escopos, plugins e ciclo compile/test/package/verify.

No projeto: pom.xml, mvnw.cmd, src/main, src/test e target.

Exercício: execute apenas ProductTest, depois toda a suíte; localize o relatório em target/surefire-reports. Explique a diferença entre código-fonte e .class.

## 3. Spring Boot e HTTP

**Gratuito oficial:** [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/) e [Building REST services with Spring](https://spring.io/guides/tutorials/rest/).

**Curso em português para aprofundar:** [Especialista Spring REST — AlgaWorks](https://lp.algaworks.com/especialista-spring-rest-matriculas/). Avalie a carga horária e os pré-requisitos depois de estudar Java e HTTP.

Estude injeção por construtor, beans, @Service, @RestController, GET/POST/PUT/DELETE, JSON, códigos HTTP, records/DTOs e validação.

No projeto: CatalogController, AdminCatalogController, CatalogService e ApiErrors.

Exercício: siga uma chamada de atualização do produto, desde o JSON até a resposta. Identifique onde cada tipo de erro é tratado.

## 4. SQL e PostgreSQL

**Gratuito oficial:** [Tutorial PostgreSQL 17](https://www.postgresql.org/docs/17/tutorial.html).

Estude tabelas, chaves primárias/estrangeiras, SELECT, JOIN, índices, UNIQUE, CHECK e transações.

No projeto: V1__accounts_catalog.sql até V4__fiscal_retry.sql.

Exercício: desenhe como accounts, addresses, carts, purchase_orders e order_lines se relacionam. Explique por que pedidos guardam uma cópia do endereço.

## 5. JPA/Hibernate

**Gratuito oficial:** [Hibernate — Getting Started](https://hibernate.org/orm/documentation/getting-started/) e [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/).

Estude @Entity, @Id, construtor protegido sem argumentos, @ManyToOne, @ElementCollection, lazy loading, dirty checking, @Version e bloqueios pessimistas.

No projeto: Variant.product, OrderRepository.lock e OrderService.checkout.

Exercícios:

1. Explique por que um setter não executa imediatamente um UPDATE fora de uma transação.
2. Compare bloquear uma linha no banco com usar synchronized em Java.
3. Explique por que synchronized não resolve duas instâncias da aplicação.
4. Rode CheckoutIntegrationTest contra PostgreSQL.

O teste com PostgreSQL detectou nesta entrega um problema de tipos SQL em filtros nulos que H2 não detectou. Use isso como exemplo prático dos limites de bancos substitutos.

## 6. JUnit e Mockito

**Guias oficiais:** [JUnit User Guide](https://docs.junit.org/current/user-guide/) e [Mockito](https://site.mockito.org/).

JUnit executa testes e oferece asserções. Mockito cria substitutos de dependências para testar uma unidade isolada.

No projeto:

- ProductTest/VariantTest: regras de objetos.
- AccountServiceTest: repositório e encoder substituídos por mocks.
- FiscalServiceTest: fornecedor indisponível.
- CheckoutIntegrationTest: banco real ou H2, sem simular persistência.
- LoginIntegrationTest: autenticação e sessões por HTTP.
- ShopFlowIntegrationTest: fluxo completo de compra.

Leia um teste em três partes:

1. Arrange: prepara objetos e dependências.
2. Act: executa o comportamento.
3. Assert: verifica o resultado observável.

Exercício: no AccountServiceTest, explique when, thenReturn, assertEquals e verify. Depois escreva um teste para rejeitar tentativa de publicar produto sem variante.

O projeto configura Mockito como javaagent no Surefire para instrumentação explícita na JVM de testes.

## 7. Segurança web

**Gratuito oficial:** [Securing a Web Application](https://spring.io/guides/gs/securing-web/) e [Spring Security Reference](https://docs.spring.io/spring-security/reference/).

Estude autenticação versus autorização, hash de senha, BCrypt, sessão, cookie HttpOnly/Secure/SameSite, CSRF e controle de acesso a um objeto de outro usuário.

No projeto: SecurityConfig, PasswordConfiguration, AuthController, AuthRateLimitFilter e LoginIntegrationTest.

Exercício: explique por que ocultar o botão do painel administrativo no frontend não protege o endpoint.

## 8. Arquitetura e sistemas distribuídos

Comece pela [documentação de arquitetura do projeto](arquitetura-e-pastas.md). Em seguida, pratique separar uma integração atrás de uma interface usando FiscalGateway.

Estude coesão, acoplamento, portas/adaptadores, idempotência, consistência, retries, timeouts, filas e outbox.

**Prática oficial de testes de infraestrutura:** [Testcontainers com Spring Boot — Docker](https://docs.docker.com/guides/testcontainers-java-spring-boot-rest-api/).

Exercício: descreva o que acontece se o worker parar por 20 minutos. Depois explique o que muda se o PostgreSQL parar. Não confunda processos separados com bancos independentes.

Antes de adotar microserviços, implemente e teste uma integração real pequena. A separação completa envolve operação e dados, além de pacotes Java.

## 9. Scrum e Git

**Fonte oficial:** [Scrum Guide — download em português brasileiro](https://scrumguides.org/download).

Scrum envolve objetivo de produto, backlog, planejamento, inspeção e adaptação. Um commit é um registro de código; não é uma sprint.

Aqui usamos incrementos técnicos registrados como sprints didáticas. Você participa como responsável pelas prioridades e pela aceitação do produto. Em um time real, reserve períodos de trabalho e eventos apropriados; não reduza Scrum a nomear commits.

Exercício por incremento:

1. Escolha uma história do backlog.
2. Defina como comprovar sua conclusão.
3. Implemente um bloco pequeno.
4. Execute testes relevantes.
5. Revise o diff e registre o commit.
6. Demonstre o comportamento e escreva uma melhoria para o próximo ciclo.

## Sugestão de ritmo

| Etapa | Prática sugerida |
|---|---|
| 1 | Java/POO e testes de Product |
| 2 | Maven, HTTP e um controller |
| 3 | SQL, JPA e migrações |
| 4 | Segurança e sessões |
| 5 | Checkout, transações e concorrência |
| 6 | Financeiro, fiscal e worker |
| 7 | Uma melhoria de backlog feita por você |

Avance pela compreensão, não pelo número de vídeos assistidos. Exemplos de cursos podem usar versões anteriores de Java/Spring: compare com o pom.xml antes de copiar dependências ou configurações de segurança.
