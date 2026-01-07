package com.poalim.mybank.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_id")
    private Long accountId;
    
    @Column(name = "operation_type", nullable = false)
    private String operationType;
    
    @Column(name = "method_name", nullable = false)
    private String methodName;
    
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;
    
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;
    
    @Column(name = "execution_time")
    private Long executionTime;
    
    @Column(name = "success", nullable = false)
    private Boolean success;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "session_id")
    private String sessionId;
}
