# 🔔 Guide des Notifications en Mode Backend

## 🎯 Architecture des Notifications

### Mode Mock vs Mode Backend

| Aspect | Mode Mock | Mode Backend |
|--------|-----------|--------------|
| Génération | Frontend (NotificationService.addNotification()) | Backend Java |
| Stockage | Mémoire frontend | Base de données |
| Récupération | Immédiate | Polling (30s) ou WebSocket |
| Persistance | Non (perdue au refresh) | Oui (persistante) |
| Multi-utilisateur | Non | Oui |

---

## 🔄 Flux en Mode Backend

### 1. Opération CRUD Effectuée

```
Utilisateur → Frontend → API Gateway → Backend Java
```

### 2. Backend Génère la Notification

```java
// Dans le service Java après une opération réussie
notificationService.createNotification(
    userId,
    NotificationType.SUCCESS,
    "Client créé",
    "Le client Jean Dupont a été créé avec succès",
    "view-client",
    Map.of("clientId", client.getId())
);
```

### 3. Frontend Récupère les Notifications

```
Polling (30s) → GET /api/v1/notifications → Frontend affiche
```

---

## 📊 Implémentation Backend Java

### 1. Entité Notification

```java
@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId; // ID de l'utilisateur destinataire
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // SUCCESS, INFO, WARNING, ERROR
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(nullable = false)
    private Boolean read = false;
    
    private String action; // view-client, view-account, etc.
    
    @Column(columnDefinition = "TEXT")
    private String actionData; // JSON: {"clientId": 123}
    
    // Getters et Setters
}

public enum NotificationType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}
```

---

### 2. Repository

```java
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByDateDesc(Long userId);
    
    List<Notification> findByUserIdAndReadFalseOrderByDateDesc(Long userId);
    
    Long countByUserIdAndReadFalse(Long userId);
    
    void deleteByUserIdAndReadTrue(Long userId);
}
```

---

### 3. Service de Notifications

```java
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Crée une nouvelle notification
     */
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String action,
            Map<String, Object> actionData) {
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDate(LocalDateTime.now());
        notification.setRead(false);
        notification.setAction(action);
        
        if (actionData != null) {
            try {
                notification.setActionData(objectMapper.writeValueAsString(actionData));
            } catch (JsonProcessingException e) {
                // Log error
            }
        }
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByDateDesc(userId);
    }
    
    /**
     * Récupère les notifications non lues
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByDateDesc(userId);
    }
    
    /**
     * Marque une notification comme lue
     */
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId)
            .filter(n -> n.getUserId().equals(userId))
            .ifPresent(notification -> {
                notification.setRead(true);
                notificationRepository.save(notification);
            });
    }
    
    /**
     * Marque toutes les notifications comme lues
     */
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository
            .findByUserIdAndReadFalseOrderByDateDesc(userId);
        
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
    
    /**
     * Supprime une notification
     */
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId)
            .filter(n -> n.getUserId().equals(userId))
            .ifPresent(notificationRepository::delete);
    }
    
    /**
     * Supprime toutes les notifications lues
     */
    @Transactional
    public void deleteAllRead(Long userId) {
        notificationRepository.deleteByUserIdAndReadTrue(userId);
    }
    
    /**
     * Compte les notifications non lues
     */
    public Long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}
```

---

### 4. Controller REST

```java
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * GET /api/v1/notifications
     * Récupère toutes les notifications de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        
        return ResponseEntity.ok(
            notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList())
        );
    }
    
    /**
     * GET /api/v1/notifications/unread
     * Récupère les notifications non lues
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        
        return ResponseEntity.ok(
            notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList())
        );
    }
    
    /**
     * PUT /api/v1/notifications/{id}/read
     * Marque une notification comme lue
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.markAsRead(id, userId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/v1/notifications/read-all
     * Marque toutes les notifications comme lues
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * DELETE /api/v1/notifications/{id}
     * Supprime une notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.deleteNotification(id, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DELETE /api/v1/notifications/read
     * Supprime toutes les notifications lues
     */
    @DeleteMapping("/read")
    public ResponseEntity<Void> deleteAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.deleteAllRead(userId);
        
        return ResponseEntity.noContent().build();
    }
    
    private Long getUserId(UserDetails userDetails) {
        // Récupérer l'ID de l'utilisateur depuis UserDetails
        // Implémentation dépend de votre système d'authentification
        return 1L; // Exemple
    }
    
    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name().toLowerCase());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setDate(notification.getDate().toString());
        dto.setRead(notification.getRead());
        dto.setAction(notification.getAction());
        dto.setActionData(notification.getActionData());
        return dto;
    }
}
```

---

### 5. DTO

```java
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private String date;
    private Boolean read;
    private String action;
    private String actionData;
    
    // Getters et Setters
}
```

---

## 🎯 Génération de Notifications par Opération

### 1. Service Client

```java
@Service
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Crée un client et génère une notification
     */
    @Transactional
    public Client createClient(ClientDTO clientDTO, Long userId) {
        Client client = new Client();
        // Mapper les données
        client.setNom(clientDTO.getNom());
        client.setPrenom(clientDTO.getPrenom());
        client.setEmail(clientDTO.getEmail());
        
        Client savedClient = clientRepository.save(client);
        
        // Générer la notification
        notificationService.createNotification(
            userId,
            NotificationType.SUCCESS,
            "Client créé",
            String.format("Le client \"%s %s\" a été créé avec succès", 
                client.getNom(), client.getPrenom()),
            "view-client",
            Map.of("clientId", savedClient.getId())
        );
        
        return savedClient;
    }
    
    /**
     * Met à jour un client et génère une notification
     */
    @Transactional
    public Client updateClient(Long id, ClientDTO clientDTO, Long userId) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        
        // Mettre à jour les données
        client.setNom(clientDTO.getNom());
        client.setPrenom(clientDTO.getPrenom());
        client.setEmail(clientDTO.getEmail());
        
        Client updatedClient = clientRepository.save(client);
        
        // Générer la notification
        notificationService.createNotification(
            userId,
            NotificationType.INFO,
            "Client modifié",
            String.format("Les informations du client \"%s %s\" ont été mises à jour", 
                client.getNom(), client.getPrenom()),
            "view-client",
            Map.of("clientId", updatedClient.getId())
        );
        
        return updatedClient;
    }
    
    /**
     * Supprime un client et génère une notification
     */
    @Transactional
    public void deleteClient(Long id, Long userId) {
        Client client = clientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        
        String clientName = client.getNom() + " " + client.getPrenom();
        
        clientRepository.delete(client);
        
        // Générer la notification
        notificationService.createNotification(
            userId,
            NotificationType.WARNING,
            "Client supprimé",
            String.format("Le client \"%s\" a été supprimé", clientName),
            null,
            null
        );
    }
}
```

---

### 2. Service Compte

```java
@Service
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Crée un compte et génère des notifications
     */
    @Transactional
    public Account createAccount(AccountDTO accountDTO, Long userId) {
        Account account = new Account();
        account.setType(accountDTO.getType());
        account.setSolde(accountDTO.getSolde());
        account.setClientId(accountDTO.getClientId());
        account.setNumeroCompte(generateAccountNumber());
        account.setDateCreation(LocalDate.now());
        
        Account savedAccount = accountRepository.save(account);
        
        // Notification principale
        notificationService.createNotification(
            userId,
            NotificationType.SUCCESS,
            "Nouveau compte",
            String.format("Un compte %s a été créé avec un solde de %.2f€", 
                account.getType(), account.getSolde()),
            "view-account",
            Map.of("accountId", savedAccount.getId())
        );
        
        // Notification supplémentaire si solde faible
        if (account.getSolde() < 100) {
            notificationService.createNotification(
                userId,
                NotificationType.WARNING,
                "Solde faible",
                "Le nouveau compte a un solde inférieur à 100€",
                "view-account",
                Map.of("accountId", savedAccount.getId())
            );
        }
        
        return savedAccount;
    }
    
    /**
     * Met à jour un compte et génère une notification
     */
    @Transactional
    public Account updateAccount(Long id, AccountDTO accountDTO, Long userId) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        account.setType(accountDTO.getType());
        account.setSolde(accountDTO.getSolde());
        
        Account updatedAccount = accountRepository.save(account);
        
        // Générer la notification
        notificationService.createNotification(
            userId,
            NotificationType.INFO,
            "Compte modifié",
            String.format("Le compte %s a été mis à jour", account.getType()),
            "view-account",
            Map.of("accountId", updatedAccount.getId())
        );
        
        return updatedAccount;
    }
    
    /**
     * Supprime un compte et génère une notification
     */
    @Transactional
    public void deleteAccount(Long id, Long userId) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        
        String accountInfo = account.getType() + " (" + account.getNumeroCompte() + ")";
        
        accountRepository.delete(account);
        
        // Générer la notification
        notificationService.createNotification(
            userId,
            NotificationType.WARNING,
            "Compte supprimé",
            String.format("Le compte %s a été supprimé", accountInfo),
            null,
            null
        );
    }
    
    private String generateAccountNumber() {
        // Logique de génération de numéro de compte
        return "FR76" + System.currentTimeMillis();
    }
}
```

---

### 3. Service Transaction

```java
@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Crée une transaction et génère des notifications
     */
    @Transactional
    public Transaction createTransaction(TransactionDTO transactionDTO, Long userId) {
        Transaction transaction = new Transaction();
        transaction.setType(transactionDTO.getType());
        transaction.setMontant(transactionDTO.getMontant());
        transaction.setTelephoneSource(transactionDTO.getTelephoneSource());
        transaction.setTelephoneDest(transactionDTO.getTelephoneDest());
        transaction.setDate(LocalDate.now());
        transaction.setStatut("completed");
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Notification principale
        notificationService.createNotification(
            userId,
            NotificationType.SUCCESS,
            "Transaction effectuée",
            String.format("%s de %.2f€ effectué avec succès", 
                capitalizeFirst(transaction.getType()), transaction.getMontant()),
            "view-transaction",
            Map.of("transactionId", savedTransaction.getId())
        );
        
        // Notification supplémentaire si montant important
        if (transaction.getMontant() > 1000) {
            notificationService.createNotification(
                userId,
                NotificationType.INFO,
                "Transaction importante",
                String.format("Une transaction de %.2f€ a été effectuée", 
                    transaction.getMontant()),
                "view-transaction",
                Map.of("transactionId", savedTransaction.getId())
            );
        }
        
        return savedTransaction;
    }
    
    /**
     * Met à jour une transaction et génère une notification
     */
    @Transactional
    public Transaction updateTransaction(Long id, TransactionDTO transactionDTO, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        transaction.setType(transactionDTO.getType());
        transaction.setMontant(transactionDTO.getMontant());
        
        Transaction updatedTransaction = transactionRepository.save(transaction);
        
        // Générer la notification
        notificationService.createNotification(
            userId,
            NotificationType.INFO,
            "Transaction modifiée",
            String.format("La transaction de %.2f€ a été mise à jour", 
                transaction.getMontant()),
            "view-transaction",
            Map.of("transactionId", updatedTransaction.getId())
        );
        
        return updatedTransaction;
    }
    
    /**
     * Supprime une transaction et génère une notification
     */
    @Transactional
    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        double montant = transaction.getMontant();
        
        transactionRepository.delete(transaction);
        
        // Générer la notification
        notificationService.createNotification(
            userId,
            NotificationType.ERROR,
            "Transaction supprimée",
            String.format("La transaction de %.2f€ a été supprimée", montant),
            null,
            null
        );
    }
    
    private String capitalizeFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
```

---

## 🔄 Frontend : Désactiver le Mode Mock

Pour passer en mode backend, il suffit de changer une ligne :

```typescript
// src/app/services/notification.service.ts
const MOCK_MODE = false; // Changer de true à false
```

Le frontend continuera de fonctionner exactement de la même manière, mais récupérera les notifications depuis le backend via le polling.

---

## 📊 Tableau Récapitulatif

| Opération | Notification Backend | Type | Règle Spéciale |
|-----------|---------------------|------|----------------|
| Client créé | ✅ Oui | SUCCESS | - |
| Client modifié | ✅ Oui | INFO | - |
| Client supprimé | ✅ Oui | WARNING | - |
| Compte créé | ✅ Oui | SUCCESS | Si solde < 100€ → WARNING |
| Compte modifié | ✅ Oui | INFO | - |
| Compte supprimé | ✅ Oui | WARNING | - |
| Transaction créée | ✅ Oui | SUCCESS | Si montant > 1000€ → INFO |
| Transaction modifiée | ✅ Oui | INFO | - |
| Transaction supprimée | ✅ Oui | ERROR | - |

---

## 🚀 Avantages du Mode Backend

1. **Persistance** - Les notifications survivent au refresh de la page
2. **Multi-utilisateur** - Chaque utilisateur a ses propres notifications
3. **Historique** - Toutes les notifications sont conservées en base
4. **Sécurité** - Les notifications sont liées à l'utilisateur authentifié
5. **Scalabilité** - Peut gérer des milliers d'utilisateurs
6. **Audit** - Traçabilité complète des opérations

---

## 🔐 Sécurité

### Vérifications Importantes

1. **Authentification** - Vérifier que l'utilisateur est connecté
2. **Autorisation** - Un utilisateur ne peut voir que ses notifications
3. **Validation** - Valider les données avant création
4. **Injection SQL** - Utiliser des requêtes préparées (JPA le fait)

```java
// Toujours vérifier que la notification appartient à l'utilisateur
notificationRepository.findById(notificationId)
    .filter(n -> n.getUserId().equals(userId)) // ← Important !
    .ifPresent(notification -> {
        // Traitement
    });
```

---

## 📝 Script SQL de Création

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    date TIMESTAMP NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    action VARCHAR(100),
    action_data TEXT,
    
    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, read),
    INDEX idx_date (date)
);
```

---

## ✅ Checklist d'Implémentation Backend

- [ ] Créer la table `notifications` en base de données
- [ ] Créer l'entité `Notification` et l'enum `NotificationType`
- [ ] Créer le `NotificationRepository`
- [ ] Créer le `NotificationService`
- [ ] Créer le `NotificationController` avec tous les endpoints
- [ ] Intégrer la génération de notifications dans `ClientService`
- [ ] Intégrer la génération de notifications dans `AccountService`
- [ ] Intégrer la génération de notifications dans `TransactionService`
- [ ] Tester tous les endpoints avec Postman
- [ ] Désactiver le mode mock dans le frontend
- [ ] Tester l'intégration complète

---

## 🎉 Résultat Final

Une fois le backend implémenté :

1. ✅ Toutes les opérations CRUD génèrent des notifications côté serveur
2. ✅ Les notifications sont persistantes en base de données
3. ✅ Chaque utilisateur a ses propres notifications
4. ✅ Le frontend récupère automatiquement les notifications via polling
5. ✅ Le système fonctionne de manière identique au mode mock
6. ✅ Les notifications survivent au refresh de la page
7. ✅ Multi-utilisateur supporté

---

*Guide créé le 8 Décembre 2025*
