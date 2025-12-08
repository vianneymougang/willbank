# Guide de Déploiement - Microservice Notifications

## Déploiement Local

### Avec Docker Compose (Recommandé)

```bash
# 1. Cloner le projet
cd notification-service

# 2. Créer le fichier .env
cp .env.example .env

# 3. Éditer .env avec vos configurations
nano .env

# 4. Placer le fichier Firebase credentials
cp /path/to/firebase-service-account.json src/main/resources/

# 5. Démarrer tous les services
docker-compose up -d

# 6. Vérifier les logs
docker-compose logs -f notification-service

# 7. Tester l'API
curl http://localhost:8082/api/notifications/history/stats
```

### Sans Docker

```bash
# 1. Démarrer PostgreSQL
sudo service postgresql start

# 2. Créer la base de données
psql -U postgres -c "CREATE DATABASE notifications_db;"

# 3. Démarrer RabbitMQ
rabbitmq-server -detached

# 4. Compiler et démarrer le service
cd notification-service
mvn clean install
mvn spring-boot:run
```

## Déploiement sur Serveur Linux

### Prérequis

```bash
# Installer Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Installer PostgreSQL
sudo apt install postgresql postgresql-contrib

# Installer RabbitMQ
sudo apt install rabbitmq-server
sudo systemctl enable rabbitmq-server
sudo systemctl start rabbitmq-server
```

### Configuration

```bash
# 1. Créer un utilisateur système
sudo useradd -r -s /bin/false notification-service

# 2. Créer les répertoires
sudo mkdir -p /opt/notification-service
sudo mkdir -p /var/log/notification-service
sudo mkdir -p /etc/notification-service

# 3. Copier le JAR
sudo cp target/notification-service-1.0.0.jar /opt/notification-service/

# 4. Copier la configuration
sudo cp src/main/resources/application.yml /etc/notification-service/
sudo cp firebase-service-account.json /etc/notification-service/

# 5. Définir les permissions
sudo chown -R notification-service:notification-service /opt/notification-service
sudo chown -R notification-service:notification-service /var/log/notification-service
sudo chown -R notification-service:notification-service /etc/notification-service
```

### Service Systemd

Créer `/etc/systemd/system/notification-service.service`:

```ini
[Unit]
Description=Notification Service
After=syslog.target network.target postgresql.service rabbitmq-server.service

[Service]
User=notification-service
Group=notification-service
Type=simple
WorkingDirectory=/opt/notification-service
ExecStart=/usr/bin/java -jar /opt/notification-service/notification-service-1.0.0.jar \
  --spring.config.location=/etc/notification-service/application.yml
SuccessExitStatus=143
StandardOutput=append:/var/log/notification-service/application.log
StandardError=append:/var/log/notification-service/error.log
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Activer et démarrer:

```bash
sudo systemctl daemon-reload
sudo systemctl enable notification-service
sudo systemctl start notification-service
sudo systemctl status notification-service
```

## Déploiement avec Kubernetes

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: notification-service-config
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:postgresql://postgres-service:5432/notifications_db
      rabbitmq:
        host: rabbitmq-service
      mail:
        host: smtp.gmail.com
        port: 587
```

### Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: notification-service-secret
type: Opaque
stringData:
  db-password: your-db-password
  mail-username: student@example.com
  mail-password: your-app-password
  firebase-credentials: |
    {
      "type": "service_account",
      ...
    }
```

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: notification-service
  template:
    metadata:
      labels:
        app: notification-service
    spec:
      containers:
      - name: notification-service
        image: notification-service:1.0.0
        ports:
        - containerPort: 8082
        env:
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: notification-service-secret
              key: db-password
        - name: MAIL_USERNAME
          valueFrom:
            secretKeyRef:
              name: notification-service-secret
              key: mail-username
        - name: MAIL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: notification-service-secret
              key: mail-password
        volumeMounts:
        - name: config
          mountPath: /etc/notification-service
        - name: firebase-credentials
          mountPath: /etc/firebase
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8082
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 5
      volumes:
      - name: config
        configMap:
          name: notification-service-config
      - name: firebase-credentials
        secret:
          secretName: notification-service-secret
          items:
          - key: firebase-credentials
            path: firebase-service-account.json
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: notification-service
spec:
  selector:
    app: notification-service
  ports:
  - protocol: TCP
    port: 8082
    targetPort: 8082
  type: ClusterIP
```

## Déploiement sur Cloud

### AWS (Elastic Beanstalk)

```bash
# 1. Installer EB CLI
pip install awsebcli

# 2. Initialiser
eb init -p docker notification-service

# 3. Créer l'environnement
eb create notification-service-prod

# 4. Déployer
eb deploy

# 5. Ouvrir l'application
eb open
```

### Google Cloud Platform (Cloud Run)

```bash
# 1. Build l'image
gcloud builds submit --tag gcr.io/PROJECT_ID/notification-service

# 2. Déployer
gcloud run deploy notification-service \
  --image gcr.io/PROJECT_ID/notification-service \
  --platform managed \
  --region europe-west1 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod"
```

### Azure (Container Instances)

```bash
# 1. Créer un registre de conteneurs
az acr create --resource-group myResourceGroup \
  --name notificationServiceRegistry --sku Basic

# 2. Build et push l'image
az acr build --registry notificationServiceRegistry \
  --image notification-service:1.0.0 .

# 3. Déployer
az container create --resource-group myResourceGroup \
  --name notification-service \
  --image notificationServiceRegistry.azurecr.io/notification-service:1.0.0 \
  --dns-name-label notification-service \
  --ports 8082
```

## Configuration de Production

### application-prod.yml

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}

logging:
  level:
    root: INFO
    com.example.notificationservice: INFO
  file:
    name: /var/log/notification-service/application.log
    max-size: 10MB
    max-history: 30
```

## Monitoring

### Prometheus

Ajouter dans `pom.xml`:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Endpoint: `http://localhost:8082/actuator/prometheus`

### Grafana Dashboard

Importer le dashboard ID: 4701 (JVM Micrometer)

## Backup et Restauration

### Base de données

```bash
# Backup
pg_dump -U postgres notifications_db > backup.sql

# Restauration
psql -U postgres notifications_db < backup.sql
```

### RabbitMQ

```bash
# Export des définitions
rabbitmqadmin export definitions.json

# Import
rabbitmqadmin import definitions.json
```

## Sécurité

### SSL/TLS

Configurer dans `application.yml`:

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### Authentification API Gateway

Ajouter un filtre Spring Security pour valider les tokens JWT de l'API Gateway.

## Rollback

```bash
# Docker Compose
docker-compose down
docker-compose up -d --force-recreate

# Kubernetes
kubectl rollout undo deployment/notification-service

# Systemd
sudo systemctl stop notification-service
sudo cp /backup/notification-service-old.jar /opt/notification-service/notification-service-1.0.0.jar
sudo systemctl start notification-service
```
