package com.poalim.mybank.events.repository;

import com.poalim.mybank.events.entity.DailyTrafficStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTrafficStatsRepository extends JpaRepository<DailyTrafficStats, LocalDate> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyTrafficStats d WHERE d.date = :date")
    Optional<DailyTrafficStats> findByDateForUpdate(@Param("date") LocalDate date);
    
    List<DailyTrafficStats> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(d.totalVolume) FROM DailyTrafficStats d WHERE d.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalVolumeBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(d.totalCount) FROM DailyTrafficStats d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalCountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
