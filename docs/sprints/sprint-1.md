# Sprint 1 — Identidade e catálogo

Objetivo: disponibilizar autenticação, permissões e gestão do catálogo com persistência.

## Critérios de aceite

- Cadastro público sempre cria CUSTOMER e armazena somente hash BCrypt.
- Cliente e equipe usam contextos de autenticação separados; CSRF obrigatório.
- Catálogo público lista apenas publicados, incluindo UNISEX nos filtros WOMEN/MEN.
- Proprietário/CATALOG cria, edita, clona, arquiva e publica produtos e controla variantes.
- Flyway cria o schema; Hibernate valida o mapeamento.
- Testes JUnit/Mockito e testes de contexto/HTTP passam.

## Revisão

Validação realizada antes do commit desta sprint. H2 em modo PostgreSQL é usado nos testes rápidos; não substitui a validação final com PostgreSQL.
Clonagem inicia em rascunho e estoque zero. Exclusão é arquivamento para preservar histórico.

## Retrospectiva

As regras de cadastro e estoque ficam testáveis sem iniciar o servidor.
O próximo incremento é o checkout transacional com carrinho de visitante e idempotência.
