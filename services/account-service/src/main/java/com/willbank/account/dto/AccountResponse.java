package com.willbank.account.dto;

import com.willbank.account.domain.AccountStatus;
import com.willbank.account.domain.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long id;
    private Long customerId;
    private String accountNumber;
    private AccountType type;
    private BigDecimal balance;
    private AccountStatus status;
    private String label;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
