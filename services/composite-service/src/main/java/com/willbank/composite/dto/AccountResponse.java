package com.willbank.composite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Response DTO (mirrors account-service AccountResponse)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bank account information")
public class AccountResponse {

    @Schema(description = "Account ID", example = "1")
    private Long id;

    @Schema(description = "Customer ID", example = "1")
    private Long customerId;

    @Schema(description = "Account number", example = "FR7630006000011234567890189")
    private String accountNumber;

    @Schema(description = "Account type (CHECKING/SAVINGS)", example = "CHECKING")
    private String type;

    @Schema(description = "Current balance", example = "1500.50")
    private BigDecimal balance;

    @Schema(description = "Account status (ACTIVE/SUSPENDED/CLOSED)", example = "ACTIVE")
    private String status;

    @Schema(description = "Account label/alias", example = "Main Account")
    private String label;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
