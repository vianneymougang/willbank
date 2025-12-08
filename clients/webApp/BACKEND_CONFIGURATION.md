# 🔧 Configuration Backend - API Gateway

**Document pour le collaborateur Backend**

Ce document liste tous les endpoints que l'API Gateway doit exposer pour le frontend Angular WillBank.

---

## 📋 Informations Générales

- **URL API Gateway:** `http://localhost:8080`
- **Frontend URL:** `http://localhost:4200`
- **Authentification:** JWT Token (Bearer)
- **Format:** JSON
- **CORS:** Autoriser `http://localhost:4200`

---

## 🔐 Authentification

### Headers Requis

```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

### Endpoints Authentification

#### 1. Login
```http
POST /auth/login
Content-Type: application/json

Request Body:
{
  "username": "string",
  "password": "string"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "username": "string",
    "role": "string"
  }
}

Response: 401 Unauthorized
{
  "error": "Invalid credentials"
}
```

#### 2. Mot de Passe Oublié
```http
POST /auth/forgot-password
Content-Type: application/json

Request Body:
{
  "email": "string"
}

Response: 200 OK
{
  "message": "Email sent successfully"
}
```

#### 3. Réinitialiser Mot de Passe
```http
POST /auth/reset-password
Content-Type: application/json

Request Body:
{
  "token": "string",
  "newPassword": "string"
}

Response: 200 OK
{
  "message": "Password reset successfully"
}
```

---

## 👥 Clients (6 endpoints)

### 1. Liste des Clients
```http
GET /api/v1/clients
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com",
    "telephone": "0612345678",
    "adresse": "123 Rue de Paris"
  }
]
```

### 2. Détail Client
```http
GET /api/v1/clients/:id
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@email.com",
  "telephone": "0612345678",
  "adresse": "123 Rue de Paris"
}

Response: 404 Not Found
{
  "error": "Client not found"
}
```

### 3. Recherche Clients
```http
GET /api/v1/clients/search?q=<searchText>
Authorization: Bearer <token>

Query Parameters:
- q: string (texte de recherche)

Response: 200 OK
[
  {
    "id": 1,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com",
    "telephone": "0612345678",
    "adresse": "123 Rue de Paris"
  }
]
```

### 4. Créer Client
```http
POST /api/v1/clients
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "nom": "string",
  "prenom": "string",
  "email": "string",
  "telephone": "string",
  "adresse": "string"
}

Response: 201 Created
{
  "data": {
    "id": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com",
    "telephone": "0612345678",
    "adresse": "123 Rue de Paris"
  },
  "notification": {
    "type": "success",
    "title": "Client créé",
    "message": "Le client \"Jean Dupont\" a été créé avec succès",
    "action": "view-client",
    "actionData": { "clientId": 123 }
  }
}
```

### 5. Modifier Client
```http
PUT /api/v1/clients/:id
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "nom": "string",
  "prenom": "string",
  "email": "string",
  "telephone": "string",
  "adresse": "string"
}

Response: 200 OK
{
  "data": {
    "id": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com",
    "telephone": "0612345678",
    "adresse": "123 Rue de Paris"
  },
  "notification": {
    "type": "info",
    "title": "Client modifié",
    "message": "Les informations du client ont été mises à jour"
  }
}
```

### 6. Supprimer Client
```http
DELETE /api/v1/clients/:id
Authorization: Bearer <token>

Response: 200 OK
{
  "notification": {
    "type": "warning",
    "title": "Client supprimé",
    "message": "Le client a été supprimé"
  }
}
```

---

## 💳 Comptes (7 endpoints)

### 1. Liste des Comptes
```http
GET /api/v1/accounts
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "type": "courant",
    "solde": 2500.50,
    "clientId": 1,
    "numeroCompte": "FR7612345678901234567890123",
    "dateCreation": "2024-01-15"
  }
]
```

### 2. Détail Compte
```http
GET /api/v1/accounts/:id
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "type": "courant",
  "solde": 2500.50,
  "clientId": 1,
  "numeroCompte": "FR7612345678901234567890123",
  "dateCreation": "2024-01-15"
}
```

### 3. Comptes par Client
```http
GET /api/v1/clients/:clientId/accounts
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "type": "courant",
    "solde": 2500.50,
    "clientId": 1,
    "numeroCompte": "FR7612345678901234567890123",
    "dateCreation": "2024-01-15"
  }
]
```

### 4. Créer Compte
```http
POST /api/v1/accounts
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "type": "courant|epargne|professionnel",
  "solde": 0,
  "clientId": 1
}

Response: 201 Created
{
  "data": {
    "id": 123,
    "type": "courant",
    "solde": 0,
    "clientId": 1,
    "numeroCompte": "FR7612345678901234567890123",
    "dateCreation": "2024-12-08"
  },
  "notifications": [
    {
      "type": "success",
      "title": "Nouveau compte",
      "message": "Un compte courant a été créé",
      "action": "view-account",
      "actionData": { "accountId": 123 }
    }
  ]
}
```

### 5. Modifier Compte
```http
PUT /api/v1/accounts/:id
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "type": "courant|epargne|professionnel",
  "solde": 2500.50
}

Response: 200 OK
{
  "data": {
    "id": 123,
    "type": "courant",
    "solde": 2500.50,
    "clientId": 1,
    "numeroCompte": "FR7612345678901234567890123",
    "dateCreation": "2024-01-15"
  },
  "notification": {
    "type": "info",
    "title": "Compte modifié",
    "message": "Le compte a été mis à jour"
  }
}
```

### 6. Supprimer Compte
```http
DELETE /api/v1/accounts/:id
Authorization: Bearer <token>

Response: 200 OK
{
  "notification": {
    "type": "warning",
    "title": "Compte supprimé",
    "message": "Le compte a été supprimé"
  }
}
```

---

## 💸 Transactions (6 endpoints)

### 1. Liste des Transactions
```http
GET /api/v1/transactions
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "type": "virement|depot|retrait",
    "montant": 500.00,
    "telephoneSource": "0612345678",
    "telephoneDest": "0623456789",
    "date": "2024-11-25",
    "statut": "completed|pending|failed"
  }
]
```

### 2. Détail Transaction
```http
GET /api/v1/transactions/:id
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "type": "virement",
  "montant": 500.00,
  "telephoneSource": "0612345678",
  "telephoneDest": "0623456789",
  "date": "2024-11-25",
  "statut": "completed"
}
```

### 3. Créer Virement
```http
POST /api/v1/transactions/transfer
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "type": "virement|depot|retrait",
  "montant": 500.00,
  "telephoneSource": "0612345678",
  "telephoneDest": "0623456789"  // null pour dépôt/retrait
}

Response: 201 Created
{
  "id": 123,
  "type": "virement",
  "montant": 500.00,
  "telephoneSource": "0612345678",
  "telephoneDest": "0623456789",
  "date": "2024-12-08",
  "statut": "completed"
}
```

### 4. Modifier Transaction
```http
PUT /api/v1/transactions/:id
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "statut": "completed|pending|failed"
}

Response: 200 OK
{
  "id": 123,
  "type": "virement",
  "montant": 500.00,
  "telephoneSource": "0612345678",
  "telephoneDest": "0623456789",
  "date": "2024-12-08",
  "statut": "completed"
}
```

### 5. Supprimer Transaction
```http
DELETE /api/v1/transactions/:id
Authorization: Bearer <token>

Response: 204 No Content
```

---

## 🔔 Notifications (8 endpoints)

### 1. Liste des Notifications
```http
GET /api/v1/notifications
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "type": "success|info|warning|error",
    "title": "Client créé",
    "message": "Le client a été créé avec succès",
    "date": "2024-12-08T10:30:00Z",
    "read": false,
    "action": "view-client",
    "actionData": { "clientId": 123 }
  }
]
```

### 2. Notifications Non Lues
```http
GET /api/v1/notifications/unread
Authorization: Bearer <token>

Response: 200 OK
[
  {
    "id": 1,
    "type": "success",
    "title": "Client créé",
    "message": "Le client a été créé avec succès",
    "date": "2024-12-08T10:30:00Z",
    "read": false
  }
]
```

### 3. Marquer comme Lu
```http
PUT /api/v1/notifications/:id/read
Authorization: Bearer <token>

Response: 200 OK
```

### 4. Tout Marquer comme Lu
```http
PUT /api/v1/notifications/read-all
Authorization: Bearer <token>

Response: 200 OK
```

### 5. Supprimer Notification
```http
DELETE /api/v1/notifications/:id
Authorization: Bearer <token>

Response: 204 No Content
```

### 6. Supprimer Toutes les Lues
```http
DELETE /api/v1/notifications/read
Authorization: Bearer <token>

Response: 200 OK
```

### 7. Envoyer Notification
```http
POST /api/v1/notifications/send
Authorization: Bearer <token>
Content-Type: application/json

Request Body:
{
  "clientId": 1,
  "type": "success|info|warning|error",
  "title": "string",
  "message": "string"
}

Response: 201 Created
{
  "id": 123,
  "clientId": 1,
  "type": "success",
  "title": "Notification envoyée",
  "message": "Votre message",
  "date": "2024-12-08T10:30:00Z",
  "read": false
}
```

---

## 🎯 Composite (3 endpoints)

### 1. Dashboard Client
```http
GET /api/v1/composite/clients/:clientId/dashboard
Authorization: Bearer <token>

Response: 200 OK
{
  "client": {
    "id": 1,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com"
  },
  "accounts": [
    {
      "id": 1,
      "type": "courant",
      "solde": 2500.50,
      "numeroCompte": "FR7612345678901234567890123"
    }
  ],
  "totalSolde": 2500.50,
  "nombreComptes": 1,
  "recentTransactions": [
    {
      "id": 1,
      "type": "virement",
      "montant": 500.00,
      "date": "2024-11-25"
    }
  ]
}
```

### 2. Relevé de Compte
```http
GET /api/v1/composite/accounts/:accountId/statement?period=<period>
Authorization: Bearer <token>

Query Parameters:
- period: string (monthly|quarterly|yearly)

Response: 200 OK
{
  "account": {
    "id": 1,
    "type": "courant",
    "solde": 2500.50,
    "numeroCompte": "FR7612345678901234567890123"
  },
  "period": "monthly",
  "transactions": [
    {
      "id": 1,
      "type": "virement",
      "montant": 500.00,
      "date": "2024-11-25"
    }
  ],
  "soldeDebut": 2000.50,
  "soldeFin": 2500.50
}
```

### 3. Recherche Transactions
```http
GET /api/v1/composite/transactions/search?type=<type>&dateDebut=<date>&dateFin=<date>
Authorization: Bearer <token>

Query Parameters:
- type: string (virement|depot|retrait) - optionnel
- dateDebut: string (YYYY-MM-DD) - optionnel
- dateFin: string (YYYY-MM-DD) - optionnel

Response: 200 OK
[
  {
    "id": 1,
    "type": "virement",
    "montant": 500.00,
    "telephoneSource": "0612345678",
    "telephoneDest": "0623456789",
    "date": "2024-11-25",
    "statut": "completed"
  }
]
```

---

## 📊 Format ApiResponse (Recommandé)

Pour les opérations CRUD, il est recommandé d'utiliser ce format pour inclure des notifications automatiques :

```json
{
  "data": {
    // Données de l'entité créée/modifiée
  },
  "notification": {
    "type": "success|info|warning|error",
    "title": "Titre de la notification",
    "message": "Message détaillé",
    "action": "view-client|view-account|view-transaction",
    "actionData": {
      "clientId": 123,
      "accountId": 456,
      "transactionId": 789
    }
  },
  "notifications": [
    // Pour plusieurs notifications
  ]
}
```

---

## 🔒 Codes d'Erreur

```
200 OK              - Succès
201 Created         - Ressource créée
204 No Content      - Succès sans contenu
400 Bad Request     - Données invalides
401 Unauthorized    - Non authentifié
403 Forbidden       - Accès refusé
404 Not Found       - Ressource introuvable
500 Server Error    - Erreur serveur
```

---

## 🌐 Configuration CORS

L'API Gateway doit autoriser :

```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type
Access-Control-Allow-Credentials: true
```

---

## 📋 Checklist Backend

- [ ] API Gateway sur le port 8080
- [ ] CORS configuré pour http://localhost:4200
- [ ] JWT Token validation
- [ ] 33 endpoints exposés
- [ ] Format JSON pour toutes les réponses
- [ ] Codes d'erreur HTTP standards
- [ ] Format ApiResponse avec notifications (optionnel)
- [ ] Logging des requêtes
- [ ] Rate limiting (optionnel)

---

## 🧪 Tests avec cURL

### Test Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### Test Get Clients
```bash
curl -X GET http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer <your_token>"
```

### Test Create Client
```bash
curl -X POST http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com",
    "telephone": "0612345678",
    "adresse": "123 Rue de Paris"
  }'
```

---

## 📞 Contact

Pour toute question sur les endpoints ou le format des données, contactez l'équipe frontend.

---

*Document créé le 8 Décembre 2025*
*Frontend: Angular 18*
*Backend: API Gateway + Microservices*
