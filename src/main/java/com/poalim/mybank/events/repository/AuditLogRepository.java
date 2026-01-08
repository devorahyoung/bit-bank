package com.poalim.mybank.events.repository;

import com.poalim.mybank.events.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTransactionId(String transactionId);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findBySourceAccountIdOrTargetAccountId(String sourceAccountId, String targetAccountId);
}
