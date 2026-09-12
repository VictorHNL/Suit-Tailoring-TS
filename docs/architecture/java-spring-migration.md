# Migração do backend para Java e Spring Boot

## Decisão

O novo backend será desenvolvido em Java 25 LTS com Spring Boot 4.1.1 e Maven. O backend Django permanece versionado como histórico de aprendizado e não será apagado nesta etapa.

## Estrutura inicial

`backend-java` é uma aplicação Spring Boot única, organizada como monólito modular. Cada domínio será implementado como um módulo independente antes de qualquer extração para microserviço.

Domínios planejados: identidade, catálogo, estoque, carrinho, pedidos, CRM, financeiro e notificações.

## Regras de evolução distribuída

- Não criar microserviços antes de existir uma necessidade mensurável.
- Cada módulo terá domínio, aplicação, infraestrutura e API.
- Uma futura extração de serviço terá banco de dados próprio e contrato de API/eventos explícito.
- Operações críticas usarão transações, idempotência, health checks e observabilidade.

## Stack

- Java 25 LTS
- Spring Boot 4.1.1
- Maven Wrapper
- Spring Web MVC
- Spring Data JPA e Hibernate
- Spring Security
- Bean Validation
- Flyway
- PostgreSQL via JDBC
- Spring Boot Actuator

## Banco de dados

O backend Java usará um banco PostgreSQL próprio, separado do banco usado no experimento Django. As migrações serão versionadas em `src/main/resources/db/migration` e executadas pelo Flyway.
