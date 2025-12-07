package com.willbank.userservice.controller;

import com.willbank.userservice.audit.AuditAction;
import com.willbank.userservice.audit.AuditService;
import com.willbank.userservice.dto.CreateCustomerRequest;
import com.willbank.userservice.dto.CreateCustomerResponse;
import com.willbank.userservice.event.CustomerCreatedEvent;
import com.willbank.userservice.event.EventPublisher;
import com.willbank.userservice.model.Customer;
import com.willbank.userservice.model.User;
import com.willbank.userservice.model.UserRole;
import com.willbank.userservice.repository.UserRepository;
import com.willbank.userservice.security.RateLimitingService;
import com.willbank.userservice.service.TemporaryPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Admin-only endpoints for user management
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - User Management", description = "Admin endpoints for managing users")
public class AdminController {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final TemporaryPasswordService temporaryPasswordService;
        private final com.willbank.userservice.service.PasswordGenerationService passwordGenerationService;
        private final EventPublisher eventPublisher;
        private final AuditService auditService;
        private final RateLimitingService rateLimitingService;

        /**
         * Create a new customer with auto-generated password
         * Rate limited: 10 per hour per admin
         */
        @PostMapping("/customers")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create customer", description = "Creates a new customer with auto-generated temporary password")
        public ResponseEntity<?> createCustomer(@Valid @RequestBody CreateCustomerRequest request,
                        HttpServletRequest httpRequest) {

                // Extract admin info from headers
                String adminIdHeader = httpRequest.getHeader("X-User-Id");
                String adminEmail = httpRequest.getHeader("X-User-Email");
                String adminRole = httpRequest.getHeader("X-User-Role");

                if (adminIdHeader == null || adminEmail == null) {
                        log.error("Missing authentication headers");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing authentication");
                }

                Long adminId = Long.parseLong(adminIdHeader);

                // Rate limiting check
                if (!rateLimitingService.allowAdminCreation(adminId)) {
                        long retryAfter = rateLimitingService.getAdminCreationRetryAfter(adminId);

                        auditService.logAction(adminId, adminEmail, adminRole,
                                        AuditAction.USER_CREATED, "Customer", null,
                                        "Rate limit exceeded for customer creation",
                                        auditService.getClientIp(httpRequest), false);

                        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                        .header("Retry-After", String.valueOf(retryAfter))
                                        .body("Rate limit exceeded. Try again in " + retryAfter + " seconds");
                }

                // Check if email already exists
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        auditService.logAction(adminId, adminEmail, adminRole,
                                        AuditAction.USER_CREATED, "Customer", null,
                                        "Failed: Email already exists - " + request.getEmail(),
                                        auditService.getClientIp(httpRequest), false);

                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("Email already registered");
                }

                // Generate secure temporary password FIRST
                String temporaryPassword = passwordGenerationService.generateSecurePassword();

                // Create customer with the hashed password
                Customer customer = new Customer();
                customer.setName(request.getName());
                customer.setSurname(request.getSurname());
                customer.setEmail(request.getEmail());
                customer.setPhone(request.getPhone());
                customer.setAddress(request.getAddress());
                customer.setPassword(passwordEncoder.encode(temporaryPassword)); // Set password before saving
                customer.setRole(UserRole.CUSTOMER);

                // Save customer (to get ID)
                customer = (Customer) userRepository.save(customer);

                // Store the SAME temporary password in encrypted form (for verification)
                temporaryPasswordService.storePassword(customer.getId(), temporaryPassword);

                log.info("Admin {} created customer: {} (ID: {})", adminEmail, customer.getEmail(), customer.getId());

                // Publish event for notification service (stub for now)
                CustomerCreatedEvent event = CustomerCreatedEvent.builder()
                                .customerId(customer.getId())
                                .email(customer.getEmail())
                                .name(customer.getName())
                                .surname(customer.getSurname())
                                .temporaryPassword(temporaryPassword)
                                .createdAt(LocalDateTime.now())
                                .build();

                eventPublisher.publishCustomerCreated(event);

                // Audit log
                auditService.logAction(adminId, adminEmail, adminRole,
                                AuditAction.USER_CREATED, "Customer", customer.getId(),
                                "Created customer: " + customer.getEmail(),
                                auditService.getClientIp(httpRequest), true);

                // Return response with temporary password
                CreateCustomerResponse response = CreateCustomerResponse.builder()
                                .customerId(customer.getId())
                                .email(customer.getEmail())
                                .name(customer.getName())
                                .surname(customer.getSurname())
                                .temporaryPassword(temporaryPassword)
                                .message("Customer created successfully. Temporary password will be sent via email.")
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Reset password for a customer (generates new temporary password)
         */
        @PostMapping("/{userId}/reset-password")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Reset customer password", description = "Resets customer password and generates new temporary password")
        public ResponseEntity<?> resetPassword(@PathVariable Long userId,
                        HttpServletRequest httpRequest) {

                String adminIdHeader = httpRequest.getHeader("X-User-Id");
                String adminEmail = httpRequest.getHeader("X-User-Email");
                String adminRole = httpRequest.getHeader("X-User-Role");
                Long adminId = Long.parseLong(adminIdHeader);

                // Find user
                User user = userRepository.findById(userId)
                                .orElse(null);

                if (user == null) {
                        auditService.logAction(adminId, adminEmail, adminRole,
                                        AuditAction.PASSWORD_RESET, "User", userId,
                                        "Failed: User not found",
                                        auditService.getClientIp(httpRequest), false);

                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }

                // Generate new temporary password
                String newTempPassword = temporaryPasswordService.createTemporaryPassword(userId);

                // Update user's password
                user.setPassword(passwordEncoder.encode(newTempPassword));
                userRepository.save(user);

                log.info("Admin {} reset password for user {} (ID: {})", adminEmail, user.getEmail(), userId);

                // Audit log
                auditService.logAction(adminId, adminEmail, adminRole,
                                AuditAction.PASSWORD_RESET, "User", userId,
                                "Reset password for user: " + user.getEmail(),
                                auditService.getClientIp(httpRequest), true);

                // Return new temporary password
                return ResponseEntity.ok(CreateCustomerResponse.builder()
                                .customerId(userId)
                                .email(user.getEmail())
                                .temporaryPassword(newTempPassword)
                                .message("Password reset successfully. New temporary password generated.")
                                .build());
        }
}
