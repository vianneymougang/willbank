# Plan d'Implémentation Complet - Transaction Service

## 📋 Table des Matières
1. [Analyse de l'Architecture Existante](#analyse-architecture)
2. [Conventions Techniques Identifiées](#conventions-techniques)
3. [Synthèse des Bonnes Pratiques](#bonnes-pratiques)
4. [Structure du Transaction Service](#structure-service)
5. [Modèle de Données](#modele-donnees)
6. [Endpoints REST](#endpoints-rest)
7. [Feign Clients](#feign-clients)
8. [Événements RabbitMQ](#evenements-rabbitmq)
9. [Configurations](#configurations)
10. [Dépendances Maven](#dependances-maven)
11. [Checklist d'Erreurs à Éviter](#checklist-erreurs)
12. [Tests Unitaires](#tests-unitaires)

---

## 1. Analyse de l'Architecture Existante {#analyse-architecture}

### Services Existants
- **config-server** (Port 8888) - Spring Cloud Config avec profil `native`, configurations dans `/configurations`
- **discovery-server** (Port 8761) - Eureka Server
- **api-gateway** (Port 8080) - Point d'entrée unique avec routing vers tous les microservices
- **auth-service** (Port 8083) - Authentification JWT avec Feign Client vers user-service
- **user-service** (Port 8081) - Gestion users/admins/customers + RabbitMQ publisher
- **account-service** (Port 8082) - Gestion comptes bancaires + sécurité JWT via headers
- **notification-service** (Port 8084) - MongoDB + RabbitMQ consumer + Email (MailDev)
- **transaction-service** (Port à définir: **8085**) - **À IMPLÉMENTER**

### Stack Technique Standardisée
- **Spring Boot**: 3.5.8
- **Spring Cloud**: 2025.0.0
- **Java**: 21
- **Base de données**: PostgreSQL (pour services transactionnels)
- **Message Broker**: RabbitMQ
- **Service Discovery**: Eureka
- **Config Management**: Spring Cloud Config
- **Documentation**: SpringDoc OpenAPI 2.x
- **Build Tool**: Maven
- **Conteneurisation**: Docker (tous les services utilisent `expose` sauf API Gateway)

### Patterns Identifiés
1. **Communication Synchrone**: REST via Feign Clients (avec Eureka discovery)
2. **Communication Asynchrone**: RabbitMQ avec TopicExchange + Dead Letter Queue
3. **Sécurité**: JWT propagé via headers `X-User-Id`, `X-User-Email`, `X-User-Roles` par API Gateway
4. **Configuration**: Centralisée dans Config Server, `application.yml` local minimal
5. **Structuration**: Package par couche (controller, service, repository, dto, domain/model, event, config, security)

---

## 2. Conventions Techniques Identifiées {#conventions-techniques}

### 2.1 Structure des Packages

**Pattern standard observé** (exemple user-service):
```
com.willbank.{service-name}/
├── config/          # Configurations (RabbitMQ, Security, etc.)
├── controller/      # REST Controllers
├── dto/             # Data Transfer Objects
├── event/           # RabbitMQ Event POJOs
├── mapper/          # Entity ↔ DTO mappers (si présents)
├── model/ ou domain/# Entités JPA
├── repository/      # Spring Data Repositories
├── security/        # Filtres et services de sécurité
└── service/         # Logique métier (Interface + Impl)
```

### 2.2 Conventions de Nommage

#### Controllers
- Nom: `{Entity}Controller` (ex: `UserController`, `AccountController`)
- Annotation: `@RestController`, `@RequestMapping("/api/{entities}")`, `@RequiredArgsConstructor`, `@Slf4j`
- Swagger: `@Tag(name = "...", description = "...")`
- Pattern méthodes:
  ```java
  @PostMapping
  public ResponseEntity<{Entity}Response> create{Entity}(@Valid @RequestBody {Entity}Request request)
  
  @GetMapping
  public ResponseEntity<List<{Entity}Response>> getAll{Entities}()
  
  @GetMapping("/{id}")
  public ResponseEntity<{Entity}Response> get{Entity}ById(@PathVariable Long id)
  
  @PutMapping("/{id}")
  public ResponseEntity<{Entity}Response> update{Entity}(@PathVariable Long id, @Valid @RequestBody {Entity}Request request)
  
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete{Entity}(@PathVariable Long id)
  ```

#### Services
- Interface: `{Entity}Service`
- Implémentation: `{Entity}ServiceImpl`
- Annotation Impl: `@Service`, `@RequiredArgsConstructor`, `@Slf4j`, `@Transactional`

#### DTOs
- Request: `{Entity}Request` avec validation Jakarta (`@NotNull`, `@NotBlank`, etc.)
- Response: `{Entity}Response` (pas de validation)
- Annotation: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

#### Entities
- Nom: Nom de l'entité (ex: `Account`, `User`, `Transaction`)
- Annotation: `@Entity`, `@Table(name = "...")`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- ID: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
- Timestamps: `@PrePersist` et `@PreUpdate` pour `createdAt` et `updatedAt`

#### Events
- Nom: `{Entity}{Action}Event` (ex: `CustomerCreatedEvent`, `TransactionCompletedEvent`)
- Annotation: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

### 2.3 Configuration RabbitMQ

**Pattern standardisé** (extrait de user-service):
```java
@Configuration
public class RabbitMQConfig {
    
    // Exchanges
    public static final String {ENTITY}_EXCHANGE = "willbank.{entity}.exchange";
    
    // Queues
    public static final String {ENTITY}_{ACTION}_QUEUE = "willbank.{entity}.{action}";
    
    // Routing Keys
    public static final String {ENTITY}_{ACTION}_ROUTING_KEY = "{entity}.{action}";
    
    // Dead Letter
    public static final String DLX_EXCHANGE = "willbank.dlx";
    public static final String DLQ_QUEUE = "willbank.dlq";
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    
    @Bean
    public TopicExchange {entity}Exchange() {
        return new TopicExchange({ENTITY}_EXCHANGE);
    }
    
    @Bean
    public Queue {entity}{Action}Queue() {
        return QueueBuilder.durable({ENTITY}_{ACTION}_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }
    
    @Bean
    public Binding {entity}{Action}Binding() {
        return BindingBuilder
                .bind({entity}{Action}Queue())
                .to({entity}Exchange())
                .with({ENTITY}_{ACTION}_ROUTING_KEY);
    }
}
```

### 2.4 Configuration de Sécurité

**Pattern standardisé** (extrait de account-service):
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderAuthenticationFilter headerAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/internal/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated());
        return http.build();
    }
}
```

**HeaderAuthenticationFilter** extrait headers JWT:
- `X-User-Id`: ID de l'utilisateur connecté
- `X-User-Email`: Email
- `X-User-Roles`: Rôles (ADMIN, CUSTOMER)

### 2.5 Configuration application.yml

**Local (minimal)**:
```yaml
spring:
  application:
    name: transaction-service
  config:
    import: optional:configserver:http://localhost:8888
```

**Config Server** (`configurations/transaction-service.yml`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/willbank_transactions
    username: ${PG_DB_USERNAME:postgres}
    password: ${PG_DB_PASSWORD:}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.value}

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

logging:
  level:
    com.willbank.transaction: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG

server:
  port: 8085
```

---

## 3. Synthèse des Bonnes Pratiques {#bonnes-pratiques}

### 3.1 Recherche Web - Transaction Services Bancaires

**Points clés identifiés**:

1. **Atomicité dans les systèmes distribués**
   - ❌ **2PC (Two-Phase Commit)**: Non recommandé pour microservices (complexe, lent, single point of failure)
   - ✅ **Saga Pattern**: Pattern recommandé pour transactions distribuées
     - **Orchestration Saga**: Coordinateur central (meilleur pour workflows complexes)
     - **Choreography Saga**: Événements entre services (plus simple)
   - ✅ **Compensating Transactions**: Actions d'annulation en cas d'échec

2. **Consistency Models**
   - **Strong Consistency**: Requis pour opérations critiques (virements, soldes)
   - **Eventual Consistency**: Acceptable pour notifications, historique

3. **Patterns essentiels**
   - **Idempotency**: Opérations répétables sans effet secondaire (clé: `idempotencyKey` dans requêtes)
   - **Retry Mechanisms**: Avec exponential backoff pour erreurs transitoires
   - **Circuit Breaker**: Resilience4j pour appels externes (Feign)
   - **Outbox Pattern**: Garantie atomicité entre DB locale et publication événements

4. **Best Practices Spring Boot**
   - `@Transactional` pour transactions locales (dans le service)
   - Event-driven avec RabbitMQ pour communications asynchrones
   - Feign avec Eureka pour communications synchrones
   - Distributed tracing (optionnel: Zipkin/Jaeger)
   - Monitoring robuste (Actuator)

### 3.2 Application au Transaction Service

**Pour WillBank, nous allons implémenter**:

1. **Opérations locales atomiques** avec `@Transactional`
   - Dépôt, retrait: transactions locales simples
   - Mise à jour solde compte via appel synchrone (Feign) au Account Service

2. **Communication synchrone** (Feign Client)
   - Validation compte existe
   - Vérification solde suffisant (pour retraits/virements)
   - Mise à jour solde (+/-)

3. **Communication asynchrone** (RabbitMQ)
   - Publication événements `TransactionCompletedEvent` pour notifications
   - Publication `LargeTransactionAlertEvent` pour alertes (montant > seuil)

4. **Gestion d'erreurs robuste**
   - Validation métier stricte (solde, compte actif, montants positifs)
   - Gestion exceptions Feign (compte non trouvé, solde insuffisant)
   - Retry automatique avec Resilience4j pour appels Feign

5. **Idempotency**
   - Champ `idempotencyKey` optionnel dans `TransactionRequest`
   - Vérification duplicata avant traitement

---

## 4. Structure du Transaction Service {#structure-service}

### 4.1 Arborescence Complète

```
transaction-service/
├── pom.xml (déjà présent, à compléter)
├── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/com/willbank/transaction/
│   │   │   ├── TransactionServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── RabbitMQConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── FeignConfig.java (optionnel: config Resilience4j)
│   │   │   ├── controller/
│   │   │   │   └── TransactionController.java
│   │   │   ├── dto/
│   │   │   │   ├── TransactionRequest.java
│   │   │   │   ├── TransactionResponse.java
│   │   │   │   └── AccountBalanceResponse.java (pour Feign)
│   │   │   ├── domain/
│   │   │   │   ├── Transaction.java (Entity JPA)
│   │   │   │   ├── TransactionType.java (Enum)
│   │   │   │   └── TransactionStatus.java (Enum)
│   │   │   ├── event/
│   │   │   │   ├── TransactionCompletedEvent.java
│   │   │   │   └── LargeTransactionAlertEvent.java
│   │   │   ├── feign/
│   │   │   │   ├── AccountClient.java
│   │   │   │   └── dto/ (DTOs partagés pour Feign)
│   │   │   │       └── AccountResponse.java
│   │   │   ├── repository/
│   │   │   │   └── TransactionRepository.java
│   │   │   ├── security/
│   │   │   │   ├── HeaderAuthenticationFilter.java
│   │   │   │   └── AuthorizationService.java
│   │   │   └── service/
│   │   │       ├── TransactionService.java (Interface)
│   │   │       └── TransactionServiceImpl.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       └── java/com/willbank/transaction/
│           ├── TransactionServiceApplicationTests.java
│           ├── controller/
│           │   └── TransactionControllerTest.java
│           ├── service/
│           │   └── TransactionServiceImplTest.java
│           └── repository/
│               └── TransactionRepositoryTest.java
└── target/
```

### 4.2 Fichiers à NE PAS Créer / Dupliquer

**Ces éléments existent déjà dans d'autres services ou dans des modules communs**:
- ❌ Configurations Docker Compose (déjà dans `/docker-compose.yml` racine)
- ❌ Configuration Config Server (on va juste ajouter `transaction-service.yml` dans `/configurations`)
- ❌ Exceptions custom génériques (à évaluer: on pourrait créer des custom exceptions spécifiques si besoin)
- ❌ Utilities génériques (DateUtils, etc.) - utiliser les standards Java 21

**Fichiers à copier/adapter depuis d'autres services**:
- ✅ `HeaderAuthenticationFilter.java` (depuis account-service)
- ✅ `SecurityConfig.java` (depuis account-service)
- ✅ `Dockerfile` (depuis account-service ou user-service)

---

## 5. Modèle de Données {#modele-donnees}

### 5.1 Entité Transaction

**Table**: `transactions`

```java
package com.willbank.transaction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Compte concerné
    @Column(nullable = false)
    private Long accountId;
    
    // Pour virements: compte destination
    @Column
    private Long destinationAccountId;
    
    // Type d'opération
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;
    
    // Montant
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    // Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;
    
    // Solde avant transaction
    @Column(precision = 15, scale = 2)
    private BigDecimal balanceBefore;
    
    // Solde après transaction
    @Column(precision = 15, scale = 2)
    private BigDecimal balanceAfter;
    
    // Description optionnelle
    @Column(length = 255)
    private String description;
    
    // Pour idempotency
    @Column(unique = true, length = 100)
    private String idempotencyKey;
    
    // Raison d'échec (si status = FAILED)
    @Column(length = 500)
    private String failureReason;
    
    // Métadonnées
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 5.2 Enum TransactionType

```java
package com.willbank.transaction.domain;

public enum TransactionType {
    DEPOSIT,      // Dépôt
    WITHDRAWAL,   // Retrait
    TRANSFER      // Virement (débit du compte source)
}
```

### 5.3 Enum TransactionStatus

```java
package com.willbank.transaction.domain;

public enum TransactionStatus {
    PENDING,      // En cours de traitement
    COMPLETED,    // Réussie
    FAILED        // Échouée
}
```

### 5.4 Index Recommandés

**À créer via migration Flyway (optionnel) ou Hibernate auto**:
```sql
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_destination_account_id ON transactions(destination_account_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE UNIQUE INDEX idx_transactions_idempotency_key ON transactions(idempotency_key) WHERE idempotency_key IS NOT NULL;
```

---

## 6. Endpoints REST {#endpoints-rest}

### 6.1 TransactionController

**Base Path**: `/api/transactions`

| Méthode | Endpoint | Description | Sécurité | Request Body | Response |
|---------|----------|-------------|----------|--------------|----------|
| POST | `/deposit` | Effectuer un dépôt | Authentifié | `TransactionRequest` | `TransactionResponse` |
| POST | `/withdraw` | Effectuer un retrait | Authentifié | `TransactionRequest` | `TransactionResponse` |
| POST | `/transfer` | Effectuer un virement | Authentifié | `TransactionRequest` | `TransactionResponse` |
| GET | `/account/{accountId}` | Historique transactions d'un compte | Authentifié (+ authz) | - | `List<TransactionResponse>` |
| GET | `/{id}` | Détail d'une transaction | Authentifié (+ authz) | - | `TransactionResponse` |
| GET | `/account/{accountId}/paginated` | Historique paginé | Authentifié (+ authz) | Params: page, size, sort | `Page<TransactionResponse>` |

### 6.2 DTOs

#### TransactionRequest

```java
package com.willbank.transaction.dto;

import com.willbank.transaction.domain.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    // Pour virements uniquement
    private Long destinationAccountId;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    // Optionnel: pour idempotency
    @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
    private String idempotencyKey;
}
```

#### TransactionResponse

```java
package com.willbank.transaction.dto;

import com.willbank.transaction.domain.TransactionStatus;
import com.willbank.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    
    private Long id;
    private Long accountId;
    private Long destinationAccountId;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 7. Feign Clients {#feign-clients}

### 7.1 AccountClient

**Interface Feign** pour communiquer avec account-service:

```java
package com.willbank.transaction.feign;

import com.willbank.transaction.feign.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Feign Client to communicate with account-service
 * Uses Eureka for service discovery
 */
@FeignClient(name = "account-service")
public interface AccountClient {
    
    /**
     * Get account details by ID
     * Internal endpoint (no auth required from account-service side)
     */
    @GetMapping("/internal/accounts/{id}")
    AccountResponse getAccountById(@PathVariable("id") Long id);
    
    /**
     * Update account balance
     * Internal endpoint for atomic balance updates
     * 
     * @param id Account ID
     * @param amount Amount to add (positive) or subtract (negative)
     * @return Updated account with new balance
     */
    @PatchMapping("/internal/accounts/{id}/balance")
    AccountResponse updateBalance(
        @PathVariable("id") Long id,
        @RequestParam("amount") BigDecimal amount
    );
    
    /**
     * Check if account has sufficient balance
     * Internal endpoint
     */
    @GetMapping("/internal/accounts/{id}/has-sufficient-balance")
    Boolean hasSufficientBalance(
        @PathVariable("id") Long id,
        @RequestParam("amount") BigDecimal amount
    );
}
```

### 7.2 AccountResponse DTO (pour Feign)

```java
package com.willbank.transaction.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO mirroring account-service AccountResponse
 * Used for Feign Client communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private Long customerId;
    private String accountNumber;
    private String type; // AccountType as String
    private BigDecimal balance;
    private String status; // AccountStatus as String
    private String label;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 7.3 Configuration Feign + Resilience4j

**Application Main Class**:
```java
package com.willbank.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TransactionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
```

**FeignConfig** (avec Resilience4j):
```java
package com.willbank.transaction.config;

import feign.Logger;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class FeignConfig {
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build())
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slidingWindowSize(10)
                .build())
            .build());
    }
}
```

### 7.4 ⚠️ Modification REQUISE dans account-service

**Ajouter des endpoints internes** dans account-service pour supporter les appels du transaction-service:

**Nouveau contrôleur**: `InternalAccountController.java`

```java
package com.willbank.account.controller;

import com.willbank.account.dto.AccountResponse;
import com.willbank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Internal endpoints for service-to-service communication
 * These endpoints are NOT exposed through API Gateway
 * Security filter allows /internal/** without authentication
 */
@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
@Slf4j
public class InternalAccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        log.info("Internal call: Get account {}", id);
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> updateBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        log.info("Internal call: Update balance for account {} by {}", id, amount);
        AccountResponse response = accountService.updateBalance(id, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/has-sufficient-balance")
    public ResponseEntity<Boolean> hasSufficientBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        log.info("Internal call: Check sufficient balance for account {}", id);
        boolean hasSufficient = accountService.hasSufficientBalance(id, amount);
        return ResponseEntity.ok(hasSufficient);
    }
}
```

**Ajouter méthodes dans AccountService**:
```java
AccountResponse updateBalance(Long id, BigDecimal amount);
boolean hasSufficientBalance(Long id, BigDecimal amount);
```

---

## 8. Événements RabbitMQ {#evenements-rabbitmq}

### 8.1 Événements à Publier

#### TransactionCompletedEvent

```java
package com.willbank.transaction.event;

import com.willbank.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a transaction is successfully completed
 * Consumed by notification-service to send confirmation emails/notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCompletedEvent {
    
    private Long transactionId;
    private Long accountId;
    private Long destinationAccountId; // Null pour DEPOSIT/WITHDRAWAL
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String description;
    private LocalDateTime completedAt;
    
    // Info pour notification (récupérée du compte)
    private String customerEmail; // À récupérer via Feign ou inclure dans le context
}
```

#### LargeTransactionAlertEvent

```java
package com.willbank.transaction.event;

import com.willbank.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a large transaction is detected
 * Consumed by notification-service for fraud alerts or admin notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LargeTransactionAlertEvent {
    
    private Long transactionId;
    private Long accountId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime occurredAt;
    
    // Metadata
    private String customerEmail;
    private String accountNumber;
}
```

### 8.2 Configuration RabbitMQ

**RabbitMQConfig.java**:

```java
package com.willbank.transaction.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Transaction Service
 * Publishes transaction events to notification-service
 */
@Configuration
public class RabbitMQConfig {

    // ============================================
    // Exchange Names
    // ============================================
    public static final String TRANSACTION_EXCHANGE = "willbank.transaction.exchange";

    // ============================================
    // Routing Keys
    // ============================================
    public static final String TRANSACTION_COMPLETED_ROUTING_KEY = "transaction.completed";
    public static final String LARGE_TRANSACTION_ALERT_ROUTING_KEY = "transaction.large.alert";

    // ============================================
    // Message Converter Configuration (JSON)
    // ============================================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ============================================
    // Transaction Exchange
    // ============================================

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    // Note: Les queues sont déclarées dans notification-service et user-service
    // On déclare seulement l'exchange ici pour publier
}
```

### 8.3 Publication d'Événements

**Dans TransactionServiceImpl**:

```java
private final RabbitTemplate rabbitTemplate;

private void publishTransactionCompletedEvent(Transaction transaction) {
    TransactionCompletedEvent event = TransactionCompletedEvent.builder()
        .transactionId(transaction.getId())
        .accountId(transaction.getAccountId())
        .destinationAccountId(transaction.getDestinationAccountId())
        .type(transaction.getType())
        .amount(transaction.getAmount())
        .newBalance(transaction.getBalanceAfter())
        .description(transaction.getDescription())
        .completedAt(transaction.getUpdatedAt())
        .build();
    
    log.info("📤 Publishing TransactionCompletedEvent: {}", event);
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.TRANSACTION_EXCHANGE,
        RabbitMQConfig.TRANSACTION_COMPLETED_ROUTING_KEY,
        event
    );
}

private void publishLargeTransactionAlert(Transaction transaction) {
    // Seuil configurable (par exemple 10000€)
    BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("10000.00");
    
    if (transaction.getAmount().compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
        LargeTransactionAlertEvent event = LargeTransactionAlertEvent.builder()
            .transactionId(transaction.getId())
            .accountId(transaction.getAccountId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .occurredAt(transaction.getCreatedAt())
            .build();
        
        log.warn("🚨 Publishing LargeTransactionAlertEvent: {}", event);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TRANSACTION_EXCHANGE,
            RabbitMQConfig.LARGE_TRANSACTION_ALERT_ROUTING_KEY,
            event
        );
    }
}
```

---

## 9. Configurations {#configurations}

### 9.1 application.yaml (local)

**`src/main/resources/application.yaml`**:
```yaml
spring:
  application:
    name: transaction-service
  config:
    import: optional:configserver:http://localhost:8888
```

### 9.2 transaction-service.yml (Config Server)

**Créer**: `/services/config-server/src/main/resources/configurations/transaction-service.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/willbank_transactions
    username: ${PG_DB_USERNAME:postgres}
    password: ${PG_DB_PASSWORD:}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.value}

# Swagger/OpenAPI Configuration
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

# Logging
logging:
  level:
    com.willbank.transaction: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.springframework.cloud.openfeign: DEBUG

# Business Configuration
transaction:
  large-amount-threshold: 10000.00 # Seuil pour alert transactions importantes

server:
  port: 8085
```

### 9.3 transaction-service-docker.yml (Config Server)

**Créer**: `/services/config-server/src/main/resources/configurations/transaction-service-docker.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/willbank_transactions
    username: ${PG_DB_USERNAME:postgres}
    password: ${PG_DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false

  rabbitmq:
    host: rabbitmq
    port: 5672
    username: guest
    password: guest

eureka:
  client:
    service-url:
      defaultZone: http://discovery-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${HOSTNAME}

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

logging:
  level:
    com.willbank.transaction: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
    org.springframework.cloud.openfeign: INFO

transaction:
  large-amount-threshold: 10000.00

server:
  port: 8085
```

### 9.4 Mise à jour API Gateway

**Ajouter route dans** `/services/config-server/src/main/resources/configurations/api-gateway.yml`:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            # ... routes existantes ...
            
            - id: transaction-service
              uri: lb://transaction-service
              predicates:
                - Path=/api/transactions/**
              filters:
                - StripPrefix=0
```

### 9.5 Docker Compose

**Ajouter dans** `/docker-compose.yml`:

```yaml
  transaction-service:
    build: ./services/transaction-service
    container_name: willbank-transaction-service
    expose:
      - "8085"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - PG_DB_USERNAME=postgres
      - PG_DB_PASSWORD=password
    depends_on:
      - postgres
      - rabbitmq
      - config-server
      - discovery-server
    networks:
      - willbank-network
  
  # Ajouter base de données dans init script postgres
  # Dans postgres service, volumes/init scripts:
  # CREATE DATABASE willbank_transactions;
```

---

## 10. Dépendances Maven {#dependances-maven}

### 10.1 pom.xml Complet

**Le `pom.xml` existe déjà dans transaction-service. Voici les dépendances manquantes à ajouter**:

```xml
<!-- Dépendances DÉJÀ présentes (vérifiées): -->
<!-- ✅ spring-boot-starter-web -->
<!-- ✅ spring-boot-starter-data-jpa -->
<!-- ✅ spring-boot-starter-validation -->
<!-- ✅ spring-boot-starter-security -->
<!-- ✅ spring-boot-starter-actuator -->
<!-- ✅ spring-boot-starter-amqp -->
<!-- ✅ spring-cloud-starter-config -->
<!-- ✅ spring-cloud-starter-netflix-eureka-client -->
<!-- ✅ spring-cloud-starter-openfeign -->
<!-- ✅ spring-cloud-starter-circuitbreaker-resilience4j -->
<!-- ✅ postgresql -->
<!-- ✅ lombok -->
<!-- ✅ springdoc-openapi -->

<!-- Dépendances MANQUANTES à ajouter: -->

<!-- JWT Dependencies (pour validation dans SecurityConfig) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Bucket4j for Rate Limiting (optionnel mais cohérent avec autres services) -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>

<!-- Test Dependencies -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 10.2 Version SpringDoc à corriger

**⚠️ IMPORTANT**: Le `pom.xml` existant a une version obsolète de SpringDoc:

**Remplacer**:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version> <!-- ❌ Trop ancien -->
</dependency>
```

**Par**:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.4</version> <!-- ✅ Cohérent avec account-service -->
</dependency>
```

---

## 11. Checklist d'Erreurs à Éviter {#checklist-erreurs}

### ❌ ERREURS FRÉQUENTES À NE PAS FAIRE

1. **Configuration**
   - ❌ Ne pas mettre de config DB/RabbitMQ dans `application.yaml` local → Tout dans Config Server
   - ❌ Ne pas oublier `@EnableFeignClients` dans la classe principale
   - ❌ Ne pas oublier d'enregistrer le service dans Eureka

2. **Sécurité**
   - ❌ Ne pas exposer `/api/**` sans authentification
   - ❌ Ne pas oublier de permettre `/internal/**` et `/actuator/**` sans auth
   - ❌ Ne pas oublier le `HeaderAuthenticationFilter` pour extraire user info des headers

3. **Transactions**
   - ❌ Ne pas oublier `@Transactional` sur les méthodes de service
   - ❌ Ne pas faire de validations métier dans le Controller (→ Service)
   - ❌ Ne pas oublier de gérer les exceptions Feign (compte pas trouvé, etc.)

4. **Feign Clients**
   - ❌ Ne pas appeler directement account-service via `/api/**` → Utiliser `/internal/**`
   - ❌ Ne pas oublier de créer les endpoints `/internal/**` dans account-service
   - ❌ Ne pas oublier Resilience4j pour retry/circuit breaker

5. **RabbitMQ**
   - ❌ Ne pas oublier de publier les événements APRÈS commit de transaction (utiliser `@TransactionalEventListener` si nécessaire)
   - ❌ Ne pas oublier le `MessageConverter` JSON dans RabbitMQConfig
   - ❌ Ne pas oublier les Bindings dans notification-service pour consommer les événements

6. **Données**
   - ❌ Ne pas permettre montants négatifs dans les requêtes (validation DTO)
   - ❌ Ne pas oublier de vérifier solde suffisant AVANT de créer la transaction
   - ❌ Ne pas oublier de gérer l'idempotency (vérifier duplicata sur `idempotencyKey`)

7. **Docker**
   - ❌ Ne pas exposer le port 8085 publiquement → Utiliser `expose:` dans docker-compose
   - ❌ Ne pas oublier de créer la base `willbank_transactions` dans postgres init script

### ✅ BONNES PRATIQUES À SUIVRE

1. **Logging**
   - ✅ Logger TOUTES les étapes critiques (début/fin transaction, appels Feign, événements RabbitMQ)
   - ✅ Utiliser des niveaux appropriés (DEBUG pour détails, INFO pour flux, WARN pour alertes, ERROR pour échecs)

2. **Gestion d'erreurs**
   - ✅ Créer des exceptions custom (`InsufficientBalanceException`, `AccountNotFoundException`, etc.)
   - ✅ Implémenter un `@ControllerAdvice` pour gérer les exceptions globalement
   - ✅ Renvoyer des messages d'erreur clairs et cohérents

3. **Performance**
   - ✅ Utiliser des index sur colonnes fréquemment requêtées (`account_id`, `created_at`)
   - ✅ Implémenter pagination pour l'historique des transactions

4. **Tests**
   - ✅ Tester TOUS les scénarios (succès, échecs, edge cases)
   - ✅ Mocker les Feign Clients dans les tests
   - ✅ Tester l'idempotency

---

## 12. Tests Unitaires {#tests-unitaires}

### 12.1 Tests à Écrire

#### TransactionServiceImplTest

**Scénarios à couvrir**:
```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountClient accountClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private AuthorizationService authorizationService;
    
    @InjectMocks private TransactionServiceImpl transactionService;
    
    // DEPOSIT Tests
    @Test void deposit_Success_ShouldCreateTransactionAndUpdateBalance() {}
    @Test void deposit_NegativeAmount_ShouldThrowException() {}
    @Test void deposit_AccountNotFound_ShouldThrowException() {}
    @Test void deposit_IdempotencyKey_ShouldPreventDuplicate() {}
    
    // WITHDRAWAL Tests
    @Test void withdraw_Success_ShouldCreateTransactionAndDebitAccount() {}
    @Test void withdraw_InsufficientBalance_ShouldThrowException() {}
    @Test void withdraw_AccountBlocked_ShouldThrowException() {}
    
    // TRANSFER Tests
    @Test void transfer_Success_ShouldDebitSourceAndCreditDestination() {}
    @Test void transfer_SourceAndDestinationSame_ShouldThrowException() {}
    @Test void transfer_DestinationAccountNotFound_ShouldThrowException() {}
    @Test void transfer_InsufficientBalance_ShouldRollback() {}
    
    // EVENTS Tests
    @Test void successfulTransaction_ShouldPublishCompletedEvent() {}
    @Test void largeTransaction_ShouldPublishAlertEvent() {}
    
    // AUTHORIZATION Tests
    @Test void getTransactionHistory_CustomerCanOnlySeeOwnAccounts() {}
    @Test void getTransactionHistory_AdminCanSeeAll() {}
}
```

#### TransactionControllerTest

**Tests d'intégration avec MockMvc**:
```java
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @MockBean private TransactionService transactionService;
    
    @Test void deposit_ValidRequest_ShouldReturn200() {}
    @Test void deposit_InvalidRequest_ShouldReturn400() {}
    @Test void withdraw_Unauthorized_ShouldReturn403() {}
    @Test void getTransactionHistory_Paginated_ShouldReturnPage() {}
}
```

#### TransactionRepositoryTest

```java
@DataJpaTest
class TransactionRepositoryTest {
    
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TestEntityManager entityManager;
    
    @Test void findByAccountId_ShouldReturnAllTransactionsForAccount() {}
    @Test void findByIdempotencyKey_ShouldReturnExistingTransaction() {}
    @Test void findByAccountIdAndStatus_ShouldFilterByStatus() {}
    @Test void findByAccountIdOrderByCreatedAtDesc_ShouldSortDescending() {}
}
```

### 12.2 Configuration Tests

**`src/test/resources/application.yml`**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  cloud:
    config:
      enabled: false
eureka:
  client:
    enabled: false
```

---

## 📌 PLAN D'IMPLÉMENTATION PRIORITAIRE

### Phase 1 - Configuration & Infrastructure (Jour 1)
1. ✅ Compléter `pom.xml` avec dépendances manquantes
2. ✅ Créer `transaction-service.yml` dans Config Server
3. ✅ Créer `transaction-service-docker.yml` dans Config Server
4. ✅ Ajouter route dans `api-gateway.yml`
5. ✅ Ajouter service dans `docker-compose.yml`
6. ✅ Créer base de données dans init script Postgres

### Phase 2 - Modèle de Données (Jour 1-2)
7. ✅ Créer entité `Transaction`
8. ✅ Créer enums `TransactionType`, `TransactionStatus`
9. ✅ Créer `TransactionRepository`
10. ✅ Créer DTOs (`TransactionRequest`, `TransactionResponse`)

### Phase 3 - Feign Clients (Jour 2)
11. ✅ Créer `AccountClient` interface
12. ✅ Créer `AccountResponse` DTO pour Feign
13. ⚠️ **CRITIQUE**: Créer `InternalAccountController` dans account-service
14. ⚠️ **CRITIQUE**: Implémenter méthodes internes dans `AccountService`

### Phase 4 - Sécurité (Jour 2)
15. ✅ Copier `HeaderAuthenticationFilter` depuis account-service
16. ✅ Créer `SecurityConfig`
17. ✅ Créer `AuthorizationService`

### Phase 5 - Logique Métier (Jour 3-4)
18. ✅ Créer `TransactionService` interface
19. ✅ Implémenter `TransactionServiceImpl`
    - Méthode `deposit()`
    - Méthode `withdraw()`
    - Méthode `transfer()`
    - Gestion idempotency
    - Validation métier
20. ✅ Créer `TransactionController`

### Phase 6 - RabbitMQ (Jour 4)
21. ✅ Créer `RabbitMQConfig`
22. ✅ Créer événements (`TransactionCompletedEvent`, `LargeTransactionAlertEvent`)
23. ✅ Implémenter publication événements dans service
24. ⚠️ Ajouter consumers dans `notification-service` (optionnel pour MVP)

### Phase 7 - Tests (Jour 5)
25. ✅ Écrire tests unitaires `TransactionServiceImplTest`
26. ✅ Écrire tests `TransactionControllerTest`
27. ✅ Écrire tests `TransactionRepositoryTest`
28. ✅ Tester intégration avec Feign

### Phase 8 - Déploiement & Validation (Jour 5-6)
29. ✅ Build et test local (`mvn clean install`)
30. ✅ Build Docker image
31. ✅ Tester avec `docker-compose up`
32. ✅ Valider via Swagger UI (`http://localhost:8080/api/transactions`)
33. ✅ Tester scénarios complets (dépôt, retrait, virement)
34. ✅ Vérifier événements dans RabbitMQ Management
35. ✅ Vérifier données dans PostgreSQL

---

## 🎯 RÉSUMÉ EXÉCUTIF

### Ce qui EST cohérent avec l'architecture existante ✅
- Structure packages identique aux autres services
- Configuration centralisée via Config Server
- Sécurité via headers JWT (pattern déjà utilisé)
- RabbitMQ avec TopicExchange + DLQ (pattern standardisé)
- Feign + Eureka pour communication inter-services
- PostgreSQL avec JPA/Hibernate
- Spring Boot 3.5.8 + Spring Cloud 2025.0.0
- Swagger/OpenAPI pour documentation

### Ce qui DOIT être créé spécifiquement ✅
- Entité `Transaction` avec champs métier
- Endpoints REST pour dépôt/retrait/virement
- Feign Client `AccountClient` pour appeler account-service
- Événements RabbitMQ pour notifications
- Logique métier avec validation et gestion erreurs
- Configuration transaction-service.yml

### Ce qui DOIT être modifié dans d'autres services ⚠️
- **account-service**: Ajouter `InternalAccountController` avec endpoints internes
- **notification-service**: Ajouter consumers pour `TransactionCompletedEvent` (optionnel)
- **api-gateway**: Ajouter route `/api/transactions/**`
- **config-server**: Ajouter fichiers de configuration
- **docker-compose.yml**: Ajouter service transaction-service

### Prochaines étapes recommandées
1. Valider ce plan avec l'équipe
2. Commencer par Phase 1-2 (infra + modèle)
3. Implémenter Phase 3-4 (Feign + sécurité) **en parallèle** avec modifications account-service
4. Phases 5-6 (logique métier + events)
5. Phase 7-8 (tests + déploiement)

---

**Document généré le**: 2025-12-08  
**Auteur**: Antigravity AI Assistant  
**Version**: 1.0  
**Statut**: Prêt pour implémentation
