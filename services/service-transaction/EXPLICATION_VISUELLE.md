# 🎨 Explication Visuelle - Service Transaction

## 📖 Comment ça marche en images

### 1️⃣ Vue d'ensemble simplifiée

```
Toi (Client)
    │
    │ 1. Envoie une requête HTTP
    │    POST /api/transactions/depot
    │    { "accountIdSource": "ACC001", "amount": 1000 }
    ▼
┌─────────────────────────────────────────┐
│   🖥️  APPLICATION SPRING BOOT           │
│   (Port 8082)                           │
│                                         │
│   ┌─────────────────────────────────┐  │
│   │  📥 Controller                  │  │
│   │  Reçoit la requête              │  │
│   └──────────┬──────────────────────┘  │
│              │                          │
│              ▼                          │
│   ┌─────────────────────────────────┐  │
│   │  🧠 Service                     │  │
│   │  - Valide les données           │  │
│   │  - Vérifie le solde             │  │
│   │  - Crée la transaction          │  │
│   └──────────┬──────────────────────┘  │
│              │                          │
│              ├──────────┬───────────┐   │
│              ▼          ▼           ▼   │
│   ┌──────────────┐ ┌────────┐ ┌────────┐
│   │ 💾 Repository│ │ 📨 MQ  │ │ 🔗 API │
│   │ (PostgreSQL) │ │ Events │ │ Client │
│   └──────────────┘ └────────┘ └────────┘
└─────────────────────────────────────────┘
    │              │           │
    ▼              ▼           ▼
┌─────────┐  ┌──────────┐  ┌──────────┐
│ 🗄️ DB   │  │ 🐰 Rabbit│  │ Service  │
│ Postgres│  │   MQ     │  │ Account  │
└─────────┘  └──────────┘  └──────────┘
```

---

## 2️⃣ Flux détaillé d'une transaction

### Exemple : Créer un dépôt de 1000€

```
┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 1 : Tu envoies la requête                                │
└─────────────────────────────────────────────────────────────────┘

POST http://localhost:8082/api/transactions/depot
Content-Type: application/json

{
  "accountIdSource": "ACC001",
  "amount": 1000.00,
  "type": "DEPOT",
  "description": "Salaire"
}

        │
        │ HTTP Request
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 2 : Controller reçoit et valide                          │
└─────────────────────────────────────────────────────────────────┘

@PostMapping("/depot")
public ResponseEntity<TransactionResponseDTO> createDepot(
    @Valid @RequestBody TransactionRequestDTO request) {
    
    ✅ Vérifie que le JSON est valide
    ✅ Vérifie que les champs obligatoires sont présents
    ✅ Vérifie que amount est un nombre
    
    transactionService.createDepot(request);
}

        │
        │ Appel de méthode
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 3 : Service - Validation métier                          │
└─────────────────────────────────────────────────────────────────┘

public TransactionResponseDTO createDepot(TransactionRequestDTO request) {
    
    validateDepot(request);
    ├─ ✅ amount > 0 ?
    └─ ✅ accountIdSource non vide ?
    
    // Pour un dépôt, pas besoin de vérifier le solde
}

        │
        │ Validation OK
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 4 : Service - Création de l'objet Transaction            │
└─────────────────────────────────────────────────────────────────┘

Transaction transaction = Transaction.builder()
    .accountIdSource("ACC001")          // Compte source
    .accountIdDestination(null)         // Pas de destination pour un dépôt
    .amount(1000.00)                    // Montant
    .type(TransactionType.DEPOT)        // Type
    .status(TransactionStatus.COMPLETED)// Statut
    .description("Salaire")             // Description
    .completedAt(LocalDateTime.now())   // Date de complétion
    .build();

// createdAt est automatiquement rempli par @PrePersist

        │
        │ Objet créé en mémoire
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 5 : Repository - Sauvegarde dans PostgreSQL              │
└─────────────────────────────────────────────────────────────────┘

transaction = transactionRepository.save(transaction);

JPA génère automatiquement le SQL :

INSERT INTO transactions (
    account_id_source,
    account_id_destination,
    amount,
    type,
    status,
    description,
    created_at,
    completed_at
) VALUES (
    'ACC001',
    NULL,
    1000.00,
    'DEPOT',
    'COMPLETED',
    'Salaire',
    '2024-12-07 10:30:00',
    '2024-12-07 10:30:00'
) RETURNING id;

PostgreSQL retourne : id = 1

        │
        │ Transaction sauvegardée avec id=1
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 6 : Service - Publication événement RabbitMQ             │
└─────────────────────────────────────────────────────────────────┘

TransactionEventDTO event = TransactionEventDTO.builder()
    .transactionId(1)
    .accountIdSource("ACC001")
    .accountIdDestination(null)
    .amount(1000.00)
    .type(TransactionType.DEPOT)
    .status(TransactionStatus.COMPLETED)
    .timestamp(LocalDateTime.now())
    .build();

rabbitMQProducerService.sendTransactionEvent(event);

        │
        │ Message envoyé
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 7 : RabbitMQ - Stockage de l'événement                   │
└─────────────────────────────────────────────────────────────────┘

Exchange: transaction.exchange
    │
    │ Routing Key: transaction.created
    ▼
Queue: transaction.queue
    │
    │ Message stocké :
    │ {
    │   "transactionId": 1,
    │   "accountIdSource": "ACC001",
    │   "amount": 1000.00,
    │   "type": "DEPOT",
    │   "status": "COMPLETED",
    │   "timestamp": "2024-12-07T10:30:00"
    │ }
    ▼
Disponible pour les consumers (autres services)

        │
        │ Événement publié
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 8 : Service - Mapping vers DTO de réponse                │
└─────────────────────────────────────────────────────────────────┘

TransactionResponseDTO response = TransactionResponseDTO.builder()
    .id(1)
    .accountIdSource("ACC001")
    .accountIdDestination(null)
    .amount(1000.00)
    .type(TransactionType.DEPOT)
    .status(TransactionStatus.COMPLETED)
    .description("Salaire")
    .createdAt(transaction.getCreatedAt())
    .completedAt(transaction.getCompletedAt())
    .failureReason(null)
    .build();

return response;

        │
        │ DTO retourné au Controller
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 9 : Controller - Réponse HTTP                            │
└─────────────────────────────────────────────────────────────────┘

return new ResponseEntity<>(response, HttpStatus.CREATED);

HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1,
  "accountIdSource": "ACC001",
  "accountIdDestination": null,
  "amount": 1000.00,
  "type": "DEPOT",
  "status": "COMPLETED",
  "description": "Salaire",
  "createdAt": "2024-12-07T10:30:00.123",
  "completedAt": "2024-12-07T10:30:00.123",
  "failureReason": null
}

        │
        │ Réponse HTTP
        ▼

┌─────────────────────────────────────────────────────────────────┐
│ ÉTAPE 10 : Tu reçois la réponse                                │
└─────────────────────────────────────────────────────────────────┘

✅ Transaction créée avec succès !
✅ ID généré : 1
✅ Sauvegardée dans PostgreSQL
✅ Événement publié dans RabbitMQ
```

---

## 3️⃣ Cas d'erreur : Solde insuffisant

### Exemple : Retrait de 5000€ alors que le solde est de 1000€

```
POST /api/transactions/retrait
{
  "accountIdSource": "ACC001",
  "amount": 5000.00,
  "type": "RETRAIT"
}

        │
        ▼

Controller ✅ Validation OK
        │
        ▼

Service - Validation métier ✅ OK
        │
        ▼

Service - Vérification du solde
        │
        ├─ Appelle AccountServiceClient.getAccountBalance("ACC001")
        │
        ├─ Reçoit : 1000.00
        │
        └─ Compare : 1000.00 >= 5000.00 ?
                     ❌ NON !

        │
        ▼

Service - Exception
        │
        └─ throw new InsufficientBalanceException(
               "Insufficient balance for withdrawal"
           )

        │
        ▼

GlobalExceptionHandler intercepte
        │
        ├─ Crée ErrorResponse
        │  ├─ status: 400
        │  ├─ message: "Insufficient balance for withdrawal"
        │  └─ timestamp: "2024-12-07T10:30:00"
        │
        └─ return ResponseEntity(error, HttpStatus.BAD_REQUEST)

        │
        ▼

HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "status": 400,
  "message": "Insufficient balance for withdrawal",
  "timestamp": "2024-12-07T10:30:00"
}

        │
        ▼

❌ Transaction refusée
❌ Rien n'est sauvegardé dans la base
❌ Aucun événement publié
```

---

## 4️⃣ Les différents types de transactions

### 🟢 DEPOT (Dépôt)

```
Avant :                    Après :
┌──────────┐              ┌──────────┐
│ ACC001   │              │ ACC001   │
│ 1000€    │  +500€       │ 1500€    │
└──────────┘  ────▶       └──────────┘

✅ Pas de vérification de solde
✅ Pas de compte destination
✅ Toujours accepté (si montant > 0)
```

### 🔴 RETRAIT (Retrait)

```
Avant :                    Après :
┌──────────┐              ┌──────────┐
│ ACC001   │              │ ACC001   │
│ 1000€    │  -200€       │ 800€     │
└──────────┘  ────▶       └──────────┘

⚠️ Vérification de solde obligatoire
✅ Pas de compte destination
❌ Refusé si solde insuffisant
```

### 🔵 VIREMENT_INTERNE (Virement)

```
Avant :                              Après :
┌──────────┐                        ┌──────────┐
│ ACC001   │                        │ ACC001   │
│ 1000€    │  -250€                 │ 750€     │
└──────────┘  ────▶                 └──────────┘
                   │
                   │ 250€
                   │
                   ▼
              ┌──────────┐          ┌──────────┐
              │ ACC002   │          │ ACC002   │
              │ 500€     │  +250€   │ 750€     │
              └──────────┘  ────▶   └──────────┘

⚠️ Vérification de solde obligatoire
✅ Compte destination obligatoire
❌ Refusé si source = destination
❌ Refusé si solde insuffisant
```

---

## 5️⃣ Architecture des packages

```
src/main/java/com/willbank/servicetransaction/
│
├── 📁 client/
│   └── AccountServiceClient.java
│       ↳ Communique avec le service Account
│       ↳ Vérifie les soldes
│
├── 📁 config/
│   └── RabbitMQConfig.java
│       ↳ Configure RabbitMQ
│       ↳ Crée exchange, queue, binding
│
├── 📁 controller/
│   └── TransactionController.java
│       ↳ Endpoints REST
│       ↳ Reçoit les requêtes HTTP
│       ↳ Retourne les réponses HTTP
│
├── 📁 dto/
│   ├── TransactionRequestDTO.java
│   │   ↳ Données reçues du client
│   ├── TransactionResponseDTO.java
│   │   ↳ Données envoyées au client
│   └── TransactionEventDTO.java
│       ↳ Événement publié dans RabbitMQ
│
├── 📁 entity/
│   └── Transaction.java
│       ↳ Modèle de données
│       ↳ Mappé vers la table PostgreSQL
│
├── 📁 enums/
│   ├── TransactionType.java
│   │   ↳ DEPOT, RETRAIT, VIREMENT_INTERNE, etc.
│   └── TransactionStatus.java
│       ↳ PENDING, COMPLETED, FAILED, CANCELLED
│
├── 📁 exception/
│   ├── InsufficientBalanceException.java
│   ├── InvalidTransactionException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
│       ↳ Intercepte toutes les exceptions
│       ↳ Retourne des réponses HTTP appropriées
│
├── 📁 repository/
│   └── TransactionRepository.java
│       ↳ Interface JPA
│       ↳ Méthodes de recherche
│       ↳ Accès à PostgreSQL
│
└── 📁 service/
    ├── TransactionService.java
    │   ↳ Logique métier principale
    │   ↳ Validation
    │   ↳ Création des transactions
    └── RabbitMQProducerService.java
        ↳ Publication d'événements
        ↳ Envoie vers RabbitMQ
```

---

## 6️⃣ Base de données PostgreSQL

### Table : transactions

```
┌────┬─────────────────┬──────────────────────┬────────┬──────────────────┬───────────┬─────────────┬─────────────────────┬─────────────────────┬───────────────┐
│ id │ account_id_src  │ account_id_dest      │ amount │ type             │ status    │ description │ created_at          │ completed_at        │ failure_reason│
├────┼─────────────────┼──────────────────────┼────────┼──────────────────┼───────────┼─────────────┼─────────────────────┼─────────────────────┼───────────────┤
│ 1  │ ACC001          │ NULL                 │ 1000.00│ DEPOT            │ COMPLETED │ Salaire     │ 2024-12-07 10:30:00 │ 2024-12-07 10:30:00 │ NULL          │
│ 2  │ ACC001          │ ACC002               │ 250.00 │ VIREMENT_INTERNE │ COMPLETED │ Loyer       │ 2024-12-07 10:35:00 │ 2024-12-07 10:35:00 │ NULL          │
│ 3  │ ACC001          │ NULL                 │ 100.00 │ RETRAIT          │ COMPLETED │ Courses     │ 2024-12-07 10:40:00 │ 2024-12-07 10:40:00 │ NULL          │
└────┴─────────────────┴──────────────────────┴────────┴──────────────────┴───────────┴─────────────┴─────────────────────┴─────────────────────┴───────────────┘
```

### Requêtes utiles

```sql
-- Voir toutes les transactions
SELECT * FROM transactions ORDER BY created_at DESC;

-- Historique d'un compte
SELECT * FROM transactions 
WHERE account_id_source = 'ACC001' 
   OR account_id_destination = 'ACC001'
ORDER BY created_at DESC;

-- Total des dépôts
SELECT SUM(amount) FROM transactions 
WHERE type = 'DEPOT' AND status = 'COMPLETED';

-- Total des retraits
SELECT SUM(amount) FROM transactions 
WHERE type = 'RETRAIT' AND status = 'COMPLETED';

-- Nombre de transactions par type
SELECT type, COUNT(*) as total 
FROM transactions 
GROUP BY type;
```

---

## 7️⃣ RabbitMQ - Événements

### Configuration

```
Exchange: transaction.exchange
   Type: topic
   Durable: true
   
   │
   │ Binding
   │ Routing Key: transaction.created
   ▼
   
Queue: transaction.queue
   Durable: true
   Auto-delete: false
```

### Format de l'événement

```json
{
  "transactionId": 1,
  "accountIdSource": "ACC001",
  "accountIdDestination": "ACC002",
  "amount": 250.00,
  "type": "VIREMENT_INTERNE",
  "status": "COMPLETED",
  "timestamp": "2024-12-07T10:35:00"
}
```

### Qui peut consommer ces événements ?

```
transaction.queue
    │
    ├─▶ Service Notification
    │   └─ Envoie un email/SMS au client
    │
    ├─▶ Service Analytics
    │   └─ Calcule des statistiques
    │
    ├─▶ Service Audit
    │   └─ Enregistre pour la conformité
    │
    └─▶ Service Fraud Detection
        └─ Détecte les transactions suspectes
```

---

## 8️⃣ Résumé en une image

```
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICE TRANSACTION                          │
│                                                                 │
│  1. Reçoit requête HTTP                                        │
│  2. Valide les données                                         │
│  3. Vérifie le solde (si nécessaire)                          │
│  4. Crée la transaction                                        │
│  5. Sauvegarde dans PostgreSQL                                 │
│  6. Publie événement dans RabbitMQ                            │
│  7. Retourne la réponse HTTP                                   │
│                                                                 │
│  ✅ Transactions atomiques (tout ou rien)                      │
│  ✅ Gestion d'erreurs complète                                 │
│  ✅ Événements asynchrones                                     │
│  ✅ API REST documentée                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

**Voilà ! Tu as maintenant une compréhension complète du fonctionnement ! 🎉**
