package com.willbank.userservice.repository;

import com.willbank.userservice.model.TemporaryPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TemporaryPasswordRepository extends JpaRepository<TemporaryPassword, Long> {

    /**
     * Find active temporary password for a user
     */
    Optional<TemporaryPassword> findByUserIdAndUsedFalse(Long userId);

    /**
     * Delete all expired temporary passwords (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM TemporaryPassword t WHERE t.expiresAt < ?1")
    int deleteExpired(LocalDateTime now);

    /**
     * Count active temporary passwords for a user
     */
    long countByUserIdAndUsedFalse(Long userId);
}
