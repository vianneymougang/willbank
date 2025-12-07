package com.willbank.servicetransaction.dto;

import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEventDTO implements Serializable {
    private Long transactionId;
    private String accountIdSource;
    private String accountIdDestination;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime timestamp;
}
