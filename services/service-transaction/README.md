# Service Transaction - WillBank

Microservice de gestion des transactions bancaires avec Spring Boot, PostgreSQL et RabbitMQ.

## Fonctionnalités

- **Virement interne** : Transfert entre comptes de la même banque
- **Dépôt** : Ajout de fonds sur un compte
- **Retrait** : Retrait de fonds d'un compte
- **Historique des transactions** : Consultation de toutes les transactions d'un compte
- **Recherche et filtres** : Recherche par type, statut, période
- **Validation des soldes** : Vérification avant chaque transaction
- **Événements RabbitMQ** : Émission d'événements après chaque transaction

## Technologies

- Java 17
- Spring Boot 4.0.0
- Spring Data JPA
- PostgreSQL
- RabbitMQ
- Lombok
- H2 (tests)
- JUnit 5 & Mockito

## Prérequis

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- RabbitMQ 3.8+

## Configuration

### Base de données PostgreSQL

Créer la base de données :
```sql
CREATE DATABASE willbank_transactions;
```

Configuration dans `application.properties` :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/willbank_transactions
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### RabbitMQ

Configuration par défaut :
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Démarrage

```bash
# Compiler le projet
./mvnw clean install

# Lancer l'application
./mvnw spring-boot:run
```

L'application démarre sur le port **8082**.

## API Endpoints

### Créer un virement interne
```http
POST /api/transactions/virement-interne
Content-Type: application/json

{
  "accountIdSource": "ACC001",
  "accountIdDestination": "ACC002",
  "amount": 100.00,
  "type": "VIREMENT_INTERNE",
  "description": "Virement test"
}
```

### Créer un dépôt
```http
POST /api/transactions/depot
Content-Type: application/json

{
  "accountIdSource": "ACC001",
  "amount": 500.00,
  "type": "DEPOT",
  "description": "Dépôt en espèces"
}
```

### Créer un retrait
```http
POST /api/transactions/retrait
Content-Type: application/json

{
  "accountIdSource": "ACC001",
  "amount": 50.00,
  "type": "RETRAIT",
  "description": "Retrait DAB"
}
```

### Historique des transactions
```http
GET /api/transactions/history/{accountId}
```

### Recherche avec filtres
```http
GET /api/transactions/search?accountId=ACC001&type=DEPOT&status=COMPLETED
```

### Recherche par période
```http
GET /api/transactions/date-range?accountId=ACC001&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
```

## Types de transactions

- `VIREMENT_INTERNE` : Virement entre comptes internes
- `DEPOT` : Dépôt de fonds
- `RETRAIT` : Retrait de fonds
- `VIREMENT_EXTERNE` : Virement vers une autre banque
- `PAIEMENT` : Paiement

## Statuts de transaction

- `PENDING` : En attente
- `COMPLETED` : Complétée
- `FAILED` : Échouée
- `CANCELLED` : Annulée

## Tests

```bash
# Exécuter tous les tests
./mvnw test

# Tests unitaires uniquement
./mvnw test -Dtest=*Test

# Tests d'intégration uniquement
./mvnw test -Dtest=*IntegrationTest
```

## Structure du projet

```
src/
├── main/
│   ├── java/com/willbank/servicetransaction/
│   │   ├── client/          # Clients REST
│   │   ├── config/          # Configuration RabbitMQ
│   │   ├── controller/      # Controllers REST
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # Entités JPA
│   │   ├── enums/           # Énumérations
│   │   ├── exception/       # Gestion des exceptions
│   │   ├── repository/      # Repositories JPA
│   │   └── service/         # Services métier
│   └── resources/
│       └── application.properties
└── test/
    ├── java/com/willbank/servicetransaction/
    │   ├── controller/      # Tests controllers
    │   ├── integration/     # Tests d'intégration
    │   ├── repository/      # Tests repositories
    │   └── service/         # Tests services
    └── resources/
        └── application-test.properties
```

## Événements RabbitMQ

Après chaque transaction, un événement est publié sur RabbitMQ :

**Exchange** : `transaction.exchange`  
**Queue** : `transaction.queue`  
**Routing Key** : `transaction.created`

Format de l'événement :
```json
{
  "transactionId": 1,
  "accountIdSource": "ACC001",
  "accountIdDestination": "ACC002",
  "amount": 100.00,
  "type": "VIREMENT_INTERNE",
  "status": "COMPLETED",
  "timestamp": "2024-12-07T10:30:00"
}
```

## Démarrage Rapide avec Docker

```bash
# Démarrer PostgreSQL et RabbitMQ
docker-compose up -d

# Vérifier que les services sont démarrés
docker-compose ps

# Lancer l'application
./mvnw spring-boot:run
```

## Scripts de Test

### Windows
```bash
# Tester tous les endpoints
scripts\test-all-endpoints.bat
```

### Linux/Mac
```bash
# Rendre le script exécutable
chmod +x scripts/test-all-endpoints.sh

# Tester tous les endpoints
./scripts/test-all-endpoints.sh
```

## Collection Postman

Importer le fichier `postman_collection.json` dans Postman pour tester facilement tous les endpoints.

## Documentation Complète

Voir `GUIDE_CONFIGURATION_TEST.md` pour :
- Installation détaillée de PostgreSQL et RabbitMQ
- Configuration complète
- Tous les scénarios de test
- Troubleshooting
- Requêtes SQL utiles
