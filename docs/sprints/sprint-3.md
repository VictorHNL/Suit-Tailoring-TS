# Sprint 3 — Operação da loja

Objetivo: disponibilizar APIs de dashboard, CRM, financeiro, fiscal e mídia.

## Critérios de aceite

- Dashboard informa pedidos e estoque baixo.
- OWNER gerencia cadastro da equipe CATALOG/FINANCE e consulta auditoria.
- CRM lista clientes, pedidos e notas internas.
- Ledger registra pagamentos, estornos e despesas; resumo mostra saldo operacional.
- Simulação de pagamento/estorno só existe no perfil dev e para OWNER.
- Fiscal persiste solicitação e informa PENDING_PROVIDER sem inventar nota emitida.
- Upload valida conteúdo JPEG/PNG, proporção 3:4 e até 5 MB; ignora nomes fornecidos.
- Testes verificam login real com sessões separadas, mídia e idempotência financeira.

## Retrospectiva

Chamadas fiscais são separadas do checkout. Provedor fiscal, gateway real e transportadora permanecem dependências externas ainda não selecionadas.
Não há emissão fiscal válida, cobrança bancária nem cálculo tributário nesta entrega.
