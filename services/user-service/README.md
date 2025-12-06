# User Service

Microservice de gestion des utilisateurs pour WillBank - Cameroun.

## Description

Ce service gère les utilisateurs de la plateforme WillBank, incluant les **Admins** (employés) et les **Customers** (clients). Il utilise l'héritage JPA Single Table pour différencier les deux types d'utilisateurs et inclut la gestion du statut KYC pour les clients.

## Technologies

- **Java 21**
- **Spring Boot 3.5.8**
- **Spring Data JPA** (Single Table Inheritance)
- **PostgreSQL 16**
- **Spring Cloud Netflix Eureka** (Service Discovery)
- **Swagger/OpenAPI** (Documentation)
- **BCrypt** (Hashage des mots de passe)
- **Lombok** (Réduction du code boilerplate)

## Fonctionnalités

### Gestion des Utilisateurs
- ✅ **Enregistrement** avec hashage BCrypt des mots de passe
- ✅ Création d'Admins (employés WillBank)
- ✅ Création de Customers (clients) avec statut KYC automatique (PENDING)
- ✅ Consultation des utilisateurs (tous, par ID, par rôle, par statut)
- ✅ Mise à jour des informations
- ✅ Gestion des statuts (ACTIVE, SUSPENDED)
- ✅ Suppression d'utilisateurs
- ✅ Validation unicité email et téléphone

### Modèle de Données
- **User** (abstract): entité parente avec héritage JPA
  - **Admin**: employés WillBank
  - **Customer**: clients avec statut KYC (PENDING, VERIFIED, REJECTED)
  
### Sécurité
- 🔐 **Hashage BCrypt** des mots de passe lors de l'enregistrement
- 🔐 Prêt pour intégration avec auth-service (microservice d'authentification futur)

## Configuration

### Base de données PostgreSQL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_service_db
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
- Config Server et Discovery Server en cours d'exécution

### Lancer localement

```bash
# Créer la base de données
createdb user_service_db

# Compiler et démarrer depuis services/user-service
./mvnw spring-boot:run
```

### Lancer avec Docker Compose

```bash
# Depuis la racine du projet
docker-compose up --build
```

<Ceci démarre automatiquement :
- `postgres-users` (port 5433)
- `user-service` (port 8081)
- Ainsi que config-server, discovery-server, etc.

## API Endpoints

Le service expose les endpoints suivants sur le port **8081** :

### Utilisateurs

- `POST /api/users/register` - Créer un utilisateur (Admin ou Customer)
- `GET /api/users` - Lister tous les utilisateurs
- `GET /api/users/{id}` - Obtenir un utilisateur par ID
- `GET /api/users/role/{role}` - Obtenir les utilisateurs par rôle (ADMIN/CUSTOMER)
- `GET /api/users/status/{status}` - Obtenir les utilisateurs par statut (ACTIVE/SUSPENDED)
- `PUT /api/users/{id}` - Mettre à jour un utilisateur
- `PATCH /api/users/{id}/status` - Changer le statut d'un utilisateur
- `DELETE /api/users/{id}` - Supprimer un utilisateur

### Documentation Swagger

Une fois le service démarré, accédez à la documentation interactive :

**http://localhost:8081/swagger-ui.html**

## Exemples d'Utilisation

### Créer un Admin

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Amadou",
    "surname": "Nkotto",
    "email": "admin@willbank.cm",
    "password": "SecurePass2025!",
    "phone": "+237677123456",
    "address": "Avenue Kennedy, Immeuble WillBank, Douala",
    "role": "ADMIN"
  }'
```

### Créer un Customer

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Marie",
    "surname": "Fotso",
    "email": "student@example.com",
    "password": "MySecurePass123!",
    "phone": "+237698765432",
    "address": "Quartier Bastos, Yaoundé",
    "role": "CUSTOMER"
  }'
```

**Note**: Le Customer est créé avec `kycStatus: PENDING` automatiquement.

## Modèle de Réponse

```json
{
  "id": 1,
  "name": "Marie",
  "surname": "Fotso",
  "email": "student@example.com",
  "phone": "+237698765432",
  "address": "Quartier Bastos, Yaoundé",
  "createdAt": "2025-12-06T19:00:00",
  "updatedAt": "2025-12-06T19:00:00",
  "status": "ACTIVE",
  "role": "CUSTOMER",
  "kycStatus": "PENDING",
  "userType": "CUSTOMER"
}
```

**Important**: Le champ `password` n'est jamais retourné dans les réponses pour des raisons de sécurité.

## Tests

```bash
# Exécuter les tests unitaires
./mvnw test

# Exécuter les tests avec couverture
./mvnw test jacoco:report
```

**Couverture**: 15 tests unitaires couvrant tous les cas d'usage.

## Logs

Les logs sont configurés au niveau DEBUG pour le package `com.willbank.userservice`.

```yaml
logging:
  level:
    com.willbank.userservice: DEBUG
```

## Contexte Camerounais

Le service est adapté au contexte camerounais :
- Format téléphone: +237 (9-12 chiffres)
- Adresses: Douala, Yaoundé, Bastos, etc.
- Domaine email: willbank.cm

## Architecture

Ce microservice s'intègre dans l'architecture globale WillBank :
- Dépend de **config-server** pour la configuration centralisée
- S'enregistre auprès de **discovery-server** (Eureka)
- Sera consommé par un futur **auth-service** pour l'authentification JWT

## Prochaines Étapes

- 🔜 Intégration avec auth-service (génération JWT, login)
- 🔜 Implémentation complète du workflow KYC pour les customers
- 🔜 Ajout de champs spécifiques pour Admin (department, employeeId)

## Auteur

WillBank Team - Cameroun
