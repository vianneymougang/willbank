package com.willbank.composite.controller;

import com.willbank.composite.dto.DashboardResponse;
import com.willbank.composite.service.DashboardService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Dashboard Controller
 * Provides aggregated customer dashboard data
 */
@RestController
@RequestMapping("/api/composite/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Customer dashboard aggregation endpoints - combines user info, accounts, and recent transactions")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer dashboard", description = "Aggregates user information, all customer accounts, and recent transactions (last 10 per account) into a single response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully", content = @Content(schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User doesn't have permission"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "503", description = "Service unavailable - downstream service error")
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<DashboardResponse> getCustomerDashboard(
            @Parameter(description = "Customer ID", required = true, example = "1") @PathVariable Long customerId) {

        log.info("REST API: Get dashboard for customer ID: {}", customerId);
        DashboardResponse dashboard = dashboardService.getCustomerDashboard(customerId);
        return ResponseEntity.ok(dashboard);
    }
}
