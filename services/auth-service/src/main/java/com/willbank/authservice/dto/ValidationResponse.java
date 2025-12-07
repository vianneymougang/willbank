package com.willbank.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token validation response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private String role;
    private String message;

    /**
     * Create a successful validation response
     */
    public static ValidationResponse success(Long userId, String email, String role) {
        return ValidationResponse.builder()
                .valid(true)
                .userId(userId)
                .email(email)
                .role(role)
                .message("Token is valid")
                .build();
    }

    /**
     * Create a failed validation response
     */
    public static ValidationResponse failure(String message) {
        return ValidationResponse.builder()
                .valid(false)
                .message(message)
                .build();
    }
}
