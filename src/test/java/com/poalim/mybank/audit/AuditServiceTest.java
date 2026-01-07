package com.poalim.mybank.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditService auditService;

    private Audit sampleAudit1;
    private Audit sampleAudit2;

    @BeforeEach
    void setUp() {
        sampleAudit1 = new Audit();
        sampleAudit1.setId(1L);
        sampleAudit1.setAccountId(100L);
        sampleAudit1.setOperationType("DEPOSIT");
        sampleAudit1.setTimestamp(LocalDateTime.now());

        sampleAudit2 = new Audit();
        sampleAudit2.setId(2L);
        sampleAudit2.setAccountId(100L);
        sampleAudit2.setOperationType("WITHDRAWAL");
        sampleAudit2.setTimestamp(LocalDateTime.now().plusHours(1));
    }

    @Test
    void getAuditsByAccountId_ShouldReturnAuditsForAccount() {
        // Arrange
        List<Audit> expectedAudits = Arrays.asList(sampleAudit1, sampleAudit2);
        when(auditRepository.findByAccountIdOrderByTimestampDesc(100L)).thenReturn(expectedAudits);

        // Act
        List<Audit> actualAudits = auditService.getAuditsByAccountId(100L);

        // Assert
        assertThat(actualAudits).hasSize(2);
        assertThat(actualAudits).isEqualTo(expectedAudits);
        verify(auditRepository).findByAccountIdOrderByTimestampDesc(100L);
    }

    @Test
    void getAuditById_WhenAuditExists_ShouldReturnAudit() {
        // Arrange
        when(auditRepository.findById(1L)).thenReturn(Optional.of(sampleAudit1));

        // Act
        Optional<Audit> result = auditService.getAuditById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(sampleAudit1);
        verify(auditRepository).findById(1L);
    }

    @Test
    void getAuditById_WhenAuditDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(auditRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Audit> result = auditService.getAuditById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(auditRepository).findById(999L);
    }

    @Test
    void getAuditsByAccountIdAndOperation_ShouldReturnFilteredAudits() {
        // Arrange
        List<Audit> expectedAudits = List.of(sampleAudit1);
        when(auditRepository.findByAccountIdAndOperationTypeOrderByTimestampDesc(100L, "DEPOSIT"))
                .thenReturn(expectedAudits);

        // Act
        List<Audit> actualAudits = auditService.getAuditsByAccountIdAndOperation(100L, "DEPOSIT");

        // Assert
        assertThat(actualAudits).hasSize(1);
        assertThat(actualAudits.get(0).getOperationType()).isEqualTo("DEPOSIT");
        verify(auditRepository).findByAccountIdAndOperationTypeOrderByTimestampDesc(100L, "DEPOSIT");
    }

    @Test
    void getAllAudits_ShouldReturnAllAudits() {
        // Arrange
        List<Audit> expectedAudits = Arrays.asList(sampleAudit1, sampleAudit2);
        when(auditRepository.findAll()).thenReturn(expectedAudits);

        // Act
        List<Audit> actualAudits = auditService.getAllAudits();

        // Assert
        assertThat(actualAudits).hasSize(2);
        assertThat(actualAudits).isEqualTo(expectedAudits);
        verify(auditRepository).findAll();
    }
}
