# Guide de Démarrage Rapide

## 🚀 Démarrage en 5 minutes

### Étape 1: Prérequis

Vérifiez que vous avez installé:
- ✅ Java 17+: `java -version`
- ✅ Maven: `mvn -version`
- ✅ Docker & Docker Compose: `docker --version`

### Étape 2: Configuration

```bash
cd notification-service

# Copier les fichiers de configuration
cp .env.example .env
cp src/main/resources/firebase-service-account.example.json src/main/resources/firebase-service-account.json

# Éditer .env avec vos configurations
nano .env
```

**Configuration minimale dans .env:**
```bash
MAIL_USERNAME=student@example.com
MAIL_PASSWORD=votre-app-password-gmail
```

### Étape 3: Démarrer avec Docker

```bash
# Démarrer tous les services (PostgreSQL, RabbitMQ, Service)
docker-compose up -d

# Vérifier que tout fonctionne
docker-compose ps

# Voir les logs
docker-compose logs -f notification-service
```

### Étape 4: Tester

```bash
# Test de santé
curl http://localhost:8082/api/notifications/history/stats

# Envoyer un email de test
bash scripts/test-email.sh

# Publier un événement RabbitMQ
python3 scripts/test-rabbitmq.py
```

### Étape 5: Interface de test (Frontend Angular)

```bash
# Dans le dossier racine du projet Angular
cd ..
npm install
npm start

# Accéder à l'interface de test
# http://localhost:4200/notifications-test
```

## 🎯 Tests Rapides

### Test Email

```bash
curl -X POST http://localhost:8082/api/notifications/history/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "votre-email@example.com",
    "subject": "Test",
    "content": "<h1>Test</h1>",
    "type": "EMAIL"
  }'
```

### Test Push (FCM)

```bash
curl -X POST http://localhost:8082/api/notifications/history/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Test Push",
    "content": "Message de test",
    "type": "PUSH",
    "fcmToken": "VOTRE_TOKEN_FCM"
  }'
```

### Consulter l'historique

```bash
curl http://localhost:8082/api/notifications/history
```

## 📊 Interfaces Web

- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Frontend Angular**: http://localhost:4200/notifications-test
- **API Swagger** (si activé): http://localhost:8082/swagger-ui.html

## 🔧 Commandes Utiles

```bash
# Arrêter les services
docker-compose down

# Redémarrer
docker-compose restart

# Voir les logs
docker-compose logs -f

# Nettoyer tout
docker-compose down -v

# Rebuild
docker-compose up -d --build
```

## 🐛 Dépannage Rapide

### Le service ne démarre pas

```bash
# Vérifier les logs
docker-compose logs notification-service

# Vérifier PostgreSQL
docker-compose logs postgres

# Vérifier RabbitMQ
docker-compose logs rabbitmq
```

### Erreur d'envoi d'email

1. Vérifiez vos credentials dans `.env`
2. Pour Gmail, créez un "App Password": https://myaccount.google.com/apppasswords
3. Activez la validation en 2 étapes

### Erreur Firebase

1. Téléchargez le fichier credentials depuis Firebase Console
2. Placez-le dans `src/main/resources/firebase-service-account.json`
3. Vérifiez les permissions du fichier

### RabbitMQ ne reçoit pas les messages

```bash
# Vérifier que RabbitMQ est démarré
docker-compose ps rabbitmq

# Vérifier les queues
curl -u guest:guest http://localhost:15672/api/queues
```

## 📚 Documentation Complète

- **README.md**: Documentation générale
- **DEPLOYMENT.md**: Guide de déploiement
- **NOTIFICATION_SERVICE_GUIDE.md**: Guide d'intégration

## 🆘 Support

En cas de problème:
1. Vérifiez les logs: `docker-compose logs -f`
2. Consultez la documentation
3. Vérifiez la configuration réseau et les ports
