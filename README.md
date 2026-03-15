# GestaoExtintores

## Executive Summary

GestaoExtintores is an enterprise-oriented web application for operational governance of fire extinguisher assets across multiple branches. It consolidates registration, allocation, maintenance workflows, remittance for recharge, and role-based administration in a single server-side platform built with Java Servlet, JSP, and DAO patterns.

The solution is positioned for organizations that require traceability, procedural discipline, and auditability throughout the extinguisher lifecycle. Its current implementation supports both centralized administrative control and branch-level technical execution.

## Business Context

Fire extinguisher management is operationally sensitive and compliance-driven. Organizations need to know:

- where each extinguisher is located
- which branch and sector it belongs to
- its technical characteristics and validity date
- whether it is operational, in remittance, or in recharge
- who initiated and approved each movement in the maintenance cycle

GestaoExtintores addresses this by organizing the domain around five main capabilities:

- branch administration
- sector administration
- user and role administration
- extinguisher inventory control
- remittance and recharge workflow management

## Enterprise Value Proposition

GestaoExtintores generates value by reducing operational ambiguity and strengthening lifecycle traceability.

- Improves visibility of extinguisher distribution across branches and sectors.
- Reduces manual effort in recharge and remittance tracking.
- Strengthens accountability by associating actions with authenticated users.
- Helps standardize execution between administrative and technical roles.
- Establishes a platform for stronger compliance support, auditability, and future reporting.

## Core Domain Features

- Authentication with session-based access control
- Role model with `Admin` and `Técnico`
- CRUD for `Filial`
- CRUD for `Setor`
- CRUD for `Usuário`
- CRUD for `Extintor`
- Remittance creation from selected extinguishers
- Approval workflow for remittance recollection
- Confirmation of recollection by technical users
- Receipt finalization with recharge date and new validity date
- Branch-based data scoping for technical users in key modules

## Security Model

The current security model is pragmatic and server-side enforced.

- Authentication is session-based through `LoginServlet`.
- `AuthenticationFilter` protects all non-public routes.
- Public entry points are limited to `login.jsp` and `LoginServlet`.
- Administrative actions are restricted to `Admin`.
- Technical users are scoped by branch in modules such as `extintor` and `remessa`.
- Sensitive state transitions in remittance flows are validated in the backend.
- Recent hardening work added protections against:
  - self-deletion of the logged-in administrator
  - invalid role-to-branch combinations for technical users
  - silent failures in user, branch, and sector operations
  - invalid remittance transitions by status

This is not yet a full enterprise IAM model. There is currently no external identity provider, centralized RBAC policy engine, CSRF layer, or fine-grained authorization framework.

## Architecture - C4 Level 1

### System Context

At C4 Level 1, the solution is a single server-side web application interacting with human users and a PostgreSQL database.

```text
+-------------------+         +-----------------------------------+         +----------------------+
| Admin User        | ----->  | GestaoExtintores Web Application | ----->  | PostgreSQL Database  |
| Tecnico User      | <-----  | Servlet + JSP + DAO monolith     | <-----  | Operational storage  |
+-------------------+         +-----------------------------------+         +----------------------+
```

### External Actors

- `Admin`
  - manages users, branches, sectors, and operational approvals
- `Técnico`
  - manages field-facing extinguisher and remittance activities within branch scope

### External System

- `PostgreSQL`
  - stores users, branches, sectors, extinguishers, remittances, and remittance items

## Architecture - Application Structure

The current solution is best described as a classic layered Java web monolith.

### Current Architectural Style

- Presentation layer with `JSP`
- Request orchestration in `Servlets`
- Persistence logic in `DAO` classes
- Domain entities in `model`
- Global request protection in `AuthenticationFilter`

This is not a true Hexagonal Architecture implementation today. There are no explicit ports/adapters boundaries, no application service layer, and no inversion around domain use cases yet.

### Conceptual Evolution Toward Hexagonal

If the project evolves, a natural path would be:

- move business rules from servlets into application services
- isolate persistence behind repository interfaces
- keep servlets/JSP as delivery adapters
- keep PostgreSQL DAO implementations as infrastructure adapters

That evolution would make the application more testable, more modular, and closer to a proper hexagonal model. For now, this README documents the architecture as it actually exists rather than as an aspirational target.

## Technology Stack

- Java 8
- Java Servlet API
- JSP with JSTL
- Apache Ant
- NetBeans project structure
- PostgreSQL
- GlassFish Server 5.x
- Bootstrap 5 on the frontend

## Local Execution

### Prerequisites

- JDK 8
- Apache Ant
- GlassFish Server
- PostgreSQL running locally
- Database `gestao_extintores`

### Current Database Configuration

At the moment, database access is configured directly in source code at [ConnectionFactory.java](C:/GestaoExtintores/GestaoExtintores/src/java/br/com/gestaoextintores/util/ConnectionFactory.java).

- Host: `localhost`
- Port: `5432`
- Database: `gestao_extintores`
- User: `postgres`

For production-grade operation, this should be externalized into environment-specific configuration.

### Build

From the repository root:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_202'
& 'C:\Program Files\NetBeans-12.1\netbeans\extide\ant\bin\ant.bat' clean dist
```

This generates:

- `dist/GestaoExtintores.war`

### Deploy

Example deployment to GlassFish:

```powershell
& 'C:\Users\Voce2\GlassFish_Server\bin\asadmin.bat' deploy --force=true --contextroot GestaoExtintores 'C:\GestaoExtintores\GestaoExtintores\dist\GestaoExtintores.war'
```

### Application URL

- `http://localhost:8080/GestaoExtintores/`

## Example Scenarios

### Scenario 1 - Administrative setup

1. Admin authenticates in the application.
2. Admin registers branches and sectors.
3. Admin creates users and assigns role plus branch where applicable.

### Scenario 2 - Technical remittance creation

1. Técnico accesses the extinguisher list.
2. Técnico selects one or more extinguishers.
3. System creates a remittance and updates extinguisher status to `Em Remessa`.

### Scenario 3 - Administrative approval

1. Admin opens remittance details.
2. Admin approves recollection.
3. System moves the remittance to the next valid workflow stage.

### Scenario 4 - Technical receipt finalization

1. Técnico confirms recollection.
2. Técnico informs recharge date and new validity date.
3. System finalizes the remittance and updates extinguisher operational data.

## Project Structure

```text
src/
  java/
    br/com/gestaoextintores/
      controller/    -> Servlets
      dao/           -> Persistence access
      filter/        -> Request authentication and access filter
      model/         -> Domain entities
      util/          -> Infrastructure helpers
web/
  extintor/         -> JSPs for extinguisher flows
  filial/           -> JSPs for branch flows
  remessa/          -> JSPs for remittance flows
  setor/            -> JSPs for sector flows
  usuario/          -> JSPs for user flows
  index.jsp         -> Home page
  login.jsp         -> Login page
build.xml           -> Ant build entry point
nbproject/          -> NetBeans project metadata
```

## Design Philosophy

The project follows a pragmatic and operationally focused design philosophy.

- Keep the application simple to run in a local enterprise environment.
- Favor direct server-side rendering over frontend complexity.
- Enforce critical business rules in the backend, not only in the UI.
- Maintain traceability for workflow transitions and role-sensitive actions.
- Prefer incremental hardening over disruptive rewrites.

Recent maintenance followed this principle directly: stabilize the current architecture first, then improve robustness around permissions, validation, and workflow state transitions.

## Roadmap

### Short Term

- externalize database credentials from source code
- centralize repeated authorization checks
- improve consistency of error and success messaging
- add safer validation around remaining CRUD flows
- expand runtime smoke tests for critical workflows

### Medium Term

- introduce service layer abstractions for business use cases
- reduce business-rule duplication across servlets and DAOs
- improve auditability and operational reporting
- strengthen deployment documentation and environment separation

### Long Term

- move toward cleaner application boundaries
- enable automated regression testing for domain workflows
- prepare architecture for broader enterprise integrations

## Notes for Maintainers

The repository has recently gone through:

- functional hardening in `usuario`, `filial`, `setor`, and `remessa`
- UTF-8 normalization in JSPs and Java sources
- cleanup of legacy invalid test data used during validation

The current codebase is materially more stable than the original baseline, but it remains a classic servlet monolith. Future improvement work should keep that reality explicit and evolve the architecture deliberately rather than cosmetically.
