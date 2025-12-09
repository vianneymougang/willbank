package com.willbank.userservice.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for audit logging of all sensitive operations
 * Logs are written asynchronously in separate transaction
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an action (asynchronous, separate transaction)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(Long userId, String userEmail, String userRole,
            AuditAction action, String entityType, Long entityId,
            String details, String ipAddress, boolean success) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            // Never fail the main operation due to audit logging
            log.error("Failed to create audit log for action: {}", action, e);
        }
    }

    /**
     * Helper to extract IP address from request
     */
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Take first IP if multiple (proxy chain)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
