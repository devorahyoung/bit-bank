package com.poalim.mybank.events.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private String transactionId;
    private LocalDateTime processedAt;
}

