# Sprint 4 — Entrega executável e aprendizado

Objetivo: tornar o backend reproduzível e documentado, com validação no banco de destino.

## Critérios de aceite

- API e worker podem iniciar em processos separados.
- Fila fiscal persiste tentativas e agenda nova execução com intervalo crescente.
- PostgreSQL temporário testa todas as migrações sem tocar no banco existente.
- Fluxo HTTP cobre publicação, compra, pagamento simulado e preservação de histórico.
- Código Java formatado consistentemente.
- Documentação de execução, endpoints, pastas/POO e trilha de cursos entregue.
- Dockerfile/Compose e workflow de CI versionados.

## Achado de integração

O PostgreSQL detectou um problema de parâmetros nulos nos filtros do catálogo (lower(bytea)).
A consulta foi substituída por Specification/Criteria que adiciona somente filtros informados.

## Revisão

A suíte valida lógica, persistência, segurança, concorrência, mídia e inicialização do worker.
O relatório final de verificação está em docs/backend/verificacao.md.

## Retrospectiva

Testar apenas com H2 não foi suficiente para garantir compatibilidade SQL.
Manter simulação explícita em dev permite estudar o fluxo sem confundir com cobrança real.
Para continuar aprendendo, selecione uma história pequena do backlog e explique o teste antes de alterar o código.
