# Guide d'utilisation du Service Client - WillBank

## 📋 Description du Micro-Service

Le **Service Client** est un micro-service Spring Boot qui gère les informations des clients d'une banque. Il suit une architecture **MVC (Model-View-Controller)** et est connecté à une base de données **PostgreSQL**.

### Architecture MVC

- **Model** : `Client.java` - Entité JPA représentant un client bancaire
- **View** : Les réponses JSON via les endpoints REST
- **Controller** : `ClientController.java` - Gère les requêtes HTTP et les réponses

### Fonctionnalités principales

1. **Gestion complète des clients** (CRUD)
2. **Recherche et filtrage** par différents critères
3. **Validation des données** avec Jakarta Validation
4. **Génération automatique** de numéros de compte uniques
5. **Gestion des statuts** (ACTIF, INACTIF, SUSPENDU)
6. **Intégration Eureka** pour la découverte de services

### Structure des données

Un client contient :
- Informations personnelles (prénom, nom, email, téléphone, date de naissance)
- Adresse complète (adresse, ville, code postal, pays)
- Numéro de compte bancaire (généré automatiquement)
- Statut du compte
- Dates de création et modification

---

## 🚀 Démarrage du Service

### Prérequis

1. **PostgreSQL** installé et démarré
2. Base de données `willbank` créée
3. Java 17 installé
4. Maven installé

### Configuration

Le service est configuré pour :
- **Port** : `8081`
- **Base de données** : `willbank` sur `localhost:5432`
- **Eureka** : `http://localhost:8761/eureka/`

### Initialisation automatique

Au démarrage, le service initialise automatiquement **15 clients camerounais de test** si la base de données est vide. Tous les clients ont l'indicatif téléphonique +237.

---

## 📡 Endpoints API REST

Base URL : `http://localhost:8081/api/clients`

### 1. **GET** `/api/clients`
Récupère la liste de tous les clients.

**Réponse** : Liste de tous les clients au format JSON

---

### 2. **GET** `/api/clients/{id}`
Récupère un client par son ID.

**Exemple** : `GET http://localhost:8081/api/clients/1`

**Réponse** : Client au format JSON

---

### 3. **GET** `/api/clients/email/{email}`
Récupère un client par son email.

**Exemple** : `GET http://localhost:8081/api/clients/email/jean.dupont@email.com`

---

### 4. **GET** `/api/clients/compte/{numeroCompte}`
Récupère un client par son numéro de compte.

**Exemple** : `GET http://localhost:8081/api/clients/compte/WBJD123456`

---

### 5. **POST** `/api/clients`
Crée un nouveau client.

**Body (JSON)** :
```json
{
  "prenom": "Jean",
  "nom": "Nkeng",
  "email": "jean.nkeng@email.com",
  "telephone": "+237612345678",
  "dateNaissance": "1985-03-15",
  "adresse": "123 Avenue Kennedy",
  "ville": "Yaoundé",
  "codePostal": "BP 1234",
  "pays": "Cameroun"
}
```

**Réponse** : Client créé avec ID et numéro de compte généré

---

### 6. **PUT** `/api/clients/{id}`
Met à jour un client existant.

**Exemple** : `PUT http://localhost:8081/api/clients/1`

**Body (JSON)** :
```json
{
  "id": 1,
  "prenom": "Jean",
  "nom": "Dupont",
  "email": "jean.dupont@email.com",
  "telephone": "0612345678",
  "dateNaissance": "1985-03-15",
  "adresse": "123 Rue de la République",
  "ville": "Paris",
  "codePostal": "75001",
  "pays": "France",
  "statut": "ACTIF"
}
```

---

### 7. **DELETE** `/api/clients/{id}`
Supprime un client.

**Exemple** : `DELETE http://localhost:8081/api/clients/1`

**Réponse** : 204 No Content

---

### 8. **GET** `/api/clients/statut/{statut}`
Récupère tous les clients avec un statut spécifique.

**Statuts possibles** : `ACTIF`, `INACTIF`, `SUSPENDU`

**Exemple** : `GET http://localhost:8081/api/clients/statut/ACTIF`

---

### 9. **GET** `/api/clients/ville/{ville}`
Récupère tous les clients d'une ville.

**Exemple** : `GET http://localhost:8081/api/clients/ville/Paris`

---

### 10. **GET** `/api/clients/search?term={terme}`
Recherche des clients par nom, prénom ou email.

**Exemple** : `GET http://localhost:8081/api/clients/search?term=Jean`

---

### 11. **PATCH** `/api/clients/{id}/statut`
Met à jour uniquement le statut d'un client.

**Exemple** : `PATCH http://localhost:8081/api/clients/1/statut?statut=INACTIF`

---

## 🧪 Tests avec Postman

### Configuration Postman

1. Créer une nouvelle collection "WillBank - Service Client"
2. Base URL : `http://localhost:8081`

### Exemples de requêtes

#### 1. Récupérer tous les clients
- **Méthode** : GET
- **URL** : `http://localhost:8081/api/clients`
- **Headers** : Aucun

#### 2. Créer un nouveau client
- **Méthode** : POST
- **URL** : `http://localhost:8081/api/clients`
- **Headers** :
  - `Content-Type: application/json`
- **Body** (raw JSON) :
```json
{
  "prenom": "Test",
  "nom": "Utilisateur",
  "email": "test.utilisateur@email.com",
  "telephone": "+237611111111",
  "dateNaissance": "1990-01-01",
  "adresse": "1 Avenue Test",
  "ville": "Yaoundé",
  "codePostal": "BP 0001",
  "pays": "Cameroun"
}
```

#### 3. Récupérer un client par ID
- **Méthode** : GET
- **URL** : `http://localhost:8081/api/clients/1`

#### 4. Mettre à jour un client
- **Méthode** : PUT
- **URL** : `http://localhost:8081/api/clients/1`
- **Headers** :
  - `Content-Type: application/json`
- **Body** (raw JSON) :
```json
{
  "id": 1,
  "prenom": "Jean",
  "nom": "Dupont Modifié",
  "email": "jean.dupont@email.com",
  "telephone": "0612345678",
  "dateNaissance": "1985-03-15",
  "adresse": "123 Rue de la République",
  "ville": "Paris",
  "codePostal": "75001",
  "pays": "France",
  "statut": "ACTIF"
}
```

#### 5. Rechercher des clients
- **Méthode** : GET
- **URL** : `http://localhost:8081/api/clients/search?term=Marie`

#### 6. Filtrer par statut
- **Méthode** : GET
- **URL** : `http://localhost:8081/api/clients/statut/ACTIF`

#### 7. Filtrer par ville
- **Méthode** : GET
- **URL** : `http://localhost:8081/api/clients/ville/Paris`

#### 8. Changer le statut d'un client
- **Méthode** : PATCH
- **URL** : `http://localhost:8081/api/clients/1/statut?statut=INACTIF`

#### 9. Supprimer un client
- **Méthode** : DELETE
- **URL** : `http://localhost:8081/api/clients/1`

---

## 📊 Codes de réponse HTTP

- **200 OK** : Requête réussie
- **201 Created** : Ressource créée avec succès
- **204 No Content** : Suppression réussie
- **400 Bad Request** : Données invalides
- **404 Not Found** : Ressource non trouvée
- **500 Internal Server Error** : Erreur serveur

---

## 🔍 Validation des données

Le service valide automatiquement :
- Email valide et unique
- Champs obligatoires (prénom, nom, email, téléphone, etc.)
- Date de naissance dans le passé
- Format des données

---

## 🗄️ Base de données

### Table `clients`

| Colonne | Type | Description |
|---------|------|-------------|
| id | BIGINT | Identifiant unique (auto-généré) |
| prenom | VARCHAR(255) | Prénom du client |
| nom | VARCHAR(255) | Nom du client |
| email | VARCHAR(255) | Email (unique) |
| telephone | VARCHAR(255) | Numéro de téléphone |
| date_naissance | DATE | Date de naissance |
| adresse | VARCHAR(255) | Adresse |
| ville | VARCHAR(255) | Ville |
| code_postal | VARCHAR(255) | Code postal |
| pays | VARCHAR(255) | Pays |
| numero_compte | VARCHAR(255) | Numéro de compte (unique) |
| statut | VARCHAR(255) | Statut (ACTIF, INACTIF, SUSPENDU) |
| date_creation | TIMESTAMP | Date de création |
| date_modification | TIMESTAMP | Date de modification |

---

## 🎯 Données de test initialisées

Le service initialise automatiquement **15 clients camerounais de test** au démarrage :
- Jean Nkeng (Yaoundé) - +237612345678
- Marie Fotso (Douala) - +237623456789
- Pierre Mvondo (Bafoussam) - +237634567890
- Sophie Tchoumi (Garoua) - +237645678901
- Luc Ndjock (Buea) - +237656789012
- Emma Tchinda (Limbé) - +237667890123
- Thomas Nguimfack (Bamenda) - +237678901234
- Julie Mefire (Kribi) - +237689012345
- Antoine Ntamack (Ebolowa) - +237690123456
- Camille Nkono (Maroua) - +237601234567
- Nicolas Tchakounte (Dschang) - +237613456789
- Laura Ngo (Foumban) - +237624567890
- Alexandre Mvogo (Bertoua) - +237635678901
- Clara Nguema (Nkongsamba) - +237646789012
- Maxime Tchouangue (Edéa) - +237657890123

---

## 🔐 Sécurité

- Les endpoints API sont accessibles sans authentification (configuration de développement)
- En production, ajouter une authentification appropriée

---

## 📝 Notes importantes

1. Le numéro de compte est généré automatiquement lors de la création
2. Les dates de création et modification sont gérées automatiquement
3. L'email doit être unique dans la base de données
4. Le statut par défaut est `ACTIF` lors de la création

