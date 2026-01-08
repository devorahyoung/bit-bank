package com.poalim.mybank.events.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String entryType; // DEBIT or CREDIT
}
