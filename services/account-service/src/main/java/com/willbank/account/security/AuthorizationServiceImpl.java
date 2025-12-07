package com.willbank.account.security;

import com.willbank.account.domain.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for authorization checks in account-service
 * Enforces customer account isolation and admin privileges
 */
@Service
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    /**
     * Checks if the current user is an ADMIN
     */
    @Override
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Checks if the current user is a CUSTOMER
     */
    @Override
    public boolean isCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
    }

    /**
     * Gets the authenticated user's ID from the security context
     */
    @Override
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            throw new RuntimeException("No authenticated user");
        }
        return Long.valueOf(auth.getDetails().toString());
    }

    /**
     * Gets the authenticated user's email
     */
    @Override
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("No authenticated user");
        }
        return auth.getPrincipal().toString();
    }

    /**
     * Checks if the current user owns the given account
     * Admins can access all accounts
     */
    @Override
    public boolean canAccessAccount(Account account) {
        if (isAdmin()) {
            log.debug("Admin user accessing account: {}", account.getId());
            return true;
        }

        Long currentUserId = getCurrentUserId();
        boolean isOwner = account.getCustomerId().equals(currentUserId);

        if (!isOwner) {
            log.warn("User {} attempted to access account {} owned by {}",
                    currentUserId, account.getId(), account.getCustomerId());
        }

        return isOwner;
    }

    /**
     * Validates that a customer can only create accounts for themselves
     * Admins can create accounts for any customer
     */
    @Override
    public void validateAccountCreation(Long targetCustomerId) {
        if (isAdmin()) {
            log.debug("Admin creating account for customer: {}", targetCustomerId);
            return;
        }

        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(targetCustomerId)) {
            log.error("Customer {} attempted to create account for customer {}",
                    currentUserId, targetCustomerId);
            throw new RuntimeException("Customers can only create accounts for themselves");
        }
    }
}
