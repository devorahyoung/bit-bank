package com.poalim.mybank.events.repository;

import com.poalim.mybank.events.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
    // Base CRUD operations are provided by JpaRepository
}
