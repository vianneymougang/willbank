package com.willbank.composite.service;

import com.willbank.composite.dto.AccountStatementResponse;

import java.time.LocalDateTime;

/**
 * Statement Service Interface
 */
public interface StatementService {

    /**
     * Get account statement for a date range
     * 
     * @param accountId Account ID
     * @param start     Start date
     * @param end       End date
     * @return Account statement with transactions and summary
     */
    AccountStatementResponse getAccountStatement(Long accountId, LocalDateTime start, LocalDateTime end);
}
