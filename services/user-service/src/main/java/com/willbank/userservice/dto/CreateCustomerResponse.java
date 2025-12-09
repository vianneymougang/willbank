package com.willbank.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for customer creation
 * Does NOT include password for security (OWASP API3 compliance)
 * Password is ONLY sent via email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCustomerResponse {

    private Long customerId;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    /**
     * Indicates if welcome email with credentials was sent
     */
    private Boolean passwordEmailSent;

    /**
     * Informative message for admin
     */
    private String message;
}
