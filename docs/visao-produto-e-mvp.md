# E-commerce de Alfaiataria — Visão do Produto e MVP

**Status:** proposta inicial  
**Data:** 8 de setembro de 2026

## 1. Objetivo

Criar uma loja de moda/alfaiataria com experiência editorial premium, catálogo para público feminino, masculino e unissex, compra online e uma área administrativa exclusiva do proprietário.

O site terá como referência de atmosfera o [Norwegian Rain](https://norwegianrain.com/): fotografia grande, navegação contida, linguagem sofisticada e foco no produto. A referência não será copiada: marca, textos, imagens, estrutura visual e interações serão próprios.

## 2. Experiência da loja

### Início e catálogo

- Escolha inicial: **Mulher** ou **Homem**; produtos unissex aparecem nos dois recortes.
- Hero visual próprio e navegação limpa: coleção, busca, conta e bolsa.
- Vitrine horizontal de peças com efeito de “lupa”: o item mais próximo do centro cresce gradualmente; os demais diminuem proporcionalmente.
- Arrastar com mouse ou toque, com inércia suave, navegação por teclado e respeito à preferência de reduzir movimento.
- Ao pausar sobre uma peça, um vídeo curto e silencioso pode substituir a foto. Em celular, o vídeo entra por toque, nunca por hover.
- Página de produto: galeria, vídeo opcional, preço, tamanhos, disponibilidade, composição/cuidados e adição à bolsa.

### Compra e conta

- Qualquer pessoa vê o catálogo e monta a bolsa sem login.
- Ao avançar para checkout, a pessoa é direcionada para entrar ou criar conta; após autenticar, retorna ao checkout com a bolsa preservada.
- Conta do cliente: dados pessoais, endereços, pedidos e rastreio.
- Pagamento, frete e emissão fiscal serão integrados em uma fase posterior do MVP, após definição de operação e fornecedor.

### Área do proprietário

Rota e autenticação separadas para a equipe, com controle de permissões. O proprietário terá uma dashboard com atalhos para:

- Produtos e estoque;
- Pedidos e clientes (CRM básico);
- Financeiro;
- Notas fiscais;
- Administração de usuários e permissões.

Não basta diferenciar uma tela: as permissões precisam ser aplicadas no servidor, e a área administrativa não deve compartilhar sessão/rotas públicas sem proteção adequada.

## 3. Cadastro de produtos

Cada produto deverá permitir:

- Criar, editar, arquivar/excluir e clonar;
- Nome, slug, descrição, categoria e coleção;
- Público: feminino, masculino ou unissex;
- Parte do corpo (ex.: tronco, pernas, pés, acessórios);
- Preço, preço promocional opcional e moeda;
- Fotos em padrão definido, ordenação e vídeo opcional;
- Variações de tamanho/cor; estoque e SKU por variação;
- Status: rascunho, publicado ou arquivado.

**Decisão recomendada:** arquivar em vez de apagar definitivamente produtos que já tiveram pedido. Isso protege o histórico financeiro e fiscal.

## 4. MVP — o que entra na primeira versão

O MVP valida venda, experiência visual e operação mínima. Ele inclui:

1. Home premium com filtro Mulher/Homem e vitrine de lupa arrastável.
2. Catálogo, filtros essenciais e página de produto.
3. Bolsa persistente, login/cadastro de cliente e checkout protegido.
4. Pedido de teste (pagamento simulado até escolher o provedor real).
5. Painel do proprietário para CRUD/clonagem de produtos, mídia, tamanhos e estoque.
6. Dashboard simples: pedidos, faturamento estimado e estoque baixo.
7. CRM básico: lista de clientes e histórico de pedidos.
8. Base de segurança: perfis, permissões, logs importantes e conformidade inicial com LGPD.

Ficam **fora do MVP**, mas previstos para fases posteriores: conciliação financeira completa, emissão real de NF-e/NFC-e, automações de CRM, cupons avançados, multi-idioma/moeda, marketplace, recomendação por IA e microserviços independentes.

## 5. Arquitetura recomendada

### Começar modular, não fragmentado

Para uma primeira versão, a melhor escolha não é iniciar com muitos microserviços. Eles elevam bastante a complexidade de deploy, observabilidade, filas, autenticação entre serviços e consistência de estoque/pagamentos. Em vez disso, começaremos com um **monólito modular**: uma aplicação única, com módulos isolados por domínio e APIs bem definidas. Ela pode ser extraída gradualmente quando houver carga e necessidades reais.

```text
Next.js (loja e painel)  ── HTTPS ──>  Spring Boot
                                          ├─ contas e permissões
                                          ├─ catálogo e mídia
                                          ├─ carrinho e pedidos
                                          ├─ estoque
                                          ├─ CRM
                                          └─ financeiro/fiscal (adaptadores)
                                                     │
                              PostgreSQL  <──────────┼──────────>  Redis/fila
                                                     │
                                               armazenamento de imagens
```

Quando a operação justificar, os candidatos naturais a serviços independentes são: busca/catálogo, processamento de mídia, notificações, fiscal e integrações de pagamento. Cada extração será feita atrás de uma fila/API e sem quebrar a loja.

### Stack proposta

| Camada | Escolha | Motivo |
|---|---|---|
| Front-end | Next.js + TypeScript + Tailwind CSS | Performance, SEO, interfaces ricas e manutenção previsível. |
| Interações | Motion/Framer Motion + drag nativo | Dá fluidez ao carrossel de lupa sem sacrificar acessibilidade. |
| Back-end | Java 25 LTS + Spring Boot | POO forte, segurança, transações, APIs REST e base modular preparada para evolução distribuída. |
| Banco | PostgreSQL | Transações robustas para estoque, pedidos e pagamentos. |
| Tarefas assíncronas | Celery + Redis | E-mails, thumbnails, sincronizações e emissão fiscal sem travar a compra. |
| Arquivos | S3 compatível | Fotos e vídeos fora do servidor de aplicação. |
| Pagamento/fiscal | Adaptadores para provedores brasileiros | Evita acoplamento e permite trocar fornecedor. |

O painel final do dono será uma experiência própria no front-end. O Spring Boot fornece a base para segurança, APIs, persistência e observabilidade, enquanto o backoffice é construído de acordo com as necessidades da operação.

## 6. Modelo de dados inicial

```text
Usuário ──< Endereço
Usuário ──< Pedido ──< Item do pedido >── Variante do produto >── Produto
Produto ──< Mídia
Produto ──< Variante (tamanho, cor, SKU, preço, estoque)
Produto >── Categoria
Produto >── Público (feminino | masculino | unissex)
```

O preço e o nome do produto também serão copiados para o item do pedido no momento da compra, preservando o histórico se o catálogo mudar depois.

## 7. Segurança e operação

- Senhas com hash seguro; autenticação por sessão HTTP-only ou tokens de curta duração.
- Papéis mínimos: cliente, operador de catálogo, financeiro e proprietário.
- Registro de alterações de produto, preço e estoque.
- Validação de estoque no servidor durante a confirmação do pedido.
- Backup do banco, monitoramento de erros e health checks por módulo.
- LGPD: consentimento quando aplicável, política de privacidade, dados mínimos e processo de exportação/remoção de dados.

## 8. Plano de construção colaborativa

Vamos construir o back-end em Java em pequenos blocos, explicando as decisões e revisando cada etapa antes da próxima:

1. Preparar JDK 25, Maven Wrapper, Spring Boot e PostgreSQL; validar o projeto e os testes.
2. Modelar usuários, papéis e login separado do proprietário.
3. Implementar catálogo, categorias, variações, mídia e estoque.
4. Criar APIs de catálogo e bolsa; conectar ao front-end.
5. Implementar pedidos e checkout de teste.
6. Construir painel do dono e dashboard básica.
7. Integrar pagamento, frete e fiscal após escolher os parceiros.
8. Publicar, monitorar e evoluir para serviços separados somente onde necessário.

## 9. Decisões necessárias antes de codificar o checkout real

- Nome da marca, domínio e identidade visual (logo, cores, tom de voz).
- País/estado de operação e regime fiscal.
- Gateway de pagamento, transportadoras e provedor de nota fiscal.
- Política de troca/devolução e regras de frete.
- Padrão de fotos e vídeos: proporção, duração, formatos e qualidade.

## 10. Critérios de aceite do MVP

- Cliente consegue encontrar uma peça por recorte de público, abrir detalhes, escolher tamanho e colocar na bolsa.
- Cliente sem sessão é levado ao login/cadastro no checkout e retorna com a bolsa intacta.
- Proprietário não pode ser acessado por conta de cliente e consegue criar, alterar, clonar e arquivar um produto com variantes e mídia.
- Estoque não fica negativo durante pedidos simultâneos.
- A vitrine horizontal funciona com mouse, toque e teclado; o movimento não compromete a navegação em celular.
