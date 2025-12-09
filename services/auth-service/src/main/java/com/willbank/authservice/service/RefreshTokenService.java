package com.willbank.authservice.service;

import com.willbank.authservice.model.RefreshToken;
import com.willbank.authservice.repository.RefreshTokenRepository;
import com.willbank.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens
 * Handles creation, validation, and deletion of refresh tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Create a new refresh token for a user
     * 
     * @param userId User ID
     * @return Refresh token string
     */
    @Transactional
    public String createRefreshToken(Long userId, String email) {
        // Generate the JWT refresh token
        String tokenValue = jwtTokenProvider.generateRefreshToken(userId, email);

        // Create and save the refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(userId)
                .expiryDate(jwtTokenProvider.getRefreshTokenExpiryDate())
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user ID: {}", userId);

        return tokenValue;
    }

    /**
     * Verify and retrieve a refresh token
     * 
     * @param token Refresh token string
     * @return Optional containing RefreshToken if valid
     */
    public Optional<RefreshToken> verifyRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(refreshToken -> {
                    if (refreshToken.isExpired()) {
                        // Delete expired token
                        refreshTokenRepository.delete(refreshToken);
                        log.warn("Refresh token expired and deleted for user ID: {}", refreshToken.getUserId());
                        return false;
                    }
                    return true;
                });
    }

    /**
     * Delete all refresh tokens for a user (used during logout)
     * 
     * @param userId User ID
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        int deleted = refreshTokenRepository.deleteByUserId(userId);
        log.info("Deleted {} refresh token(s) for user ID: {}", deleted, userId);
    }

    /**
     * Delete a specific refresh token
     * 
     * @param token Refresh token string
     */
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshTokenRepository.delete(refreshToken);
                    log.info("Deleted refresh token for user ID: {}", refreshToken.getUserId());
                });
    }

    /**
     * Cleanup expired refresh tokens (can be scheduled)
     * 
     * @return Number of tokens deleted
     */
    @Transactional
    public int cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteAllExpired(Instant.now());
        log.info("Cleaned up {} expired refresh tokens", deleted);
        return deleted;
    }
}
