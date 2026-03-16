# Gestão de Extintores

## Executive Summary

Gestão de Extintores é uma aplicação web corporativa voltada à governança operacional de extintores distribuídos em múltiplas filiais. O sistema consolida cadastro, alocação, fluxo de manutenção, remessas para recarga e administração por perfil em uma única plataforma server-side construída com Java Servlet, JSP e DAO.

A solução atende organizações que precisam de rastreabilidade, disciplina operacional e auditabilidade ao longo do ciclo de vida dos extintores. A implementação atual suporta tanto controle administrativo centralizado quanto execução técnica no nível da filial.

## Business Context

A gestão de extintores é um processo sensível do ponto de vista operacional e de conformidade. Em termos práticos, a organização precisa saber:

- onde cada extintor está localizado
- a qual filial e setor ele pertence
- quais são suas características técnicas e sua validade
- se ele está operacional, em remessa ou em recarga
- quem iniciou, aprovou ou concluiu cada movimentação do fluxo

Gestão de Extintores organiza esse domínio em cinco capacidades principais:

- administração de filiais
- administração de setores
- administração de usuários e perfis
- controle de inventário de extintores
- gestão do fluxo de remessa e recarga

## Enterprise Value Proposition

Gestão de Extintores gera valor ao reduzir ambiguidade operacional e aumentar a rastreabilidade do ciclo de vida dos ativos.

- Melhora a visibilidade da distribuição de extintores entre filiais e setores.
- Reduz esforço manual no acompanhamento de remessas e recargas.
- Reforça responsabilização ao vincular ações a usuários autenticados.
- Ajuda a padronizar a execução entre perfis administrativos e técnicos.
- Cria base para evoluções em compliance, auditoria e relatórios operacionais.

## Core Domain Features

- autenticação com sessão
- modelo de perfis com `Admin` e `Técnico`
- CRUD de `Filial`
- CRUD de `Setor`
- CRUD de `Usuário`
- CRUD de `Extintor`
- criação de remessas a partir de extintores selecionados
- aprovação administrativa do recolhimento
- confirmação técnica de recolhimento
- finalização do recebimento com data de recarga e nova validade
- escopo por filial para usuários técnicos em módulos relevantes

## Security Model

O modelo de segurança atual é pragmático e aplicado no backend.

- A autenticação é baseada em sessão, via `LoginServlet`.
- O `AuthenticationFilter` protege as rotas não públicas.
- As entradas públicas estão restritas a `login.jsp` e `LoginServlet`.
- Ações administrativas são restritas ao perfil `Admin`.
- Usuários técnicos são limitados por filial em módulos como `extintor` e `remessa`.
- Transições sensíveis do fluxo de remessa são validadas no servidor.
- O hardening recente adicionou proteções contra:
  - autoexclusão do administrador logado
  - combinações inválidas entre perfil técnico e filial
  - falhas silenciosas em operações de usuário, filial e setor
  - transições inválidas de remessa por status

Este ainda não é um modelo completo de IAM corporativo. Hoje não há provedor externo de identidade, mecanismo centralizado de RBAC, camada de CSRF ou framework de autorização fina.

## Architecture - C4 Level 1

### Contexto do Sistema

No C4 Nível 1, a solução é uma única aplicação web server-side que interage com usuários humanos e com um banco PostgreSQL.

```text
+-------------------+         +-----------------------------------+         +----------------------+
| Usuário Admin     | ----->  | Aplicação Gestão de Extintores   | ----->  | Banco PostgreSQL     |
| Usuário Técnico   | <-----  | Monólito Servlet + JSP + DAO     | <-----  | Persistência         |
+-------------------+         +-----------------------------------+         +----------------------+
```

### Atores Externos

- `Admin`
  - gerencia usuários, filiais, setores e aprovações operacionais
- `Técnico`
  - executa atividades operacionais de extintor e remessa dentro do escopo da filial

### Sistema Externo

- `PostgreSQL`
  - armazena usuários, filiais, setores, extintores, remessas e itens de remessa

## Architecture - Application Structure

A solução atual é melhor descrita como um monólito web Java clássico em camadas.

### Estilo Arquitetural Atual

- camada de apresentação com `JSP`
- orquestração de requisições com `Servlets`
- lógica de persistência em classes `DAO`
- entidades de domínio em `model`
- proteção global de requisições em `AuthenticationFilter`

Hoje o projeto não implementa Arquitetura Hexagonal de forma real. Não existem portas e adaptadores formalizados, camada de aplicação explícita nem isolamento do domínio em torno de casos de uso.

### Evolução Conceitual para Hexagonal

Se o projeto evoluir nessa direção, um caminho natural seria:

- mover regras de negócio dos servlets para serviços de aplicação
- isolar persistência atrás de interfaces de repositório
- manter servlets e JSPs como adaptadores de entrega
- manter DAOs PostgreSQL como adaptadores de infraestrutura

Essa evolução deixaria a aplicação mais testável, mais modular e mais próxima de um desenho hexagonal. Por enquanto, este README documenta a arquitetura como ela realmente existe, e não como meta aspiracional.

## Technology Stack

- Java 8
- Java Servlet API
- JSP com JSTL
- Apache Ant
- estrutura de projeto NetBeans
- PostgreSQL
- GlassFish Server 5.x
- Bootstrap 5 no frontend

## Local Execution

### Pré-requisitos

- JDK 8
- Apache Ant
- GlassFish Server
- PostgreSQL local em execução
- banco `gestao_extintores`

### Configuração Atual de Banco

No estado atual, a conexão com banco está configurada diretamente no código-fonte em [ConnectionFactory.java](C:/GestaoExtintores/GestaoExtintores/src/java/br/com/gestaoextintores/util/ConnectionFactory.java).

- Host: `localhost`
- Porta: `5432`
- Banco: `gestao_extintores`
- Usuário: `postgres`

Para operação com padrão de produção, essa configuração deve ser externalizada por ambiente.

### Build

A partir da raiz do repositório:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_202'
& 'C:\Program Files\NetBeans-12.1\netbeans\extide\ant\bin\ant.bat' clean dist
```

Artefato gerado:

- `dist/GestaoExtintores.war`

### Deploy

Exemplo de deploy no GlassFish:

```powershell
& 'C:\Users\Voce2\GlassFish_Server\bin\asadmin.bat' deploy --force=true --contextroot GestaoExtintores 'C:\GestaoExtintores\GestaoExtintores\dist\GestaoExtintores.war'
```

### URL da Aplicação

- `http://localhost:8080/GestaoExtintores/`

### Configuração de SMTP

O fluxo de remessa suporta:

- geração automática de PDF ao criar remessa
- armazenamento do PDF no banco de dados
- envio de e-mail com o PDF em anexo
- reenvio manual do e-mail pelo detalhe da remessa, para perfil `Admin`

As configurações SMTP são lidas por propriedades JVM ou variáveis de ambiente. No ambiente local validado, o e-mail fixo do admin para remessas está configurado como `justinavirtual@gmail.com`.

Propriedades suportadas:

- `gestao.remessa.admin.email`
- `gestao.mail.host`
- `gestao.mail.port`
- `gestao.mail.username`
- `gestao.mail.password`
- `gestao.mail.from`
- `gestao.mail.auth`
- `gestao.mail.starttls`

Variáveis de ambiente equivalentes:

- `GESTAO_REMESSA_ADMIN_EMAIL`
- `GESTAO_MAIL_HOST`
- `GESTAO_MAIL_PORT`
- `GESTAO_MAIL_USERNAME`
- `GESTAO_MAIL_PASSWORD`
- `GESTAO_MAIL_FROM`
- `GESTAO_MAIL_AUTH`
- `GESTAO_MAIL_STARTTLS`

Exemplo de configuração no GlassFish:

```powershell
& 'C:\Users\Voce2\GlassFish_Server\bin\asadmin.bat' create-jvm-options '"-Dgestao.remessa.admin.email=justinavirtual@gmail.com:-Dgestao.mail.host=smtp.gmail.com:-Dgestao.mail.port=587:-Dgestao.mail.username=justinavirtual@gmail.com:-Dgestao.mail.password=<APP_PASSWORD>:-Dgestao.mail.from=justinavirtual@gmail.com:-Dgestao.mail.auth=true:-Dgestao.mail.starttls=true"'
& 'C:\Users\Voce2\GlassFish_Server\bin\asadmin.bat' restart-domain domain1
```

Observações operacionais:

- para Gmail pessoal, use `App Password`, não a senha normal da conta
- em JDKs antigos, pode ser necessário importar a cadeia de confiança do servidor SMTP no truststore do GlassFish
- o sistema não cancela a criação da remessa se o envio de e-mail falhar
- quando houver falha, o `Admin` pode reenviar manualmente o e-mail a partir do detalhe da remessa

## Example Scenarios

### Cenário 1 - Estrutura organizacional

1. O administrador autentica na aplicação.
2. O administrador cadastra filiais e setores.
3. O administrador cria usuários e define perfil e filial quando aplicável.

### Cenário 2 - Criação de remessa pelo técnico

1. O técnico acessa a lista de extintores.
2. O técnico seleciona um ou mais extintores.
3. O sistema cria a remessa e atualiza o status dos extintores para `Em Remessa`.

### Cenário 3 - Aprovação administrativa

1. O administrador acessa o detalhe da remessa.
2. O administrador aprova o recolhimento.
3. O sistema move a remessa para a próxima etapa válida do fluxo.

### Cenário 4 - Finalização técnica do recebimento

1. O técnico confirma o recolhimento.
2. O técnico informa a data real da recarga e a nova validade.
3. O sistema conclui a remessa e atualiza os dados operacionais dos extintores.

## Project Structure

```text
src/
  java/
    br/com/gestaoextintores/
      controller/    -> Servlets
      dao/           -> Persistência
      filter/        -> Filtro de autenticação e acesso
      model/         -> Entidades de domínio
      util/          -> Utilitários de infraestrutura
web/
  extintor/         -> JSPs do módulo de extintores
  filial/           -> JSPs do módulo de filiais
  remessa/          -> JSPs do módulo de remessas
  setor/            -> JSPs do módulo de setores
  usuario/          -> JSPs do módulo de usuários
  index.jsp         -> Página inicial
  login.jsp         -> Página de login
build.xml           -> Entrada de build com Ant
nbproject/          -> Metadados do projeto NetBeans
```

## Roadmap

### Curto Prazo

- externalizar credenciais de banco
- centralizar verificações repetidas de autorização
- melhorar consistência de mensagens de erro e sucesso
- ampliar validações em CRUDs ainda não endurecidos
- expandir smoke tests para fluxos críticos

### Médio Prazo

- introduzir camada de serviços de aplicação
- reduzir duplicação de regras entre servlets e DAOs
- melhorar auditabilidade e relatórios operacionais
- fortalecer documentação de deploy e separação por ambiente

### Longo Prazo

- evoluir para fronteiras arquiteturais mais limpas
- habilitar regressão automatizada para fluxos de domínio
- preparar a base para integrações corporativas futuras

## Design Philosophy

O projeto segue uma filosofia pragmática e orientada à operação.

- manter a aplicação simples de executar em ambiente corporativo local
- privilegiar renderização server-side em vez de complexidade desnecessária no frontend
- aplicar regras críticas no backend, e não apenas na interface
- preservar rastreabilidade em transições de fluxo e ações sensíveis por perfil
- evoluir por hardening incremental antes de grandes reescritas

As manutenções recentes seguiram exatamente esse princípio: estabilizar a arquitetura atual primeiro e, em seguida, aumentar robustez em permissões, validações e transições de estado.

## Notes for Maintainers

O repositório passou recentemente por:

- endurecimento funcional em `usuario`, `filial`, `setor` e `remessa`
- normalização UTF-8 em JSPs e fontes Java
- limpeza de dados legados inválidos usados durante a validação

O código atual está materialmente mais estável do que a linha de base original, mas continua sendo um monólito clássico baseado em servlets. Melhorias futuras devem manter essa realidade explícita e evoluir a arquitetura de forma deliberada, não cosmética.
