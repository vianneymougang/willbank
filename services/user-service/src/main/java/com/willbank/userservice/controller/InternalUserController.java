package com.willbank.userservice.controller;

import com.willbank.userservice.dto.UserAuthDto;
import com.willbank.userservice.model.User;
import com.willbank.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST Controller for user lookups (used by auth-service)
 * These endpoints should be protected and only accessible internally
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class InternalUserController {

    private final UserRepository userRepository;

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

        User user = userRepository.findByEmail(email)
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
