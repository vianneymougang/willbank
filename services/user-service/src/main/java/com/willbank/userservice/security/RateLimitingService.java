package com.willbank.userservice.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using Bucket4j (Token Bucket algorithm)
 * 
 * Limits:
 * - Admin customer creation: 10 per hour
 * - Login attempts: 5 per 15 minutes per IP
 * - Password changes: 3 per hour per user
 */
@Service
@Slf4j
public class RateLimitingService {

    // Separate buckets for different operations
    private final Map<String, Bucket> adminCreationBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> passwordChangeBuckets = new ConcurrentHashMap<>();

    /**
     * Check if admin can create more customers
     * 
     * @param adminId Admin user ID
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowAdminCreation(Long adminId) {
        String key = "admin:" + adminId;
        Bucket bucket = adminCreationBuckets.computeIfAbsent(key, k -> createAdminCreationBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for admin {} creating customers", adminId);
            return false;
        }
    }

    /**
     * Check if login attempt is allowed from this IP
     * 
     * @param ipAddress Client IP address
     */
    public boolean allowLogin(String ipAddress) {
        String key = "login:" + ipAddress;
        Bucket bucket = loginBuckets.computeIfAbsent(key, k -> createLoginBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for login attempts from IP {}", ipAddress);
            return false;
        }
    }

    /**
     * Check if password change is allowed for this user
     * 
     * @param userId User ID
     */
    public boolean allowPasswordChange(Long userId) {
        String key = "password:" + userId;
        Bucket bucket = passwordChangeBuckets.computeIfAbsent(key, k -> createPasswordChangeBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded for password changes for user {}", userId);
            return false;
        }
    }

    /**
     * Get retry-after seconds for customer creation
     */
    public long getAdminCreationRetryAfter(Long adminId) {
        String key = "admin:" + adminId;
        Bucket bucket = adminCreationBuckets.get(key);
        if (bucket == null)
            return 0;

        return bucket.tryConsumeAndReturnRemaining(1).getNanosToWaitForRefill() / 1_000_000_000;
    }

    // Bucket factory methods

    private Bucket createAdminCreationBucket() {
        // 10 requests per hour
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createLoginBucket() {
        // 5 requests per 15 minutes
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(15))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createPasswordChangeBucket() {
        // 3 requests per hour
        Bandwidth limit = Bandwidth.builder()
                .capacity(3)
                .refillGreedy(3, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
