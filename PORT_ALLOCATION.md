# WillBank - Port Allocation Reference

Ce document liste tous les ports utilisés dans l'architecture WillBank pour éviter les conflits.

## 📊 Résumé des Ports

| Service | Port | Type | Description | Accessible via |
|---------|------|------|-------------|----------------|
| **API Gateway** | 8080 | Public | Point d'entrée unique | http://localhost:8080 |
| **Config Server** | 8888 | Public | Configuration centralisée | http://localhost:8888 |
| **Discovery Server (Eureka)** | 8761 | Public | Service discovery UI | http://localhost:8761 |
| **User Service** | 8081 | Interne | Gestion users/customers | Via API Gateway uniquement |
| **Account Service** | 8082 | Interne | Gestion comptes bancaires | Via API Gateway uniquement |
| **Auth Service** | 8083 | Interne | Authentification JWT | Via API Gateway uniquement |
| **Notification Service** | 8084 | Interne | Notifications email/push | Via API Gateway uniquement |
| **Transaction Service** | **8085** | **Interne** | **Transactions bancaires** | **Via API Gateway uniquement** |
| **Composite Service** | **8086** | **Interne** | **Service composite** (à implémenter) | **Via API Gateway uniquement** |
| **Mongo Express** | 8087 | Public | MongoDB UI | http://localhost:8087 |
| **RabbitMQ Management** | 15672 | Public | RabbitMQ UI | http://localhost:15672 |
| **RabbitMQ AMQP** | 5672 | Interne | Message broker | N/A (clients AMQP) |
| **MongoDB** | 27017 | Interne | Base NoSQL | N/A (clients MongoDB) |
| **MailDev Web UI** | 1080 | Public | Email testing UI | http://localhost:1080 |
| **MailDev SMTP** | 1025 | Interne | SMTP server | N/A (SMTP clients) |
| **MailDev SMTP+TLS** | 1587 | Interne | SMTP with TLS | N/A (SMTP clients) |

## 🗃️ Bases de Données PostgreSQL

| Base de données | Port | Container Host | Description |
|----------------|------|----------------|-------------|
| `willbank_accounts` | 5432 | postgres-accounts | Comptes bancaires |
| `user_service_db` | 5433 | postgres-users | Utilisateurs/customers |
| `willbank_auth` | 5434 | postgres-auth | Authentification |
| `willbank_transactions` | **5432** | **postgres** (partagé) | **Transactions** |

> **Note**: Le microservice transaction-service utilisera la même instance PostgreSQL que account-service (port 5432) mais avec une base de données séparée `willbank_transactions`.

## 🔒 Politique de Sécurité des Ports

### Ports Publics (exposés sur l'hôte)
Ces ports sont accessibles depuis `localhost`:
- **8080** - API Gateway (production ready)
- **8761** - Eureka UI (monitoring)
- **8888** - Config Server (administration)
- **8087** - Mongo Express (développement)
- **15672** - RabbitMQ Management (monitoring)
- **1080** - MailDev UI (développement)
- **5432-5434** - PostgreSQL (développement uniquement, bloquer en production!)

### Ports Internes (non exposés)
Ces ports sont uniquement accessibles sur le réseau Docker interne:
- **8081-8086** - Microservices métier (via API Gateway uniquement)
- **5672** - RabbitMQ AMQP
- **27017** - MongoDB
- **1025, 1587** - MailDev SMTP

## 🚀 Ajout d'un Nouveau Service

Lors de l'ajout d'un nouveau microservice :

1. **Choisir un port disponible** : Consulter ce document pour éviter les conflits
2. **Plage recommandée** : 8087-8099 pour les microservices
3. **Exposer le port** : Utiliser `expose:` dans docker-compose, PAS `ports:`
4. **Mettre à jour ce document** : Ajouter le service dans les tableaux ci-dessus
5. **Configurer API Gateway** : Ajouter une route dans `api-gateway.yml`
6. **Config Server** : Créer `{service-name}.yml` et `{service-name}-docker.yml`

## 📝 Ports Réservés pour Futur Développement

| Port | Service Prévu | Statut | Notes |
|------|---------------|--------|-------|
| 8086 | Composite Service | À implémenter | Service d'agrégation |
| 8088 | Analytics Service | Planifié | Analyse/reporting |
| 8089 | Payment Gateway | Planifié | Intégration paiements externes |
| 8090 | Fraud Detection | Planifié | Détection fraudes |

## ⚠️ Éviter Ces Ports

Ports généralement réservés ou à éviter :
- **1-1023** : Ports système (nécessitent root)
- **3000** : Node.js par défaut
- **3306** : MySQL par défaut
- **5000** : Flask par défaut
- **5432** : PostgreSQL (déjà utilisé)
- **8000** : Django par défaut
- **9090** : Prometheus par défaut

## 🔄 Historique des Changements

| Date | Changement | Raison |
|------|-----------|--------|
| 2025-12-08 | Mongo Express: 8085 → 8087 | Libérer 8085 pour transaction-service |
| 2025-12-08 | Ajout transaction-service sur 8085 | Nouveau microservice |
| 2025-12-08 | Réservation port 8086 | Pour composite-service futur |

---

**Dernière mise à jour** : 2025-12-08  
**Maintenu par** : Équipe Infrastructure WillBank
