package com.willbank.servicetransaction.controller;

import com.willbank.servicetransaction.dto.TransactionRequestDTO;
import com.willbank.servicetransaction.dto.TransactionResponseDTO;
import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import com.willbank.servicetransaction.service.TransactionService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping("/virement-interne")
    public ResponseEntity<TransactionResponseDTO> createVirementInterne(
            @Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.createVirementInterne(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/depot")
    public ResponseEntity<TransactionResponseDTO> createDepot(
            @Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.createDepot(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/retrait")
    public ResponseEntity<TransactionResponseDTO> createRetrait(
            @Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.createRetrait(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistory(
            @PathVariable String accountId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<TransactionResponseDTO>> searchTransactions(
            @RequestParam String accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status) {
        List<TransactionResponseDTO> transactions = 
            transactionService.searchTransactions(accountId, type, status);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
            @RequestParam String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionResponseDTO> transactions = 
            transactionService.getTransactionsByDateRange(accountId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
}
