# Suit Tailoring TS

Backend de e-commerce de alfaiataria em Java 25, Spring Boot, JPA/Hibernate e PostgreSQL.

- [Guia completo](docs/backend/README.md)
- [Executar no VS Code/Windows](docs/backend/execucao.md)
- [API e permissões](docs/backend/api.md)
- [Pastas, POO e arquitetura](docs/backend/arquitetura-e-pastas.md)
- [Cursos e exercícios](docs/backend/trilha-de-estudos.md)
- [Testes e evidências](docs/backend/verificacao.md)
- [Sprints e backlog](docs/sprints/backlog.md)

A implementação cobre o MVP de contas, catálogo, carrinho, pedidos, estoque, CRM e operação.
Pagamento é simulado em dev. Emissão fiscal, frete e cobrança reais precisam de integração.
API e worker podem rodar separadamente, com banco compartilhado.

## Início rápido no Windows

Com JDK 25 e PostgreSQL disponíveis, execute na raiz do repositório:

```powershell
cd backend-java
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

Preencha `DB_URL`, `DB_USER` e `DB_PASSWORD` no arquivo `backend-java/.env` e inicie a API:

```powershell
.\scripts\run-local.ps1 -Profile dev
```

Confira a saúde da aplicação em <http://localhost:8080/actuator/health>; o status esperado é `UP`.
Para criar o banco e o proprietário inicial, siga o [guia de execução](docs/backend/execucao.md).

Para executar os testes rápidos com H2 em memória, dentro de `backend-java`:

```powershell
.\mvnw.cmd test
```
