package com.willbank.composite.controller;

import com.willbank.composite.dto.AccountStatementResponse;
import com.willbank.composite.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Statement Controller
 * Provides account statement generation
 */
@RestController
@RequestMapping("/api/composite/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Statement", description = "Account statement generation endpoints - provides transactions and summary for a date range")
@SecurityRequirement(name = "Bearer Authentication")
public class StatementController {

    private final StatementService statementService;

    @GetMapping("/{accountId}/statement")
    @Operation(summary = "Get account statement", description = "Returns account details, all transactions within the specified date range, and calculated totals (debits and credits)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statement generated successfully", content = @Content(schema = @Schema(implementation = AccountStatementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid date format or range"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User doesn't have permission"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "503", description = "Service unavailable - downstream service error")
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<AccountStatementResponse> getAccountStatement(
            @Parameter(description = "Account ID", required = true, example = "1") @PathVariable Long accountId,
            @Parameter(description = "Start date (ISO 8601 format)", required = true, example = "2025-01-01T00:00:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End date (ISO 8601 format)", required = true, example = "2025-12-31T23:59:59") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        log.info("REST API: Get statement for account ID: {} from {} to {}", accountId, start, end);
        AccountStatementResponse statement = statementService.getAccountStatement(accountId, start, end);
        return ResponseEntity.ok(statement);
    }
}
