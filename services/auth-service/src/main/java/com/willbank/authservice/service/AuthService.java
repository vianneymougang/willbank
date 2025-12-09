package com.willbank.authservice.service;

import com.willbank.authservice.client.UserClient;
import com.willbank.authservice.dto.*;
import com.willbank.authservice.model.RefreshToken;
import com.willbank.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations
 * Handles login, token refresh, and token validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserClient userClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Authenticate user and generate tokens
     * 
     * @param loginRequest Login credentials
     * @return AuthResponse containing access and refresh tokens
     * @throws RuntimeException if authentication fails
     */
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // Retrieve user from user-service
        UserDto user;
        try {
            user = userClient.getUserByEmail(loginRequest.getEmail());
        } catch (Exception e) {
            log.error("User not found: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        // Check if user is active
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("Login attempt for suspended user: {}", loginRequest.getEmail());
            throw new RuntimeException("User account is suspended");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId(), user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Refresh access token using refresh token
     * 
     * @param request Refresh token request
     * @return AuthResponse with new access token
     * @throws RuntimeException if refresh token is invalid
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh attempt");

        // Verify refresh token exists and is valid
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    log.error("Invalid or expired refresh token");
                    return new RuntimeException("Invalid or expired refresh token");
                });

        // Extract user information from the refresh token JWT
        Long userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());

        // Get fresh user data to ensure role is up to date
        UserDto user;
        try {
            user = userClient.getUserByEmail(email);
        } catch (Exception e) {
            log.error("User not found during token refresh: {}", email);
            throw new RuntimeException("User not found");
        }

        // Check if user is still active
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("Token refresh attempt for suspended user: {}", email);
            throw new RuntimeException("User account is suspended");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        log.info("Access token refreshed for user: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken()) // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Validate an access token
     * 
     * @param token Access token to validate
     * @return ValidationResponse indicating validity and user info
     */
    public ValidationResponse validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return ValidationResponse.failure("Token is missing");
        }

        try {
            // Validate token signature and expiration
            if (!jwtTokenProvider.validateToken(token)) {
                return ValidationResponse.failure("Token is invalid");
            }

            // Check if it's an access token (not a refresh token)
            String tokenType = jwtTokenProvider.getTokenType(token);
            if (!"access".equals(tokenType)) {
                return ValidationResponse.failure("Token is not an access token");
            }

            // Extract user information
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            return ValidationResponse.success(userId, email, role);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ValidationResponse.failure("Token validation failed: " + e.getMessage());
        }
    }

    /**
     * Logout user by invalidating refresh tokens
     * 
     * @param userId User ID
     */
    public void logout(Long userId) {
        log.info("Logout request for user ID: {}", userId);
        refreshTokenService.deleteByUserId(userId);
        log.info("User logged out successfully: {}", userId);
    }
}
