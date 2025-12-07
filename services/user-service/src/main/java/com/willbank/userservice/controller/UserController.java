package com.willbank.userservice.controller;

import com.willbank.userservice.dto.UserRegisterDto;
import com.willbank.userservice.dto.UserResponse;
import com.willbank.userservice.dto.UserUpdateDto;
import com.willbank.userservice.model.UserRole;
import com.willbank.userservice.model.UserStatus;
import com.willbank.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user management operations.
 * Handles both Admin and Customer user types.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing WillBank users (Admins and Customers)")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account (Admin or Customer). Passwords are hashed with BCrypt. Customers get KYC status PENDING.")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegisterDto userRegisterDto) {
        log.info("API: Register user request for email: {}", userRegisterDto.getEmail());
        UserResponse response = userService.registerUser(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("API: Get all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("API: Get user by ID: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieves all users with a specific role (ADMIN or CUSTOMER)")
    public ResponseEntity<List<UserResponse>> getUsersByRole(
            @Parameter(description = "User role (ADMIN or CUSTOMER)") @PathVariable UserRole role) {
        log.info("API: Get users by role: {}", role);
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status", description = "Retrieves all users with a specific status (ACTIVE or SUSPENDED)")
    public ResponseEntity<List<UserResponse>> getUsersByStatus(
            @Parameter(description = "User status (ACTIVE or SUSPENDED)") @PathVariable UserStatus status) {
        log.info("API: Get users by status: {}", status);
        List<UserResponse> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user information", description = "Updates user details (name, surname, email, phone, address). Email and phone uniqueness is validated.")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("API: Update user with ID: {}", id);
        UserResponse updated = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Changes user status (ACTIVE or SUSPENDED)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam UserStatus status) {
        log.info("API: Update user status for ID: {} to {}", id, status);
        UserResponse updated = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Permanently deletes a user from the system")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.info("API: Delete user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password-first-login")
    @Operation(summary = "Change password on first login", description = "Allows customers to change their temporary password. This is a PUBLIC endpoint (no authentication required).")
    public ResponseEntity<?> changePasswordFirstLogin(
            @Valid @RequestBody com.willbank.userservice.dto.ChangePasswordRequest request) {
        log.info("Password change request for: {}", request.getEmail());

        try {
            userService.changePasswordFirstLogin(request);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "Password changed successfully",
                    "email", request.getEmail()));
        } catch (RuntimeException e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", e.getMessage()));
        }
    }
}
