package com.willbank.account.security;

import com.willbank.account.domain.Account;

public interface AuthorizationService {

    boolean isAdmin();

    boolean isCustomer();

    Long getCurrentUserId();

    String getCurrentUserEmail();

    boolean canAccessAccount(Account account);

    void validateAccountCreation(Long targetCustomerId);
}
