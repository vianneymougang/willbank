package com.willbank.userservice.mapper;

import com.willbank.userservice.dto.UserResponse;
import com.willbank.userservice.model.Admin;
import com.willbank.userservice.model.Customer;
import com.willbank.userservice.model.User;

/**
 * Mapper utility for converting between User entities and DTOs.
 */
public class UserMapper {

    private UserMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a User entity to a UserResponse DTO.
     * Excludes the password field for security.
     *
     * @param user the user entity
     * @return UserResponse DTO
     */
    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .status(user.getStatus())
                .role(user.getRole());

        // Add KYC status if user is a Customer
        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            builder.kycStatus(customer.getKycStatus());
            builder.userType("CUSTOMER");
        } else if (user instanceof Admin) {
            builder.userType("ADMIN");
        }

        return builder.build();
    }
}
