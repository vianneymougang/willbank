package com.willbank.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for customer creation
 * Includes temporary password (only time it's visible)
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

    /**
     * Temporary password - ONLY included in creation response
     * Never logged, never stored in plain text after this
     */
    private String temporaryPassword;

    private String message;
}
