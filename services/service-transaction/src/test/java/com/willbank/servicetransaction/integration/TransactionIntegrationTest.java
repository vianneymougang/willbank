package com.willbank.servicetransaction.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willbank.servicetransaction.dto.TransactionRequestDTO;
import com.willbank.servicetransaction.entity.Transaction;
import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import com.willbank.servicetransaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class TransactionIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }
    
    @Test
    void createDepot_IntegrationTest() throws Exception {
        TransactionRequestDTO request = TransactionRequestDTO.builder()
            .accountIdSource("ACC001")
            .amount(new BigDecimal("500.00"))
            .type(TransactionType.DEPOT)
            .description("Initial deposit")
            .build();
        
        mockMvc.perform(post("/api/transactions/depot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accountIdSource").value("ACC001"))
            .andExpect(jsonPath("$.amount").value(500.00))
            .andExpect(jsonPath("$.type").value("DEPOT"))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        assertEquals(1, transactionRepository.count());
    }
    
    @Test
    void getTransactionHistory_IntegrationTest() throws Exception {
        Transaction transaction = Transaction.builder()
            .accountIdSource("ACC001")
            .accountIdDestination("ACC002")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.VIREMENT_INTERNE)
            .status(TransactionStatus.COMPLETED)
            .description("Test transfer")
            .createdAt(LocalDateTime.now())
            .completedAt(LocalDateTime.now())
            .build();
        
        transactionRepository.save(transaction);
        
        mockMvc.perform(get("/api/transactions/history/ACC001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountIdSource").value("ACC001"))
            .andExpect(jsonPath("$[0].amount").value(100.00));
    }
    
    @Test
    void searchTransactions_IntegrationTest() throws Exception {
        Transaction transaction1 = Transaction.builder()
            .accountIdSource("ACC001")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.DEPOT)
            .status(TransactionStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        
        Transaction transaction2 = Transaction.builder()
            .accountIdSource("ACC001")
            .amount(new BigDecimal("50.00"))
            .type(TransactionType.RETRAIT)
            .status(TransactionStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        
        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        
        mockMvc.perform(get("/api/transactions/search")
                .param("accountId", "ACC001")
                .param("type", "DEPOT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("DEPOT"));
    }
}
