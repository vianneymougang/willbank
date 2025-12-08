package com.example.notificationservice.service;

import com.example.notificationservice.entity.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    
    @Mock
    private JavaMailSender mailSender;
    
    @Mock
    private MimeMessage mimeMessage;
    
    @InjectMocks
    private EmailService emailService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }
    
    @Test
    void testSendEmail_Success() throws MessagingException {
        Notification notification = Notification.builder()
            .recipient("recipient@example.com")
            .subject("Test Subject")
            .content("Test Content")
            .type(Notification.NotificationType.EMAIL)
            .build();
        
        emailService.sendEmail(notification);
        
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
