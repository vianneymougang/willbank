package com.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    
    private String transactionId;
    private String userId;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER, etc.
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime timestamp;
    private String recipientEmail;
    private String recipientPhone;
    private String fcmToken;
    private Map<String, Object> metadata;
}
