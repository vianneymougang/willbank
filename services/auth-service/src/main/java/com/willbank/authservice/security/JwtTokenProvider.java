package com.willbank.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider for generating and validating JWT tokens
 * Access tokens: 30 minutes validity
 * Refresh tokens: 7 days validity (stored in database)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInSeconds;
    private final long refreshTokenValidityInSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds:1800}") long accessTokenValidityInSeconds, // 30 min default
            @Value("${jwt.refresh-token-validity-seconds:604800}") long refreshTokenValidityInSeconds // 7 days default
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInSeconds = accessTokenValidityInSeconds;
        this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
        log.info(
                "JWT Token Provider initialized with access token validity: {} seconds, refresh token validity: {} seconds",
                accessTokenValidityInSeconds, refreshTokenValidityInSeconds);
    }

    /**
     * Generate an access token for a user
     * 
     * @param userId User ID
     * @param email  User email
     * @param role   User role (ADMIN or CUSTOMER)
     * @return JWT access token string
     */
    public String generateAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(accessTokenValidityInSeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "access");

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Generate a refresh token (UUID-based, long-lived)
     * Note: The actual refresh token entity will be stored in the database
     * 
     * @param userId User ID
     * @param email  User email
     * @return JWT refresh token string
     */
    public String generateRefreshToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(refreshTokenValidityInSeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("type", "refresh");

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Validate a JWT token
     * 
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user ID from token
     * 
     * @param token JWT token string
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract email from token
     * 
     * @param token JWT token string
     * @return User email
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract role from token
     * 
     * @param token JWT token string
     * @return User role
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract token type (access or refresh)
     * 
     * @param token JWT token string
     * @return Token type
     */
    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * Check if token is expired
     * 
     * @param token JWT token string
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Get all claims from token
     * 
     * @param token JWT token string
     * @return Claims object
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get access token validity in seconds
     * 
     * @return validity duration in seconds
     */
    public long getAccessTokenValidityInSeconds() {
        return accessTokenValidityInSeconds;
    }

    /**
     * Get refresh token validity in seconds
     * 
     * @return validity duration in seconds
     */
    public long getRefreshTokenValidityInSeconds() {
        return refreshTokenValidityInSeconds;
    }

    /**
     * Get expiry date for a new refresh token
     * 
     * @return Instant representing expiry date
     */
    public Instant getRefreshTokenExpiryDate() {
        return Instant.now().plusSeconds(refreshTokenValidityInSeconds);
    }
}
