package com.willbank.authservice.client;

import com.willbank.authservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to communicate with user-service
 * Retrieves user information for authentication purposes
 */
@FeignClient(name = "user-service")
public interface UserClient {

    /**
     * Get user by email from user-service internal API
     * 
     * @param email User email
     * @return UserDto containing user information including password hash
     */
    @GetMapping("/internal/users/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}
