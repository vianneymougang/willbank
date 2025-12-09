package com.willbank.userservice.service;

import com.willbank.userservice.dto.UserRegisterDto;
import com.willbank.userservice.dto.UserResponse;
import com.willbank.userservice.dto.UserUpdateDto;
import com.willbank.userservice.model.UserRole;
import com.willbank.userservice.model.UserStatus;

import java.util.List;

public interface UserService {

    UserResponse registerUser(UserRegisterDto userRegisterDto);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(UserRole role);

    List<UserResponse> getUsersByStatus(UserStatus status);

    UserResponse updateUser(Long id, UserUpdateDto userUpdateDto);

    UserResponse updateUserStatus(Long id, UserStatus status);

    void deleteUser(Long userId);

    void changePasswordFirstLogin(com.willbank.userservice.dto.ChangePasswordRequest request);

    /**
     * Find user by email address
     * Used internally for authentication
     * 
     * @param email User email
     * @return User entity (not DTO) for internal use
     */
    java.util.Optional<com.willbank.userservice.model.User> findByEmail(String email);
}
