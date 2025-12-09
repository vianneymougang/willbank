package com.willbank.userservice.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find audit logs by action type
     */
    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    /**
     * Find failed operations (for security monitoring)
     */
    List<AuditLog> findBySuccessFalseOrderByTimestampDesc();

    /**
     * Count operations by user in time range (for rate limiting analysis)
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = ?1 AND a.action = ?2 AND a.timestamp >= ?3")
    long countByUserAndActionSince(Long userId, AuditAction action, LocalDateTime since);
}
