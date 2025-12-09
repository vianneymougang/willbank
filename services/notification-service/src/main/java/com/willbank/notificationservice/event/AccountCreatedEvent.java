package com.willbank.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event for account creation
 * Will be published by account-service (to be implemented)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long accountId;
    private Long customerId;
    private String accountNumber;
    private String accountType;
    private BigDecimal initialBalance;
    private LocalDateTime createdAt;
}
