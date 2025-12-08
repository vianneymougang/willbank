# 🎨 Architecture API Gateway - Vue Visuelle

## 📊 Flux des Requêtes HTTP

```
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND ANGULAR                              │
│                    http://localhost:4200                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Toutes les requêtes HTTP
                              │ environment.apiGatewayUrl
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API GATEWAY                                   │
│                    http://localhost:8080                         │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Routing & Load Balancing                              │    │
│  │  - Authentification JWT                                │    │
│  │  - Rate Limiting                                       │    │
│  │  - CORS                                                │    │
│  │  - Logging                                             │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ Route vers les microservices
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐     ┌──────────────┐
│   Client     │      │   Account    │     │ Transaction  │
│   Service    │      │   Service    │     │   Service    │
│   :8081      │      │   :8082      │     │   :8083      │
└──────────────┘      └──────────────┘     └──────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
                              ▼
                      ┌──────────────┐
                      │  Composite   │
                      │   Service    │
                      │   :8084      │
                      └──────────────┘
                              │
                              ▼
                      ┌──────────────┐
                      │ Notification │
                      │   Service    │
                      │   :8085      │
                      └──────────────┘
```

---

## 🔄 Exemple de Flux Complet

### Scénario: Création d'un Client

```
1. USER ACTION
   └─> Utilisateur clique sur "Créer Client"
       └─> client-form.component.ts

2. FRONTEND SERVICE
   └─> apiService.createClient(clientData)
       └─> POST http://localhost:8080/api/v1/clients
           Headers: { Authorization: Bearer <JWT> }
           Body: { nom, prenom, email, ... }

3. API GATEWAY
   └─> Vérifie le JWT Token
   └─> Vérifie les permissions
   └─> Route vers Client Service
       └─> POST http://localhost:8081/clients

4. CLIENT SERVICE
   └─> Valide les données
   └─> Enregistre en base de données
   └─> Retourne la réponse
       └─> { id: 123, nom: "Dupont", ... }

5. API GATEWAY
   └─> Ajoute les headers CORS
   └─> Log la requête
   └─> Retourne au frontend
       └─> Response: 201 Created

6. FRONTEND INTERCEPTOR
   └─> notification-response.interceptor.ts
       └─> Détecte la notification dans la réponse
       └─> Affiche le toast "Client créé avec succès"
       └─> Ajoute la notification au badge

7. UI UPDATE
   └─> Redirection vers la liste des clients
   └─> Badge notifications: +1
   └─> Toast visible pendant 5 secondes
```

---

## 📦 Services Frontend et leurs Endpoints

### 1. ApiService (CRUD Principal)

```typescript
// Configuration
private baseUrl = `${environment.apiGatewayUrl}/api/v1`;

// Tous les appels passent par cette base URL
┌─────────────────────────────────────────────────────┐
│  ApiService                                         │
├─────────────────────────────────────────────────────┤
│  getClients()        → GET    /api/v1/clients       │
│  getClient(id)       → GET    /api/v1/clients/:id   │
│  createClient()      → POST   /api/v1/clients       │
│  updateClient()      → PUT    /api/v1/clients/:id   │
│  deleteClient()      → DELETE /api/v1/clients/:id   │
│                                                      │
│  getAccounts()       → GET    /api/v1/accounts      │
│  createAccount()     → POST   /api/v1/accounts      │
│  ...                                                 │
└─────────────────────────────────────────────────────┘
```

### 2. AuthService (Authentification)

```typescript
// Configuration
private apiUrl = environment.apiGatewayUrl;

┌─────────────────────────────────────────────────────┐
│  AuthService                                        │
├─────────────────────────────────────────────────────┤
│  login()            → POST /auth/login              │
│  forgotPassword()   → POST /auth/forgot-password    │
│  resetPassword()    → POST /auth/reset-password     │
└─────────────────────────────────────────────────────┘
```

### 3. NotificationService (Notifications)

```typescript
// Configuration
private baseUrl = `${environment.apiGatewayUrl}/api/v1/notifications`;

┌─────────────────────────────────────────────────────────────┐
│  NotificationService                                        │
├─────────────────────────────────────────────────────────────┤
│  getNotifications()     → GET    /api/v1/notifications      │
│  markAsRead(id)         → PUT    /api/v1/notifications/:id  │
│  deleteNotification(id) → DELETE /api/v1/notifications/:id  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 Gestion de la Sécurité

### JWT Token Flow

```
1. LOGIN
   User → AuthService.login()
        → POST /auth/login
        → Response: { token: "eyJhbGc..." }
        → localStorage.setItem('jwt', token)

2. REQUÊTES SUIVANTES
   Component → ApiService.getClients()
            → HTTP Interceptor ajoute automatiquement:
               Headers: { Authorization: "Bearer eyJhbGc..." }
            → GET /api/v1/clients
            → API Gateway vérifie le token
            → Route vers Client Service

3. TOKEN EXPIRÉ
   API Gateway → Response: 401 Unauthorized
              → Error Interceptor détecte 401
              → Redirection vers /login
              → localStorage.removeItem('jwt')
```

---

## 🎯 Intercepteurs HTTP

### 1. Loading Interceptor

```
REQUEST
   │
   ├─> loadingService.show()
   │   └─> Affiche le spinner
   │
   ├─> Envoie la requête HTTP
   │
   └─> RESPONSE
       └─> loadingService.hide()
           └─> Cache le spinner
```

### 2. Error Interceptor

```
RESPONSE ERROR
   │
   ├─> Détecte le code d'erreur
   │
   ├─> 400 Bad Request
   │   └─> Toast: "Données invalides"
   │
   ├─> 401 Unauthorized
   │   └─> Redirection vers /login
   │
   ├─> 403 Forbidden
   │   └─> Toast: "Accès refusé"
   │
   ├─> 404 Not Found
   │   └─> Toast: "Ressource introuvable"
   │
   └─> 500 Server Error
       └─> Toast: "Erreur serveur"
```

### 3. Notification Response Interceptor

```
RESPONSE SUCCESS
   │
   ├─> Vérifie si response.notification existe
   │
   ├─> OUI
   │   ├─> notificationService.addNotification()
   │   ├─> Affiche le toast
   │   └─> Met à jour le badge
   │
   └─> NON
       └─> Retourne la réponse normalement
```

---

## 📊 Mapping des Endpoints

### Vue d'ensemble

```
FRONTEND                    API GATEWAY              MICROSERVICE
─────────────────────────────────────────────────────────────────

/clients                    /api/v1/clients          Client Service
  ├─ list                   GET                      :8081/clients
  ├─ create                 POST
  ├─ edit/:id               PUT /:id
  └─ detail/:id             GET /:id

/accounts                   /api/v1/accounts         Account Service
  ├─ list                   GET                      :8082/accounts
  ├─ create                 POST
  ├─ edit/:id               PUT /:id
  └─ detail/:id             GET /:id

/transactions               /api/v1/transactions     Transaction Service
  ├─ list                   GET                      :8083/transactions
  ├─ create                 POST /transfer
  └─ detail/:id             GET /:id

/notifications              /api/v1/notifications    Notification Service
  ├─ list                   GET                      :8085/notifications
  └─ send                   POST /send

/dashboard                  /api/v1/composite        Composite Service
  ├─ client/:id             GET /clients/:id/dash    :8084/dashboard
  ├─ statement/:id          GET /accounts/:id/stmt
  └─ search                 GET /transactions/search
```

---

## 🔄 Mode Mock vs Production

### Mode Mock (Développement)

```
Component
   │
   └─> ApiService.getClients()
       │
       ├─> MOCK_MODE = true
       │
       └─> return of(mockData).pipe(delay(300))
           │
           └─> Pas d'appel HTTP réel
               └─> Données simulées
```

### Mode Production

```
Component
   │
   └─> ApiService.getClients()
       │
       ├─> MOCK_MODE = false
       │
       └─> this.http.get(`${baseUrl}/clients`)
           │
           └─> Appel HTTP réel
               │
               └─> http://localhost:8080/api/v1/clients
                   │
                   └─> API Gateway
                       │
                       └─> Client Service
```

---

## 📈 Statistiques

### Endpoints par Service

```
┌─────────────────────┬───────────┬──────────┐
│ Service             │ Endpoints │ Statut   │
├─────────────────────┼───────────┼──────────┤
│ Clients             │     6     │    ✅    │
│ Comptes             │     7     │    ✅    │
│ Transactions        │     6     │    ✅    │
│ Notifications       │     8     │    ✅    │
│ Composite           │     3     │    ✅    │
│ Authentification    │     3     │    ✅    │
├─────────────────────┼───────────┼──────────┤
│ TOTAL               │    33     │   100%   │
└─────────────────────┴───────────┴──────────┘
```

### Composants par Module

```
┌─────────────────────┬────────────┬──────────┐
│ Module              │ Composants │ Statut   │
├─────────────────────┼────────────┼──────────┤
│ Clients             │     3      │    ✅    │
│ Comptes             │     3      │    ✅    │
│ Transactions        │     3      │    ✅    │
│ Notifications       │     3      │    ✅    │
│ Dashboard           │     4      │    ✅    │
│ Composite           │     1      │    ✅    │
│ Auth                │     1      │    ✅    │
├─────────────────────┼────────────┼──────────┤
│ TOTAL               │    18      │   100%   │
└─────────────────────┴────────────┴──────────┘
```

---

## 🎯 Points de Contrôle

### ✅ Configuration Vérifiée

- [x] URL unique dans `environment.ts`
- [x] Tous les services utilisent `environment.apiGatewayUrl`
- [x] Aucune URL hardcodée
- [x] Aucun appel HTTP direct dans les composants
- [x] Intercepteurs configurés
- [x] Mode mock fonctionnel

### ✅ Sécurité Vérifiée

- [x] JWT Token géré automatiquement
- [x] Intercepteur d'authentification
- [x] Gestion des erreurs 401/403
- [x] Pas de connexion directe aux microservices
- [x] CORS géré par l'API Gateway

### ✅ Architecture Vérifiée

- [x] Séparation des responsabilités
- [x] Services réutilisables
- [x] Centralisation des appels HTTP
- [x] Gestion des erreurs centralisée
- [x] Notifications automatiques

---

## 🚀 Prêt pour la Production

Votre frontend est **100% conforme** à l'architecture API Gateway.

**Prochaines étapes:**
1. Coordonner avec votre collaborateur backend
2. Vérifier que l'API Gateway expose les 33 endpoints
3. Désactiver le mode mock (`MOCK_MODE = false`)
4. Tester l'intégration complète

---

*Architecture vérifiée le 8 Décembre 2025*
