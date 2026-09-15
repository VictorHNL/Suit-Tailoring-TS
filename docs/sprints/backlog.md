# Backlog e Scrum didático

## Objetivo do produto

Permitir que a loja publique peças de alfaiataria, receba pedidos e acompanhe a operação com segurança.

## Definition of Done de cada incremento

- Critérios de aceite identificados.
- Implementação compilando.
- Testes relevantes passando.
- Migrações revisadas quando houver mudança de schema.
- Documentação atualizada.
- Diff revisado, sem segredos nem arquivos gerados.
- Commit com o incremento.

Os registros abaixo são incrementos técnicos desta construção assistida, não uma simulação de todas as cerimônias de um time Scrum. O usuário faz a revisão funcional e define prioridades para o próximo ciclo.

| Sprint | Objetivo | Registro |
|---|---|---|
| 1 | Contas e catálogo | sprint-1.md |
| 2 | Carrinho, pedidos e concorrência | sprint-2.md |
| 3 | Operação, financeiro, CRM e mídia | sprint-3.md |
| 4 | Worker, validação PostgreSQL, execução e estudos | sprint-4.md |

## Próximo backlog — fora desta implementação

| Prioridade | História | Dependência / aceite |
|---|---|---|
| P0 | Cobrar pagamento real | Escolher gateway; validar webhook assinado, valor, moeda e idempotência |
| P0 | Cotar frete | Escolher transportadora e regras; total do servidor inclui frete |
| P0 | Emitir nota fiscal válida | Fornecedor, credenciais, dados fiscais e homologação |
| P0 | Preparar produção | TLS, segredos, backup/restauração, monitoramento e revisão de segurança |
| P1 | Recuperar/verificar e-mail | Provedor de e-mail e tokens de uso único |
| P1 | Revogar acesso da equipe | Atualizar permissões e invalidar sessões; proteger último proprietário |
| P1 | Atender solicitações de dados | Política e processo de exportação/anonimização com retenções necessárias |
| P1 | Mídia em armazenamento externo | S3 compatível, CDN, políticas e processamento de vídeo |
| P1 | Frontend da loja e painel | Consumir API, cookies/CSRF, carrinho persistente e vitrine |
| P2 | Financeiro avançado | Conciliação, custos, impostos e relatórios de período |
| P2 | Devoluções após envio | Fluxo operacional e integrações de reembolso/logística |
| P2 | Categorias e coleções próprias | Cadastro hierárquico, slugs e filtros por entidade |
| P2 | Serviços independentes | Outbox/eventos, bancos próprios e observabilidade distribuída |

## Modelo de próxima história

Como proprietário, quero filtrar lançamentos por período para analisar a operação mensal.

Aceite: datas válidas; intervalo aplicado a listagem e resumo; apenas OWNER/FINANCE; teste com lançamento fora do período.

Divisão de estudo: contrato de entrada → consulta → serviço → controller → teste → documentação → commit.
