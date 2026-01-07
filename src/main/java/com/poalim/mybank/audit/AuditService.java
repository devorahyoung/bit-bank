package com.poalim.mybank.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuditService {
    
    @Autowired
    private AuditRepository auditRepository;
    
    public List<Audit> getAuditsByAccountId(Long accountId) {
        return auditRepository.findByAccountIdOrderByTimestampDesc(accountId);
    }
    
    public Optional<Audit> getAuditById(Long auditId) {
        return auditRepository.findById(auditId);
    }
    
    public List<Audit> getAuditsByAccountIdAndOperation(Long accountId, String operationType) {
        return auditRepository.findByAccountIdAndOperationTypeOrderByTimestampDesc(accountId, operationType);
    }
    
    public List<Audit> getAllAudits() {
        return auditRepository.findAll();
    }
}
