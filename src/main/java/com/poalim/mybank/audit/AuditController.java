package com.poalim.mybank.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Audit trail APIs")
public class AuditController {
    
    @Autowired
    private AuditService auditService;
    
    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get audit records by account ID", description = "Retrieve all audit records for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved audit records"),
            @ApiResponse(responseCode = "404", description = "No audit records found for the account")
    })
    public ResponseEntity<List<Audit>> getAuditsByAccountId(@PathVariable Long accountId) {
        List<Audit> audits = auditService.getAuditsByAccountId(accountId);
        return ResponseEntity.ok(audits);
    }
    
    @GetMapping("/{auditId}")
    @Operation(summary = "Get audit record by ID", description = "Retrieve a specific audit record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved audit record"),
            @ApiResponse(responseCode = "404", description = "Audit record not found")
    })
    public ResponseEntity<Audit> getAuditById(@PathVariable Long auditId) {
        Optional<Audit> audit = auditService.getAuditById(auditId);
        return audit.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/account/{accountId}/operation/{operationType}")
    @Operation(summary = "Get audit records by account ID and operation type", 
              description = "Retrieve audit records for a specific account and operation type")
    public ResponseEntity<List<Audit>> getAuditsByAccountIdAndOperation(
            @PathVariable Long accountId, 
            @PathVariable String operationType) {
        List<Audit> audits = auditService.getAuditsByAccountIdAndOperation(accountId, operationType);
        return ResponseEntity.ok(audits);
    }
}
