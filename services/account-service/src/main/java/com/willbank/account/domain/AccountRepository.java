package com.willbank.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Find all accounts for a specific customer
     */
    List<Account> findByCustomerId(Long customerId);
    
    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);
    
    /**
     * Find all accounts by status
     */
    List<Account> findByStatus(AccountStatus status);
    
    /**
     * Find all accounts by type
     */
    List<Account> findByType(AccountType type);
    
    /**
     * Check if account number already exists
     */
    boolean existsByAccountNumber(String accountNumber);
}
