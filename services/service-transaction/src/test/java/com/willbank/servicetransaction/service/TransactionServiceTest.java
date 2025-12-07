package com.willbank.servicetransaction.service;

import com.willbank.servicetransaction.client.AccountServiceClient;
import com.willbank.servicetransaction.dto.TransactionRequestDTO;
import com.willbank.servicetransaction.dto.TransactionResponseDTO;
import com.willbank.servicetransaction.entity.Transaction;
import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import com.willbank.servicetransaction.exception.InsufficientBalanceException;
import com.willbank.servicetransaction.exception.InvalidTransactionException;
import com.willbank.servicetransaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private AccountServiceClient accountServiceClient;
    
    @Mock
    private RabbitMQProducerService rabbitMQProducerService;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private TransactionRequestDTO validVirementRequest;
    private Transaction savedTransaction;
    
    @BeforeEach
    void setUp() {
        validVirementRequest = TransactionRequestDTO.builder()
            .accountIdSource("ACC001")
            .accountIdDestination("ACC002")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.VIREMENT_INTERNE)
            .description("Test transfer")
            .build();
        
        savedTransaction = Transaction.builder()
            .id(1L)
            .accountIdSource("ACC001")
            .accountIdDestination("ACC002")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.VIREMENT_INTERNE)
            .status(TransactionStatus.COMPLETED)
            .description("Test transfer")
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    void createVirementInterne_Success() {
        when(accountServiceClient.getAccountBalance("ACC001")).thenReturn(new BigDecimal("500.00"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        
        TransactionResponseDTO result = transactionService.createVirementInterne(validVirementRequest);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ACC001", result.getAccountIdSource());
        assertEquals("ACC002", result.getAccountIdDestination());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(rabbitMQProducerService, times(1)).sendTransactionEvent(any());
    }
    
    @Test
    void createVirementInterne_InsufficientBalance() {
        when(accountServiceClient.getAccountBalance("ACC001")).thenReturn(new BigDecimal("50.00"));
        
        assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.createVirementInterne(validVirementRequest);
        });
        
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void createVirementInterne_SameSourceAndDestination() {
        TransactionRequestDTO invalidRequest = TransactionRequestDTO.builder()
            .accountIdSource("ACC001")
            .accountIdDestination("ACC001")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.VIREMENT_INTERNE)
            .build();
        
        assertThrows(InvalidTransactionException.class, () -> {
            transactionService.createVirementInterne(invalidRequest);
        });
    }
    
    @Test
    void createDepot_Success() {
        TransactionRequestDTO depotRequest = TransactionRequestDTO.builder()
            .accountIdSource("ACC001")
            .amount(new BigDecimal("200.00"))
            .type(TransactionType.DEPOT)
            .description("Cash deposit")
            .build();
        
        Transaction depotTransaction = Transaction.builder()
            .id(2L)
            .accountIdSource("ACC001")
            .amount(new BigDecimal("200.00"))
            .type(TransactionType.DEPOT)
            .status(TransactionStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(depotTransaction);
        
        TransactionResponseDTO result = transactionService.createDepot(depotRequest);
        
        assertNotNull(result);
        assertEquals(TransactionType.DEPOT, result.getType());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
        
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
    
    @Test
    void createRetrait_Success() {
        TransactionRequestDTO retraitRequest = TransactionRequestDTO.builder()
            .accountIdSource("ACC001")
            .amount(new BigDecimal("50.00"))
            .type(TransactionType.RETRAIT)
            .description("ATM withdrawal")
            .build();
        
        when(accountServiceClient.getAccountBalance("ACC001")).thenReturn(new BigDecimal("500.00"));
        
        Transaction retraitTransaction = Transaction.builder()
            .id(3L)
            .accountIdSource("ACC001")
            .amount(new BigDecimal("50.00"))
            .type(TransactionType.RETRAIT)
            .status(TransactionStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(retraitTransaction);
        
        TransactionResponseDTO result = transactionService.createRetrait(retraitRequest);
        
        assertNotNull(result);
        assertEquals(TransactionType.RETRAIT, result.getType());
        
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
    
    @Test
    void getTransactionHistory_Success() {
        List<Transaction> transactions = Arrays.asList(savedTransaction);
        when(transactionRepository.findByAccountIdSourceOrAccountIdDestination("ACC001", "ACC001"))
            .thenReturn(transactions);
        
        List<TransactionResponseDTO> result = transactionService.getTransactionHistory("ACC001");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACC001", result.get(0).getAccountIdSource());
    }
    
    @Test
    void searchTransactions_WithFilters() {
        List<Transaction> transactions = Arrays.asList(savedTransaction);
        when(transactionRepository.findByAccountIdWithFilters(
            "ACC001", TransactionType.VIREMENT_INTERNE, TransactionStatus.COMPLETED))
            .thenReturn(transactions);
        
        List<TransactionResponseDTO> result = transactionService.searchTransactions(
            "ACC001", TransactionType.VIREMENT_INTERNE, TransactionStatus.COMPLETED);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
