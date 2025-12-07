# Guide Complet - Configuration et Tests

## 1. Installation et Configuration de PostgreSQL

### Installation Windows
```bash
# Télécharger depuis https://www.postgresql.org/download/windows/
# Ou avec Chocolatey
choco install postgresql
```

### Créer la base de données
```sql
-- Se connecter à PostgreSQL
psql -U postgres

-- Créer la base de données
CREATE DATABASE willbank_transactions;

-- Créer un utilisateur (optionnel)
CREATE USER willbank WITH PASSWORD 'willbank123';
GRANT ALL PRIVILEGES ON DATABASE willbank_transactions TO willbank;

-- Vérifier
\l
\q
```

### Configuration dans application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/willbank_transactions
spring.datasource.username=postgres
spring.datasource.password=votre_mot_de_passe
```

---

## 2. Installation et Configuration de RabbitMQ

### Installation Windows

#### Option 1 : Avec Chocolatey (Recommandé)
```bash
# Installer Erlang (prérequis)
choco install erlang

# Installer RabbitMQ
choco install rabbitmq

# Démarrer le service
rabbitmq-service start
```

#### Option 2 : Installation manuelle
1. Télécharger Erlang : https://www.erlang.org/downloads
2. Télécharger RabbitMQ : https://www.rabbitmq.com/download.html
3. Installer dans l'ordre : Erlang puis RabbitMQ

### Activer le Management Plugin
```bash
# Activer le plugin de management (interface web)
rabbitmq-plugins enable rabbitmq_management

# Redémarrer RabbitMQ
rabbitmq-service stop
rabbitmq-service start
```

### Accéder à l'interface web
- URL : http://localhost:15672
- Username : `guest`
- Password : `guest`

### Créer un utilisateur personnalisé (optionnel)
```bash
# Créer un utilisateur
rabbitmqctl add_user willbank willbank123

# Donner les permissions
rabbitmqctl set_user_tags willbank administrator
rabbitmqctl set_permissions -p / willbank ".*" ".*" ".*"
```

### Vérifier que RabbitMQ fonctionne
```bash
# Vérifier le statut
rabbitmqctl status

# Lister les utilisateurs
rabbitmqctl list_users

# Lister les queues
rabbitmqctl list_queues
```

---

## 3. Démarrage de l'Application

### Compiler et lancer
```bash
# Compiler
mvnw clean install

# Lancer l'application
mvnw spring-boot:run
```

### Vérifier le démarrage
L'application crée automatiquement :
- Exchange : `transaction.exchange`
- Queue : `transaction.queue`
- Binding avec routing key : `transaction.created`

Vérifier dans l'interface RabbitMQ (http://localhost:15672) :
- Onglet "Exchanges" → voir `transaction.exchange`
- Onglet "Queues" → voir `transaction.queue`

---

## 4. Tests avec Postman / cURL

### 4.1 Créer un Dépôt

**Requête :**
```bash
curl -X POST http://localhost:8082/api/transactions/depot \
  -H "Content-Type: application/json" \
  -d '{
    "accountIdSource": "ACC001",
    "amount": 1000.00,
    "type": "DEPOT",
    "description": "Dépôt initial"
  }'
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
  "description": "Dépôt initial",
  "createdAt": "2024-12-07T10:30:00",
  "completedAt": "2024-12-07T10:30:00",
  "failureReason": null
}
```

### 4.2 Créer un Virement Interne

**Requête :**
```bash
curl -X POST http://localhost:8082/api/transactions/virement-interne \
  -H "Content-Type: application/json" \
  -d '{
    "accountIdSource": "ACC001",
    "accountIdDestination": "ACC002",
    "amount": 250.00,
    "type": "VIREMENT_INTERNE",
    "description": "Virement vers compte épargne"
  }'
```

**Cas d'erreur - Solde insuffisant :**
```bash
curl -X POST http://localhost:8082/api/transactions/virement-interne \
  -H "Content-Type: application/json" \
  -d '{
    "accountIdSource": "ACC001",
    "accountIdDestination": "ACC002",
    "amount": 5000.00,
    "type": "VIREMENT_INTERNE",
    "description": "Montant trop élevé"
  }'
```

**Réponse erreur :**
```json
{
  "status": 400,
  "message": "Insufficient balance for transfer",
  "timestamp": "2024-12-07T10:35:00"
}
```

### 4.3 Créer un Retrait

**Requête :**
```bash
curl -X POST http://localhost:8082/api/transactions/retrait \
  -H "Content-Type: application/json" \
  -d '{
    "accountIdSource": "ACC001",
    "amount": 100.00,
    "type": "RETRAIT",
    "description": "Retrait DAB"
  }'
```

### 4.4 Consulter l'Historique

**Requête :**
```bash
curl -X GET http://localhost:8082/api/transactions/history/ACC001
```

**Réponse :**
```json
[
  {
    "id": 1,
    "accountIdSource": "ACC001",
    "amount": 1000.00,
    "type": "DEPOT",
    "status": "COMPLETED",
    "createdAt": "2024-12-07T10:30:00"
  },
  {
    "id": 2,
    "accountIdSource": "ACC001",
    "accountIdDestination": "ACC002",
    "amount": 250.00,
    "type": "VIREMENT_INTERNE",
    "status": "COMPLETED",
    "createdAt": "2024-12-07T10:32:00"
  }
]
```

### 4.5 Recherche avec Filtres

**Par type :**
```bash
curl -X GET "http://localhost:8082/api/transactions/search?accountId=ACC001&type=DEPOT"
```

**Par statut :**
```bash
curl -X GET "http://localhost:8082/api/transactions/search?accountId=ACC001&status=COMPLETED"
```

**Par type ET statut :**
```bash
curl -X GET "http://localhost:8082/api/transactions/search?accountId=ACC001&type=VIREMENT_INTERNE&status=COMPLETED"
```

### 4.6 Recherche par Période

**Requête :**
```bash
curl -X GET "http://localhost:8082/api/transactions/date-range?accountId=ACC001&startDate=2024-12-01T00:00:00&endDate=2024-12-31T23:59:59"
```

---

## 5. Vérifier les Événements RabbitMQ

### Via l'Interface Web

1. Aller sur http://localhost:15672
2. Onglet "Queues" → Cliquer sur `transaction.queue`
3. Section "Get messages" → Cliquer sur "Get Message(s)"
4. Voir les événements publiés

**Format de l'événement :**
```json
{
  "transactionId": 1,
  "accountIdSource": "ACC001",
  "accountIdDestination": "ACC002",
  "amount": 250.00,
  "type": "VIREMENT_INTERNE",
  "status": "COMPLETED",
  "timestamp": "2024-12-07T10:32:00"
}
```

### Via Code - Créer un Consumer

Créer un listener pour tester la réception :

```java
@Component
@Slf4j
public class TransactionEventListener {
    
    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_QUEUE)
    public void handleTransactionEvent(TransactionEventDTO event) {
        log.info("Received transaction event: {}", event);
        // Traiter l'événement
    }
}
```

---

## 6. Tests Automatisés

### Exécuter tous les tests
```bash
mvnw test
```

### Tests unitaires uniquement
```bash
mvnw test -Dtest=TransactionServiceTest
mvnw test -Dtest=TransactionRepositoryTest
mvnw test -Dtest=TransactionControllerTest
```

### Tests d'intégration
```bash
mvnw test -Dtest=TransactionIntegrationTest
```

### Voir les résultats détaillés
```bash
mvnw test -X
```

---

## 7. Scénarios de Test Complets

### Scénario 1 : Cycle de vie complet d'un compte

```bash
# 1. Dépôt initial
curl -X POST http://localhost:8082/api/transactions/depot \
  -H "Content-Type: application/json" \
  -d '{"accountIdSource": "ACC001", "amount": 5000.00, "type": "DEPOT", "description": "Salaire"}'

# 2. Virement vers un autre compte
curl -X POST http://localhost:8082/api/transactions/virement-interne \
  -H "Content-Type: application/json" \
  -d '{"accountIdSource": "ACC001", "accountIdDestination": "ACC002", "amount": 1500.00, "type": "VIREMENT_INTERNE", "description": "Loyer"}'

# 3. Retrait
curl -X POST http://localhost:8082/api/transactions/retrait \
  -H "Content-Type: application/json" \
  -d '{"accountIdSource": "ACC001", "amount": 200.00, "type": "RETRAIT", "description": "Courses"}'

# 4. Consulter l'historique
curl -X GET http://localhost:8082/api/transactions/history/ACC001
```

### Scénario 2 : Tests de validation

```bash
# Montant négatif (doit échouer)
curl -X POST http://localhost:8082/api/transactions/depot \
  -H "Content-Type: application/json" \
  -d '{"accountIdSource": "ACC001", "amount": -100.00, "type": "DEPOT"}'

# Compte source vide (doit échouer)
curl -X POST http://localhost:8082/api/transactions/depot \
  -H "Content-Type: application/json" \
  -d '{"accountIdSource": "", "amount": 100.00, "type": "DEPOT"}'

# Virement vers le même compte (doit échouer)
curl -X POST http://localhost:8082/api/transactions/virement-interne \
  -H "Content-Type: application/json" \
  -d '{"accountIdSource": "ACC001", "accountIdDestination": "ACC001", "amount": 100.00, "type": "VIREMENT_INTERNE"}'
```

---

## 8. Monitoring et Debugging

### Logs de l'application
```bash
# Voir les logs en temps réel
tail -f logs/spring.log
```

### Vérifier la base de données
```sql
-- Se connecter
psql -U postgres -d willbank_transactions

-- Voir toutes les transactions
SELECT * FROM transactions;

-- Compter par type
SELECT type, COUNT(*) FROM transactions GROUP BY type;

-- Compter par statut
SELECT status, COUNT(*) FROM transactions GROUP BY status;

-- Transactions d'un compte
SELECT * FROM transactions 
WHERE account_id_source = 'ACC001' 
   OR account_id_destination = 'ACC001'
ORDER BY created_at DESC;
```

### Vérifier RabbitMQ
```bash
# Statistiques des queues
rabbitmqctl list_queues name messages consumers

# Voir les connexions
rabbitmqctl list_connections

# Voir les channels
rabbitmqctl list_channels
```

---

## 9. Collection Postman

Créer une collection Postman avec ces requêtes :

**Variables d'environnement :**
- `base_url` : `http://localhost:8082`
- `account1` : `ACC001`
- `account2` : `ACC002`

**Requêtes à ajouter :**
1. POST - Créer Dépôt
2. POST - Créer Retrait
3. POST - Créer Virement Interne
4. GET - Historique Transactions
5. GET - Recherche par Type
6. GET - Recherche par Statut
7. GET - Recherche par Période

---

## 10. Troubleshooting

### RabbitMQ ne démarre pas
```bash
# Vérifier les logs
rabbitmqctl status

# Réinstaller le service
rabbitmq-service remove
rabbitmq-service install
rabbitmq-service start
```

### PostgreSQL - Erreur de connexion
```bash
# Vérifier que le service est démarré
pg_ctl status

# Redémarrer PostgreSQL
pg_ctl restart
```

### Application ne démarre pas
```bash
# Vérifier les ports
netstat -ano | findstr :8082
netstat -ano | findstr :5432
netstat -ano | findstr :5672

# Nettoyer et recompiler
mvnw clean install -DskipTests
```

### Erreur "Insufficient balance"
C'est normal si le service de comptes n'est pas démarré. Le `AccountServiceClient` retourne `BigDecimal.ZERO` par défaut. Pour tester sans le service de comptes, vous pouvez :
1. Modifier temporairement le client pour retourner un solde fictif
2. Ou créer uniquement des dépôts (qui ne vérifient pas le solde)

---

## 11. Commandes Utiles

### Maven
```bash
# Compiler sans tests
mvnw clean install -DskipTests

# Lancer en mode debug
mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Voir les dépendances
mvnw dependency:tree
```

### Docker (Alternative)
```bash
# PostgreSQL
docker run --name postgres-willbank -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=willbank_transactions -p 5432:5432 -d postgres:15

# RabbitMQ
docker run --name rabbitmq-willbank -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management

# Voir les logs
docker logs -f postgres-willbank
docker logs -f rabbitmq-willbank
```

---

## 12. Checklist de Validation

- [ ] PostgreSQL installé et base créée
- [ ] RabbitMQ installé et démarré
- [ ] Interface RabbitMQ accessible (http://localhost:15672)
- [ ] Application démarre sans erreur
- [ ] Exchange et Queue créés dans RabbitMQ
- [ ] Dépôt fonctionne
- [ ] Retrait fonctionne
- [ ] Virement interne fonctionne
- [ ] Historique retourne les transactions
- [ ] Recherche par type fonctionne
- [ ] Recherche par statut fonctionne
- [ ] Recherche par période fonctionne
- [ ] Événements publiés dans RabbitMQ
- [ ] Validations fonctionnent (montant négatif, etc.)
- [ ] Tests unitaires passent
- [ ] Tests d'intégration passent
