# Guide de Test - Système de Notification WillBank

## 🎯 Configuration MailTrap Local

**MailTrap Local** est maintenant intégré dans `docker-compose.yml` via l'image `dbck/mailtrap`.

### Ports Exposés

| Service | Port | URL | Description |
|---------|------|-----|-------------|
| **Web UI** | 1080 | http://localhost:1080 | Interface webmail pour consulter les emails |
| **SMTP** | 1025 | mailtrap:25 (interne) | Serveur SMTP pour envoi emails |

## 🚀 Workflow de Test Complet

### 1. Démarrer l'Infrastructure

```bash
# Depuis la racine du projet
docker-compose up -d

# Vérifier que tous les services sont UP
docker-compose ps
```

**Services attendus:**
- ✅ config-server (healthy)
- ✅ discovery-server (healthy)
- ✅ postgres-users (healthy)
- ✅ rabbitmq (healthy)
- ✅ mongodb (healthy)
- ✅ **mailtrap** (started)
- ✅ user-service (started)
- ✅ notification-service (started)
- ✅ api-gateway (started)

### 2. Créer un Client (Déclenche l'Email)

```http
POST http://localhost:8080/api/users/admin/customers
X-User-Id: 1
X-User-Email: admin@willbank.com
X-User-Role: ADMIN
Content-Type: application/json

{
  "email": "test.customer@example.com",
  "name": "Jean",
  "surname": "Dupont",
  "phone": "+237612345678",
  "address": "123 Rue de Test, Yaoundé"
}
```

### 3. Suivre le Flux de l'Événement

**A. Vérifier les logs user-service:**
```bash
docker-compose logs -f user-service | grep "Publishing CustomerCreatedEvent"
```

Expected output:
```
📧 Publishing CustomerCreatedEvent to RabbitMQ for customer: test.customer@example.com
✅ Successfully published CustomerCreatedEvent to exchange: willbank.customer.exchange
```

**B. Vérifier RabbitMQ Management UI:**
1. Ouvrir http://localhost:15672 (guest/guest)
2. Onglet "Queues"
3. Vérifier queue `willbank.customer.created`:
   - Messages ready: 0 (consommé)
   - Total messages: +1

**C. Vérifier les logs notification-service:**
```bash
docker-compose logs -f notification-service | grep "CustomerCreatedEvent"
```

Expected output:
```
📨 Received CustomerCreatedEvent for customer: test.customer@example.com
✅ Welcome email sent successfully to: test.customer@example.com
✅ Successfully processed CustomerCreatedEvent for: test.customer@example.com
```

**D. Vérifier MongoDB:**
```bash
docker exec -it willbank-mongodb mongosh -u admin -p admin123

# Dans mongosh
use notifications
db.notifications.find().pretty()
```

Expected:
```json
{
  "_id": ObjectId("..."),
  "customerId": 123,
  "email": "test.customer@example.com",
  "type": "CUSTOMER_WELCOME",
  "subject": "Bienvenue chez WillBank - Votre compte a été créé",
  "status": "SENT",
  "sentAt": ISODate("..."),
  "createdAt": ISODate("...")
}
```

### 4. Consulter l'Email dans MailTrap Web UI

**Accès:** http://localhost:1080

**Ce que vous verrez:**
1. Liste des emails reçus dans l'inbox
2. Cliquer sur l'email de bienvenue
3. Vérifier:
   - ✅ Header bleu (#1E3A5F) avec logo 🏦 WillBank
   - ✅ Message de bienvenue personnalisé (Jean Dupont)
   - ✅ Credentials box avec email et mot de passe temporaire
   - ✅ Alerte sécurité (changement obligatoire)
   - ✅ Bouton CTA bleu "Se connecter maintenant"
   - ✅ Footer avec infos WillBank

## 📊 Événements Implémentés

### ✅ CustomerCreatedEvent (Actif)

**Flux complet:**
```
user-service (AdminController)
    ↓ publish
RabbitMQ (willbank.customer.created queue)
    ↓ consume
notification-service (CustomerCreatedEventConsumer)
    ↓ send email
MailTrap Local (SMTP:1025)
    ↓ store
MailTrap Web UI (http://localhost:1080)
```

**Template email:** Welcome email avec credentials

### ⏳ AccountCreatedEvent (Préparé)

**Status:** Consumer prêt, attend publishers dans account-service

**Pour activer:**
1. Ajouter RabbitMQ au account-service
2. Publier AccountCreatedEvent lors création compte
3. Email automatique envoyé

### ⏳ TransactionCompletedEvent (Préparé)

**Status:** Infrastructure RabbitMQ créée, attend transaction-service

## 🛠️ Outils de Debug

### MailTrap Web UI
- **URL**: http://localhost:1080
- **Features**:
  - Liste tous les emails interceptés
  - Preview HTML/Text
  - Raw source
  - Headers analysis

### RabbitMQ Management
- **URL**: http://localhost:15672
- **Login**: guest / guest
- **Vérifier**:
  - Exchanges: `willbank.customer.exchange`
  - Queues: `willbank.customer.created`, `willbank.dlq`
  - Message rates

### Accès Mongo Express (MongoDB UI)
- **URL**: http://localhost:8087
- **Login**: admin / admin123
- **Database**: notifications
- **Collection**: notifications

### Eureka Discovery
- **URL**: http://localhost:8761
- **Vérifier**: notification-service registered

## 🔧 Troubleshooting

### Email non reçu dans MailTrap

**1. Vérifier MailTrap service:**
```bash
docker-compose ps mailtrap
# Should be "Up"
```

**2. Vérifier logs notification-service:**
```bash
docker-compose logs notification-service | grep -i mail
```

**3. Vérifier config SMTP:**
```bash
docker exec willbank-notification-service env | grep SPRING_MAIL
```

### Message dans Dead Letter Queue

```bash
# RabbitMQ Management UI → Queues → willbank.dlq
# Get Messages → Get Message(s)
```

**Causes communes:**
- Email service down
- MongoDB connection failure
- Event deserialization error

### Redémarrer un Service

```bash
# Notification service uniquement
docker-compose restart notification-service

# MailTrap uniquement
docker-compose restart mailtrap

# Tout reconstruire
docker-compose down
docker-compose up -d --build
```

## 📈 Tests Additionnels

### Test Multiple Customers

```bash
# Script pour créer 5 customers
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/users/admin/customers \
    -H "X-User-Id: 1" \
    -H "X-User-Email: admin@willbank.com" \
    -H "X-User-Role: ADMIN" \
    -H "Content-Type: application/json" \
    -d "{
      \"email\": \"customer$i@test.com\",
      \"name\": \"Customer\",
      \"surname\": \"Test$i\",
      \"phone\": \"+23761234567$i\",
      \"address\": \"Address $i\"
    }"
  sleep 2
done
```

**Résultat attendu:**
- 5 emails dans MailTrap Web UI
- 5 records dans MongoDB
- 5 messages consommés sur RabbitMQ

## 🎓 Résumé

**Configuration Actuelle:**
- ✅ MailTrap Local (dbck/mailtrap) - SMTP local, pas de credentials SaaS requis
- ✅ Web UI accessible sur http://localhost:1080
- ✅ Tous les emails interceptés automatiquement
- ✅ Pas de vraie livraison email (safe pour dev/test)

**Avantages:**
- 🚀 Pas besoin de compte MailTrap SaaS
- 🔒 100% local et offline
- 👀 Interface web simple et claire
- 📝 Tous les emails sauvegardés pour inspection
