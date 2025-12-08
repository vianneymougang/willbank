package com.willbank.transaction.service;

import com.willbank.transaction.domain.Transaction;
import com.willbank.transaction.domain.TransactionStatus;
import com.willbank.transaction.domain.TransactionType;
import com.willbank.transaction.dto.TransactionRequest;
import com.willbank.transaction.dto.TransactionResponse;
import com.willbank.transaction.exception.*;
import com.willbank.transaction.feign.AccountClient;
import com.willbank.transaction.feign.dto.AccountResponse;
import com.willbank.transaction.repository.TransactionRepository;
import com.willbank.transaction.security.AuthorizationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction Service Implementation
 * Handles all transaction business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final AuthorizationService authorizationService;
    private final RabbitTemplate rabbitTemplate;

    @org.springframework.beans.factory.annotation.Value("${transaction.large-amount-threshold:10000.00}")
    private BigDecimal largeAmountThreshold;

    @Override
    @Transactional
    public TransactionResponse processDeposit(TransactionRequest request) {
        log.info("Processing deposit: {} to account {}", request.getAmount(), request.getAccountId());

        // Validate request
        validateDepositRequest(request);

        // Check for duplicate (idempotency)
        if (request.getIdempotencyKey() != null) {
            checkIdempotency(request.getIdempotencyKey());
        }

        // Get account details via Feign
        AccountResponse account = getAccount(request.getAccountId());

        // Validate account is active
        validateAccountActive(account);

        // Validate authorization (customer can only deposit to their own accounts)
        authorizationService.validateAccountAccess(account.getCustomerId());

        BigDecimal balanceBefore = account.getBalance();

        // Create pending transaction
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .balanceBefore(balanceBefore)
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        transaction = transactionRepository.save(transaction);

        try {
            // Update account balance via Feign (add amount)
            AccountResponse updatedAccount = accountClient.updateBalance(
                    request.getAccountId(),
                    request.getAmount());

            // Update transaction as completed
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(updatedAccount.getBalance());
            transaction = transactionRepository.save(transaction);

            log.info("Deposit completed successfully. Transaction ID: {}", transaction.getId());

            // Publish events
            publishTransactionCompletedEvent(transaction, updatedAccount);
            publishLargeTransactionAlertIfNeeded(transaction, updatedAccount);

            return mapToResponse(transaction);

        } catch (FeignException e) {
            log.error("Failed to update balance via Feign: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Failed to update account balance: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Failed to process deposit", e);
        }
    }

    @Override
    @Transactional
    public TransactionResponse processWithdrawal(TransactionRequest request) {
        log.info("Processing withdrawal: {} from account {}", request.getAmount(), request.getAccountId());

        // Validate request
        validateWithdrawalRequest(request);

        // Check for duplicate (idempotency)
        if (request.getIdempotencyKey() != null) {
            checkIdempotency(request.getIdempotencyKey());
        }

        // Get account details
        AccountResponse account = getAccount(request.getAccountId());

        // Validate account is active
        validateAccountActive(account);

        // Validate authorization
        authorizationService.validateAccountAccess(account.getCustomerId());

        // Check sufficient balance
        if (!accountClient.hasSufficientBalance(request.getAccountId(), request.getAmount())) {
            throw new InsufficientBalanceException(
                    request.getAccountId(),
                    request.getAmount(),
                    account.getBalance());
        }

        BigDecimal balanceBefore = account.getBalance();

        // Create pending transaction
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .balanceBefore(balanceBefore)
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        transaction = transactionRepository.save(transaction);

        try {
            // Update account balance (subtract amount)
            AccountResponse updatedAccount = accountClient.updateBalance(
                    request.getAccountId(),
                    request.getAmount().negate() // Negative for withdrawal
            );

            // Update transaction as completed
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setBalanceAfter(updatedAccount.getBalance());
            transaction = transactionRepository.save(transaction);

            log.info("Withdrawal completed successfully. Transaction ID: {}", transaction.getId());

            // Publish events
            publishTransactionCompletedEvent(transaction, updatedAccount);
            publishLargeTransactionAlertIfNeeded(transaction, updatedAccount);

            return mapToResponse(transaction);

        } catch (FeignException e) {
            log.error("Failed to process withdrawal: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Failed to update account balance: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Failed to process withdrawal", e);
        }
    }

    @Override
    @Transactional
    public TransactionResponse processTransfer(TransactionRequest request) {
        log.info("Processing transfer: {} from account {} to account {}",
                request.getAmount(), request.getAccountId(), request.getDestinationAccountId());

        // Validate request
        validateTransferRequest(request);

        // Check for duplicate
        if (request.getIdempotencyKey() != null) {
            checkIdempotency(request.getIdempotencyKey());
        }

        // Validate source and destination are different
        if (request.getAccountId().equals(request.getDestinationAccountId())) {
            throw new InvalidTransactionException("Source and destination accounts cannot be the same");
        }

        // Get source account
        AccountResponse sourceAccount = getAccount(request.getAccountId());
        validateAccountActive(sourceAccount);
        authorizationService.validateAccountAccess(sourceAccount.getCustomerId());

        // Get destination account
        AccountResponse destinationAccount = getAccount(request.getDestinationAccountId());
        validateAccountActive(destinationAccount);

        // Check sufficient balance
        if (!accountClient.hasSufficientBalance(request.getAccountId(), request.getAmount())) {
            throw new InsufficientBalanceException(
                    request.getAccountId(),
                    request.getAmount(),
                    sourceAccount.getBalance());
        }

        BigDecimal sourceBalanceBefore = sourceAccount.getBalance();

        // Create pending transaction
        Transaction transaction = Transaction.builder()
                .accountId(request.getAccountId())
                .destinationAccountId(request.getDestinationAccountId())
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .balanceBefore(sourceBalanceBefore)
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        transaction = transactionRepository.save(transaction);

        try {
            // Step 1: Debit source account
            AccountResponse updatedSourceAccount = accountClient.updateBalance(
                    request.getAccountId(),
                    request.getAmount().negate());

            log.debug("Source account debited. New balance: {}", updatedSourceAccount.getBalance());

            try {
                // Step 2: Credit destination account
                AccountResponse updatedDestinationAccount = accountClient.updateBalance(
                        request.getDestinationAccountId(),
                        request.getAmount());

                log.debug("Destination account credited. New balance: {}", updatedDestinationAccount.getBalance());

                // Update transaction as completed
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setBalanceAfter(updatedSourceAccount.getBalance());
                transaction = transactionRepository.save(transaction);

                log.info("Transfer completed successfully. Transaction ID: {}", transaction.getId());

                // Publish events
                publishTransactionCompletedEvent(transaction, updatedSourceAccount);
                publishLargeTransactionAlertIfNeeded(transaction, updatedSourceAccount);

                return mapToResponse(transaction);

            } catch (FeignException e) {
                // Rollback: Credit back source account
                log.error("Failed to credit destination account. Rolling back source debit: {}", e.getMessage());
                try {
                    accountClient.updateBalance(request.getAccountId(), request.getAmount());
                    log.info("Rollback successful: source account credited back");
                } catch (FeignException rollbackException) {
                    log.error("CRITICAL: Rollback failed! Manual intervention required: {}",
                            rollbackException.getMessage());
                }

                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason("Failed to credit destination account (rollback executed)");
                transactionRepository.save(transaction);

                throw new RuntimeException("Transfer failed: could not credit destination account", e);
            }

        } catch (FeignException e) {
            log.error("Failed to debit source account: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason("Failed to debit source account: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Failed to process transfer", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        log.debug("Fetching transaction by ID: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        log.debug("Fetching transactions for account: {}", accountId);

        // Validate access to account
        AccountResponse account = getAccount(accountId);
        authorizationService.validateAccountAccess(account.getCustomerId());

        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        log.debug("Fetching paginated transactions for account: {}", accountId);

        // Validate access
        AccountResponse account = getAccount(accountId);
        authorizationService.validateAccountAccess(account.getCustomerId());

        return transactionRepository.findByAccountId(accountId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsForAccount(Long accountId) {
        log.debug("Fetching all transactions (including transfers) for account: {}", accountId);

        // Validate access
        AccountResponse account = getAccount(accountId);
        authorizationService.validateAccountAccess(account.getCustomerId());

        return transactionRepository.findByAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(accountId, accountId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Fetching transactions for account {} between {} and {}", accountId, startDate, endDate);

        // Validate access
        AccountResponse account = getAccount(accountId);
        authorizationService.validateAccountAccess(account.getCustomerId());

        return transactionRepository.findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                accountId, startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // Private Helper Methods
    // ============================================

    private void validateDepositRequest(TransactionRequest request) {
        if (request.getAccountId() == null) {
            throw new InvalidTransactionException("Account ID is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
        if (request.getType() != TransactionType.DEPOSIT) {
            throw new InvalidTransactionException("Invalid transaction type for deposit");
        }
    }

    private void validateWithdrawalRequest(TransactionRequest request) {
        if (request.getAccountId() == null) {
            throw new InvalidTransactionException("Account ID is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
        if (request.getType() != TransactionType.WITHDRAWAL) {
            throw new InvalidTransactionException("Invalid transaction type for withdrawal");
        }
    }

    private void validateTransferRequest(TransactionRequest request) {
        if (request.getAccountId() == null) {
            throw new InvalidTransactionException("Source account ID is required");
        }
        if (request.getDestinationAccountId() == null) {
            throw new InvalidTransactionException("Destination account ID is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
        if (request.getType() != TransactionType.TRANSFER) {
            throw new InvalidTransactionException("Invalid transaction type for transfer");
        }
    }

    private void checkIdempotency(String idempotencyKey) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Duplicate transaction detected with idempotency key: {}", idempotencyKey);
            throw DuplicateTransactionException.forIdempotencyKey(idempotencyKey);
        }
    }

    private AccountResponse getAccount(Long accountId) {
        try {
            return accountClient.getAccountById(accountId);
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException(accountId);
        } catch (FeignException e) {
            log.error("Error fetching account {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Error communicating with account service", e);
        }
    }

    private void validateAccountActive(AccountResponse account) {
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountNotActiveException(account.getId(), account.getStatus());
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    // ============================================
    // Event Publishing Methods
    // ============================================

    private void publishTransactionCompletedEvent(Transaction transaction, AccountResponse account) {
        // Get customer email and first name from security context
        String customerEmail = authorizationService.getCurrentUserEmail();
        String customerFirstName = authorizationService.getCurrentUserFirstName();

        com.willbank.transaction.event.TransactionCompletedEvent event = com.willbank.transaction.event.TransactionCompletedEvent
                .builder()
                .transactionId(transaction.getId())
                .accountId(transaction.getAccountId())
                .destinationAccountId(transaction.getDestinationAccountId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .newBalance(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .completedAt(transaction.getUpdatedAt())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getType()) // SAVINGS, CHECKING, etc.
                .customerId(account.getCustomerId())
                .customerEmail(customerEmail)
                .customerFirstName(customerFirstName)
                .build();

        log.info("📤 Publishing TransactionCompletedEvent for transaction {} to {} ({})",
                transaction.getId(), customerEmail, customerFirstName);
        rabbitTemplate.convertAndSend(
                com.willbank.transaction.config.RabbitMQConfig.TRANSACTION_EXCHANGE,
                com.willbank.transaction.config.RabbitMQConfig.TRANSACTION_COMPLETED_ROUTING_KEY,
                event);
    }

    private void publishLargeTransactionAlertIfNeeded(Transaction transaction, AccountResponse account) {
        if (transaction.getAmount().compareTo(largeAmountThreshold) > 0) {
            com.willbank.transaction.event.LargeTransactionAlertEvent event = com.willbank.transaction.event.LargeTransactionAlertEvent
                    .builder()
                    .transactionId(transaction.getId())
                    .accountId(transaction.getAccountId())
                    .type(transaction.getType())
                    .amount(transaction.getAmount())
                    .threshold(largeAmountThreshold)
                    .occurredAt(transaction.getCreatedAt())
                    .accountNumber(account.getAccountNumber())
                    .customerId(account.getCustomerId())
                    .build();

            log.warn("🚨 Publishing LargeTransactionAlertEvent for transaction {}: amount {} exceeds threshold {}",
                    transaction.getId(), transaction.getAmount(), largeAmountThreshold);
            rabbitTemplate.convertAndSend(
                    com.willbank.transaction.config.RabbitMQConfig.TRANSACTION_EXCHANGE,
                    com.willbank.transaction.config.RabbitMQConfig.LARGE_TRANSACTION_ALERT_ROUTING_KEY,
                    event);
        }
    }
}
