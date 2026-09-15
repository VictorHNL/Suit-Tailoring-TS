# Backend Java — guia da entrega

Data: 15/09/2026.

O projeto implementa o backend do **MVP documentado**: catálogo, contas, permissões, carrinho, pedidos, estoque, CRM, dashboard, financeiro básico, gestão de mídia e fila fiscal.

## Comece aqui

1. [Executar no Windows, VS Code e Docker](execucao.md).
2. [Entender as pastas, POO e arquitetura](arquitetura-e-pastas.md).
3. [Consultar endpoints, exemplos e permissões](api.md).
4. [Estudar cada assunto com cursos e exercícios](trilha-de-estudos.md).
5. [Ver backlog e organização das sprints](../sprints/backlog.md).

## O que funciona

| Área | Comportamento entregue |
|---|---|
| Contas | Cadastro de cliente, login/logout, consulta de perfil e endereços |
| Equipe | Login separado, bootstrap do proprietário, criação de operadores CATALOG/FINANCE |
| Catálogo | Criar, consultar, editar, publicar, arquivar e clonar produtos |
| Variações | SKU, tamanho, cor e estoque disponível por variante |
| Filtros | Público, categoria, nome e paginação; UNISEX aparece em MEN e WOMEN |
| Mídia | Upload JPEG/PNG validado, galeria ordenada e URL HTTPS para vídeo |
| Carrinho | Persistência de visitante por token opaco; quantidades e subtotal |
| Pedidos | Checkout autenticado, endereço próprio, preço histórico, idempotência e concorrência |
| Estoque | Reserva transacional, cancelamento e expiração com devolução |
| Logística | Registro manual de rastreamento, envio e entrega |
| Financeiro | Recebimentos simulados, estornos simulados, despesas e saldo operacional |
| CRM | Clientes, histórico de pedidos e notas internas |
| Fiscal | Solicitações persistentes, consulta, tentativas e estado PENDING_PROVIDER |
| Operação | Auditoria, health endpoint, worker sem HTTP e CI para PostgreSQL |

## Limites explícitos

- Pagamento e estorno são **simulações exclusivas do perfil dev**, acionadas pelo proprietário. Nenhum cartão, Pix ou banco é cobrado.
- Nota fiscal real não é emitida. Faltam fornecedor, credenciais, dados da empresa/produtos e homologação.
- Frete não é cotado: o total do pedido representa apenas os produtos. Rastreamento é preenchido manualmente.
- O frontend/carrossel não faz parte desta entrega de backend.
- API e worker podem executar em processos separados, mas compartilham banco e código. Isso **não é uma arquitetura de microserviços independentes**.
- O financeiro é um controle operacional simples, sem conciliação bancária, impostos, contas a pagar parceladas ou contabilidade.
- Categorias/coleções são campos textuais controlados no produto; não há cadastro hierárquico independente.
- Não há e-mail transacional, recuperação de senha, verificação de e-mail, MFA, automação de marketing nem processo automatizado de exportação/anonimização de dados.
- Não se deve publicar uma loja real antes de fechar integrações, revisão de segurança, backup e condições operacionais.

Essas pendências estão separadas no backlog. A aplicação não simula sucesso de emissão fiscal nem fornece uma implementação fictícia de gateway real.

## Tecnologias usadas

- Java 25; Maven Wrapper; Spring Boot 4.1.1.
- Spring Web MVC: endpoints HTTP e conversão de JSON.
- Spring Security: sessão, permissões, BCrypt e CSRF.
- Jakarta Validation: validação dos contratos de entrada.
- JPA: anotações e contratos de persistência.
- Hibernate: implementação de JPA que traduz operações para SQL.
- Spring Data JPA: implementação dos repositórios.
- PostgreSQL 17; Flyway para evolução do schema.
- JUnit Jupiter, Mockito, MockMvc e H2 para testes rápidos.
- Docker Compose opcional; GitHub Actions para teste com PostgreSQL.

Não foram instalados serviços novos permanentes no Windows. O formatador Java e dependências Maven ficam no diretório ignorado target. A instância PostgreSQL de verificação é temporária e seu script a encerra ao finalizar.
