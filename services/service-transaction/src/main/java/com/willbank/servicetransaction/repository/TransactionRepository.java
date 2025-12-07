package com.willbank.servicetransaction.repository;

import com.willbank.servicetransaction.entity.Transaction;
import com.willbank.servicetransaction.enums.TransactionStatus;
import com.willbank.servicetransaction.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByAccountIdSourceOrAccountIdDestination(String accountIdSource, String accountIdDestination);
    
    List<Transaction> findByAccountIdSource(String accountIdSource);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByType(TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.accountIdSource = :accountId OR t.accountIdDestination = :accountId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByAccountIdAndDateRange(
        @Param("accountId") String accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.accountIdSource = :accountId OR t.accountIdDestination = :accountId) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:status IS NULL OR t.status = :status)")
    List<Transaction> findByAccountIdWithFilters(
        @Param("accountId") String accountId,
        @Param("type") TransactionType type,
        @Param("status") TransactionStatus status
    );
}
