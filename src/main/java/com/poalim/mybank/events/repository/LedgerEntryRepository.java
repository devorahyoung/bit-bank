package com.poalim.mybank.events.repository;

import com.poalim.mybank.events.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByTransactionId(String transactionId);
    List<LedgerEntry> findByAccountId(String accountId);
    
    @Query("SELECT SUM(l.amount) FROM LedgerEntry l WHERE l.accountId = :accountId AND l.timestamp <= :asOf")
    BigDecimal getAccountBalanceAsOf(@Param("accountId") String accountId, @Param("asOf") LocalDateTime asOf);
    
    @Query("SELECT l FROM LedgerEntry l WHERE l.accountId = :accountId AND l.timestamp BETWEEN :startDate AND :endDate")
    List<LedgerEntry> findAccountEntriesBetween(
        @Param("accountId") String accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
