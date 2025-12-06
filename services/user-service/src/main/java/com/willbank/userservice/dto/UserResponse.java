package com.willbank.userservice.dto;

import com.willbank.userservice.model.KycStatus;
import com.willbank.userservice.model.UserRole;
import com.willbank.userservice.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User responses (excludes password for security).
 * Used when returning user information to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserStatus status;
    private UserRole role;

    /**
     * KYC status - only present for customers, null for admins
     */
    private KycStatus kycStatus;

    /**
     * Discriminator value indicating user type (ADMIN or CUSTOMER)
     */
    private String userType;
}
