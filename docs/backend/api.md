# Contrato da API

Base local: http://localhost:8080. Conteúdo JSON, exceto upload multipart e leitura de imagem.
UUIDs são strings. Valores monetários usam BigDecimal, com até duas casas decimais, em BRL.

## Convenções

- Paginação: ?page=0, tamanho fixo de 20.
- Resposta paginada: content e page (size, number, totalElements, totalPages).
- 200: operação concluída; 201: criação/cadastro/checkout; comandos sem corpo podem responder 200 vazio.
- 400: validação ou regra de negócio; 401: sessão/credenciais ausentes; 403: permissão ou CSRF.
- 404: registro não encontrado; 409: duplicidade/conflito concorrente; 429: limite de requisições.
- Erros tratados usam ProblemDetail com status/detail. Erros produzidos pela cadeia de segurança/container podem ter outro corpo; sempre confira o status HTTP.
- Nenhuma API retorna hash de senha.

## Autenticação

| Método | Caminho | Acesso |
|---|---|---|
| GET | /api/auth/csrf | Público |
| POST | /api/auth/register | Público + CSRF |
| POST | /api/auth/login | Público + CSRF, somente CUSTOMER |
| GET | /api/auth/me | CUSTOMER |
| POST | /api/auth/logout | CUSTOMER + CSRF |
| GET | /api/admin/auth/csrf | Público |
| POST | /api/admin/auth/login | Público + CSRF, somente equipe |
| GET | /api/admin/auth/me | Equipe |
| POST | /api/admin/auth/logout | Equipe + CSRF |

Cadastro:

```json
{"name":"Cliente Exemplo","email":"cliente@example.com","password":"uma-senha-longa"}
```

Login:

```json
{"email":"cliente@example.com","password":"uma-senha-longa"}
```

Resposta:

```json
{"id":"UUID","name":"Cliente Exemplo","email":"cliente@example.com","role":"CUSTOMER"}
```

O cadastro não faz login automaticamente. Faça login em seguida. A sessão do dono é separada da sessão do cliente, mesmo se o navegador possuir ambas.

## Endereços do cliente

| Método | Caminho | Uso |
|---|---|---|
| GET | /api/addresses | Listar próprios |
| POST | /api/addresses | Criar |
| PUT | /api/addresses/{id} | Editar próprio |
| DELETE | /api/addresses/{id} | Excluir próprio |

```json
{"street":"Rua Exemplo, 10, apto 2","city":"São Paulo","state":"SP","postalCode":"01001000"}
```

CEP tem oito dígitos, UF duas letras maiúsculas. Isso valida o formato, não consulta os Correios.
Um pedido mantém a cópia do endereço mesmo se o cadastro for alterado/excluído.

## Catálogo público

| Método | Caminho | Uso |
|---|---|---|
| GET | /api/catalog/products | Publicados com filtros |
| GET | /api/catalog/products/{id} | Detalhe publicado |

Exemplo: /api/catalog/products?audience=WOMEN&category=Blazers&search=Classic&page=0.

Públicos: WOMEN, MEN, UNISEX. WOMEN/MEN incluem UNISEX. Sem público, retorna todos os publicados.
Categoria é comparada sem diferenciar maiúsculas. Busca por nome aceita os curingas SQL % e _.

## Administração de catálogo — OWNER/CATALOG

| Método | Caminho | Uso |
|---|---|---|
| GET/POST | /api/admin/products | Listar todos os estados / criar |
| GET/PUT | /api/admin/products/{id} | Consultar / editar |
| POST | /api/admin/products/{id}/publish | Publicar |
| DELETE | /api/admin/products/{id} | Arquivar |
| POST | /api/admin/products/{id}/clone | Clonar para rascunho |
| POST | /api/admin/products/{id}/variants | Criar variante |
| PUT | /api/admin/variants/{id} | Editar SKU, tamanho, cor e estoque |
| PUT | /api/admin/variants/{id}/stock | Definir estoque disponível |

Criar/editar produto (PUT substitui os campos do contrato):

```json
{
  "name":"Blazer Classic",
  "description":"Blazer de alfaiataria",
  "slug":"blazer-classic",
  "category":"Blazers",
  "collectionName":"Essenciais",
  "care":"Lavagem conforme etiqueta",
  "audience":"UNISEX",
  "bodyPart":"TORSO",
  "price":499.90,
  "promotionalPrice":449.90,
  "images":["https://seu-cdn.example/blazer.jpg"],
  "videoUrl":null
}
```

Partes do corpo: TORSO, LEGS, FEET, FULL_BODY, ACCESSORIES.
Preço deve ser positivo; promoção não pode superar preço normal.
Publicação exige imagem e pelo menos uma variante. Rascunho pode ter images vazio.
URLs externas precisam ser HTTPS; fotos enviadas pelo backend usam /api/media/UUID.png ou .jpg.
As URLs externas não são baixadas/validadas pelo servidor. O frontend deve tratar indisponibilidade e configurar os domínios permitidos.

Variante:

```json
{"sku":"BLZ-CLASSIC-M-PRETO","size":"M","color":"Preto","stock":10}
```

Ajuste de estoque:

```json
{"quantity":8}
```

O ajuste define quantidade disponível, não adiciona oito unidades. Clonagem copia variantes com SKU novo e estoque zero. Produtos arquivados continuam no histórico; podem ser editados e republicados.

## Mídia

- POST /api/admin/media: OWNER/CATALOG, multipart/form-data, campo file.
- GET /api/media/{nome}: público.
- Limites: JPEG/PNG real, até 5 MB, proporção 3:4, entre 600x800 e 3000x4000 pixels.
- Resposta: {"url":"/api/media/UUID.png","width":600,"height":800}.
- Vídeo é cadastrado por URL HTTPS. Upload/transcodificação de vídeo ficam para integração de armazenamento.

## Carrinho

| Método | Caminho | Uso |
|---|---|---|
| POST | /api/cart | Criar carrinho vazio |
| GET | /api/cart | Consultar |
| PUT | /api/cart/items/{variantId} | Definir quantidade |
| DELETE | /api/cart/items/{variantId} | Remover |

Guarde token da resposta de criação. Nas demais operações, envie X-Cart-Token com esse UUID.
Requisições de escrita exigem CSRF, mesmo para visitante.

```json
{"quantity":2}
```

Quantidade zero remove. Até 99 unidades por variante e 50 variantes por carrinho.
O carrinho informa preços atuais e disponibilidade, mas somente o checkout reserva estoque.
Após login, reaproveite o mesmo token. Após checkout, crie outro carrinho para uma nova compra.

## Pedidos do cliente

| Método | Caminho | Uso |
|---|---|---|
| POST | /api/orders | Checkout; X-Cart-Token obrigatório |
| GET | /api/orders | Meus pedidos |
| GET | /api/orders/{id} | Meu pedido |
| POST | /api/orders/{id}/cancel | Cancelar enquanto aguarda pagamento |

```json
{"addressId":"UUID-DO-ENDERECO","idempotencyKey":"uma-chave-unica-por-compra"}
```

Use uma chave nova por compra, até 80 caracteres. Se houver timeout, repita exatamente a mesma requisição e chave. O retorno terá o mesmo pedido.
Outra chave não permite reutilizar carrinho consumido. Outra pessoa não pode consultar o pedido.

Resposta contém id, customerId, status, total, currency, shippingAddress, trackingCode, createdAt, expiresAt e lines.
Cada linha contém variantId, productName, sku, quantity e unitPrice.

Estados:

```text
AWAITING_PAYMENT -> PAID -> SHIPPED -> DELIVERED
        |             |
        v             v
    CANCELLED      REFUNDED
```

Sem pagamento, expira após 30 minutos. O job roda a cada minuto e processa até 100 pedidos por rodada.
Estorno automático de mercadoria já enviada/entregue não faz parte do fluxo entregue.

## Pedidos administrativos — OWNER

| Método | Caminho | Uso |
|---|---|---|
| GET | /api/admin/orders | Todos os pedidos |
| GET | /api/admin/orders/{id} | Detalhe |
| POST | /api/admin/orders/{id}/cancel | Cancelar pendente |
| POST | /api/admin/orders/{id}/ship | {"trackingCode":"CODIGO"} |
| POST | /api/admin/orders/{id}/deliver | Marcar entregue |

## Simulações — somente perfil dev e OWNER

- POST /api/admin/dev/orders/{id}/pay.
- POST /api/admin/dev/orders/{id}/refund.

Pagamento registra um recebimento e uma solicitação fiscal. Repetição não duplica os lançamentos.
Estorno registra uma saída e devolve estoque. Notas pendentes são canceladas.
Nenhum valor é movimentado fora do banco de desenvolvimento.

## Financeiro/fiscal — OWNER/FINANCE

| Método | Caminho | Uso |
|---|---|---|
| GET | /api/admin/finance/summary | Recebido, estornado, despesas e saldo |
| GET | /api/admin/finance/entries | Histórico paginado |
| POST | /api/admin/finance/expenses | {"amount":100.00,"description":"Embalagens"} |
| GET | /api/admin/invoices | Solicitações fiscais |
| POST | /api/admin/invoices/{id}/retry | Nova tentativa |

Lançamentos PAYMENT, REFUND e EXPENSE armazenam valor positivo; o resumo subtrai estornos/despesas. O saldo não representa lucro contábil.
Solicitações possuem status, attempts, nextAttemptAt, lastError e documentUrl. O adaptador atual não emite notas e deixa documentUrl nulo.

## CRM — OWNER

- GET /api/admin/crm/customers: clientes paginados.
- GET /api/admin/crm/customers/{id}/orders: histórico.
- GET /api/admin/crm/customers/{id}/notes: notas internas.
- POST /api/admin/crm/customers/{id}/notes: {"content":"Cliente prefere contato à tarde"}.

Evite registrar dados sensíveis desnecessários em notas.

## Administração — OWNER

- GET /api/admin/dashboard: total de pedidos, aguardando pagamento e variantes com estoque <= 5.
- GET /api/admin/users: usuários sem senha/hash.
- POST /api/admin/users: cria equipe com name, email, password e role (CATALOG ou FINANCE).
- GET /api/admin/audit: ator, ação, alvo e data.

Alterar/revogar permissões de usuários existentes e gerenciar múltiplos proprietários são itens do backlog. O bootstrap não promove uma conta já existente.

## Exemplo PowerShell: login de cliente

```powershell
$base = "http://localhost:8080"
$csrf = Invoke-RestMethod "$base/api/auth/csrf" -SessionVariable loja
$headers = @{}
$headers[$csrf.headerName] = $csrf.token
$email = Read-Host "E-mail"
$senha = [System.Net.NetworkCredential]::new("", (Read-Host "Senha" -AsSecureString)).Password
$json = @{ email = $email; password = $senha } | ConvertTo-Json
Invoke-RestMethod "$base/api/auth/login" -Method Post -WebSession $loja -Headers $headers -ContentType "application/json" -Body $json
Invoke-RestMethod "$base/api/auth/me" -WebSession $loja
```

Para o proprietário, use /api/admin/auth/csrf e /api/admin/auth/login e guarde uma sessão PowerShell separada.
