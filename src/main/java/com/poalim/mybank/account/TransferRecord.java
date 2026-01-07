package com.poalim.mybank.account;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String idempotencyKey;
    
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private TransferStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
