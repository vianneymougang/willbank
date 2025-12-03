# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project overview

This repository is a polyglot monorepo with three main areas:

- `clients/mobileApp`: React Native mobile app built with Expo and Expo Router.
- `clients/webApp`: Angular web application generated with Angular CLI.
- `services`: Spring Boot / Spring Cloud microservices for configuration, service discovery, and an API gateway.

The backend is infrastructure-focused: a Spring Cloud Config Server, a Eureka Discovery Server, and an API Gateway. Application configuration is centralized in the Config Server, and Docker is used to orchestrate the three services for local development.

## High-level architecture

### Backend services

- **Config Server (`services/config-server`)**
  - Spring Boot app annotated with `@EnableConfigServer`.
  - Serves configuration from `classpath:/configurations` as configured in `services/config-server/src/main/resources/application.yaml`.
  - Per-service configuration files live in `services/config-server/src/main/resources/configurations/`:
    - `api-gateway.yml` and `discovery-server.yml` for local development.
    - `api-gateway-docker.yml` and `discovery-server-docker.yml` for Docker deployments.
  - Exposes HTTP on port `8888`.

- **Discovery Server (`services/discovery-server`)**
  - Spring Boot app annotated with `@EnableEurekaServer`.
  - Runtime configuration is pulled from the Config Server (see `services/discovery-server/src/main/resources/application.yaml` and the corresponding `discovery-server*.yml` files in the Config Server).
  - Runs the Eureka dashboard and registry on port `8761`.

- **API Gateway (`services/api-gateway`)**
  - Spring Boot app annotated with `@EnableDiscoveryClient` and using Spring Cloud Gateway.
  - Configuration (Eureka client, CORS, logging, JWT, actuator) is externalized to the Config Server via `application.yaml` and `application-docker.yml`.
  - The CORS configuration in the Config Server YAMLs is tuned to allow the Angular dev server at `http://localhost:4200`.
  - Exposes HTTP on port `8080` and is intended to be the single entry point for client applications.

- **Configuration flow (local vs Docker)**
  - **Local:**
    - Each service reads base config from its own `application.yaml` and imports from `http://localhost:8888`.
    - The Config Server uses the `native` profile to read YAML files from `src/main/resources/configurations`.
  - **Docker:**
    - `docker-compose.yml` builds and runs the three services in a shared `willbank-microservices` bridge network.
    - The `SPRING_PROFILES_ACTIVE=docker` environment variable and `*-docker.yml` files adjust endpoints and hostnames for container networking (e.g., `config-server`, `discovery-server`).
    - Health checks use `/actuator/health` for each service to gate startup dependencies.

### Frontend clients

- **Angular web app (`clients/webApp`)**
  - Standard Angular CLI layout with `src/` as the main source tree.
  - Uses Jasmine/Karma for unit tests and supports e2e tests via `ng e2e`.
  - Designed to run on `http://localhost:4200`, matching the CORS configuration in the backend.

- **Expo mobile app (`clients/mobileApp`)**
  - Expo project created with `create-expo-app` using Expo Router for file-based routing (see `app/` directory as noted in its README).
  - Uses TypeScript and ESLint via `eslint-config-expo`.

## Common commands

All commands below assume the repository root (`willbank`) as the starting directory.

### Backend: Spring Boot / Maven

Each service has a Maven Wrapper (`mvnw`), so prefer using it over a globally installed Maven.

#### Run all backend services with Docker

From the repo root:

```bash path=null start=null
docker-compose up --build
```

- Builds and starts `config-server`, `discovery-server`, and `api-gateway`.
- Exposed ports:
  - Config Server: `http://localhost:8888`
  - Discovery Server (Eureka): `http://localhost:8761`
  - API Gateway: `http://localhost:8080`

#### Run services locally without Docker

Run each service in its own terminal, in this order:

1. **Config Server**

   ```bash path=null start=null
   cd services/config-server
   ./mvnw spring-boot:run
   ```

2. **Discovery Server**

   ```bash path=null start=null
   cd services/discovery-server
   ./mvnw spring-boot:run
   ```

3. **API Gateway**

   ```bash path=null start=null
   cd services/api-gateway
   ./mvnw spring-boot:run
   ```

#### Build and test backend services

From an individual service directory, e.g. `services/api-gateway`:

- **Run all tests**

  ```bash path=null start=null
  ./mvnw test
  ```

- **Build a runnable JAR**

  ```bash path=null start=null
  ./mvnw clean package
  ```

- **Run a single test class** (Maven)

  ```bash path=null start=null
  ./mvnw -Dtest=ApiGatewayApplicationTests test
  ```

  Adjust `ApiGatewayApplicationTests` to the desired test class name. You can also target a single test method with `ClassName#methodName` if needed.

### Frontend: Angular web app (`clients/webApp`)

Assumes `pnpm` (recommended due to the committed `pnpm-lock.yaml`), but `npm` can be used similarly (`npm install`, `npm run …`).

From `clients/webApp`:

- **Install dependencies**

  ```bash path=null start=null
  cd clients/webApp
  pnpm install
  ```

- **Start development server**

  ```bash path=null start=null
  pnpm start
  ```

  This runs `ng serve` and serves the app at `http://localhost:4200/` with live reload.

- **Build for production**

  ```bash path=null start=null
  pnpm build
  ```

- **Run all unit tests**

  ```bash path=null start=null
  pnpm test
  ```

- **Run tests in CI mode (no watch, with coverage)**

  ```bash path=null start=null
  pnpm test:ci
  ```

- **Run a single test file** (Angular CLI `--include`)

  ```bash path=null start=null
  pnpm test -- --include src/app/path/to/your.component.spec.ts
  ```

  Replace the path with the specific spec file you want to run.

- **Run end-to-end tests** (if configured)

  ```bash path=null start=null
  pnpm ng e2e
  ```

### Frontend: Expo mobile app (`clients/mobileApp`)

From `clients/mobileApp`:

- **Install dependencies**

  ```bash path=null start=null
  cd clients/mobileApp
  npm install
  ```

  (If you prefer `pnpm`, you can use `pnpm install` instead; a `pnpm-lock.yaml` is present.)

- **Start the Expo development server**

  ```bash path=null start=null
  npx expo start
  ```

  The Expo CLI output will guide you to open the app in a simulator, device, or web browser.

- **Platform-specific shortcuts** (via package scripts)

  ```bash path=null start=null
  npm run android   # Start with Android emulator
  npm run ios       # Start with iOS simulator
  npm run web       # Run as a web app
  ```

- **Lint the mobile app**

  ```bash path=null start=null
  npm run lint
  ```

## Repo-specific considerations for agents

- Backend configuration is centralized in the Config Server; when adding or modifying backend services, update both the local (`*.yml`) and Docker (`*-docker.yml`) configuration files under `services/config-server/src/main/resources/configurations/` and adjust `docker-compose.yml` as needed.
- CORS and JWT settings for the API Gateway are defined in the Config Server configuration files, not in code. Prefer changing those YAMLs over hard-coding values in Java.
- For new Java microservices, follow the existing pattern: create a Spring Boot app with the appropriate Spring Cloud starter dependencies, register it with Eureka, provide corresponding Config Server YAML, and wire it into Docker if it should participate in the docker-compose stack.
