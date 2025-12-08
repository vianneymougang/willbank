package com.willbank.notificationservice.service;

import com.willbank.notificationservice.domain.Notification;
import com.willbank.notificationservice.domain.NotificationStatus;
import com.willbank.notificationservice.domain.NotificationType;
import com.willbank.notificationservice.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Service for sending emails and managing notification records
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    @Value("${willbank.branding.primary-color}")
    private String primaryColor;
    @Value("${willbank.branding.app-name}")
    private String appName;
    @Value("${willbank.branding.support-email}")
    private String supportEmail;
    @Value("${willbank.branding.from-email}")
    private String fromEmail;

    /**
     * Send welcome email to newly created customer
     */
    public void sendWelcomeEmail(Long customerId, String email, String name,
            String surname, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String subject = String.format("Bienvenue chez %s - Votre compte a été créé", appName);
            helper.setFrom(fromEmail, appName);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(buildWelcomeEmailContent(name, surname, email, temporaryPassword), true);
            // Save notification record
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(email)
                    .type(NotificationType.CUSTOMER_WELCOME)
                    .subject(subject)
                    .content(String.format("Welcome email sent to %s %s", name, surname))
                    .status(NotificationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(customerId.toString())
                    .relatedEntityType("CUSTOMER")
                    .build();
            mailSender.send(message);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("✅ Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", email, e.getMessage(), e);
            // Save failure record
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(email)
                    .type(NotificationType.CUSTOMER_WELCOME)
                    .subject(String.format("Bienvenue chez %s", appName))
                    .content("Failed to send welcome email")
                    .status(NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(customerId.toString())
                    .relatedEntityType("CUSTOMER")
                    .build();
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send account creation confirmation email
     */
    public void sendAccountCreatedEmail(Long customerId, String email, String accountNumber,
            String accountType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String subject = String.format("%s - Nouveau compte créé", appName);
            helper.setFrom(fromEmail, appName);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(buildAccountCreatedEmailContent(accountNumber, accountType), true);
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(email)
                    .type(NotificationType.ACCOUNT_CREATED)
                    .subject(subject)
                    .content(String.format("Account created: %s (%s)", accountNumber, accountType))
                    .status(NotificationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(accountNumber)
                    .relatedEntityType("ACCOUNT")
                    .build();
            mailSender.send(message);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("✅ Account creation email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send account creation email: {}", e.getMessage(), e);
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(email)
                    .type(NotificationType.ACCOUNT_CREATED)
                    .subject(String.format("%s - Nouveau compte", appName))
                    .status(NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(accountNumber)
                    .relatedEntityType("ACCOUNT")
                    .build();
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Build HTML content for welcome email
     */
    private String buildWelcomeEmailContent(String name, String surname,
            String email, String password) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html lang="fr">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Bienvenue chez %s</title>
                            <style>
                                body {
                                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                                    line-height: 1.6;
                                    color: #1A1A2E;
                                    background-color: #F8FAFC;
                                    margin: 0;
                                    padding: 0;
                                }
                                .container {
                                    max-width: 600px;
                                    margin: 0 auto;
                                    background-color: white;
                                }
                                .header {
                                    background-color: %s;
                                    padding: 40px 20px;
                                    text-align: center;
                                }
                                .header h1 {
                                    color: white;
                                    margin: 0;
                                    font-size: 28px;
                                    font-weight: 700;
                                }
                                .content {
                                    padding: 40px 30px;
                                }
                                .greeting {
                                    font-size: 20px;
                                    font-weight: 600;
                                    color: %s;
                                    margin-bottom: 20px;
                                }
                                .credentials-box {
                                    background-color: #F8FAFC;
                                    border-left: 4px solid %s;
                                    padding: 20px;
                                    margin: 30px 0;
                                    border-radius: 8px;
                                }
                                .credentials-box h3 {
                                    margin-top: 0;
                                    color: %s;
                                    font-size: 16px;
                                }
                                .credential-item {
                                    margin: 12px 0;
                                    font-size: 14px;
                                }
                                .credential-label {
                                    font-weight: 600;
                                    color: #64748B;
                                    display: inline-block;
                                    width: 120px;
                                }
                                .credential-value {
                                    color: #1A1A2E;
                                    font-weight: 500;
                                }
                                .password {
                                    background-color: white;
                                    padding: 12px 16px;
                                    border-radius: 6px;
                                    font-family: 'Courier New', monospace;
                                    font-size: 18px;
                                    font-weight: 700;
                                    color: %s;
                                    letter-spacing: 2px;
                                    display: inline-block;
                                    margin-top: 8px;
                                    border: 2px solid %s;
                                }
                                .security-notice {
                                    background-color: #FFF9E6;
                                    border-left: 4px solid %s;
                                    padding: 15px;
                                    margin: 20px 0;
                                    border-radius: 6px;
                                }
                                .security-notice p {
                                    margin: 0;
                                    color: #856404;
                                    font-size: 14px;
                                }
                                .cta-button {
                                    display: inline-block;
                                    background-color: %s;
                                    color: white;
                                    padding: 14px 32px;
                                    text-decoration: none;
                                    border-radius: 8px;
                                    font-weight: 600;
                                    margin: 20px 0;
                                }
                                .footer {
                                    background-color: #F8FAFC;
                                    padding: 30px;
                                    text-align: center;
                                    border-top: 1px solid #E2E8F0;
                                }
                                .footer p {
                                    color: #64748B;
                                    font-size: 14px;
                                    margin: 8px 0;
                                }
                                .footer a {
                                    color: %s;
                                    text-decoration: none;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>🏦 %s</h1>
                                </div>
                                <div class="content">
                                    <p class="greeting">Bonjour %s %s,</p>
                                    <p>Bienvenue chez <strong>%s</strong> ! 🎉</p>
                                    <p>Votre compte client a été créé avec succès par notre équipe. Vous pouvez maintenant accéder à tous nos services bancaires en ligne.</p>
                                    <div class="credentials-box">
                                        <h3>🔐 Vos informations de connexion</h3>
                                        <div class="credential-item">
                                            <span class="credential-label">Email :</span>
                                            <span class="credential-value">%s</span>
                                        </div>
                                        <div class="credential-item">
                                            <span class="credential-label">Mot de passe :</span>
                                            <div class="password">%s</div>
                                        </div>
                                    </div>
                                    <div class="security-notice">
                                        <p><strong>⚠️ Important pour votre sécurité :</strong></p>
                                        <p>Ce mot de passe est temporaire. Vous <strong>devrez le changer</strong> lors de votre première connexion.</p>
                                    </div>
                                    <center>
                                        <a href="https://app.willbank.com" class="cta-button">Se connecter maintenant</a>
                                    </center>
                                    <p style="margin-top: 30px;">Si vous avez des questions ou besoin d'assistance, n'hésitez pas à nous contacter.</p>
                                </div>
                                <div class="footer">
                                    <p><strong>%s</strong></p>
                                    <p>Email : <a href="mailto:%s">%s</a></p>
                                    <p style="font-size: 12px; margin-top: 20px;">
                                        Cet email contient des informations confidentielles. Si vous l'avez reçu par erreur, veuillez le supprimer.
                                    </p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                appName, // title
                primaryColor, // header background (bleu)
                primaryColor, // greeting color (bleu)
                primaryColor, // credentials box border (bleu)
                primaryColor, // credentials box title (bleu)
                primaryColor, // password color (bleu)
                primaryColor, // password border (bleu)
                primaryColor, // security notice border (bleu)
                primaryColor, // CTA button background (bleu)
                primaryColor, // footer link color (bleu)
                appName, // header text
                name, surname, // greeting
                appName, // welcome text
                email, // credential email
                password, // credential password
                appName, // footer app name
                supportEmail, supportEmail // footer email
        );
    }

    /**
     * Build HTML content for account creation email
     */
    private String buildAccountCreatedEmailContent(String accountNumber, String accountType) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html lang="fr">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Nouveau compte - %s</title>
                            <style>
                                body {
                                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                    line-height: 1.6;
                                    color: #1A1A2E;
                                    background-color: #F8FAFC;
                                    margin: 0;
                                    padding: 0;
                                }
                                .container {
                                    max-width: 600px;
                                    margin: 0 auto;
                                    background-color: white;
                                }
                                .header {
                                    background-color: %s;
                                    padding: 40px 20px;
                                    text-align: center;
                                }
                                .header h1 {
                                    color: white;
                                    margin: 0;
                                    font-size: 24px;
                                }
                                .content {
                                    padding: 40px 30px;
                                }
                                .account-box {
                                    background-color: #F8FAFC;
                                    border-left: 4px solid %s;
                                    padding: 20px;
                                    margin: 20px 0;
                                    border-radius: 8px;
                                }
                                .account-number {
                                    font-family: 'Courier New', monospace;
                                    font-size: 20px;
                                    font-weight: 700;
                                    color: %s;
                                    margin: 10px 0;
                                }
                                .footer {
                                    background-color: #F8FAFC;
                                    padding: 20px;
                                    text-align: center;
                                    border-top: 1px solid #E2E8F0;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>🏦 Nouveau compte créé</h1>
                                </div>
                                <div class="content">
                                    <p>Bonjour,</p>
                                    <p>Nous avons le plaisir de vous informer que votre nouveau compte <strong>%s</strong> a été créé avec succès.</p>
                                    <div class="account-box">
                                        <p><strong>Numéro de compte :</strong></p>
                                        <div class="account-number">%s</div>
                                    </div>
                                    <p>Vous pouvez dès à présent utiliser ce compte pour effectuer vos opérations bancaires.</p>
                                </div>
                                <div class="footer">
                                    <p>%s - <a href="mailto:%s">%s</a></p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                appName,
                primaryColor,
                primaryColor,
                primaryColor,
                accountType,
                accountNumber,
                appName,
                supportEmail,
                supportEmail);
    }

    /**
     * Send transaction completed confirmation email
     */
    public void sendTransactionCompletedEmail(Long customerId, String email, Long transactionId,
            String type, java.math.BigDecimal amount, java.math.BigDecimal newBalance,
            String accountNumber, String description, java.time.LocalDateTime completedAt,
            Long destinationAccountId, String firstName, String accountType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String subject = buildTransactionEmailSubject(type);
            helper.setFrom(fromEmail, appName);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(buildTransactionEmailContent(type, transactionId, amount, newBalance,
                    accountNumber, description, completedAt, destinationAccountId, firstName, accountType), true);
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(email)
                    .type(NotificationType.TRANSACTION_COMPLETED)
                    .subject(subject)
                    .content(String.format("Transaction %s completed: %s", type, transactionId))
                    .status(NotificationStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(transactionId.toString())
                    .relatedEntityType("TRANSACTION")
                    .build();
            mailSender.send(message);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("✅ Transaction confirmation email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to send transaction email to {}: {}", email, e.getMessage(), e);
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(email)
                    .type(NotificationType.TRANSACTION_COMPLETED)
                    .subject(buildTransactionEmailSubject(type))
                    .status(NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(transactionId.toString())
                    .relatedEntityType("TRANSACTION")
                    .build();
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send transaction email", e);
        }
    }

    /**
     * Send large transaction alert email (customer + admin)
     */
    public void sendLargeTransactionAlert(Long customerId, String customerEmail, Long transactionId,
            String type, java.math.BigDecimal amount, java.math.BigDecimal threshold,
            String accountNumber, java.time.LocalDateTime occurredAt) {
        try {
            // Send to customer (security notification)
            MimeMessage customerMessage = mailSender.createMimeMessage();
            MimeMessageHelper customerHelper = new MimeMessageHelper(customerMessage, true, "UTF-8");
            String customerSubject = "🔔 Alerte Sécurité: Transaction Importante - " + appName;
            customerHelper.setFrom(fromEmail, appName);
            customerHelper.setTo(customerEmail);
            customerHelper.setSubject(customerSubject);
            customerHelper.setText(buildLargeTransactionCustomerAlertContent(transactionId, type,
                    amount, accountNumber, occurredAt), true);
            mailSender.send(customerMessage);
            // Send to admin (compliance alert)
            String adminEmail = "compliance@willbank.com"; // TODO: from config
            MimeMessage adminMessage = mailSender.createMimeMessage();
            MimeMessageHelper adminHelper = new MimeMessageHelper(adminMessage, true, "UTF-8");
            String adminSubject = String.format("⚠️ Alerte Transaction: %s$ - Compte %s",
                    amount, accountNumber);
            adminHelper.setFrom(fromEmail, appName);
            adminHelper.setTo(adminEmail);
            adminHelper.setSubject(adminSubject);
            adminHelper.setText(buildLargeTransactionAdminAlertContent(transactionId, type, amount,
                    threshold, accountNumber, customerId, customerEmail, occurredAt), true);
            mailSender.send(adminMessage);
            // Save notification record
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(customerEmail)
                    .type(NotificationType.LARGE_TRANSACTION_ALERT)
                    .subject(customerSubject)
                    .content(String.format("Large transaction alert: %s$ (threshold: %s$)", amount, threshold))
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(transactionId.toString())
                    .relatedEntityType("TRANSACTION")
                    .build();
            notificationRepository.save(notification);
            log.warn("🚨 Large transaction alert sent for transaction {}: {}$ (threshold: {}$)",
                    transactionId, amount, threshold);
        } catch (Exception e) {
            log.error("❌ Failed to send large transaction alert: {}", e.getMessage(), e);
            Notification notification = Notification.builder()
                    .customerId(customerId)
                    .email(customerEmail)
                    .type(NotificationType.LARGE_TRANSACTION_ALERT)
                    .subject("Alerte Transaction")
                    .status(NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .relatedEntityId(transactionId.toString())
                    .relatedEntityType("TRANSACTION")
                    .build();
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send large transaction alert", e);
        }
    }

    private String buildTransactionEmailSubject(String type) {
        return switch (type) {
            case "DEPOSIT" -> "💰 Dépôt confirmé - " + appName;
            case "WITHDRAWAL" -> "💸 Retrait confirmé - " + appName;
            case "TRANSFER" -> "🔄 Virement effectué - " + appName;
            default -> "✅ Transaction confirmée - " + appName;
        };
    }

    private String buildTransactionEmailContent(String type, Long transactionId,
            java.math.BigDecimal amount, java.math.BigDecimal newBalance, String accountNumber,
            String description, java.time.LocalDateTime completedAt, Long destinationAccountId,
            String firstName, String accountType) {
        String emoji = switch (type) {
            case "DEPOSIT" -> "💰";
            case "WITHDRAWAL" -> "💸";
            case "TRANSFER" -> "🔄";
            default -> "✅";
        };
        String title = switch (type) {
            case "DEPOSIT" -> "Dépôt confirmé";
            case "WITHDRAWAL" -> "Retrait confirmé";
            case "TRANSFER" -> "Virement effectué";
            default -> "Transaction confirmée";
        };
        String operationText = switch (type) {
            case "DEPOSIT" ->
                String.format("Un dépôt de <strong>%s FCFA</strong> a été effectué sur votre compte.", amount);
            case "WITHDRAWAL" ->
                String.format("Un retrait de <strong>%s FCFA</strong> a été effectué depuis votre compte.", amount);
            case "TRANSFER" ->
                String.format("Un virement de <strong>%s FCFA</strong> a été effectué depuis votre compte.", amount);
            default -> "Une transaction de <strong>%s FCFA</strong> a été effectuée.";
        };

        // Translate account type to French
        String accountTypeFr = switch (accountType != null ? accountType.toUpperCase() : "") {
            case "SAVINGS" -> "Compte Épargne";
            case "CHECKING" -> "Compte Courant";
            case "BUSINESS" -> "Compte Professionnel";
            default -> "Compte";
        };
        return String.format(
                """
                        <!DOCTYPE html>
                        <html lang="fr">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>%s - %s</title>
                            <style>
                                body {
                                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                    line-height: 1.6;
                                    color: #1A1A2E;
                                    background-color: #F8FAFC;
                                    margin: 0;
                                    padding: 0;
                                }
                                .container {
                                    max-width: 600px;
                                    margin: 0 auto;
                                    background-color: white;
                                }
                                .header {
                                    background-color: %s;
                                    padding: 40px 20px;
                                    text-align: center;
                                }
                                .header h1 {
                                    color: white;
                                    margin: 0;
                                    font-size: 26px;
                                }
                                .content {
                                    padding: 40px 30px;
                                }
                                .transaction-box {
                                    background-color: #F8FAFC;
                                    border-left: 4px solid %s;
                                    padding: 25px;
                                    margin: 25px 0;
                                    border-radius: 8px;
                                }
                                .transaction-detail {
                                    margin: 15px 0;
                                    font-size: 15px;
                                }
                                .detail-label {
                                    font-weight: 600;
                                    color: #64748B;
                                    display: inline-block;
                                    width: 180px;
                                }
                                .detail-value {
                                    color: #1A1A2E;
                                    font-weight: 500;
                                }
                                .amount {
                                    font-size: 32px;
                                    font-weight: 700;
                                    color: %s;
                                    margin: 20px 0;
                                }
                                .new-balance {
                                    background-color: white;
                                    padding: 15px;
                                    border-radius: 8px;
                                    margin: 20px 0;
                                    border: 2px solid %s;
                                }
                                .new-balance-label {
                                    font-size: 14px;
                                    color: #64748B;
                                    margin-bottom: 5px;
                                }
                                .new-balance-value {
                                    font-size: 24px;
                                    font-weight: 700;
                                    color: %s;
                                }
                                .footer {
                                    background-color: #F8FAFC;
                                    padding: 30px;
                                    text-align: center;
                                    border-top: 1px solid #E2E8F0;
                                }
                                .footer p {
                                    color: #64748B;
                                    font-size: 14px;
                                    margin: 8px 0;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>%s %s</h1>
                                </div>

                                <div class="content">
                                    <p>Bonjour %s,</p>

                                    <p>%s</p>

                                    <div class="transaction-box">
                                        <div class="amount">%s FCFA</div>

                                        <div class="transaction-detail">
                                            <span class="detail-label">ID Transaction :</span>
                                            <span class="detail-value">%s</span>
                                        </div>

                                        <div class="transaction-detail">
                                            <span class="detail-label">Compte :</span>
                                            <span class="detail-value">%s - %s</span>
                                        </div>

                                        <div class="transaction-detail">
                                            <span class="detail-label">Date et heure :</span>
                                            <span class="detail-value">%s</span>
                                        </div>

                                        %s

                                        <div class="new-balance">
                                            <div class="new-balance-label">Nouveau solde</div>
                                            <div class="new-balance-value">%s FCFA</div>
                                        </div>
                                    </div>

                                    <p style="margin-top: 30px; font-size: 14px; color: #64748B;">
                                        Si vous n'êtes pas à l'origine de cette transaction, veuillez contacter immédiatement notre service client.
                                    </p>
                                </div>

                                <div class="footer">
                                    <p><strong>%s</strong></p>
                                    <p>Email : <a href="mailto:%s" style="color: %s;">%s</a></p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                title, appName, // title
                primaryColor, // header background
                primaryColor, // transaction box border
                primaryColor, // amount color
                primaryColor, // new balance border
                primaryColor, // new balance value color
                emoji, title, // header text
                firstName != null && !firstName.isEmpty() ? firstName : "", // customer first name
                operationText, // operation description
                amount, // amount value
                transactionId, // transaction ID
                accountTypeFr, accountNumber, // account type + number
                completedAt, // date time
                description != null && !description.isEmpty()
                        ? String.format(
                                "<div class=\"transaction-detail\"><span class=\"detail-label\">Description :</span><span class=\"detail-value\">%s</span></div>",
                                description)
                        : "", // optional description
                newBalance, // new balance
                appName, // footer
                supportEmail, primaryColor, supportEmail // footer email
        );
    }

    private String buildLargeTransactionCustomerAlertContent(Long transactionId, String type,
            java.math.BigDecimal amount, String accountNumber, java.time.LocalDateTime occurredAt) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html lang="fr">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Alerte Sécurité - %s</title>
                            <style>
                                body {
                                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                    line-height: 1.6;
                                    color: #1A1A2E;
                                    background-color: #F8FAFC;
                                    margin: 0;
                                    padding: 0;
                                }
                                .container {
                                    max-width: 600px;
                                    margin: 0 auto;
                                    background-color: white;
                                }
                                .header {
                                    background-color: #DC2626;
                                    padding: 40px 20px;
                                    text-align: center;
                                }
                                .header h1 {
                                    color: white;
                                    margin: 0;
                                    font-size: 24px;
                                }
                                .content {
                                    padding: 40px 30px;
                                }
                                .alert-box {
                                    background-color: #FFF9E6;
                                    border-left: 4px solid #FFA500;
                                    padding: 20px;
                                    margin: 20px 0;
                                    border-radius: 8px;
                                }
                                .cta-button {
                                    display: inline-block;
                                    background-color: %s;
                                    color: white;
                                    padding: 14px 32px;
                                    text-decoration: none;
                                    border-radius: 8px;
                                    font-weight: 600;
                                    margin: 20px 0;
                                }
                                .footer {
                                    background-color: #F8FAFC;
                                    padding: 30px;
                                    text-align: center;
                                    border-top: 1px solid #E2E8F0;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>🔔 Alerte Sécurité</h1>
                                </div>

                                <div class="content">
                                    <p><strong>Bonjour,</strong></p>

                                    <p>Nous vous informons qu'une <strong>transaction importante</strong> a été effectuée sur votre compte.</p>

                                    <div class="alert-box">
                                        <p><strong>Détails de la transaction :</strong></p>
                                        <p>• Type : <strong>%s</strong></p>
                                        <p>• Montant : <strong>%s $</strong></p>
                                        <p>• Compte : <strong>%s</strong></p>
                                        <p>• Date : <strong>%s</strong></p>
                                        <p>• ID Transaction : <strong>%s</strong></p>
                                    </div>

                                    <p>Si vous <strong>n'êtes pas à l'origine</strong> de cette transaction, veuillez <strong>nous contacter immédiatement</strong>.</p>

                                    <center>
                                        <a href="tel:1-800-WILLBANK" class="cta-button">☎️ Appeler le service sécurité</a>
                                    </center>

                                    <p style="margin-top: 30px; font-size: 14px; color: #64748B;">
                                        <strong>Hotline Sécurité :</strong> 1-800-WILLBANK<br>
                                        <strong>Email :</strong> security@willbank.com
                                    </p>
                                </div>

                                <div class="footer">
                                    <p><strong>%s - Service Sécurité</strong></p>
                                    <p>Email : <a href="mailto:%s" style="color: %s;">%s</a></p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                appName, // title
                primaryColor, // CTA button
                type, // transaction type
                amount, // amount
                accountNumber, // account number
                occurredAt, // occurred at
                transactionId, // transaction ID
                appName, // footer
                supportEmail, primaryColor, supportEmail // footer email
        );
    }

    private String buildLargeTransactionAdminAlertContent(Long transactionId, String type,
            java.math.BigDecimal amount, java.math.BigDecimal threshold, String accountNumber,
            Long customerId, String customerEmail, java.time.LocalDateTime occurredAt) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <title>Alerte Compliance - %s</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            line-height: 1.6;
                            color: #1A1A2E;
                            background-color: #F8FAFC;
                        }
                        .container {
                            max-width: 700px;
                            margin: 20px auto;
                            background-color: white;
                            border: 2px solid #DC2626;
                        }
                        .header {
                            background-color: #DC2626;
                            padding: 30px;
                            color: white;
                        }
                        .content {
                            padding: 30px;
                        }
                        .data-table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin: 20px 0;
                        }
                        .data-table td {
                            padding: 10px;
                            border-bottom: 1px solid #E2E8F0;
                        }
                        .data-label {
                            font-weight: 600;
                            width: 200px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>⚠️ ALERTE TRANSACTION IMPORTANTE</h1>
                        </div>

                        <div class="content">
                            <p><strong>Une transaction dépassant le seuil de surveillance a été détectée.</strong></p>

                            <table class="data-table">
                                <tr>
                                    <td class="data-label">ID Transaction :</td>
                                    <td><strong>%s</strong></td>
                                </tr>
                                <tr>
                                    <td class="data-label">Type :</td>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <td class="data-label">Montant :</td>
                                    <td><strong style="color: #DC2626;">%s $</strong></td>
                                </tr>
                                <tr>
                                    <td class="data-label">Seuil :</td>
                                    <td>%s $</td>
                                </tr>
                                <tr>
                                    <td class="data-label">Compte :</td>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <td class="data-label">ID Client :</td>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <td class="data-label">Email Client :</td>
                                    <td>%s</td>
                                </tr>
                                <tr>
                                    <td class="data-label">Date/Heure :</td>
                                    <td>%s</td>
                                </tr>
                            </table>

                            <p><strong>Action requise :</strong></p>
                            <ul>
                                <li>Vérifier les indicateurs de fraude</li>
                                <li>Valider l'identité du client si nécessaire</li>
                                <li>Mettre à jour les registres de compliance</li>
                            </ul>

                            <p style="font-size: 12px; color: #64748B; margin-top: 30px;">
                                Ceci est une alerte automatique du système de surveillance des transactions.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                appName, // title
                transactionId, // transaction ID
                type, // type
                amount, // amount
                threshold, // threshold
                accountNumber, // account number
                customerId, // customer ID
                customerEmail, // customer email
                occurredAt // occurred at
        );
    }
}
