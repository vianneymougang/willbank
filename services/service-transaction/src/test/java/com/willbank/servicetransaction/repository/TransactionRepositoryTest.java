package com.willbank.servicetransaction.repository;

import com.willbank.servicetransaction.entity.Transaction;
import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TransactionRepositoryTest {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    private Transaction transaction1;
    private Transaction transaction2;
    
    @BeforeEach
    void setUp() {
        transaction1 = Transaction.builder()
            .accountIdSource("ACC001")
            .accountIdDestination("ACC002")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.VIREMENT_INTERNE)
            .status(TransactionStatus.COMPLETED)
            .description("Test transfer 1")
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
        
        transaction2 = Transaction.builder()
            .accountIdSource("ACC002")
            .accountIdDestination("ACC003")
            .amount(new BigDecimal("200.00"))
            .type(TransactionType.VIREMENT_INTERNE)
            .status(TransactionStatus.PENDING)
            .description("Test transfer 2")
            .createdAt(LocalDateTime.now())
            .build();
        
        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
    }
    
    @Test
    void findByAccountIdSourceOrAccountIdDestination_Success() {
        List<Transaction> transactions = transactionRepository
            .findByAccountIdSourceOrAccountIdDestination("ACC001", "ACC001");
        
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("ACC001", transactions.get(0).getAccountIdSource());
    }
    
    @Test
    void findByAccountIdSource_Success() {
        List<Transaction> transactions = transactionRepository.findByAccountIdSource("ACC002");
        
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("ACC002", transactions.get(0).getAccountIdSource());
    }
    
    @Test
    void findByStatus_Success() {
        List<Transaction> completedTransactions = transactionRepository
            .findByStatus(TransactionStatus.COMPLETED);
        
        assertNotNull(completedTransactions);
        assertEquals(1, completedTransactions.size());
        assertEquals(TransactionStatus.COMPLETED, completedTransactions.get(0).getStatus());
    }
    
    @Test
    void findByType_Success() {
        List<Transaction> virementTransactions = transactionRepository
            .findByType(TransactionType.VIREMENT_INTERNE);
        
        assertNotNull(virementTransactions);
        assertEquals(2, virementTransactions.size());
    }
    
    @Test
    void findByAccountIdWithFilters_Success() {
        List<Transaction> transactions = transactionRepository.findByAccountIdWithFilters(
            "ACC001", TransactionType.VIREMENT_INTERNE, TransactionStatus.COMPLETED);
        
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("ACC001", transactions.get(0).getAccountIdSource());
        assertEquals(TransactionStatus.COMPLETED, transactions.get(0).getStatus());
    }
    
    @Test
    void findByAccountIdAndDateRange_Success() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(
            "ACC001", startDate, endDate);
        
        assertNotNull(transactions);
        assertTrue(transactions.size() > 0);
    }
}
