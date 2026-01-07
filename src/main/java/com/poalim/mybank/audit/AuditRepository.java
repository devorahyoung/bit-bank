package com.poalim.mybank.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    
    List<Audit> findByAccountIdOrderByTimestampDesc(Long accountId);
    
    List<Audit> findByAccountIdAndOperationTypeOrderByTimestampDesc(Long accountId, String operationType);
}
