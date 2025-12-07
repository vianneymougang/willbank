package com.willbank.authservice.controller;

import com.willbank.authservice.dto.*;
import com.willbank.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 * Handles login, token refresh, validation, and logout
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint - authenticate user and return tokens
     * 
     * @param loginRequest Login credentials
     * @return AuthResponse with access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request for email: {}", loginRequest.getEmail());
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Refresh token endpoint - get new access token using refresh token
     * 
     * @param request Refresh token request
     * @return AuthResponse with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Validate token endpoint - used by API Gateway to validate access tokens
     * 
     * @param authHeader Authorization header containing Bearer token
     * @return ValidationResponse with user information if valid
     */
    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ValidationResponse.failure("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        ValidationResponse response = authService.validateToken(token);

        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint - invalidate all refresh tokens for a user
     * 
     * @param userId User ID to logout
     * @return Success message
     */
    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        log.info("Logout request for user ID: {}", userId);
        authService.logout(userId);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Health check endpoint
     * 
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
