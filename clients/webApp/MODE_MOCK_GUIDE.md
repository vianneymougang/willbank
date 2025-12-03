# 🧪 Guide du Mode Mock - WillBank CRM

## 📋 Vue d'ensemble

Le mode mock est **ACTIVÉ** pour tester toutes les fonctionnalités sans backend. Toutes les données sont simulées en mémoire.

## ✅ Fonctionnalités mockées

### 1. 🔐 Authentification
- **Login** : Accepte n'importe quel username/password
- **Token** : Génère un token JWT fictif
- **Session** : Stockée dans localStorage
- **Logout** : Supprime le token et redirige vers login

### 2. 👥 Clients (5 clients mockés)
```javascript
1. Jean Dupont - 0612345678
2. Marie Martin - 0623456789
3. Pierre Bernard - 0634567890
4. Sophie Dubois - 0645678901
5. Luc Thomas - 0656789012
```

**Opérations disponibles** :
- ✅ Liste des clients
- ✅ Détail d'un client
- ✅ Créer un client
- ✅ Modifier un client
- ✅ Supprimer un client

### 3. 💰 Comptes (5 comptes mockés)
```javascript
1. Compte Courant - Jean Dupont - 2500.50€
2. Compte Épargne - Jean Dupont - 15000.00€
3. Compte Courant - Marie Martin - 3200.75€
4. Compte Professionnel - Pierre Bernard - 45000.00€
5. Compte Courant - Sophie Dubois - 1800.25€
```

**Opérations disponibles** :
- ✅ Liste des comptes
- ✅ Détail d'un compte
- ✅ Comptes par client
- ✅ Créer un compte
- ✅ Modifier un compte
- ✅ Supprimer un compte

### 4. 💸 Transactions (5 transactions mockées)
```javascript
1. Virement - 500€ - 0612345678 → 0623456789 - Completed
2. Dépôt - 1000€ - 0634567890 - Completed
3. Retrait - 200€ - 0645678901 - Completed
4. Virement - 750.50€ - 0623456789 → 0656789012 - Pending
5. Dépôt - 2000€ - 0612345678 - Completed
```

**Opérations disponibles** :
- ✅ Liste des transactions
- ✅ Détail d'une transaction
- ✅ Créer une transaction (virement/dépôt/retrait)
- ✅ Modifier une transaction
- ✅ Supprimer une transaction

### 5. 🔔 Notifications (3 notifications mockées)
```javascript
1. Virement effectué - Jean Dupont - Lu
2. Nouveau dépôt - Marie Martin - Non lu
3. Relevé disponible - Jean Dupont - Non lu
```

**Opérations disponibles** :
- ✅ Liste des notifications
- ✅ Envoyer une notification

### 6. 📊 Composite (Dashboard agrégé)
**Opérations disponibles** :
- ✅ Dashboard client (comptes + solde total + transactions récentes)
- ✅ Relevé de compte par période
- ✅ Recherche de transactions (par type, date)

## 🎯 Comment tester

### Étape 1 : Connexion
1. Ouvrez l'application → Page de login
2. Entrez n'importe quoi :
   - Username : `admin`
   - Password : `password`
3. Cliquez sur "Se connecter"
4. ✅ Vous êtes connecté et redirigé vers le dashboard

### Étape 2 : Tester les Clients
1. Cliquez sur "Clients" dans la sidebar
2. Vous voyez 5 clients
3. **Créer** : Cliquez sur "Nouveau Client"
4. **Voir** : Cliquez sur l'icône œil
5. **Éditer** : Cliquez sur l'icône crayon
6. **Supprimer** : Cliquez sur l'icône poubelle

### Étape 3 : Tester les Comptes
1. Cliquez sur "Comptes" dans la sidebar
2. Vous voyez 5 comptes
3. **Créer** : Cliquez sur "Nouveau Compte"
   - Type : Courant/Épargne/Professionnel
   - Solde : 1000
   - Client ID : 1
4. **Voir/Éditer/Supprimer** : Comme pour les clients

### Étape 4 : Tester les Transactions
1. Cliquez sur "Transactions" dans la sidebar
2. Vous voyez 5 transactions
3. **Créer** : Cliquez sur "Nouvelle Transaction"
   - Type : Virement/Dépôt/Retrait
   - Montant : 500
   - Téléphone source : 0612345678
   - Téléphone destination : 0623456789 (si virement)
4. **Voir/Éditer/Supprimer** : Comme pour les clients

### Étape 5 : Tester les Notifications
1. Cliquez sur "Notifications" dans la sidebar
2. Vous voyez 3 notifications
3. **Envoyer** : Testez l'envoi de notification

### Étape 6 : Déconnexion
1. Cliquez sur "Déconnexion" dans la toolbar
2. ✅ Vous êtes redirigé vers la page de login

## 🔧 Configuration

### Activer/Désactiver le mode mock

**Dans `src/app/services/api.service.ts`** :
```typescript
const MOCK_MODE = true;  // true = mock, false = backend réel
```

**Dans `src/app/services/auth.service.ts`** :
```typescript
const MOCK_MODE = true;  // true = mock, false = backend réel
```

### Modifier les données mockées

Éditez les tableaux dans `api.service.ts` :
```typescript
private mockClients = [ /* vos données */ ];
private mockAccounts = [ /* vos données */ ];
private mockTransactions = [ /* vos données */ ];
private mockNotifications = [ /* vos données */ ];
```

## 📊 Données persistantes

⚠️ **Important** : Les données mockées sont en mémoire uniquement !
- ✅ Persistent pendant la session
- ❌ Perdues au rechargement de la page
- ❌ Non partagées entre onglets

Pour des données persistantes, utilisez :
- localStorage
- sessionStorage
- IndexedDB
- Ou connectez-vous au vrai backend

## 🎨 Fonctionnalités testables

### Interface
- ✅ Sidebar de navigation
- ✅ Toolbar avec notifications
- ✅ Formulaires avec validation
- ✅ Tableaux avec tri et pagination
- ✅ Cartes statistiques
- ✅ Animations et transitions
- ✅ Responsive design

### CRUD complet
- ✅ Create (Créer)
- ✅ Read (Lire)
- ✅ Update (Modifier)
- ✅ Delete (Supprimer)

### Validation
- ✅ Champs requis
- ✅ Format téléphone (10 chiffres)
- ✅ Format email
- ✅ Montants positifs
- ✅ Messages d'erreur

## 🚀 Passer en mode production

Quand votre backend est prêt :

1. **Désactivez le mode mock** :
```typescript
const MOCK_MODE = false;
```

2. **Configurez l'URL du backend** dans `src/environments/environment.ts` :
```typescript
export const environment = {
  production: false,
  apiGatewayUrl: 'http://localhost:8080'  // Votre URL
};
```

3. **Testez la connexion** :
- Les appels HTTP réels seront effectués
- Vérifiez les CORS
- Vérifiez l'authentification JWT

## 📝 Notes

### Délais simulés
Tous les appels mock ont un délai de **300-500ms** pour simuler la latence réseau.

### IDs auto-générés
Les nouveaux éléments reçoivent automatiquement un ID = max(IDs existants) + 1

### Numéros de compte
Générés automatiquement au format IBAN français : `FR76...`

### Statuts de transaction
- `completed` : Transaction terminée
- `pending` : Transaction en attente
- `failed` : Transaction échouée

## 🎉 Profitez du mode mock !

Vous pouvez maintenant tester toutes les fonctionnalités de l'application sans avoir besoin d'un backend. Toutes les opérations CRUD fonctionnent, les données sont cohérentes, et l'expérience utilisateur est complète !

---

**Version** : 1.0.0  
**Date** : Novembre 2024  
**Mode** : Mock activé ✅
