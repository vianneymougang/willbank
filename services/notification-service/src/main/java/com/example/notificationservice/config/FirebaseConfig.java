package com.example.notificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {
    
    @Value("${firebase.credentials-path}")
    private Resource credentialsPath;
    
    @Value("${firebase.database-url}")
    private String databaseUrl;
    
    @Bean
    public FirebaseApp initializeFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsPath.getInputStream()))
                    .setDatabaseUrl(databaseUrl)
                    .build();
                
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
                return app;
            } catch (Exception e) {
                log.warn("Firebase initialization failed (optional): {}", e.getMessage());
                log.info("Continuing without Firebase - Push notifications will not work");
                // Return null instead of throwing - Firebase is optional
                return null;
            }
        }
        return FirebaseApp.getInstance();
    }
}
