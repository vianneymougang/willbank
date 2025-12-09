package com.willbank.userservice.controller;

import com.willbank.userservice.dto.UserAuthDto;
import com.willbank.userservice.dto.UserResponse;
import com.willbank.userservice.model.User;
import com.willbank.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST Controller for user lookups (used by other microservices)
 * These endpoints should be protected and only accessible internally
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class InternalUserController {

    private final UserService userService;

    /**
     * Get user by ID - used by composite-service and other services
     * 
     * @param id User ID
     * @return User data
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.debug("Internal API: Get user by ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by email - used by auth-service for authentication
     * This endpoint returns password hash for authentication purposes
     * 
     * @param email User email
     * @return User data including password hash
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserAuthDto> getUserByEmail(@PathVariable String email) {
        log.debug("Internal API: Get user by email: {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Create response including password for auth verification
        UserAuthDto response = UserAuthDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .password(user.getPassword()) // Include password hash for auth-service
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();

        return ResponseEntity.ok(response);
    }
}
