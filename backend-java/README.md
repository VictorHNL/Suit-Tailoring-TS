# Backend Java

Backend do Suit Tailoring em Java 25 LTS e Spring Boot 4.1.1.

## Documentação

Veja [o guia completo do backend](../docs/backend/README.md), com APIs, pastas, arquitetura, execução, testes e cursos.

Backend do MVP: catálogo, contas, carrinho, pedidos, estoque, CRM, financeiro básico e fila fiscal.
Pagamento é simulado somente em dev. Gateway/frete/emissão fiscal reais dependem de integração.

## Executar localmente

Após instalar a JDK 25, use o Maven Wrapper:

```powershell
.\scripts\run-local.ps1 -Profile dev
```

Configure DB_URL, DB_USER e DB_PASSWORD no ambiente ou em .env (copiado de .env.example).
O script carrega .env. Flyway aplica as migrações; Hibernate valida o schema.

Testes rápidos: .\mvnw.cmd test.
PostgreSQL temporário no Windows: .\scripts\test-postgres.ps1.
Worker separado: .\scripts\run-local.ps1 -Profile worker.
