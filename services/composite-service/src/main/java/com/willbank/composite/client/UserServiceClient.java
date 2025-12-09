package com.willbank.composite.client;

import com.willbank.composite.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for User Service
 * Calls internal endpoints that bypass authentication
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User information
     */
    @GetMapping("/internal/users/{id}")
    UserResponse getUserById(@PathVariable Long id);
}
