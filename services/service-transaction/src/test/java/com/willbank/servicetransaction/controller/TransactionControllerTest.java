package com.willbank.servicetransaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willbank.servicetransaction.dto.TransactionRequestDTO;
import com.willbank.servicetransaction.dto.TransactionResponseDTO;
import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import com.willbank.servicetransaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TransactionService transactionService;
    
    private TransactionRequestDTO validRequest;
    private TransactionResponseDTO responseDTO;
    
    @BeforeEach
    void setUp() {
        validRequest = TransactionRequestDTO.builder()
            .accountIdSource("ACC001")
            .accountIdDestination("ACC002")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.VIREMENT_INTERNE)
            .description("Test transfer")
            .build();
        
        responseDTO = TransactionResponseDTO.builder()
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
    void createVirementInterne_Success() throws Exception {
        when(transactionService.createVirementInterne(any(TransactionRequestDTO.class)))
            .thenReturn(responseDTO);
        
        mockMvc.perform(post("/api/transactions/virement-interne")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.accountIdSource").value("ACC001"))
            .andExpect(jsonPath("$.accountIdDestination").value("ACC002"))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void createVirementInterne_InvalidRequest() throws Exception {
        TransactionRequestDTO invalidRequest = TransactionRequestDTO.builder()
            .accountIdSource("")
            .amount(new BigDecimal("-10.00"))
            .build();
        
        mockMvc.perform(post("/api/transactions/virement-interne")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void createDepot_Success() throws Exception {
        TransactionRequestDTO depotRequest = TransactionRequestDTO.builder()
            .accountIdSource("ACC001")
            .amount(new BigDecimal("200.00"))
            .type(TransactionType.DEPOT)
            .build();
        
        TransactionResponseDTO depotResponse = TransactionResponseDTO.builder()
            .id(2L)
            .accountIdSource("ACC001")
            .amount(new BigDecimal("200.00"))
            .type(TransactionType.DEPOT)
            .status(TransactionStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
        
        when(transactionService.createDepot(any(TransactionRequestDTO.class)))
            .thenReturn(depotResponse);
        
        mockMvc.perform(post("/api/transactions/depot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depotRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.type").value("DEPOT"));
    }
    
    @Test
    void getTransactionHistory_Success() throws Exception {
        List<TransactionResponseDTO> transactions = Arrays.asList(responseDTO);
        when(transactionService.getTransactionHistory("ACC001")).thenReturn(transactions);
        
        mockMvc.perform(get("/api/transactions/history/ACC001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountIdSource").value("ACC001"));
    }
    
    @Test
    void searchTransactions_Success() throws Exception {
        List<TransactionResponseDTO> transactions = Arrays.asList(responseDTO);
        when(transactionService.searchTransactions(any(), any(), any()))
            .thenReturn(transactions);
        
        mockMvc.perform(get("/api/transactions/search")
                .param("accountId", "ACC001")
                .param("type", "VIREMENT_INTERNE")
                .param("status", "COMPLETED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("VIREMENT_INTERNE"));
    }
}
