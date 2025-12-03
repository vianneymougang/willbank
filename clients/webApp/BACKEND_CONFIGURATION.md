# 🔧 Configuration Backend - WillBank CRM

## 📋 État actuel

**Mode Mock** : ❌ DÉSACTIVÉ  
**Backend** : ✅ REQUIS

L'application est maintenant configurée pour communiquer avec un backend réel.

## 🌐 Configuration de l'URL

### Fichier de configuration
`src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiGatewayUrl: 'http://localhost:8080'  // ⚠️ À configurer selon votre backend
};
```

### Pour la production
`src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiGatewayUrl: 'https://api.willbank.com'  // URL de production
};
```

## 📡 Endpoints attendus

### Base URL
```
{apiGatewayUrl}/api/v1
```

### 🔐 Authentification

#### POST `/auth/login`
**Request:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "jwt-token-string",
  "user": {
    "username": "string",
    "role": "string"
  }
}
```

### 👥 Clients

#### GET `/clients`
Liste tous les clients

**Response:**
```json
[
  {
    "id": 1,
    "nom": "string",
    "prenom": "string",
    "email": "string",
    "telephone": "string",
    "adresse": "string"
  }
]
```

#### GET `/clients/:id`
Détail d'un client

#### POST `/clients`
Créer un client

**Request:**
```json
{
  "nom": "string",
  "prenom": "string",
  "email": "string",
  "telephone": "string",
  "adresse": "string"
}
```

#### PUT `/clients/:id`
Modifier un client

#### DELETE `/clients/:id`
Supprimer un client

### 💰 Comptes

#### GET `/accounts`
Liste tous les comptes

**Response:**
```json
[
  {
    "id": 1,
    "type": "courant|epargne|professionnel",
    "solde": 0.00,
    "clientId": 1,
    "numeroCompte": "string",
    "dateCreation": "YYYY-MM-DD"
  }
]
```

#### GET `/accounts/:id`
Détail d'un compte

#### GET `/clients/:clientId/accounts`
Comptes d'un client

#### POST `/accounts`
Créer un compte

**Request:**
```json
{
  "type": "courant|epargne|professionnel",
  "solde": 0.00,
  "clientId": 1
}
```

#### PUT `/accounts/:id`
Modifier un compte

#### DELETE `/accounts/:id`
Supprimer un compte

### 💸 Transactions

#### GET `/transactions`
Liste toutes les transactions

**Response:**
```json
[
  {
    "id": 1,
    "type": "virement|depot|retrait",
    "montant": 0.00,
    "telephoneSource": "string",
    "telephoneDest": "string",
    "date": "YYYY-MM-DD",
    "statut": "completed|pending|failed"
  }
]
```

#### GET `/transactions/:id`
Détail d'une transaction

#### POST `/transactions/transfer`
Créer une transaction

**Request:**
```json
{
  "type": "virement|depot|retrait",
  "montant": 0.00,
  "telephoneSource": "0612345678",
  "telephoneDest": "0623456789"
}
```

#### PUT `/transactions/:id`
Modifier une transaction

#### DELETE `/transactions/:id`
Supprimer une transaction

### 🔔 Notifications

#### GET `/notifications`
Liste des notifications

**Response:**
```json
[
  {
    "id": 1,
    "clientId": 1,
    "message": "string",
    "date": "YYYY-MM-DD",
    "type": "transaction|info|alert",
    "lu": false
  }
]
```

#### POST `/notifications/send`
Envoyer une notification

**Request:**
```json
{
  "clientId": 1,
  "message": "string",
  "type": "transaction|info|alert"
}
```

### 📊 Composite (Endpoints agrégés)

#### GET `/composite/clients/:clientId/dashboard`
Dashboard client complet

**Response:**
```json
{
  "client": { /* objet client */ },
  "accounts": [ /* liste comptes */ ],
  "totalSolde": 0.00,
  "nombreComptes": 0,
  "recentTransactions": [ /* 5 dernières transactions */ ]
}
```

#### GET `/composite/accounts/:accountId/statement?period=string`
Relevé de compte

**Response:**
```json
{
  "account": { /* objet compte */ },
  "period": "string",
  "transactions": [ /* liste transactions */ ],
  "soldeDebut": 0.00,
  "soldeFin": 0.00
}
```

#### GET `/composite/transactions/search?type=&dateDebut=&dateFin=`
Recherche de transactions

**Query params:**
- `type`: virement|depot|retrait
- `dateDebut`: YYYY-MM-DD
- `dateFin`: YYYY-MM-DD

## 🔒 Authentification JWT

### Header requis
```
Authorization: Bearer {token}
```

### Intercepteur configuré
L'application utilise un intercepteur JWT (`jwt.interceptor.ts`) qui ajoute automatiquement le token à toutes les requêtes.

### Gestion du token
- **Stockage** : localStorage (`jwt`)
- **Ajout automatique** : Sur toutes les requêtes API
- **Suppression** : À la déconnexion

## 🚨 CORS

Votre backend doit autoriser les requêtes depuis :
```
http://localhost:4200
```

### Configuration Spring Boot (exemple)
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

## 🧪 Tester la connexion

### 1. Vérifier que le backend est démarré
```bash
curl http://localhost:8080/api/v1/clients
```

### 2. Tester l'authentification
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### 3. Démarrer l'application Angular
```bash
ng serve
```

### 4. Ouvrir le navigateur
```
http://localhost:4200
```

## 🔄 Réactiver le mode mock

Si vous voulez revenir au mode mock pour les tests :

**Dans `src/app/services/auth.service.ts`** :
```typescript
const MOCK_MODE = true;
```

**Dans `src/app/services/api.service.ts`** :
```typescript
const MOCK_MODE = true;
```

## 📝 Gestion des erreurs

L'application gère automatiquement :
- ✅ Erreurs réseau (affichage dans la console)
- ✅ Erreurs 401 (redirection vers login)
- ✅ Erreurs 404 (données non trouvées)
- ✅ Erreurs 500 (erreur serveur)

### Ajouter un toast pour les erreurs (optionnel)

Installez PrimeNG Toast :
```bash
npm install primeng
```

Ajoutez dans votre composant :
```typescript
import { MessageService } from 'primeng/api';

constructor(private messageService: MessageService) {}

showError(message: string) {
  this.messageService.add({
    severity: 'error',
    summary: 'Erreur',
    detail: message
  });
}
```

## 🎯 Checklist de déploiement

- [ ] Backend démarré et accessible
- [ ] CORS configuré
- [ ] Endpoints implémentés
- [ ] JWT configuré
- [ ] URL backend configurée dans `environment.ts`
- [ ] Mode mock désactivé (`MOCK_MODE = false`)
- [ ] Tests de connexion effectués
- [ ] Gestion des erreurs vérifiée

## 📞 Support

En cas de problème :
1. Vérifiez la console du navigateur (F12)
2. Vérifiez les logs du backend
3. Vérifiez la configuration CORS
4. Vérifiez l'URL de l'API Gateway

---

**Version** : 1.0.0  
**Date** : Novembre 2024  
**Mode** : Backend réel ✅
