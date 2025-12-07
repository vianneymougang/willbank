package com.willbank.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing simplified user data from user-service
 * Used for authentication and token generation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String password; // BCrypt hashed
    private String role; // ADMIN or CUSTOMER
    private String status; // ACTIVE or SUSPENDED
    private String name;
    private String surname;
}
