# Sprint 2 — Carrinho e pedidos

Objetivo: permitir compra autenticada a partir de um carrinho de visitante.

## Critérios de aceite

- Visitante guarda token opaco do carrinho e informa variantes/quantidades.
- Checkout exige cliente autenticado e endereço pertencente a ele.
- Valores são calculados no servidor e copiados para os itens do pedido.
- Chave de idempotência impede pedido duplicado; reuso com outros dados é rejeitado.
- Transação e bloqueios ordenados impedem estoque negativo em compras simultâneas.
- Cancelamento e expiração após 30 minutos devolvem estoque uma única vez.
- Cliente consulta somente seus pedidos; proprietário controla envio e entrega.

## Revisão e retrospectiva

Testes de integração verificam rollback, idempotência, autorização e concorrência com duas threads.
Frete real ainda depende de transportadora: o total desta etapa é somente a soma dos produtos.
Próxima sprint: pagamento simulado, financeiro, CRM, fiscal e mídia.
