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
        log.debug("Fetching account by ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));
        return mapToResponse(account);
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
}
