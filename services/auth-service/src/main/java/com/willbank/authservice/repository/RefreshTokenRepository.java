package com.willbank.authservice.repository;

import com.willbank.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for managing RefreshToken entities
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its token string
     * 
     * @param token the token string
     * @return Optional containing the RefreshToken if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Delete all refresh tokens for a specific user
     * 
     * @param userId the user ID
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
    int deleteByUserId(Long userId);

    /**
     * Delete all expired refresh tokens (cleanup job)
     * 
     * @param now current timestamp
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteAllExpired(Instant now);

    /**
     * Check if a user has any valid refresh tokens
     * 
     * @param userId the user ID
     * @return true if at least one valid token exists
     */
    boolean existsByUserId(Long userId);
}
