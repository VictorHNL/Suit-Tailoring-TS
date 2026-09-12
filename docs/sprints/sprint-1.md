# Sprint 1 — Autenticação e acesso

## Objetivo
Permitir que clientes criem conta e entrem na loja, enquanto proprietário e equipe acessam somente áreas autorizadas.

## Histórias
- Como cliente, quero criar uma conta com e-mail e senha.
- Como cliente, quero entrar na minha conta.
- Como proprietário, quero entrar na área administrativa.
- Como usuário autenticado, quero consultar meus dados atuais.

## Critérios de aceite
- E-mail é único.
- Senha nunca é salva em texto puro.
- Cliente não acessa endpoints administrativos.
- Proprietário consegue ser identificado pelo papel owner.
- Os endpoints possuem testes.
