# Account Service

Microservice de gestion des comptes bancaires pour WillBank.

## Description

Ce service gère les comptes bancaires (comptes courants et comptes épargne) des clients de WillBank. Il fournit des APIs REST pour créer, consulter, modifier et supprimer des comptes, ainsi que pour gérer les soldes et les statuts.

## Technologies

- **Java 21**
- **Spring Boot 3.5.8**
- **Spring Data JPA**
- **PostgreSQL 16**
- **Spring Cloud Netflix Eureka** (Service Discovery)
- **Swagger/OpenAPI** (Documentation)
- **Lombok** (Réduction du code boilerplate)

## Fonctionnalités

- ✅ Création de comptes (courant, épargne)
- ✅ Consultation des comptes
- ✅ Mise à jour des informations
- ✅ Gestion des statuts (ACTIVE, BLOCKED, CLOSED)
- ✅ Consultation des soldes
- ✅ Suppression de comptes (si solde = 0)
- ✅ Génération automatique de numéros de compte
- ✅ Enregistrement automatique dans Eureka

## Configuration

### Base de données PostgreSQL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/willbank_accounts
    username: postgres
    password: postgres
```

### Eureka Discovery

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Démarrage

### Prérequis

- Java 21
- Maven 3.9+
- PostgreSQL 16
- Discovery Server en cours d'exécution

### Lancer localement

```bash
# Créer la base de données
createdb willbank_accounts

# Compiler et démarrer
cd services/account-service
mvn clean install
mvn spring-boot:run
```

### Lancer avec Docker

```bash
# Depuis la racine du projet
docker-compose up postgres-accounts account-service
```

## API Endpoints

Le service expose les endpoints suivants sur le port **8082** :

### Comptes

- `POST /api/accounts` - Créer un compte
- `GET /api/accounts` - Lister tous les comptes
- `GET /api/accounts/{id}` - Obtenir un compte par ID
- `GET /api/accounts/customer/{customerId}` - Obtenir les comptes d'un client
- `PUT /api/accounts/{id}` - Mettre à jour un compte
- `DELETE /api/accounts/{id}` - Supprimer un compte
- `PATCH /api/accounts/{id}/status` - Changer le statut
- `GET /api/accounts/{id}/balance` - Obtenir le solde

### Documentation Swagger

Une fois le service démarré, accédez à la documentation interactive :

**http://localhost:8082/swagger-ui.html**

## Modèle de données

### Account

```python
{
  "id": 1,
  "customerId": 100,
  "accountNumber": "WB12345678901234",
  "type": "CURRENT",  // CURRENT | SAVINGS
  "balance": 1000.00,
  "status": "ACTIVE",  // ACTIVE | BLOCKED | CLOSED
  "label": "Compte Principal",
  "createdAt": "2025-12-01T20:00:00",
  "updatedAt": "2025-12-01T20:00:00"
}
```

## Tests

```bash
# Exécuter les tests unitaires
mvn test

# Exécuter les tests avec couverture
mvn test jacoco:report
```

## Logs

Les logs sont configurés au niveau DEBUG pour le package `com.willbank.account`.

```yaml
logging:
  level:
    com.willbank.account: DEBUG
```

## Auteur

WillBank Team
