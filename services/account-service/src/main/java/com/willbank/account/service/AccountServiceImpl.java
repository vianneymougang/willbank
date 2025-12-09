package com.willbank.account.service;

import com.willbank.account.domain.Account;
import com.willbank.account.domain.AccountRepository;
import com.willbank.account.domain.AccountStatus;
import com.willbank.account.dto.AccountRequest;
import com.willbank.account.dto.AccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final com.willbank.account.security.AuthorizationService authorizationService;
    private final Random random = new Random();

    @Override
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creating new account for customer: {}", request.getCustomerId());

        // Generate unique account number
        String accountNumber = generateAccountNumber();

        // Build account entity
        Account account = Account.builder()
                .customerId(request.getCustomerId())
                .accountNumber(accountNumber)
                .type(request.getType())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .label(request.getLabel())
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully: {}", savedAccount.getAccountNumber());

        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        log.debug("Request to get Account : {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        // Authorization check: ensure user can access this account
        if (!authorizationService.canAccessAccount(account)) {
            throw new RuntimeException("Unauthorized access to account: " + id);
        }

        return mapToResponse(account);
    }

    // ============================================
    // Internal Methods (No Authorization Checks)
    // For service-to-service communication
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByIdInternal(Long id) {
        log.debug("Internal: Get account {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        return mapToResponse(account);
    }

    @Override
    public AccountResponse updateBalanceInternal(Long id, BigDecimal amount) {
        log.info("Internal: Updating balance for account {} by {}", id, amount);
        return updateBalance(id, amount); // Reuse existing method
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalanceInternal(Long id, BigDecimal amount) {
        log.debug("Internal: Checking sufficient balance for account {}, amount: {}", id, amount);
        return hasSufficientBalance(id, amount); // Reuse existing method
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountActiveInternal(Long id) {
        log.debug("Internal: Checking if account {} is active", id);
        return isAccountActive(id); // Reuse existing method
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        log.debug("Fetching all accounts");
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        log.debug("Fetching accounts for customer: {}", customerId);
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        log.info("Updating account: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));

        // Update fields
        if (request.getLabel() != null) {
            account.setLabel(request.getLabel());
        }

        Account updatedAccount = accountRepository.save(account);
        log.info("Account updated successfully: {}", id);

        return mapToResponse(updatedAccount);
    }

    @Override
    public void deleteAccount(Long id) {
        log.info("Deleting account: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));

        // Check if balance is zero before deletion
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Cannot delete account with non-zero balance");
        }

        accountRepository.delete(account);
        log.info("Account deleted successfully: {}", id);
    }

    @Override
    public AccountResponse updateAccountStatus(Long id, AccountStatus status) {
        log.info("Updating account status: {} to {}", id, status);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));

        account.setStatus(status);
        Account updatedAccount = accountRepository.save(account);

        log.info("Account status updated successfully: {}", id);
        return mapToResponse(updatedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(Long id) {
        log.debug("Fetching balance for account: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));
        return account.getBalance();
    }

    /**
     * Generate a unique 16-digit account number
     */
    private String generateAccountNumber() {
        String accountNumber;
        do {
            // Generate format: WB + 14 digits
            long number = 10000000000000L + (long) (random.nextDouble() * 90000000000000L);
            accountNumber = "WB" + number;
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    /**
     * Map Account entity to AccountResponse DTO
     */
    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomerId())
                .accountNumber(account.getAccountNumber())
                .type(account.getType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .label(account.getLabel())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    // ============================================
    // Internal Methods for Transaction Service
    // ============================================

    @Override
    public AccountResponse updateBalance(Long id, BigDecimal amount) {
        log.info("Internal: Updating balance for account {} by {}", id, amount);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));

        // Check account is active
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active. Status: " + account.getStatus());
        }

        BigDecimal newBalance = account.getBalance().add(amount);

        // Prevent negative balance
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "Insufficient balance. Current: " + account.getBalance() + ", Requested: " + amount);
        }

        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);

        log.info("Balance updated successfully. New balance: {}", newBalance);
        return mapToResponse(updatedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(Long id, BigDecimal amount) {
        log.debug("Internal: Checking sufficient balance for account {}, amount: {}", id, amount);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));

        boolean sufficient = account.getBalance().compareTo(amount) >= 0;
        log.debug("Sufficient balance check result: {}", sufficient);

        return sufficient;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountActive(Long id) {
        log.debug("Internal: Checking if account {} is active", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));

        boolean isActive = account.getStatus() == AccountStatus.ACTIVE;
        log.debug("Account active status: {}", isActive);

        return isActive;
    }
}
