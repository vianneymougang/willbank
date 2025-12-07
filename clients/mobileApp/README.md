# WillBank Mobile App

Application mobile bancaire pour WillBank, développée avec React Native et Expo.

## 🏗️ Architecture

L'application suit une **Clean Architecture** avec une approche **Feature-First** :

```
src/
├── core/                      # Couche Domain
│   └── entities/              # Modèles métier (User, Account, Transaction)
│
├── config/                    # Configuration
│   ├── constants.ts           # Constantes de l'app
│   ├── theme.ts               # Design System
│   └── firebase.ts            # Configuration FCM
│
├── data/                      # Couche Data
│   ├── api/                   # Client HTTP & endpoints
│   └── services/              # Services (auth, dashboard)
│
├── presentation/              # Couche Présentation
│   ├── providers/             # Context (Auth, Theme)
│   └── components/ui/         # Composants réutilisables
│
├── features/                  # Modules fonctionnels
│   ├── auth/                  # Authentification
│   ├── dashboard/             # Accueil
│   ├── accounts/              # Gestion des comptes
│   ├── transactions/          # Historique
│   └── settings/              # Paramètres
│
app/                           # Navigation (expo-router)
├── (auth)/                    # Écrans non connectés
├── (tabs)/                    # Écrans connectés (tabs)
└── _layout.tsx                # Root layout
```

## 🚀 Installation

```bash
# Installer les dépendances
pnpm install

# Lancer l'application
pnpm start

# Lancer sur Android
pnpm android

# Lancer sur iOS
pnpm ios
```

## 📱 Fonctionnalités

### Sprint 1 (Actuel)
- ✅ Structure Clean Architecture
- ✅ Design System (thème clair/sombre)
- ✅ Navigation (auth flow + tabs)
- ✅ Écran de connexion
- ✅ Écran d'inscription
- ✅ Dashboard avec solde et transactions
- ✅ Liste des comptes
- ✅ Historique des transactions
- ✅ Paramètres utilisateur
- ✅ Service API (mock)
- 🔲 Intégration Firebase FCM (préparé)

### Écrans
| Écran | Route | Description |
|-------|-------|-------------|
| Splash | `/` | Écran de chargement |
| Login | `/(auth)/login` | Connexion |
| Register | `/(auth)/register` | Inscription |
| Forgot Password | `/(auth)/forgot-password` | Récupération MDP |
| Dashboard | `/(tabs)/` | Accueil avec solde |
| Accounts | `/(tabs)/accounts` | Liste des comptes |
| Transactions | `/(tabs)/transactions` | Historique |
| Settings | `/(tabs)/settings` | Paramètres |

## 🎨 Design System

### Couleurs
```typescript
primary: '#1E3A5F'    // Bleu marine (confiance)
secondary: '#00B894'  // Vert menthe (succès)
accent: '#F39C12'     // Orange (CTA)
```

### Composants UI
- `Button` - Primary, Secondary, Outline, Ghost
- `Input` - Text, Password, avec icônes
- `Card` - Default, Elevated, Outlined

## 🔌 API Gateway

L'application communique avec l'API Gateway Spring Cloud :

```bash
// Endpoints principaux
/auth/login          // Connexion
/auth/register       // Inscription
/clients/me          // Profil utilisateur
/accounts            // Liste des comptes
/transactions        // Historique
/composite/dashboard // Dashboard agrégé
/notifications/fcm   // Push notifications
```

## 📦 Dépendances principales

- **expo** - Framework React Native
- **expo-router** - Navigation file-based
- **react-native-reanimated** - Animations
- **expo-secure-store** - Stockage sécurisé
- **expo-local-authentication** - Biométrie

## 🔔 Push Notifications (Firebase)

Pour activer les notifications push :

1. Créer un projet Firebase
2. Ajouter `google-services.json` (Android) et `GoogleService-Info.plist` (iOS)
3. Installer les packages Firebase :
```bash
npx expo install @react-native-firebase/app @react-native-firebase/messaging
```

## 👥 Équipe

Projet réalisé dans le cadre du TP Microservices - WillBank.

---

© 2024 WillBank. Tous droits réservés.
