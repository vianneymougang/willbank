package com.willbank.composite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Account Statement Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Account statement with transactions and summary for a date range")
public class AccountStatementResponse {

    @Schema(description = "Account details")
    private AccountResponse account;

    @Schema(description = "Transactions in the date range")
    private List<TransactionResponse> transactions;

    @Schema(description = "Statement start date")
    private LocalDateTime startDate;

    @Schema(description = "Statement end date")
    private LocalDateTime endDate;

    @Schema(description = "Total debits in period", example = "500.00")
    private BigDecimal totalDebits;

    @Schema(description = "Total credits in period", example = "1500.00")
    private BigDecimal totalCredits;
}
