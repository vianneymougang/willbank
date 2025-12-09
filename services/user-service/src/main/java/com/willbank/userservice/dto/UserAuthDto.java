package com.willbank.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for internal user data transfer (auth-service communication)
 * Includes password hash for authentication purposes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthDto {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private String password; // BCrypt hashed
    private String role; // ADMIN or CUSTOMER
    private String status; // ACTIVE or SUSPENDED
}
