package com.willbank.servicetransaction.service;

import com.willbank.servicetransaction.client.AccountServiceClient;
import com.willbank.servicetransaction.dto.TransactionEventDTO;
import com.willbank.servicetransaction.dto.TransactionRequestDTO;
import com.willbank.servicetransaction.dto.TransactionResponseDTO;
import com.willbank.servicetransaction.entity.Transaction;
import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import com.willbank.servicetransaction.exception.InsufficientBalanceException;
import com.willbank.servicetransaction.exception.InvalidTransactionException;
import com.willbank.servicetransaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final RabbitMQProducerService rabbitMQProducerService;
    
    @Transactional
    public TransactionResponseDTO createVirementInterne(TransactionRequestDTO request) {
        validateVirementInterne(request);
        
        BigDecimal balance = accountServiceClient.getAccountBalance(request.getAccountIdSource());
        if (balance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer");
        }
        
        Transaction transaction = Transaction.builder()
            .accountIdSource(request.getAccountIdSource())
            .accountIdDestination(request.getAccountIdDestination())
            .amount(request.getAmount())
            .type(TransactionType.VIREMENT_INTERNE)
            .status(TransactionStatus.COMPLETED)
            .description(request.getDescription())
            .completedAt(LocalDateTime.now())
            .build();
        
        transaction = transactionRepository.save(transaction);
        sendTransactionEvent(transaction);
        
        return mapToResponseDTO(transaction);
    }
    
    @Transactional
    public TransactionResponseDTO createDepot(TransactionRequestDTO request) {
        validateDepot(request);
        
        Transaction transaction = Transaction.builder()
            .accountIdSource(request.getAccountIdSource())
            .amount(request.getAmount())
            .type(TransactionType.DEPOT)
            .status(TransactionStatus.COMPLETED)
            .description(request.getDescription())
            .completedAt(LocalDateTime.now())
            .build();
        
        transaction = transactionRepository.save(transaction);
        sendTransactionEvent(transaction);
        
        return mapToResponseDTO(transaction);
    }
    
    @Transactional
    public TransactionResponseDTO createRetrait(TransactionRequestDTO request) {
        validateRetrait(request);
        
        BigDecimal balance = accountServiceClient.getAccountBalance(request.getAccountIdSource());
        if (balance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }
        
        Transaction transaction = Transaction.builder()
            .accountIdSource(request.getAccountIdSource())
            .amount(request.getAmount())
            .type(TransactionType.RETRAIT)
            .status(TransactionStatus.COMPLETED)
            .description(request.getDescription())
            .completedAt(LocalDateTime.now())
            .build();
        
        transaction = transactionRepository.save(transaction);
        sendTransactionEvent(transaction);
        
        return mapToResponseDTO(transaction);
    }
    
    public List<TransactionResponseDTO> getTransactionHistory(String accountId) {
        List<Transaction> transactions = transactionRepository
            .findByAccountIdSourceOrAccountIdDestination(accountId, accountId);
        return transactions.stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponseDTO> searchTransactions(
            String accountId, 
            TransactionType type, 
            TransactionStatus status) {
        List<Transaction> transactions = transactionRepository
            .findByAccountIdWithFilters(accountId, type, status);
        return transactions.stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponseDTO> getTransactionsByDateRange(
            String accountId, 
            LocalDateTime startDate, 
            LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository
            .findByAccountIdAndDateRange(accountId, startDate, endDate);
        return transactions.stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    private void validateVirementInterne(TransactionRequestDTO request) {
        if (request.getAccountIdDestination() == null || request.getAccountIdDestination().trim().isEmpty()) {
            throw new InvalidTransactionException("Destination account is required for internal transfer");
        }
        if (request.getAccountIdSource().equals(request.getAccountIdDestination())) {
            throw new InvalidTransactionException("Source and destination accounts cannot be the same");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
    }
    
    private void validateDepot(TransactionRequestDTO request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be greater than zero");
        }
    }
    
    private void validateRetrait(TransactionRequestDTO request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be greater than zero");
        }
    }
    
    private void sendTransactionEvent(Transaction transaction) {
        TransactionEventDTO event = TransactionEventDTO.builder()
            .transactionId(transaction.getId())
            .accountIdSource(transaction.getAccountIdSource())
            .accountIdDestination(transaction.getAccountIdDestination())
            .amount(transaction.getAmount())
            .type(transaction.getType())
            .status(transaction.getStatus())
            .timestamp(LocalDateTime.now())
            .build();
        
        rabbitMQProducerService.sendTransactionEvent(event);
    }
    
    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
            .id(transaction.getId())
            .accountIdSource(transaction.getAccountIdSource())
            .accountIdDestination(transaction.getAccountIdDestination())
            .amount(transaction.getAmount())
            .type(transaction.getType())
            .status(transaction.getStatus())
            .description(transaction.getDescription())
            .createdAt(transaction.getCreatedAt())
            .completedAt(transaction.getCompletedAt())
            .failureReason(transaction.getFailureReason())
            .build();
    }
}
