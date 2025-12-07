package com.willbank.userservice.service;

import com.willbank.userservice.model.TemporaryPassword;
import com.willbank.userservice.repository.TemporaryPasswordRepository;
import com.willbank.userservice.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing temporary passwords
 * Handles encryption/decryption and lifecycle management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemporaryPasswordService {

    private final TemporaryPasswordRepository repository;
    private final EncryptionService encryptionService;
    private final PasswordGenerationService passwordGenerationService;

    /**
     * Create and store a temporary password for a user
     * Returns the plaintext password (for sending to user)
     */
    @Transactional
    public String createTemporaryPassword(Long userId) {
        // Generate secure password
        String plainPassword = passwordGenerationService.generateSecurePassword();

        // Encrypt before storage
        String encryptedPassword = encryptionService.encrypt(plainPassword);

        // Delete any existing temporary password for this user
        repository.findByUserIdAndUsedFalse(userId).ifPresent(repository::delete);

        // Create new temporary password
        TemporaryPassword tempPassword = TemporaryPassword.builder()
                .userId(userId)
                .encryptedPassword(encryptedPassword)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .used(false)
                .mustChangePassword(true)
                .build();

        repository.save(tempPassword);
        log.info("Created temporary password for user {}, expires at {}", userId, tempPassword.getExpiresAt());

        // Return plaintext (only time it's available)
        return plainPassword;
    }

    /**
     * Find active temporary password for a user
     */
    public Optional<TemporaryPassword> findActiveByUserId(Long userId) {
        Optional<TemporaryPassword> tempPassword = repository.findByUserIdAndUsedFalse(userId);

        // Check if expired
        if (tempPassword.isPresent() && tempPassword.get().isExpired()) {
            log.warn("Temporary password for user {} is expired", userId);
            return Optional.empty();
        }

        return tempPassword;
    }

    /**
     * Mark temporary password as used
     */
    @Transactional
    public void markAsUsed(Long userId) {
        repository.findByUserIdAndUsedFalse(userId).ifPresent(tempPassword -> {
            tempPassword.setUsed(true);
            repository.save(tempPassword);
            log.info("Marked temporary password as used for user {}", userId);
        });
    }

    /**
     * Verify if the provided password matches the temporary password
     */
    public boolean verify(Long userId, String providedPassword) {
        Optional<TemporaryPassword> tempPasswordOpt = findActiveByUserId(userId);

        if (tempPasswordOpt.isEmpty()) {
            return false;
        }

        TemporaryPassword tempPassword = tempPasswordOpt.get();

        try {
            String decryptedPassword = encryptionService.decrypt(tempPassword.getEncryptedPassword());
            return decryptedPassword.equals(providedPassword);
        } catch (Exception e) {
            log.error("Failed to verify temporary password for user {}", userId, e);
            return false;
        }
    }

    /**
     * Cleanup expired temporary passwords
     * Runs every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredPasswords() {
        int deleted = repository.deleteExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired temporary passwords", deleted);
        }
    }
}
