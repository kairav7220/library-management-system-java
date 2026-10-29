# Library Management System — Java Edition

A library management system with a natural-language AI concierge, built with **Spring Boot 3** + **Thymeleaf** + **Spring Data JPA** (TiDB Cloud MySQL) + **LangChain4j / Mistral AI**.

> **Status:** in build. This repo is the Java neighbour of
> [`../library-management-system-dbms`](../library-management-system-dbms) (Python 3 + Flask + LangGraph).

## Stack

- Java 21 · Spring Boot 4.1 · Maven
- Spring Data JPA · MySQL Connector/J (TiDB Cloud MySQL-compatible, TLS, port 4000)
- Spring Security (form login, BCrypt + legacy scrypt verify, CSRF)
- LangChain4j 1.23.0 + Mistral AI — single agent + 12 tools, SSE streaming
- Thymeleaf + static design assets

## Quick start

```bash
cd library-management-system-java
./mvnw spring-boot:run
# http://localhost:5000
```

Copy env template, fill in TiDB + Mistral keys (`MYSQL_*`, `MISTRAL_API_KEY`).

## Deploy

Render blueprint included: `render.yaml` (Java 21 runtime, jar start command, `MYSQL_*` / `MISTRAL_API_KEY` as sync env vars).

## Documentation

- [AGENTS.md](AGENTS.md) — agent / AI-assistant conventions, DB rules, security, AI layer
- [PLAN.md](PLAN.md) — full build plan, phases, decisions, risks, demo script
- [TABLES.md](TABLES.md) — database schema reference (12 tables)
- [TOOLS.md](TOOLS.md) — AI concierge tool contract (12 tools)

## Demo flow

1. Login (admin/member/employee).
2. Browse books / members / issues via admin pages.
3. Chat with concierge: "Which DBMS books are available?", "What do I have issued?", "Issue book X to member Y".
4. Streaming answers, done event, friendly errors.
