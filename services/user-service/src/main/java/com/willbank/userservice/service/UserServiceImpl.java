package com.willbank.userservice.service;

import com.willbank.userservice.dto.UserRegisterDto;
import com.willbank.userservice.dto.UserResponse;
import com.willbank.userservice.dto.UserUpdateDto;
import com.willbank.userservice.mapper.UserMapper;
import com.willbank.userservice.model.*;
import com.willbank.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserService with business logic for user management.
 * Includes BCrypt password hashing for security.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserResponse registerUser(UserRegisterDto dto) {
        log.info("Registering new user with email: {}", dto.getEmail());

        // Validate unique constraints
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("Email already exists: {}", dto.getEmail());
            throw new RuntimeException("Email already in use: " + dto.getEmail());
        }

        if (userRepository.existsByPhone(dto.getPhone())) {
            log.error("Phone number already exists: {}", dto.getPhone());
            throw new RuntimeException("Phone number already in use: " + dto.getPhone());
        }

        // Create appropriate user type based on role
        User user;
        if (dto.getRole() == UserRole.ADMIN) {
            user = new Admin();
            log.debug("Creating Admin user");
        } else if (dto.getRole() == UserRole.CUSTOMER) {
            user = new Customer();
            log.debug("Creating Customer user");
        } else {
            throw new RuntimeException("Invalid user role: " + dto.getRole());
        }

        // Set common user properties
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(dto.getRole());

        // Hash password with BCrypt
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(hashedPassword);
        log.debug("Password hashed successfully");

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return UserMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.debug("Fetching users with role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        log.debug("Fetching users with status: {}", status);
        return userRepository.findByStatus(status).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateDto dto) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Update only provided fields
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getSurname() != null) {
            user.setSurname(dto.getSurname());
        }
        if (dto.getEmail() != null) {
            // Check if new email is already in use by another user
            if (!dto.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email already in use: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            // Check if new phone is already in use by another user
            if (!dto.getPhone().equals(user.getPhone()) && userRepository.existsByPhone(dto.getPhone())) {
                throw new RuntimeException("Phone number already in use: " + dto.getPhone());
            }
            user.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", id);

        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserStatus(Long id, UserStatus status) {
        log.info("Updating user status for ID: {} to {}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);

        log.info("User status updated successfully");
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", userId);
    }
}
