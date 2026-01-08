package com.poalim.mybank.events.service;

import com.poalim.mybank.config.KafkaTopicsConfiguration;
import com.poalim.mybank.events.TransactionCompletedEvent;
import com.poalim.mybank.events.entity.AuditLog;
import com.poalim.mybank.events.entity.DailyTrafficStats;
import com.poalim.mybank.events.entity.LedgerEntry;
import com.poalim.mybank.events.entity.ProcessedEvent;
import com.poalim.mybank.events.repository.AuditLogRepository;
import com.poalim.mybank.events.repository.DailyTrafficStatsRepository;
import com.poalim.mybank.events.repository.LedgerEntryRepository;
import com.poalim.mybank.events.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionEventListener {
    
    private final ProcessedEventRepository processedEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final DailyTrafficStatsRepository dailyTrafficStatsRepository;
    
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        autoCreateTopics = "false",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = KafkaTopicsConfiguration.TRANSACTION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void processTransactionEvent(TransactionCompletedEvent event) {
        // Idempotency check
        if (processedEventRepository.existsById(event.transactionId())) {
            log.info("Event already processed: {}", event.transactionId());
            return;
        }
        
        // Save audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setTransactionId(event.transactionId());
        auditLog.setSourceAccountId(event.sourceAccountId());
        auditLog.setTargetAccountId(event.targetAccountId());
        auditLog.setAmount(event.amount());
        auditLog.setTimestamp(event.timestamp());
        auditLogRepository.save(auditLog);
        
        // Create ledger entries
        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setTransactionId(event.transactionId());
        debitEntry.setAccountId(event.sourceAccountId());
        debitEntry.setAmount(event.amount().negate());
        debitEntry.setTimestamp(event.timestamp());
        debitEntry.setEntryType("DEBIT");
        ledgerEntryRepository.save(debitEntry);
        
        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setTransactionId(event.transactionId());
        creditEntry.setAccountId(event.targetAccountId());
        creditEntry.setAmount(event.amount());
        creditEntry.setTimestamp(event.timestamp());
        creditEntry.setEntryType("CREDIT");
        ledgerEntryRepository.save(creditEntry);
        
        // Update daily stats with pessimistic locking
        LocalDate today = event.timestamp().toLocalDate();
        DailyTrafficStats stats = dailyTrafficStatsRepository
            .findById(today)
            .orElse(new DailyTrafficStats());
        
        stats.setDate(today);
        stats.setTotalCount(stats.getTotalCount() + 1);
        stats.setTotalVolume(stats.getTotalVolume().add(event.amount()));
        dailyTrafficStatsRepository.save(stats);
        
        // Mark event as processed
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setTransactionId(event.transactionId());
        processedEvent.setProcessedAt(LocalDateTime.now());
        processedEventRepository.save(processedEvent);
    }
}
