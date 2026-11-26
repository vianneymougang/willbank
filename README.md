# WillBank

WillBank is a banking backend split into Spring Boot microservices. It manages customers, bank accounts and money transactions, and sends notifications when events happen.

Team project. My role: project lead, backend lead and software architecture.

Built for an information systems architecture course at Saint Jean Ingénieur, 2025.

## Main features

- Login with JWT access and refresh tokens, token validation and logout.
- Admins create customer accounts with a temporary password. The customer must change it at first login.
- Bank accounts: create, update, close, change status, check balance, list the accounts of the current user.
- Deposits, withdrawals and transfers. Each request can carry an idempotency key so the same transaction is not processed twice. Transactions above 10,000 (configurable) publish a large-transaction alert.
- Transaction history per account, with paging and date ranges.
- A composite service that builds a customer dashboard and an account statement from the user, account and transaction services.
- A notification service that listens to RabbitMQ events (customer created, account created, transaction completed, large transaction) and sends emails. It stores each notification in MongoDB.
- A mobile app (Expo) with login, registration, password reset, dashboard, accounts, transactions and settings screens. It uses mock data for now and does not call the backend yet.

## Architecture and stack

Backend: Java 21, Spring Boot 3.5.8, Spring Cloud 2025.0.0, Maven. Each service has its own Dockerfile.

| Service | Notes |
|---|---|
| config-server | Spring Cloud Config, port 8888. Service configs are in `services/config-server/src/main/resources/configurations` |
| discovery-server | Eureka server, port 8761 |
| api-gateway | Spring Cloud Gateway (WebFlux), port 8080. Routes `/api/auth`, `/api/users`, `/api/admin`, `/api/accounts` and `/api/transactions` |
| auth-service | Login, refresh, logout. PostgreSQL 16, OpenFeign to user-service |
| user-service | Users, roles, temporary passwords. PostgreSQL 16, RabbitMQ |
| account-service | Accounts and balances. PostgreSQL 16 |
| transaction-service | Deposits, withdrawals, transfers. PostgreSQL 16, RabbitMQ, OpenFeign to account-service, Resilience4j retry |
| composite-service | Dashboard and statements. OpenFeign to user, account and transaction services |
| notification-service | RabbitMQ consumers, email over SMTP. MongoDB |

Each data service has its own PostgreSQL database. Services exchange events through RabbitMQ 3. API docs use springdoc-openapi.

Clients in `clients/`:

- `mobileApp`: React Native 0.81 with Expo 54 and Expo Router.
- `webApp`: Angular 20 project. On `main` it only contains the Angular CLI starter. A teammate built the web app on the `feature/web-app` branch.

## How to run it locally

You need Docker. The compose file builds every service from source and starts PostgreSQL, MongoDB, RabbitMQ, Mongo Express and a local mail catcher.

```bash
docker compose up -d --build
```

The gateway listens on http://localhost:8080. Eureka is on http://localhost:8761, RabbitMQ on http://localhost:15672, Mongo Express on http://localhost:8087 and the mail catcher on http://localhost:1080.

Example requests are in `HttpRequests/`. This script logs in, then runs deposits and withdrawals through the gateway (it needs `curl` and a running stack):

```bash
./test-transactions-e2e.sh
```

This one builds, tests and packages the composite service with Maven and Docker:

```bash
./test-composite-service-pipeline.sh
```

Start the mobile app:

```bash
cd clients/mobileApp
npm install
npm start
```
