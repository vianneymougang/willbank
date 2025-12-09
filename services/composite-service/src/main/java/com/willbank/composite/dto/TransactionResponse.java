package com.willbank.composite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Response DTO (mirrors transaction-service TransactionResponse)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Transaction information")
public class TransactionResponse {

    @Schema(description = "Transaction ID", example = "1")
    private Long id;

    @Schema(description = "Source account ID", example = "1")
    private Long accountId;

    @Schema(description = "Destination account ID (for transfers)", example = "2")
    private Long destinationAccountId;

    @Schema(description = "Transaction type (DEPOSIT/WITHDRAWAL/TRANSFER)", example = "DEPOSIT")
    private String type;

    @Schema(description = "Transaction amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Transaction status (PENDING/COMPLETED/FAILED)", example = "COMPLETED")
    private String status;

    @Schema(description = "Account balance before transaction", example = "1000.00")
    private BigDecimal balanceBefore;

    @Schema(description = "Account balance after transaction", example = "1100.00")
    private BigDecimal balanceAfter;

    @Schema(description = "Transaction description", example = "Salary deposit")
    private String description;

    @Schema(description = "Failure reason (if status is FAILED)")
    private String failureReason;

    @Schema(description = "Transaction creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Transaction last update timestamp")
    private LocalDateTime updatedAt;
}
