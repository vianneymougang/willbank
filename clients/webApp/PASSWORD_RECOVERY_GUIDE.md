# 🔐 Guide de Récupération de Mot de Passe - WillBank CRM

## ✅ Fonctionnalité implémentée

La récupération de mot de passe par email est maintenant intégrée à l'application.

## 🎯 Flux utilisateur

### 1. Page de connexion
- Lien "Mot de passe oublié ?" sous le formulaire
- Redirige vers `/forgot-password`

### 2. Page "Mot de passe oublié"
**Route** : `/forgot-password`

**Fonctionnalités** :
- Formulaire avec champ email
- Validation de l'email
- Bouton "Envoyer le lien"
- Loading state pendant l'envoi
- Messages d'erreur si problème

**Après soumission** :
- ✅ Message de confirmation
- ✅ Email envoyé (simulé en mode mock)
- ✅ Bouton "Renvoyer" si email non reçu
- ✅ Lien retour vers login

### 3. Email de réinitialisation
**Contenu attendu** :
```
Bonjour,

Vous avez demandé la réinitialisation de votre mot de passe.

Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :
https://app.willbank.com/reset-password?token=ABC123XYZ

Ce lien expire dans 24 heures.

Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.

Cordialement,
L'équipe WillBank
```

### 4. Page "Réinitialiser le mot de passe"
**Route** : `/reset-password?token=ABC123XYZ`

**Fonctionnalités** :
- Validation du token (depuis l'URL)
- Formulaire avec 2 champs :
  - Nouveau mot de passe (avec indicateur de force)
  - Confirmation du mot de passe
- Validation :
  - Minimum 8 caractères
  - Les 2 mots de passe doivent correspondre
- Bouton "Réinitialiser le mot de passe"
- Loading state pendant la réinitialisation

**Après soumission** :
- ✅ Message de succès
- ✅ Bouton "Se connecter"
- ✅ Redirection vers login

**Si token invalide** :
- ❌ Message d'erreur
- ❌ Bouton "Demander un nouveau lien"

## 🎨 Design

### Style uniforme
- Même design que la page de login
- Dégradé de fond (bleu → vert)
- Carte blanche centrée
- Animations fluides
- Responsive design

### Icônes
- 🔒 Cadenas : Mot de passe oublié
- 🔑 Clé : Réinitialisation
- ✅ Check : Succès
- ❌ Croix : Erreur

### Couleurs
- Primary : #1E3A5F (Bleu marine)
- Secondary : #00B894 (Vert émeraude)
- Success : #00B894
- Error : #e74c3c

## 📡 API Endpoints

### POST `/auth/forgot-password`
Demande de réinitialisation

**Request** :
```json
{
  "email": "user@example.com"
}
```

**Response** :
```json
{
  "message": "Email envoyé avec succès"
}
```

### POST `/auth/reset-password`
Réinitialisation du mot de passe

**Request** :
```json
{
  "token": "ABC123XYZ",
  "newPassword": "nouveauMotDePasse123"
}
```

**Response** :
```json
{
  "message": "Mot de passe réinitialisé avec succès"
}
```

## 🧪 Mode Mock

En mode mock (`MOCK_MODE = true`), les fonctionnalités sont simulées :

### Forgot Password
```typescript
forgotPassword(email: string) {
  console.log(`Email de réinitialisation envoyé à: ${email}`);
  return of({ message: 'Email envoyé' }).pipe(delay(1000));
}
```

### Reset Password
```typescript
resetPassword(token: string, newPassword: string) {
  console.log(`Mot de passe réinitialisé avec le token: ${token}`);
  return of({ message: 'Réinitialisé' }).pipe(delay(1000));
}
```

## 🔒 Sécurité

### Token
- Généré côté backend
- Unique et aléatoire
- Expire après 24h
- Usage unique (invalidé après utilisation)

### Mot de passe
- Minimum 8 caractères
- Indicateur de force (faible/moyen/fort)
- Masquage avec toggle
- Validation côté client et serveur

### Email
- Validation du format
- Vérification de l'existence
- Rate limiting (backend)
- Protection contre le spam

## 🎯 Implémentation Backend

### 1. Génération du token
```java
String token = UUID.randomUUID().toString();
LocalDateTime expiry = LocalDateTime.now().plusHours(24);

PasswordResetToken resetToken = new PasswordResetToken();
resetToken.setToken(token);
resetToken.setUser(user);
resetToken.setExpiryDate(expiry);
resetTokenRepository.save(resetToken);
```

### 2. Envoi de l'email
```java
String resetUrl = "https://app.willbank.com/reset-password?token=" + token;

emailService.sendEmail(
    user.getEmail(),
    "Réinitialisation de mot de passe",
    "Cliquez sur ce lien : " + resetUrl
);
```

### 3. Validation du token
```java
PasswordResetToken resetToken = resetTokenRepository.findByToken(token);

if (resetToken == null || resetToken.isExpired()) {
    throw new InvalidTokenException();
}

User user = resetToken.getUser();
user.setPassword(passwordEncoder.encode(newPassword));
userRepository.save(user);

resetTokenRepository.delete(resetToken); // Usage unique
```

## 📧 Configuration Email (Backend)

### Spring Boot (application.properties)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=student@example.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Service Email
```java
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@willbank.com");
        
        mailSender.send(message);
    }
}
```

## 🧪 Tests

### Test manuel

1. **Demande de réinitialisation** :
   ```
   1. Aller sur /login
   2. Cliquer sur "Mot de passe oublié ?"
   3. Entrer un email
   4. Cliquer sur "Envoyer le lien"
   5. Vérifier le message de confirmation
   ```

2. **Réinitialisation** :
   ```
   1. Aller sur /reset-password?token=test123
   2. Entrer un nouveau mot de passe
   3. Confirmer le mot de passe
   4. Cliquer sur "Réinitialiser"
   5. Vérifier le message de succès
   6. Se connecter avec le nouveau mot de passe
   ```

3. **Token invalide** :
   ```
   1. Aller sur /reset-password (sans token)
   2. Vérifier le message d'erreur
   3. Cliquer sur "Demander un nouveau lien"
   ```

## 📱 Responsive

- ✅ Mobile (< 576px)
- ✅ Tablet (576px - 768px)
- ✅ Desktop (> 768px)

## ♿ Accessibilité

- ✅ Labels explicites
- ✅ Messages d'erreur clairs
- ✅ Navigation au clavier
- ✅ Contraste des couleurs
- ✅ Focus states visibles

## 🎉 Résultat

L'utilisateur peut maintenant :
1. ✅ Demander une réinitialisation par email
2. ✅ Recevoir un lien sécurisé
3. ✅ Réinitialiser son mot de passe
4. ✅ Se reconnecter avec le nouveau mot de passe

---

**Version** : 1.0.0  
**Date** : Novembre 2024  
**Statut** : ✅ Implémenté
