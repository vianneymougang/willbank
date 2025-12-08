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
}
