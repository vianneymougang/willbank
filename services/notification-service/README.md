# Notification Service

Microservice de gestion des notifications (Email et Push) avec intégration RabbitMQ.

## Fonctionnalités

- ✅ Envoi d'emails via JavaMailSender
- ✅ Envoi de notifications push via Firebase Cloud Messaging
- ✅ Consommation d'événements RabbitMQ (transactions)
- ✅ Historique des notifications (PostgreSQL)
- ✅ Templates de notifications
- ✅ API REST pour consultation de l'historique
- ✅ Retry automatique des notifications échouées
- ✅ Configuration pour API Gateway

## Prérequis

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- RabbitMQ 3.8+
- Compte Firebase (pour FCM)
- Compte Gmail (pour SMTP) ou autre serveur SMTP

## Configuration

### 1. Base de données PostgreSQL

```sql
CREATE DATABASE notifications_db;
```

### 2. RabbitMQ

Assurez-vous que RabbitMQ est démarré:
```bash
rabbitmq-server
```

### 3. Firebase Cloud Messaging

1. Créez un projet Firebase: https://console.firebase.google.com/
2. Téléchargez le fichier de credentials (Service Account)
3. Placez-le dans `src/main/resources/firebase-service-account.json`

### 4. Configuration Email

Modifiez `application.yml`:
```yaml
spring:
  mail:
    username: student@example.com
    password: votre-app-password
```

Pour Gmail, créez un App Password: https://myaccount.google.com/apppasswords

### 5. Variables d'environnement

```bash
export MAIL_USERNAME=student@example.com
export MAIL_PASSWORD=votre-app-password
export FIREBASE_CREDENTIALS_PATH=classpath:firebase-service-account.json
export FIREBASE_DATABASE_URL=https://votre-projet.firebaseio.com
```

## Démarrage

```bash
cd notification-service
mvn clean install
mvn spring-boot:run
```

Le service démarre sur: http://localhost:8082/api/notifications

## API Endpoints

### Envoyer une notification
```http
POST /api/notifications/history/send
Content-Type: application/json

{
  "recipient": "user@example.com",
  "subject": "Test Notification",
  "content": "Contenu du message",
  "type": "EMAIL",
  "userId": "user123",
  "fcmToken": "firebase-token-here"
}
```

### Historique des notifications
```http
GET /api/notifications/history?page=0&size=20
```

### Notifications d'un utilisateur
```http
GET /api/notifications/history/user/{userId}
```

### Statistiques
```http
GET /api/notifications/history/stats
```

### Réessayer les notifications échouées
```http
POST /api/notifications/history/retry-failed
```

## Intégration RabbitMQ

Le service écoute la queue `transaction.events` pour les événements de transactions.

Format d'événement attendu:
```json
{
  "transactionId": "TXN123",
  "userId": "user123",
  "type": "DEPOSIT",
  "amount": 100.00,
  "currency": "EUR",
  "status": "COMPLETED",
  "timestamp": "2024-01-01T10:00:00",
  "recipientEmail": "user@example.com",
  "fcmToken": "firebase-token"
}
```

## Tests

### Test Email
```bash
curl -X POST http://localhost:8082/api/notifications/history/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "test@example.com",
    "subject": "Test Email",
    "content": "<h1>Test</h1><p>Ceci est un test</p>",
    "type": "EMAIL"
  }'
```

### Test Push Notification
```bash
curl -X POST http://localhost:8082/api/notifications/history/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Test Push",
    "content": "Message de test",
    "type": "PUSH",
    "fcmToken": "VOTRE_FCM_TOKEN"
  }'
```

### Test via RabbitMQ
Publiez un message sur l'exchange `transaction.exchange` avec routing key `transaction.completed`

## Configuration API Gateway

Le service est configuré pour communiquer avec une API Gateway sur `http://localhost:8080`.

Headers requis depuis l'API Gateway:
- `X-User-Id`: ID de l'utilisateur
- `X-Service-Id`: notification-service

## Structure du projet

```
notification-service/
├── src/main/java/com/example/notificationservice/
│   ├── config/          # Configurations (RabbitMQ, Firebase, CORS)
│   ├── controller/      # REST Controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # Entités JPA
│   ├── listener/       # RabbitMQ Listeners
│   ├── repository/     # Repositories JPA
│   └── service/        # Services métier
├── src/main/resources/
│   ├── application.yml
│   └── firebase-service-account.json
└── pom.xml
```

## Logs

Les logs sont disponibles dans la console. Niveau de log: DEBUG

## Troubleshooting

### Erreur Firebase
- Vérifiez que le fichier credentials est présent
- Vérifiez les permissions du Service Account

### Erreur Email
- Vérifiez les credentials SMTP
- Pour Gmail, activez "App Passwords"
- Vérifiez les paramètres de sécurité du compte

### Erreur RabbitMQ
- Vérifiez que RabbitMQ est démarré
- Vérifiez les credentials (guest/guest par défaut)

## Licence

MIT
