package com.poalim.mybank.events.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "daily_traffic_stats")
public class DailyTrafficStats {
    @Id
    private LocalDate date;
    private Long totalCount = 0L;
    private BigDecimal totalVolume = BigDecimal.ZERO;
    @Version
    private Long version;
}
