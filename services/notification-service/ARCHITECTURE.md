# 🏗️ Architecture du Microservice de Notifications

## Vue d'Ensemble

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          FRONTEND ANGULAR                               │
│                           (Port 4200)                                   │
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐    │
│  │  Dashboard   │  │   Clients    │  │  Notification Test       │    │
│  │              │  │              │  │  /notification-test      │    │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘    │
│                                                                         │
│  Service: notification.service.ts                                      │
└────────────────────────────┬────────────────────────────────────────────┘
                             │ HTTP/REST
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY                                    │
│                           (Port 8080)                                   │
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐    │
│  │   Routing    │  │     CORS     │  │   Authentication         │    │
│  │              │  │              │  │   (JWT, OAuth2)          │    │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘    │
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐    │
│  │Rate Limiting │  │Load Balancing│  │   Circuit Breaker        │    │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────────────┘
                             │ HTTP/REST
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    NOTIFICATION MICROSERVICE                            │
│                           (Port 8082)                                   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                      CONTROLLER LAYER                           │  │
│  │                                                                  │  │
│  │  NotificationController                                         │  │
│  │  - POST /send                                                   │  │
│  │  - GET /history                                                 │  │
│  │  - GET /user/{userId}                                           │  │
│  │  - POST /retry-failed                                           │  │
│  │  - GET /stats                                                   │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                             │                                           │
│                             ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                      SERVICE LAYER                              │  │
│  │                                                                  │  │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │  │
│  │  │NotificationService│  │  EmailService    │  │PushService   │ │  │
│  │  │                  │  │                  │  │              │ │  │
│  │  │- Create          │  │- sendEmail()     │  │- sendPush()  │ │  │
│  │  │- Send            │  │- sendHtml()      │  │- multicast() │ │  │
│  │  │- Retry           │  │                  │  │              │ │  │
│  │  │- Stats           │  │                  │  │              │ │  │
│  │  └──────────────────┘  └──────────────────┘  └──────────────┘ │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                             │                                           │
│                             ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    REPOSITORY LAYER                             │  │
│  │                                                                  │  │
│  │  ┌──────────────────────┐  ┌──────────────────────────────┐   │  │
│  │  │NotificationRepository│  │NotificationTemplateRepository│   │  │
│  │  │                      │  │                              │   │  │
│  │  │- findByUserId()      │  │- findByName()                │   │  │
│  │  │- findByStatus()      │  │- findByNameAndActiveTrue()   │   │  │
│  │  │- countByStatus()     │  │                              │   │  │
│  │  └──────────────────────┘  └──────────────────────────────┘   │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                             │                                           │
│                             ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                      ENTITY LAYER                               │  │
│  │                                                                  │  │
│  │  ┌──────────────────┐  ┌──────────────────────────────────┐   │  │
│  │  │  Notification    │  │  NotificationTemplate            │   │  │
│  │  │                  │  │                                  │   │  │
│  │  │- id              │  │- id                              │   │  │
│  │  │- recipient       │  │- name                            │   │  │
│  │  │- subject         │  │- subject                         │   │  │
│  │  │- content         │  │- emailTemplate                   │   │  │
│  │  │- type            │  │- pushTemplate                    │   │  │
│  │  │- status          │  │- type                            │   │  │
│  │  │- userId          │  │- active                          │   │  │
│  │  │- transactionId   │  │                                  │   │  │
│  │  │- createdAt       │  │                                  │   │  │
│  │  │- sentAt          │  │                                  │   │  │
│  │  └──────────────────┘  └──────────────────────────────────┘   │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    LISTENER LAYER                               │  │
│  │                                                                  │  │
│  │  TransactionEventListener                                       │  │
│  │  - @RabbitListener(queues = "transaction.events")              │  │
│  │  - handleTransactionEvent()                                     │  │
│  │  - processTransactionEvent()                                    │  │
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                   CONFIGURATION LAYER                           │  │
│  │                                                                  │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐     │  │
│  │  │ CorsConfig   │  │FirebaseConfig│  │ RabbitMQConfig   │     │  │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘     │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  PostgreSQL  │    │   RabbitMQ   │    │   Firebase   │
│  (Port 5432) │    │ (Port 5672)  │    │     FCM      │
│              │    │              │    │              │
│ Tables:      │    │ Exchanges:   │    │ Services:    │
│ - notifications│   │ - transaction│    │ - Push       │
│ - templates  │    │              │    │   Notifs     │
│              │    │ Queues:      │    │              │
│ Indexes:     │    │ - transaction│    │              │
│ - user_id    │    │   .events    │    │              │
│ - status     │    │ - notification│   │              │
│ - created_at │    │   .events    │    │              │
└──────────────┘    └──────────────┘    └──────────────┘
        │                    │                    │
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Backup     │    │  Management  │    │   Console    │
│   Scripts    │    │   Interface  │    │   Firebase   │
│              │    │ (Port 15672) │    │              │
└──────────────┘    └──────────────┘    └──────────────┘
```

## Flux de Données

### 1. Envoi Direct via API

```
Frontend → API Gateway → NotificationController
                              ↓
                    NotificationService
                              ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
              EmailService      PushNotificationService
                    ↓                   ↓
                  SMTP                FCM
```

### 2. Envoi via RabbitMQ

```
Service Externe → RabbitMQ → TransactionEventListener
                                      ↓
                            NotificationService
                                      ↓
                            ┌─────────┴─────────┐
                            ↓                   ↓
                      EmailService      PushNotificationService
                            ↓                   ↓
                          SMTP                FCM
```

### 3. Consultation Historique

```
Frontend → API Gateway → NotificationController
                              ↓
                    NotificationService
                              ↓
                    NotificationRepository
                              ↓
                         PostgreSQL
```

## Composants Détaillés

### Controller Layer

**NotificationController**
- Gère les requêtes HTTP REST
- Validation des données d'entrée
- Pagination et tri
- Gestion des erreurs HTTP

### Service Layer

**NotificationService**
- Orchestration de l'envoi
- Gestion des templates
- Retry des échecs
- Statistiques

**EmailService**
- Envoi d'emails via SMTP
- Support HTML
- Gestion des erreurs SMTP

**PushNotificationService**
- Envoi via Firebase FCM
- Support multicast
- Gestion des tokens

### Repository Layer

**NotificationRepository**
- CRUD notifications
- Requêtes personnalisées
- Pagination JPA

**NotificationTemplateRepository**
- CRUD templates
- Recherche par nom
- Filtrage actif/inactif

### Listener Layer

**TransactionEventListener**
- Écoute RabbitMQ
- Parsing des événements
- Création automatique de notifications

### Configuration Layer

**CorsConfig**
- Configuration CORS
- Origines autorisées
- Headers autorisés

**FirebaseConfig**
- Initialisation Firebase
- Chargement credentials
- Configuration FCM

**RabbitMQConfig**
- Déclaration exchanges
- Déclaration queues
- Bindings
- Message converter

## Patterns Utilisés

### 1. Layered Architecture
- Controller → Service → Repository → Entity
- Séparation des responsabilités
- Testabilité

### 2. Repository Pattern
- Abstraction de la couche de données
- JPA/Hibernate
- Requêtes personnalisées

### 3. DTO Pattern
- NotificationRequest
- NotificationResponse
- TransactionEvent
- Séparation entités/API

### 4. Event-Driven Architecture
- RabbitMQ pour événements asynchrones
- Découplage des services
- Scalabilité

### 5. Template Method Pattern
- NotificationTemplate
- Réutilisation de templates
- Variables dynamiques

## Sécurité

### 1. CORS
- Configuration des origines autorisées
- Headers sécurisés
- Credentials autorisés

### 2. Validation
- @Valid sur les DTOs
- Contraintes JPA
- Validation métier

### 3. Credentials
- Variables d'environnement
- Fichiers externes (Firebase)
- Pas de hardcoding

### 4. API Gateway
- Authentification centralisée
- Rate limiting
- Circuit breaker

## Scalabilité

### 1. Horizontal Scaling
- Stateless service
- Load balancing via Gateway
- Multiple instances possibles

### 2. Database
- Index sur colonnes fréquentes
- Pagination
- Connection pooling (HikariCP)

### 3. RabbitMQ
- Queues durables
- Acknowledgment mode
- Prefetch configuration

### 4. Caching (Future)
- Redis pour templates
- Cache des statistiques
- Session management

## Monitoring

### 1. Logs
- SLF4J + Logback
- Niveaux configurables
- Rotation automatique

### 2. Actuator (Future)
- Health checks
- Metrics
- Info endpoint

### 3. Distributed Tracing (Future)
- Sleuth + Zipkin
- Correlation IDs
- Request tracing

## Resilience

### 1. Retry Mechanism
- Retry automatique des échecs
- Compteur de tentatives
- Backoff exponentiel (future)

### 2. Circuit Breaker (Future)
- Protection contre les services down
- Fallback responses
- Auto-recovery

### 3. Timeout Configuration
- Connection timeout
- Read timeout
- Write timeout

## Performance

### 1. Database
- Index optimisés
- Lazy loading
- Batch processing (future)

### 2. Connection Pooling
- HikariCP
- Configuration optimale
- Monitoring des connexions

### 3. Async Processing (Future)
- @Async pour envois
- Thread pool configuration
- Non-blocking I/O

## Technologies Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **ORM**: Hibernate/JPA
- **Build**: Maven

### Database
- **RDBMS**: PostgreSQL 15
- **Migration**: Hibernate DDL

### Messaging
- **Broker**: RabbitMQ 3.12
- **Protocol**: AMQP

### Email
- **Library**: JavaMailSender
- **Protocol**: SMTP

### Push
- **Provider**: Firebase Cloud Messaging
- **SDK**: Firebase Admin SDK 9.2.0

### Containerization
- **Container**: Docker
- **Orchestration**: Docker Compose

## Évolutions Futures

### 1. Features
- [ ] WebSocket pour notifications temps réel
- [ ] SMS via Twilio
- [ ] Notifications in-app
- [ ] Scheduling d'envois

### 2. Performance
- [ ] Redis caching
- [ ] Async processing
- [ ] Batch processing
- [ ] CDN pour assets

### 3. Monitoring
- [ ] Prometheus metrics
- [ ] Grafana dashboards
- [ ] ELK stack pour logs
- [ ] APM (Application Performance Monitoring)

### 4. Security
- [ ] OAuth2 authentication
- [ ] API key management
- [ ] Encryption at rest
- [ ] Audit logs

### 5. DevOps
- [ ] CI/CD pipeline
- [ ] Kubernetes deployment
- [ ] Auto-scaling
- [ ] Blue-green deployment
