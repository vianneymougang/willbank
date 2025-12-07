# 🏦 Guide Complet - Service Transaction WillBank

## 📚 Table des Matières
1. [Architecture et Fonctionnement](#architecture)
2. [Flux de Données Détaillé](#flux)
3. [Guide de Test Pas à Pas](#tests)
4. [Scénarios Réels](#scenarios)
5. [Debugging et Troubleshooting](#debug)

---

## 🏗️ Architecture et Fonctionnement {#architecture}

### Vue d'ensemble

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│   Client    │─────▶│  Controller      │─────▶│  Service    │
│ (Postman/   │      │  (REST API)      │      │  (Logique)  │
│  cURL)      │◀─────│                  │◀─────│             │
└─────────────┘      └──────────────────┘      └──────┬──────┘
                                                       │
                     ┌─────────────────────────────────┼─────────────┐
                     │                                 │             │
                     ▼                                 ▼             ▼
              ┌─────────────┐                  ┌─────────────┐ ┌──────────┐
              │  Repository │                  │  RabbitMQ   │ │ Account  │
              │    (JPA)    │                  │  Producer   │ │  Client  │
              └──────┬──────┘                  └──────┬──────┘ └────┬─────┘
                     │                                │             │
                     ▼                                ▼             ▼
              ┌─────────────┐                  ┌─────────────┐ ┌──────────┐
              │ PostgreSQL  │                  │  RabbitMQ   │ │ Service  │
              │   Database  │                  │   Server    │ │ Account  │
              └─────────────┘                  └─────────────┘ └──────────┘
```

### Composants Principaux

#### 1. **Controller** (`TransactionController.java`)
- **Rôle** : Point d'entrée de l'API REST
- **Responsabilités** :
  - Recevoir les requêtes HTTP
  - Valider les données d'entrée avec `@Valid`
  - Appeler le service approprié
  - Retourner les réponses HTTP

**Endpoints disponibles :**
- `POST /api/transactions/virement-interne` - Créer un virement
- `POST /api/transactions/depot` - Créer un dépôt
- `POST /api/transactions/retrait` - Créer un retrait
- `GET /api/transactions/history/{accountId}` - Historique
- `GET /api/transactions/search` - Recherche avec filtres
- `GET /api/transactions/date-range` - Recherche par période

#### 2. **Service** (`TransactionService.java`)
- **Rôle** : Logique métier
- **Responsabilités** :
  - Valider les règles métier
  - Vérifier les soldes via `AccountServiceClient`
  - Créer et sauvegarder les transactions
  - Publier les événements RabbitMQ
  - Mapper les entités vers les DTOs

#### 3. **Repository** (`TransactionRepository.java`)
- **Rôle** : Accès aux données
- **Responsabilités** :
  - Requêtes JPA vers PostgreSQL
  - Méthodes de recherche personnalisées
  - Gestion de la persistance

#### 4. **Entity** (`Transaction.java`)
- **Rôle** : Modèle de données
- **Champs** :
  - `id` : Identifiant unique (auto-généré)
  - `accountIdSource` : Compte source
  - `accountIdDestination` : Compte destination (optionnel)
  - `amount` : Montant
  - `type` : Type de transaction (DEPOT, RETRAIT, VIREMENT_INTERNE)
  - `status` : Statut (PENDING, COMPLETED, FAILED, CANCELLED)
  - `description` : Description
  - `createdAt` : Date de création
  - `completedAt` : Date de complétion
  - `failureReason` : Raison d'échec (si applicable)

#### 5. **RabbitMQ Producer** (`RabbitMQProducerService.java`)
- **Rôle** : Publication d'événements
- **Responsabilités** :
  - Envoyer les événements de transaction
  - Configuration : Exchange `transaction.exchange`, Queue `transaction.queue`

#### 6. **Account Service Client** (`AccountServiceClient.java`)
- **Rôle** : Communication avec le service de comptes
- **Responsabilités** :
  - Vérifier le solde d'un compte
  - Vérifier l'existence d'un compte
  - Gestion des erreurs (retourne 0 si le service est indisponible)

---

## 🔄 Flux de Données Détaillé {#flux}

### Exemple : Création d'un Virement Interne

```
1. CLIENT envoie POST /api/transactions/virement-interne
   {
     "accountIdSource": "ACC001",
     "accountIdDestination": "ACC002",
     "amount": 250.00,
     "type": "VIREMENT_INTERNE",
     "description": "Loyer"
   }
   
2. CONTROLLER reçoit la requête
   ├─ Valide le JSON avec @Valid
   └─ Appelle transactionService.createVirementInterne(request)
   
3. SERVICE - Validation métier
   ├─ Vérifie que accountIdDestination n'est pas null/vide
   ├─ Vérifie que source ≠ destination
   └─ Vérifie que amount > 0
   
4. SERVICE - Vérification du solde
   ├─ Appelle accountServiceClient.getAccountBalance("ACC001")
   ├─ Reçoit le solde (ex: 1000.00)
   └─ Compare : 1000.00 >= 250.00 ? ✅ OK
   
5. SERVICE - Création de la transaction
   ├─ Crée l'objet Transaction avec Builder
   ├─ Status = COMPLETED
   ├─ completedAt = maintenant
   └─ Appelle transactionRepository.save(transaction)
   
6. REPOSITORY - Sauvegarde
   ├─ JPA génère le SQL INSERT
   ├─ PostgreSQL exécute et retourne l'ID
   └─ Transaction sauvegardée avec id=1
   
7. SERVICE - Publication événement
   ├─ Crée TransactionEventDTO
   └─ Appelle rabbitMQProducerService.sendTransactionEvent(event)
   
8. RABBITMQ - Réception événement
   ├─ Message envoyé à l'exchange "transaction.exchange"
   ├─ Routé vers la queue "transaction.queue"
   └─ Disponible pour les consumers
   
9. SERVICE - Mapping réponse
   ├─ Convertit Transaction → TransactionResponseDTO
   └─ Retourne le DTO au Controller
   
10. CONTROLLER - Réponse HTTP
    ├─ Status: 201 CREATED
    └─ Body: TransactionResponseDTO en JSON
    
11. CLIENT reçoit la réponse
    {
      "id": 1,
      "accountIdSource": "ACC001",
      "accountIdDestination": "ACC002",
      "amount": 250.00,
      "type": "VIREMENT_INTERNE",
      "status": "COMPLETED",
      "description": "Loyer",
      "createdAt": "2024-12-07T10:30:00",
      "completedAt": "2024-12-07T10:30:00",
      "failureReason": null
    }
```

### Cas d'Erreur : Solde Insuffisant

```
1-4. [Même processus jusqu'à la vérification du solde]

5. SERVICE - Vérification du solde
   ├─ Appelle accountServiceClient.getAccountBalance("ACC001")
   ├─ Reçoit le solde (ex: 100.00)
   └─ Compare : 100.00 >= 250.00 ? ❌ NON
   
6. SERVICE - Exception
   └─ throw new InsufficientBalanceException("Insufficient balance for transfer")
   
7. GLOBAL EXCEPTION HANDLER intercepte
   ├─ Crée ErrorResponse
   │  ├─ status: 400
   │  ├─ message: "Insufficient balance for transfer"
   │  └─ timestamp: maintenant
   └─ Retourne ResponseEntity avec status 400
   
8. CLIENT reçoit l'erreur
   Status: 400 BAD REQUEST
   {
     "status": 400,
     "message": "Insufficient balance for transfer",
     "timestamp": "2024-12-07T10:30:00"
   }
```

---

## 🧪 Guide de Test Pas à Pas {#tests}

### Prérequis

1. **PostgreSQL** doit être démarré
2. **RabbitMQ** doit être démarré
3. **Application** doit être lancée sur le port 8082

### Vérification des Prérequis

```bash
# Vérifier PostgreSQL
psql -U postgres -c "SELECT version();"

# Vérifier RabbitMQ
rabbitmqctl status

# Vérifier l'application
curl http://localhost:8082/actuator/health
```

---

### Test 1 : Créer un Dépôt

**Objectif** : Ajouter 1000€ sur le compte ACC001

**Commande cURL :**
```bash
curl -X POST http://localhost:8082/api/transactions/depot ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 1000.00, \"type\": \"DEPOT\", \"description\": \"Salaire mensuel\"}"
```

**Réponse attendue :**
```json
{
  "id": 1,
  "accountIdSource": "ACC001",
  "accountIdDestination": null,
  "amount": 1000.00,
  "type": "DEPOT",
  "status": "COMPLETED",
  "description": "Salaire mensuel",
  "createdAt": "2024-12-07T10:30:00.123",
  "completedAt": "2024-12-07T10:30:00.123",
  "failureReason": null
}
```

**Vérifications :**
1. Status HTTP = 201 CREATED ✅
2. ID est généré (ex: 1) ✅
3. Status = COMPLETED ✅
4. createdAt et completedAt sont remplis ✅

**Vérifier dans PostgreSQL :**
```sql
psql -U postgres -d willbank_transactions
SELECT * FROM transactions WHERE id = 1;
```

**Vérifier dans RabbitMQ :**
1. Ouvrir http://localhost:15672
2. Aller dans "Queues" → "transaction.queue"
3. Cliquer "Get messages"
4. Voir l'événement publié

---

### Test 2 : Créer un Retrait

**Objectif** : Retirer 200€ du compte ACC001

**Commande cURL :**
```bash
curl -X POST http://localhost:8082/api/transactions/retrait ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 200.00, \"type\": \"RETRAIT\", \"description\": \"Retrait DAB\"}"
```

**Note importante** : Si le service Account n'est pas démarré, le `AccountServiceClient` retourne `BigDecimal.ZERO` par défaut, donc cette requête échouera avec "Insufficient balance".

**Solution temporaire pour tester sans le service Account :**
- Créer uniquement des dépôts (qui ne vérifient pas le solde)
- Ou modifier temporairement `AccountServiceClient` pour retourner un solde fictif

---

### Test 3 : Créer un Virement Interne

**Objectif** : Transférer 250€ de ACC001 vers ACC002

**Commande cURL :**
```bash
curl -X POST http://localhost:8082/api/transactions/virement-interne ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"accountIdDestination\": \"ACC002\", \"amount\": 250.00, \"type\": \"VIREMENT_INTERNE\", \"description\": \"Loyer\"}"
```

**Réponse attendue :**
```json
{
  "id": 2,
  "accountIdSource": "ACC001",
  "accountIdDestination": "ACC002",
  "amount": 250.00,
  "type": "VIREMENT_INTERNE",
  "status": "COMPLETED",
  "description": "Loyer",
  "createdAt": "2024-12-07T10:35:00.456",
  "completedAt": "2024-12-07T10:35:00.456",
  "failureReason": null
}
```

---

### Test 4 : Consulter l'Historique

**Objectif** : Voir toutes les transactions du compte ACC001

**Commande cURL :**
```bash
curl -X GET http://localhost:8082/api/transactions/history/ACC001
```

**Réponse attendue :**
```json
[
  {
    "id": 1,
    "accountIdSource": "ACC001",
    "amount": 1000.00,
    "type": "DEPOT",
    "status": "COMPLETED",
    "description": "Salaire mensuel",
    "createdAt": "2024-12-07T10:30:00.123"
  },
  {
    "id": 2,
    "accountIdSource": "ACC001",
    "accountIdDestination": "ACC002",
    "amount": 250.00,
    "type": "VIREMENT_INTERNE",
    "status": "COMPLETED",
    "description": "Loyer",
    "createdAt": "2024-12-07T10:35:00.456"
  }
]
```

---

### Test 5 : Recherche par Type

**Objectif** : Trouver tous les dépôts du compte ACC001

**Commande cURL :**
```bash
curl -X GET "http://localhost:8082/api/transactions/search?accountId=ACC001&type=DEPOT"
```

**Réponse attendue :**
```json
[
  {
    "id": 1,
    "accountIdSource": "ACC001",
    "amount": 1000.00,
    "type": "DEPOT",
    "status": "COMPLETED",
    "description": "Salaire mensuel",
    "createdAt": "2024-12-07T10:30:00.123"
  }
]
```

---

### Test 6 : Recherche par Statut

**Objectif** : Trouver toutes les transactions complétées

**Commande cURL :**
```bash
curl -X GET "http://localhost:8082/api/transactions/search?accountId=ACC001&status=COMPLETED"
```

---

### Test 7 : Recherche par Période

**Objectif** : Trouver les transactions entre deux dates

**Commande cURL :**
```bash
curl -X GET "http://localhost:8082/api/transactions/date-range?accountId=ACC001&startDate=2024-12-01T00:00:00&endDate=2024-12-31T23:59:59"
```

---

### Test 8 : Tests de Validation (Erreurs)

#### 8.1 Montant Négatif
```bash
curl -X POST http://localhost:8082/api/transactions/depot ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": -100.00, \"type\": \"DEPOT\"}"
```

**Réponse attendue :**
```json
{
  "status": 400,
  "message": "Deposit amount must be greater than zero",
  "timestamp": "2024-12-07T10:40:00"
}
```

#### 8.2 Virement vers le Même Compte
```bash
curl -X POST http://localhost:8082/api/transactions/virement-interne ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"accountIdDestination\": \"ACC001\", \"amount\": 100.00, \"type\": \"VIREMENT_INTERNE\"}"
```

**Réponse attendue :**
```json
{
  "status": 400,
  "message": "Source and destination accounts cannot be the same",
  "timestamp": "2024-12-07T10:41:00"
}
```

#### 8.3 Compte Destination Manquant
```bash
curl -X POST http://localhost:8082/api/transactions/virement-interne ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 100.00, \"type\": \"VIREMENT_INTERNE\"}"
```

**Réponse attendue :**
```json
{
  "status": 400,
  "message": "Destination account is required for internal transfer",
  "timestamp": "2024-12-07T10:42:00"
}
```

---

## 🎬 Scénarios Réels {#scenarios}

### Scénario 1 : Cycle de Vie Complet d'un Compte

**Histoire** : Un client reçoit son salaire, paie son loyer, et fait des courses.

```bash
# 1. Dépôt du salaire (3000€)
curl -X POST http://localhost:8082/api/transactions/depot ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 3000.00, \"type\": \"DEPOT\", \"description\": \"Salaire décembre\"}"

# 2. Paiement du loyer (800€)
curl -X POST http://localhost:8082/api/transactions/virement-interne ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"accountIdDestination\": \"ACC_PROPRIO\", \"amount\": 800.00, \"type\": \"VIREMENT_INTERNE\", \"description\": \"Loyer décembre\"}"

# 3. Retrait pour courses (150€)
curl -X POST http://localhost:8082/api/transactions/retrait ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 150.00, \"type\": \"RETRAIT\", \"description\": \"Courses supermarché\"}"

# 4. Virement épargne (500€)
curl -X POST http://localhost:8082/api/transactions/virement-interne ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"accountIdDestination\": \"ACC_EPARGNE\", \"amount\": 500.00, \"type\": \"VIREMENT_INTERNE\", \"description\": \"Épargne mensuelle\"}"

# 5. Consulter l'historique
curl -X GET http://localhost:8082/api/transactions/history/ACC001
```

**Solde final théorique** : 3000 - 800 - 150 - 500 = 1550€

---

### Scénario 2 : Gestion d'Erreurs

**Histoire** : Un client essaie de faire des opérations invalides.

```bash
# 1. Tentative de retrait avec montant négatif
curl -X POST http://localhost:8082/api/transactions/retrait ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": -50.00, \"type\": \"RETRAIT\"}"
# ❌ Erreur : "Withdrawal amount must be greater than zero"

# 2. Tentative de virement vers soi-même
curl -X POST http://localhost:8082/api/transactions/virement-interne ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"accountIdDestination\": \"ACC001\", \"amount\": 100.00, \"type\": \"VIREMENT_INTERNE\"}"
# ❌ Erreur : "Source and destination accounts cannot be the same"

# 3. Tentative de retrait avec solde insuffisant
curl -X POST http://localhost:8082/api/transactions/retrait ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 10000.00, \"type\": \"RETRAIT\"}"
# ❌ Erreur : "Insufficient balance for withdrawal"
```

---

## 🐛 Debugging et Troubleshooting {#debug}

### Vérifier les Logs de l'Application

```bash
# Voir les logs en temps réel
tail -f logs/spring.log

# Ou dans la console où l'application tourne
# Chercher les lignes avec "TransactionService" ou "ERROR"
```

### Vérifier PostgreSQL

```sql
-- Se connecter
psql -U postgres -d willbank_transactions

-- Voir toutes les transactions
SELECT * FROM transactions ORDER BY created_at DESC;

-- Compter par type
SELECT type, COUNT(*) as total 
FROM transactions 
GROUP BY type;

-- Compter par statut
SELECT status, COUNT(*) as total 
FROM transactions 
GROUP BY status;

-- Transactions d'un compte spécifique
SELECT * FROM transactions 
WHERE account_id_source = 'ACC001' 
   OR account_id_destination = 'ACC001'
ORDER BY created_at DESC;

-- Somme des montants par type
SELECT type, SUM(amount) as total_amount 
FROM transactions 
WHERE status = 'COMPLETED'
GROUP BY type;
```

### Vérifier RabbitMQ

**Via l'interface web :**
1. Ouvrir http://localhost:15672
2. Login : guest / guest
3. Onglet "Queues" → Voir `transaction.queue`
4. Vérifier le nombre de messages

**Via ligne de commande :**
```bash
# Lister les queues
rabbitmqctl list_queues name messages consumers

# Voir les connexions
rabbitmqctl list_connections

# Voir les exchanges
rabbitmqctl list_exchanges
```

### Problèmes Courants

#### 1. "Cannot connect to PostgreSQL"
```bash
# Vérifier que PostgreSQL est démarré
pg_ctl status

# Vérifier le port
netstat -ano | findstr :5432

# Tester la connexion
psql -U postgres -d willbank_transactions
```

#### 2. "Cannot connect to RabbitMQ"
```bash
# Vérifier que RabbitMQ est démarré
rabbitmqctl status

# Vérifier le port
netstat -ano | findstr :5672

# Redémarrer RabbitMQ
rabbitmq-service stop
rabbitmq-service start
```

#### 3. "Port 8082 already in use"
```bash
# Trouver le processus qui utilise le port
netstat -ano | findstr :8082

# Tuer le processus (remplacer PID)
taskkill /PID <PID> /F

# Ou changer le port dans application.properties
server.port=8083
```

#### 4. "Insufficient balance" même après un dépôt
**Cause** : Le service Account n'est pas démarré, donc `AccountServiceClient` retourne toujours 0.

**Solutions** :
1. Démarrer le service Account sur le port 8081
2. Ou modifier temporairement `AccountServiceClient.getAccountBalance()` pour retourner un solde fictif :
```java
public BigDecimal getAccountBalance(String accountId) {
    return new BigDecimal("10000.00"); // Solde fictif pour les tests
}
```

---

## 📊 Résumé des Endpoints

| Méthode | Endpoint | Description | Validation Solde |
|---------|----------|-------------|------------------|
| POST | `/api/transactions/depot` | Créer un dépôt | ❌ Non |
| POST | `/api/transactions/retrait` | Créer un retrait | ✅ Oui |
| POST | `/api/transactions/virement-interne` | Créer un virement | ✅ Oui |
| GET | `/api/transactions/history/{accountId}` | Historique | ❌ Non |
| GET | `/api/transactions/search` | Recherche avec filtres | ❌ Non |
| GET | `/api/transactions/date-range` | Recherche par période | ❌ Non |

---

## 🎯 Checklist de Test Complète

- [ ] PostgreSQL installé et démarré
- [ ] Base de données `willbank_transactions` créée
- [ ] RabbitMQ installé et démarré
- [ ] Interface RabbitMQ accessible (http://localhost:15672)
- [ ] Application démarre sans erreur sur port 8082
- [ ] Exchange `transaction.exchange` créé dans RabbitMQ
- [ ] Queue `transaction.queue` créée dans RabbitMQ
- [ ] Test : Créer un dépôt ✅
- [ ] Test : Créer un retrait ✅
- [ ] Test : Créer un virement interne ✅
- [ ] Test : Consulter l'historique ✅
- [ ] Test : Recherche par type ✅
- [ ] Test : Recherche par statut ✅
- [ ] Test : Recherche par période ✅
- [ ] Test : Validation montant négatif ✅
- [ ] Test : Validation virement vers soi-même ✅
- [ ] Test : Validation compte destination manquant ✅
- [ ] Vérification : Transaction sauvegardée dans PostgreSQL ✅
- [ ] Vérification : Événement publié dans RabbitMQ ✅
- [ ] Tests unitaires passent (`mvnw test`) ✅

---

## 📝 Notes Importantes

1. **Service Account** : Le service de transactions dépend du service Account pour vérifier les soldes. Si ce service n'est pas disponible, les retraits et virements échoueront.

2. **RabbitMQ Events** : Chaque transaction réussie publie un événement. Ces événements peuvent être consommés par d'autres services (notifications, analytics, etc.).

3. **Transactions JPA** : L'annotation `@Transactional` garantit que si une erreur survient, aucune donnée n'est sauvegardée (rollback automatique).

4. **Validation** : Deux niveaux de validation :
   - Validation des données (`@Valid`, `@NotNull`, etc.)
   - Validation métier (solde, comptes, montants)

5. **Gestion d'erreurs** : Le `GlobalExceptionHandler` intercepte toutes les exceptions et retourne des réponses HTTP appropriées.

---

**Bon test ! 🚀**
