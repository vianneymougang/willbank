package com.willbank.composite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Response DTO (mirrors user-service UserResponse)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "First name", example = "John")
    private String name;

    @Schema(description = "Last name", example = "Doe")
    private String surname;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+33612345678")
    private String phone;

    @Schema(description = "Address", example = "123 Main St, Paris")
    private String address;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "User status (ACTIVE/SUSPENDED)", example = "ACTIVE")
    private String status;

    @Schema(description = "User role (ADMIN/CUSTOMER)", example = "CUSTOMER")
    private String role;

    @Schema(description = "KYC status for customers (PENDING/VERIFIED/REJECTED)", example = "VERIFIED")
    private String kycStatus;

    @Schema(description = "User type discriminator (ADMIN/CUSTOMER)", example = "CUSTOMER")
    private String userType;
}
