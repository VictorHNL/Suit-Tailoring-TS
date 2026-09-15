# Relatório de verificação

Data: 15/09/2026.

## Resultados

| Verificação | Resultado |
|---|---|
| Suíte com PostgreSQL 17.11 temporário | 27 testes, zero falhas, zero erros, zero ignorados |
| Maven verify com H2 2.4.240 | 27 testes, zero falhas, zero erros, zero ignorados |
| Migrações Flyway | V1 a V4 aplicadas nos dois bancos |
| Hibernate validate | Schema validado nos dois bancos |
| JAR Spring Boot | Gerado em backend-java/target/backend-java-0.0.1-SNAPSHOT.jar |
| Scripts PowerShell | Sintaxe analisada sem erros; test-postgres executado |
| Docker/Compose | Arquivos fornecidos; Docker não disponível na máquina para execução |
| GitHub Actions | Workflow fornecido; execução remota não verificada |

## O que os testes verificam

| Classe | Quantidade | Foco |
|---|---:|---|
| AccountServiceTest | 2 | Hash, papel CUSTOMER e duplicidade com Mockito |
| ApiSecurityTest | 5 | Catálogo público, acesso administrativo e CSRF |
| BackendJavaApplicationTests | 1 | Inicialização completa |
| ProductTest | 2 | Alteração de nome e rejeição de nome vazio |
| VariantTest | 3 | Reserva/devolução e validação de construção |
| CheckoutIntegrationTest | 5 | Idempotência, rollback, isolamento, concorrência e UNISEX |
| FiscalServiceTest | 1 | Provedor ausente não gera nota fictícia |
| LoginIntegrationTest | 2 | Sessões reais de cliente/equipe via MockMvc |
| MediaTest | 2 | Conteúdo inválido e nome de arquivo gerado |
| PaymentIntegrationTest | 1 | Pagamento/estorno idempotentes e ledger |
| AuthRateLimitFilterTest | 1 | Limite por janela e reinício da contagem |
| ShopFlowIntegrationTest | 1 | Produto → carrinho → checkout → simulação → histórico |
| WorkerContextTest | 1 | Processo worker sem servidor HTTP |

MockMvc executa o fluxo HTTP dentro do contexto Spring sem abrir uma porta TCP. Os testes de persistência/concorrência usam o PostgreSQL de verdade quando executados pelo script.

## Defeito encontrado e corrigido

O H2 aceitou a consulta inicial com filtros nulos, mas o PostgreSQL rejeitou lower(bytea).
ProductRepository agora usa Specification/Criteria para construir apenas os predicados informados.
A suíte inteira foi repetida no PostgreSQL após a correção e passou.

## Ambiente e preservação de dados

- JDK Temurin 25 já instalado foi utilizado.
- O PostgreSQL 17 existente forneceu os executáveis para clusters temporários.
- Cada cluster temporário usou loopback e porta 55433 e foi encerrado pelo script.
- Nenhuma credencial do banco existente foi necessária.
- Os diretórios de teste e logs permanecem em target, ignorados pelo Git.
- Dependências Maven e o formatador Google Java Format foram baixados para target.
- Senhas, mídia e arquivos de build não fazem parte dos commits.

## Limites da evidência

Os testes cobrem cenários críticos, não todos os casos possíveis. Não houve teste de carga, auditoria externa de segurança, homologação fiscal/bancária, execução de Docker ou validação de alta disponibilidade.
Não há alegação de prontidão para vendas reais sem completar as integrações e o backlog de produção.
