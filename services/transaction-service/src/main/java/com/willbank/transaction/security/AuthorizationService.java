package com.willbank.transaction.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Authorization Service
 * Provides helper methods to check user permissions and access rights
 */
@Service
@Slf4j
public class AuthorizationService {

    /**
     * Get the current authenticated user's ID
     * 
     * @return User ID from security context
     * @throws RuntimeException if user not authenticated
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object details = authentication.getDetails();
        if (details instanceof String) {
            try {
                return Long.parseLong((String) details);
            } catch (NumberFormatException e) {
                log.error("Invalid user ID format: {}", details);
                throw new RuntimeException("Invalid user ID format");
            }
        }

        throw new RuntimeException("User ID not found in authentication context");
    }

    /**
     * Get the current authenticated user's email
     * 
     * @return User email
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return (String) authentication.getPrincipal();
    }

    /**
     * Get the current authenticated user's role
     * 
     * @return User role (ADMIN or CUSTOMER)
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        return authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");
    }

    /**
     * Check if current user is an admin
     * 
     * @return true if user has ADMIN role
     */
    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    /**
     * Check if current user is a customer
     * 
     * @return true if user has CUSTOMER role
     */
    public boolean isCustomer() {
        return "CUSTOMER".equals(getCurrentUserRole());
    }

    /**
     * Validate that customer can only create transactions for their own account
     * Admins can create for any account
     * 
     * @param accountCustomerId Customer ID who owns the account
     * @throws RuntimeException if customer tries to access another customer's
     *                          account
     */
    public void validateAccountAccess(Long accountCustomerId) {
        if (isAdmin()) {
            return; // Admins can access any account
        }

        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(accountCustomerId)) {
            log.warn("User {} attempted to access account belonging to user {}", currentUserId, accountCustomerId);
            throw new RuntimeException("Access denied: You can only manage your own accounts");
        }
    }

    /**
     * Get the current authenticated user's first name
     * Temporary solution: extracts from email (should be in JWT in production)
     * 
     * @return User first name or "Client" if cannot be determined
     */
    public String getCurrentUserFirstName() {
        try {
            String email = getCurrentUserEmail();
            if (email != null && email.contains("@")) {
                // Extract first part of email and capitalize
                String localPart = email.substring(0, email.indexOf("@"));
                if (localPart.contains(".")) {
                    // If email is firstname.lastname@domain, take firstname
                    String firstName = localPart.substring(0, localPart.indexOf("."));
                    return capitalizeFirst(firstName);
                }
                return capitalizeFirst(localPart);
            }
            return "Client";
        } catch (Exception e) {
            log.warn("Could not extract first name from email: {}", e.getMessage());
            return "Client";
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
