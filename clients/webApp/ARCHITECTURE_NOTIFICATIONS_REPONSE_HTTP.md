# 🔔 Architecture des Notifications via Réponse HTTP

## 🎯 Concept

Au lieu d'utiliser le polling, les notifications sont **renvoyées directement dans la réponse HTTP** de chaque opération CRUD.

---

## 🔄 Flux Complet

```
Frontend → API Gateway → Backend Service
                              ↓
                    Opération effectuée
                              ↓
                    Notification générée
                              ↓
Backend Service → API Gateway → Frontend
                              ↓
                    Réponse avec notification
                              ↓
                    Frontend affiche la notification
```

---

## 📊 Format de Réponse Backend

### Structure de Réponse Standard

```json
{
  "data": {
    "id": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com"
  },
  "notification": {
    "type": "success",
    "title": "Client créé",
    "message": "Le client \"Jean Dupont\" a été créé avec succès",
    "action": "view-client",
    "actionData": {
      "clientId": 123
    }
  }
}
```

### Exemples par Opération

#### 1. Création de Client
```json
POST /api/v1/clients

Response 201:
{
  "data": {
    "id": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com"
  },
  "notification": {
    "type": "success",
    "title": "Client créé",
    "message": "Le client \"Jean Dupont\" a été créé avec succès",
    "action": "view-client",
    "actionData": {"clientId": 123}
  }
}
```

#### 2. Modification de Client
```json
PUT /api/v1/clients/123

Response 200:
{
  "data": {
    "id": 123,
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@email.com"
  },
  "notification": {
    "type": "info",
    "title": "Client modifié",
    "message": "Les informations du client \"Jean Dupont\" ont été mises à jour",
    "action": "view-client",
    "actionData": {"clientId": 123}
  }
}
```

#### 3. Suppression de Client
```json
DELETE /api/v1/clients/123

Response 200:
{
  "notification": {
    "type": "warning",
    "title": "Client supprimé",
    "message": "Le client \"Jean Dupont\" a été supprimé"
  }
}
```

#### 4. Création de Compte avec Solde Faible
```json
POST /api/v1/accounts

Response 201:
{
  "data": {
    "id": 456,
    "type": "courant",
    "solde": 50,
    "numeroCompte": "FR7612345678901234567890123"
  },
  "notifications": [
    {
      "type": "success",
      "title": "Nouveau compte",
      "message": "Un compte courant a été créé avec un solde de 50€",
      "action": "view-account",
      "actionData": {"accountId": 456}
    },
    {
      "type": "warning",
      "title": "Solde faible",
      "message": "Le nouveau compte a un solde inférieur à 100€",
      "action": "view-account",
      "actionData": {"accountId": 456}
    }
  ]
}
```

---

## 🔧 Implémentation Backend Java

### 1. DTO de Réponse Wrapper

```java
public class ApiResponse<T> {
    private T data;
    private NotificationDTO notification;
    private List<NotificationDTO> notifications;
    
    public ApiResponse(T data) {
        this.data = data;
    }
    
    public ApiResponse(T data, NotificationDTO notification) {
        this.data = data;
        this.notification = notification;
    }
    
    public ApiResponse(T data, List<NotificationDTO> notifications) {
        this.data = data;
        this.notifications = notifications;
    }
    
    // Getters et Setters
}

public class NotificationDTO {
    private String type;
    private String title;
    private String message;
    private String action;
    private Map<String, Object> actionData;
    
    // Constructeurs, Getters et Setters
}
```

---

### 2. Service Client avec Notifications

```java
@Service
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;
    
    /**
     * Crée un client et retourne la réponse avec notification
     */
    @Transactional
    public ApiResponse<ClientDTO> createClient(ClientDTO clientDTO) {
        Client client = new Client();
        client.setNom(clientDTO.getNom());
        client.setPrenom(clientDTO.getPrenom());
        client.setEmail(clientDTO.getEmail());
        
        Client savedClient = clientRepository.save(client);
        ClientDTO responseDTO = toDTO(savedClient);
        
        // Créer la notification
        NotificationDTO notification = new NotificationDTO(
            "success",
            "Client créé",
            String.format("Le client \"%s %s\" a été créé avec succès", 
                client.getNom(), client.getPrenom()),
            "view-client",
            Map.of("clientId", savedClient.getId())
        );
        
        return new ApiResponse<>(responseDTO, notification);
    }
    
    /**
     * Met à jour un client et retourne la réponse avec notification
     */
    @Transactional
    public ApiResponse<ClientDTO> updateClient(Long id, ClientDTO clientDTO) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        
        client.setNom(clientDTO.getNom());
        client.setPrenom(clientDTO.getPrenom());
        client.setEmail(clientDTO.getEmail());
        
        Client updatedClient = clientRepository.save(client);
        ClientDTO responseDTO = toDTO(updatedClient);
        
        // Créer la notification
        NotificationDTO notification = new NotificationDTO(
            "info",
            "Client modifié",
            String.format("Les informations du client \"%s %s\" ont été mises à jour", 
                client.getNom(), client.getPrenom()),
            "view-client",
            Map.of("clientId", updatedClient.getId())
        );
        
        return new ApiResponse<>(responseDTO, notification);
    }
    
    /**
     * Supprime un client et retourne la notification
     */
    @Transactional
    public ApiResponse<Void> deleteClient(Long id) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        
        String clientName = client.getNom() + " " + client.getPrenom();
        clientRepository.delete(client);
        
        // Créer la notification
        NotificationDTO notification = new NotificationDTO(
            "warning",
            "Client supprimé",
            String.format("Le client \"%s\" a été supprimé", clientName),
            null,
            null
        );
        
        return new ApiResponse<>(null, notification);
    }
}
```

---

### 3. Controller avec ApiResponse

```java
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    
    @Autowired
    private ClientService clientService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ClientDTO>> createClient(
            @RequestBody @Valid ClientDTO clientDTO) {
        
        ApiResponse<ClientDTO> response = clientService.createClient(clientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientDTO>> updateClient(
            @PathVariable Long id,
            @RequestBody @Valid ClientDTO clientDTO) {
        
        ApiResponse<ClientDTO> response = clientService.updateClient(id, clientDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        ApiResponse<Void> response = clientService.deleteClient(id);
        return ResponseEntity.ok(response);
    }
}
```

---

### 4. Service Compte avec Notifications Multiples

```java
@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    /**
     * Crée un compte et retourne les notifications
     */
    @Transactional
    public ApiResponse<AccountDTO> createAccount(AccountDTO accountDTO) {
        Account account = new Account();
        account.setType(accountDTO.getType());
        account.setSolde(accountDTO.getSolde());
        account.setClientId(accountDTO.getClientId());
        account.setNumeroCompte(generateAccountNumber());
        
        Account savedAccount = accountRepository.save(account);
        AccountDTO responseDTO = toDTO(savedAccount);
        
        List<NotificationDTO> notifications = new ArrayList<>();
        
        // Notification principale
        notifications.add(new NotificationDTO(
            "success",
            "Nouveau compte",
            String.format("Un compte %s a été créé avec un solde de %.2f€", 
                account.getType(), account.getSolde()),
            "view-account",
            Map.of("accountId", savedAccount.getId())
        ));
        
        // Notification supplémentaire si solde faible
        if (account.getSolde() < 100) {
            notifications.add(new NotificationDTO(
                "warning",
                "Solde faible",
                "Le nouveau compte a un solde inférieur à 100€",
                "view-account",
                Map.of("accountId", savedAccount.getId())
            ));
        }
        
        return new ApiResponse<>(responseDTO, notifications);
    }
}
```

---

## 🎨 Implémentation Frontend

### 1. Intercepteur HTTP pour Notifications

Créons un intercepteur qui détecte automatiquement les notifications dans les réponses :

```typescript
// src/app/interceptors/notification-response.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NotificationService } from '../services/notification.service';

@Injectable()
export class NotificationResponseInterceptor implements HttpInterceptor {
  
  constructor(private notificationService: NotificationService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          const body = event.body;
          
          // Vérifier si la réponse contient une notification
          if (body && body.notification) {
            this.notificationService.addNotification({
              type: body.notification.type,
              title: body.notification.title,
              message: body.notification.message,
              read: false,
              action: body.notification.action,
              actionData: body.notification.actionData
            });
          }
          
          // Vérifier si la réponse contient plusieurs notifications
          if (body && body.notifications && Array.isArray(body.notifications)) {
            body.notifications.forEach((notif: any) => {
              this.notificationService.addNotification({
                type: notif.type,
                title: notif.title,
                message: notif.message,
                read: false,
                action: notif.action,
                actionData: notif.actionData
              });
            });
          }
        }
      })
    );
  }
}
```

---

### 2. Enregistrer l'Intercepteur

```typescript
// src/app/app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { MessageService } from 'primeng/api';
import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { ErrorInterceptor } from './interceptors/error.interceptor';
import { LoadingInterceptor } from './interceptors/loading.interceptor';
import { NotificationResponseInterceptor } from './interceptors/notification-response.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes), 
    provideClientHydration(),
    provideHttpClient(withFetch()),
    provideAnimations(),
    MessageService,
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: NotificationResponseInterceptor, multi: true }
  ]
};
```

---

### 3. Simplifier les Composants

Avec l'intercepteur, les composants n'ont plus besoin d'appeler manuellement `addNotification()` :

```typescript
// AVANT (avec appel manuel)
save() {
  if (this.clientForm.valid) {
    const data = this.clientForm.value;
    this.apiService.createClient(data).subscribe((client) => {
      // Appel manuel de la notification
      this.notificationService.addNotification({
        type: 'success',
        title: 'Client créé',
        message: `Le client "${data.nom} ${data.prenom}" a été créé avec succès`,
        read: false
      });
      this.router.navigate(['/clients']);
    });
  }
}

// APRÈS (automatique via intercepteur)
save() {
  if (this.clientForm.valid) {
    const data = this.clientForm.value;
    this.apiService.createClient(data).subscribe((response) => {
      // La notification est automatiquement affichée par l'intercepteur !
      this.router.navigate(['/clients']);
    });
  }
}
```

---

### 4. Adapter ApiService pour Backend

```typescript
// src/app/services/api.service.ts

// Pour le mode backend, les méthodes retournent ApiResponse
createClient(client: any): Observable<any> {
  if (MOCK_MODE) {
    const newClient = { ...client, id: Math.max(...this.mockClients.map(c => c.id)) + 1 };
    this.mockClients.push(newClient);
    
    // En mode mock, on simule la réponse avec notification
    return of({
      data: newClient,
      notification: {
        type: 'success',
        title: 'Client créé',
        message: `Le client "${client.nom} ${client.prenom}" a été créé avec succès`,
        action: 'view-client',
        actionData: { clientId: newClient.id }
      }
    }).pipe(delay(300));
  }
  
  // En mode backend, l'API retourne déjà le format ApiResponse
  return this.http.post<any>(`${this.baseUrl}/clients`, client);
}
```

---

## 🎯 Avantages de cette Architecture

### 1. Automatique et Transparent
- ✅ Les notifications sont automatiquement détectées et affichées
- ✅ Aucun code supplémentaire dans les composants
- ✅ Fonctionne pour toutes les requêtes HTTP

### 2. Cohérent
- ✅ Même comportement en mode mock et backend
- ✅ Format de réponse standardisé
- ✅ Notifications toujours présentes

### 3. Maintenable
- ✅ Logique centralisée dans l'intercepteur
- ✅ Facile à modifier ou désactiver
- ✅ Pas de duplication de code

### 4. Flexible
- ✅ Support de notifications simples ou multiples
- ✅ Peut inclure des actions et données
- ✅ Extensible pour d'autres types de messages

---

## 📊 Comparaison des Approches

| Aspect | Polling | Réponse HTTP |
|--------|---------|--------------|
| Latence | 0-30 secondes | Immédiate |
| Charge serveur | Élevée (requêtes régulières) | Faible (seulement lors d'opérations) |
| Complexité | Moyenne | Simple |
| Temps réel | Non | Oui |
| Notifications historiques | Oui | Non (sauf si stockées) |
| Multi-utilisateur | Oui | Non (sauf WebSocket) |

---

## 🔄 Flux Complet avec Intercepteur

```
1. Utilisateur clique sur "Enregistrer"
   ↓
2. Component appelle apiService.createClient()
   ↓
3. HTTP POST vers /api/v1/clients
   ↓
4. Backend traite la requête
   ↓
5. Backend génère la notification
   ↓
6. Backend retourne {data: {...}, notification: {...}}
   ↓
7. NotificationResponseInterceptor détecte la notification
   ↓
8. Intercepteur appelle notificationService.addNotification()
   ↓
9. Badge se met à jour (+1)
   ↓
10. Panneau affiche la notification
    ↓
11. Component reçoit la réponse et redirige
```

---

## ✅ Checklist d'Implémentation

### Backend
- [ ] Créer `ApiResponse<T>` wrapper
- [ ] Créer `NotificationDTO`
- [ ] Modifier tous les services pour retourner `ApiResponse`
- [ ] Modifier tous les controllers pour retourner `ApiResponse`
- [ ] Tester avec Postman

### Frontend
- [ ] Créer `NotificationResponseInterceptor`
- [ ] Enregistrer l'intercepteur dans `app.config.ts`
- [ ] Adapter `ApiService` pour le format `ApiResponse`
- [ ] Supprimer les appels manuels à `addNotification()` dans les composants
- [ ] Tester l'intégration complète

---

## 🎉 Résultat Final

Avec cette architecture :

1. ✅ **Notifications immédiates** - Affichées dès la réponse HTTP
2. ✅ **Automatique** - Aucun code supplémentaire dans les composants
3. ✅ **Transparent** - Fonctionne pour toutes les requêtes
4. ✅ **Cohérent** - Même comportement mock et backend
5. ✅ **Simple** - Logique centralisée dans l'intercepteur
6. ✅ **Performant** - Pas de polling inutile

---

*Architecture créée le 8 Décembre 2025*
