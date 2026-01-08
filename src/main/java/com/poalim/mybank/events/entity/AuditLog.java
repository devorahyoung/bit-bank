package com.poalim.mybank.events.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionId;
    private String sourceAccountId;
    private String targetAccountId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String eventType = "TRANSFER";
}
