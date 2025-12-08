package com.willbank.transaction.repository;

import com.willbank.transaction.domain.Transaction;
import com.willbank.transaction.domain.TransactionStatus;
import com.willbank.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TransactionRepository
 * Uses H2 in-memory database
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        // Create test transactions
        transaction1 = Transaction.builder()
                .accountId(1L)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.COMPLETED)
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(new BigDecimal("100.00"))
                .description("Test deposit")
                .build();

        transaction2 = Transaction.builder()
                .accountId(1L)
                .destinationAccountId(2L)
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("50.00"))
                .status(TransactionStatus.COMPLETED)
                .balanceBefore(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("50.00"))
                .description("Test transfer")
                .build();

        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.flush();
    }

    @Test
    void findByAccountIdOrderByCreatedAtDesc_ShouldReturnTransactionsInDescendingOrder() {
        // When
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L);

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getCreatedAt()).isAfterOrEqualTo(transactions.get(1).getCreatedAt());
    }

    @Test
    void findByIdempotencyKey_ShouldReturnTransaction_WhenExists() {
        // Given
        String idempotencyKey = "test-key-123";
        Transaction transaction = Transaction.builder()
                .accountId(3L)
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("25.00"))
                .status(TransactionStatus.COMPLETED)
                .idempotencyKey(idempotencyKey)
                .build();
        entityManager.persist(transaction);
        entityManager.flush();

        // When
        Optional<Transaction> found = transactionRepository.findByIdempotencyKey(idempotencyKey);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getIdempotencyKey()).isEqualTo(idempotencyKey);
    }

    @Test
    void findByIdempotencyKey_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<Transaction> found = transactionRepository.findByIdempotencyKey("non-existent-key");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByAccountIdAndStatus_ShouldFilterByStatus() {
        // Given
        Transaction pendingTransaction = Transaction.builder()
                .accountId(1L)
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("10.00"))
                .status(TransactionStatus.PENDING)
                .build();
        entityManager.persist(pendingTransaction);
        entityManager.flush();

        // When
        List<Transaction> completedTransactions = transactionRepository
                .findByAccountIdAndStatus(1L, TransactionStatus.COMPLETED);
        List<Transaction> pendingTransactions = transactionRepository
                .findByAccountIdAndStatus(1L, TransactionStatus.PENDING);

        // Then
        assertThat(completedTransactions).hasSize(2);
        assertThat(pendingTransactions).hasSize(1);
    }

    @Test
    void findByAccountIdAndType_ShouldFilterByType() {
        // When
        List<Transaction> deposits = transactionRepository
                .findByAccountIdAndType(1L, TransactionType.DEPOSIT);
        List<Transaction> transfers = transactionRepository
                .findByAccountIdAndType(1L, TransactionType.TRANSFER);

        // Then
        assertThat(deposits).hasSize(1);
        assertThat(transfers).hasSize(1);
    }

    @Test
    void existsByIdempotencyKey_ShouldReturnTrue_WhenExists() {
        // Given
        String key = "exists-key";
        Transaction transaction = Transaction.builder()
                .accountId(4L)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.COMPLETED)
                .idempotencyKey(key)
                .build();
        entityManager.persist(transaction);
        entityManager.flush();

        // When & Then
        assertThat(transactionRepository.existsByIdempotencyKey(key)).isTrue();
        assertThat(transactionRepository.existsByIdempotencyKey("non-existent")).isFalse();
    }

    @Test
    void findByAccountIdAndCreatedAtBetween_ShouldFilterByDateRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // When
        List<Transaction> transactions = transactionRepository
                .findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(1L, start, end);

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).allMatch(t -> t.getCreatedAt().isAfter(start) && t.getCreatedAt().isBefore(end));
    }

    @Test
    void findByAccountIdOrDestinationAccountId_ShouldIncludeTransfersInBothDirections() {
        // Given
        Transaction incomingTransfer = Transaction.builder()
                .accountId(5L)
                .destinationAccountId(1L) // Transfer TO account 1
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("75.00"))
                .status(TransactionStatus.COMPLETED)
                .build();
        entityManager.persist(incomingTransfer);
        entityManager.flush();

        // When
        List<Transaction> allTransactions = transactionRepository
                .findByAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(1L, 1L);

        // Then
        // Should find: 2 from account 1 + 1 incoming to account 1 = 3
        assertThat(allTransactions).hasSize(3);
    }
}
