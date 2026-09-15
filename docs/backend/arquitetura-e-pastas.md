# Arquitetura, pastas e orientação a objetos

## 1. Visão geral

O backend começa como monólito modular, conforme a documentação do MVP. Cada módulo representa um assunto do negócio. API e worker usam o mesmo artefato Java e selecionam comportamentos por perfil.

```text
Frontend (mesma origem / proxy)
        |
        v
API Spring Boot ---> PostgreSQL <--- Worker Java
   |                  |               |
   |                  |               +-- expira pedidos
   |                  +-- fila fiscal +-- tenta processar solicitações
   |
   +-- armazenamento local de imagens
```

O processo worker não abre servidor HTTP. Se ele parar, o catálogo continua acessível; reservas vencidas e a fila fiscal aguardam sua volta. Se a API parar, o worker pode continuar. Se o banco parar, ambos são afetados.

## 2. A árvore de pastas

```text
Suit Tailoring TS/
├── backend-java/
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   ├── .mvn/wrapper/
│   ├── scripts/
│   ├── Dockerfile
│   ├── src/main/java/br/com/suittailoring/
│   │   ├── BackendJavaApplication.java
│   │   ├── accounts/       # contas, segurança e endereços
│   │   ├── catalog/        # produtos, variantes e estoque
│   │   ├── cart/           # carrinho de visitante/cliente
│   │   ├── orders/         # checkout e ciclo do pedido
│   │   ├── finance/        # lançamentos e simulações
│   │   ├── fiscal/         # fila, adaptador fiscal e worker
│   │   ├── crm/            # relacionamento e notas
│   │   ├── operations/     # dashboard, equipe e auditoria
│   │   ├── media/          # upload e leitura de imagens
│   │   └── shared/         # erros HTTP e configuração comum
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-dev.properties
│   │   ├── application-worker.properties
│   │   └── db/migration/
│   ├── src/test/java/      # testes espelhando pacotes
│   ├── src/test/resources/
│   ├── storage/           # imagens locais; ignorado pelo Git
│   └── target/            # compilação, relatórios e cache; ignorado
├── docs/
│   ├── backend/
│   ├── architecture/
│   └── sprints/
├── compose.yaml
└── .github/workflows/backend.yml
```

Dentro dos módulos maiores:

| Pasta | Responsabilidade | Exemplo |
|---|---|---|
| domain | Estado e regras dos objetos de negócio | Variant.reserve rejeita estoque insuficiente |
| application | Coordena um caso de uso e sua transação | OrderService.checkout |
| infrastructure | Acesso ao banco, configuração e adaptadores | OrderRepository, FiscalWorker |
| api | HTTP, validação e respostas | OrderController |

Nem todo módulo precisa de todas as pastas. Evitamos diretórios vazios que não têm responsabilidade implementada.

## 3. package não é uma permissão

```java
package br.com.suittailoring.catalog.domain.model;
```

Esse nome identifica o pacote da classe. O arquivo fica dentro de src/main/java/br/com/suittailoring/catalog/domain/model. O Maven considera src/main/java a raiz do código.

Um import permite escrever Product em vez do nome completo. O acesso continua dependendo de public, protected, private ou visibilidade de pacote.

Pastas de documentação e de imagens não precisam de package. Pacotes pertencem ao código Java.

## 4. POO aplicada

- **Classe:** Product descreve um produto; Variant descreve uma combinação de tamanho/cor.
- **Objeto:** new Product cria uma instância com estado próprio.
- **Construtor:** inicializa o objeto e já valida o nome. Não permite iniciar um produto com nome vazio.
- **Encapsulamento:** stock é privado. reserve e release expressam operações válidas.
- **Getter:** getStock permite consultar o estado sem atribuir diretamente a ele.
- **Composição:** um pedido possui linhas; não precisa herdar de produto.
- **Interface/polimorfismo:** FiscalGateway define uma operação; um futuro adaptador implementará a chamada ao fornecedor.
- **Enum:** Status restringe estados conhecidos. Um pedido pago não pode ser cancelado como se ainda estivesse aguardando pagamento.
- **Record:** DTOs agrupam dados de entrada/saída sem representar uma entidade persistente.
- **Injeção de dependência:** Spring entrega os repositórios pelo construtor dos serviços. Nos testes, Mockito pode entregar substitutos.

Exemplo real:

```java
variant.reserve(2);
```

É mais expressivo que espalhar uma atribuição stock = stock - 2 em vários controllers. O método centraliza a regra; a transação e o bloqueio de banco completam a proteção contra concorrência.

## 5. Clean Architecture: escolhas e concessões

Esta implementação usa separação por responsabilidade e domínio, mas não é uma Clean Architecture estrita.

- Entidades possuem anotações JPA.
- Serviços dependem de interfaces Spring Data localizadas na infraestrutura.
- Alguns DTOs estão no pacote api e são usados por serviços.
- Módulos colaboram diretamente na mesma transação, especialmente no checkout.

Essas concessões reduzem código intermediário no MVP. Para uma versão estritamente independente do framework, a evolução seria criar portas de repositório na aplicação/domínio, adaptar Spring Data na infraestrutura, separar modelos JPA dos objetos puros e mover DTOs de caso de uso para application. O FiscalGateway já demonstra uma porta para integração externa.

## 6. Como o checkout funciona

1. Resolve o cliente pela sessão, nunca por customerId enviado no JSON.
2. Bloqueia o carrinho e verifica o proprietário, quando já associado.
3. Procura pedido com a mesma chave de idempotência.
4. Confirma que o endereço pertence ao cliente.
5. Bloqueia produtos e variantes em ordem de UUID.
6. Revalida publicação, preço e disponibilidade.
7. Reserva estoque e cria linhas com nome, SKU e preço copiados.
8. Persiste pedido e auditoria na mesma transação.

Se qualquer etapa falhar, o banco desfaz todo o conjunto. O teste concorrente executa duas compras para uma unidade e espera apenas uma conclusão.

O estoque representa quantidade **disponível**, já descontada das reservas. Não é um inventário físico detalhado com depósitos, lotes e movimentações contábeis.

## 7. JPA, Hibernate e Flyway

- @Entity marca uma classe persistida.
- @Id identifica a chave primária; UUID permite gerar identificadores na aplicação.
- @ManyToOne liga variante e produto.
- @ElementCollection persiste imagens, itens de carrinho e linhas de pedido.
- @Version permite controle otimista de versão do produto.
- @Lock(PESSIMISTIC_WRITE) coordena operações que não podem vender a mesma unidade.
- @Transactional define a unidade de trabalho.
- Flyway executa V1, V2, V3 e V4 em ordem e guarda checksums.
- ddl-auto=validate faz Hibernate conferir o schema, sem criar/alterar tabelas por conta própria.

Nunca edite uma migração já aplicada em um banco compartilhado. Crie V5, V6 etc.

## 8. Segurança e fronteiras

A sessão HTTP usa cookie HttpOnly. CSRF protege requisições que alteram dados. /api/auth e /api/admin/auth usam chaves de contexto distintas na sessão. Login renova o ID da sessão.

OWNER tem acesso administrativo; CATALOG opera produtos/mídia; FINANCE opera financeiro/fiscal. Cadastro público ignora qualquer tentativa de obter um papel administrativo porque seu contrato não aceita esse campo.

O token de carrinho funciona como uma credencial para aquele carrinho enquanto visitante. Não coloque em URL, logs ou analytics. Guarde-o no cliente e envie em X-Cart-Token. Após checkout, o carrinho também fica associado ao cliente.

Existe limitação simples de 20 requisições/minuto por IP e endpoint para login, cadastro e criação de carrinho. Em múltiplas instâncias, deve ser substituída/complementada por limite no gateway. O código usa o endereço remoto, sem confiar automaticamente em X-Forwarded-For.

## 9. Próxima etapa distribuída

Hoje há dois processos e um banco compartilhado. Para microserviços independentes:

1. Extrair fiscal e mídia, com contratos versionados.
2. Substituir consulta de fila no banco por outbox/eventos e consumidor com deduplicação.
3. Adicionar timeouts, tentativas, circuit breaker e métricas por provedor.
4. Separar os bancos por responsabilidade.
5. Compartilhar sessões via Spring Session/Redis ou adotar um provedor de identidade.
6. Projetar compensações para pagamentos/estoque, sem depender de transações entre bancos.

Separar pastas não cria tolerância a falhas por si só. Deploy, dados e operação também precisam ser separados.
