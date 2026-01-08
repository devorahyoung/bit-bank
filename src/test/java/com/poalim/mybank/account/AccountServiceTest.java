package com.poalim.mybank.account;

import com.poalim.mybank.config.KafkaTopicsConfiguration;
import com.poalim.mybank.events.TransactionCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRecordRepository transferRecordRepository;

    @Mock
    private KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private CreateAccountRequest createAccountRequest;

    @BeforeEach
    void setUp() {
        testAccount = new Account(1L, "John Doe", BigDecimal.valueOf(1000), AccountStatus.ACTIVE);
        createAccountRequest = new CreateAccountRequest("John Doe", BigDecimal.valueOf(1000));
    }

    @Test
    void createAccount_WithValidData_ShouldCreateAccount() {
        // Arrange
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        AccountResponse response = accountService.createAccount(createAccountRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testAccount.getId());
        assertThat(response.getOwnerName()).isEqualTo(testAccount.getOwnerName());
        assertThat(response.getBalance()).isEqualTo(testAccount.getBalance());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_WithInvalidOwnerName_ShouldThrowException() {
        // Arrange
        CreateAccountRequest invalidRequest = new CreateAccountRequest("", BigDecimal.valueOf(1000));

        // Act & Assert
        assertThatThrownBy(() -> accountService.createAccount(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Owner name must have at least 3 characters");
    }

    @Test
    void createAccount_WithNegativeBalance_ShouldThrowException() {
        // Arrange
        CreateAccountRequest invalidRequest = new CreateAccountRequest("John Doe", BigDecimal.valueOf(-100));

        // Act & Assert
        assertThatThrownBy(() -> accountService.createAccount(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Initial balance must be greater than or equal to 0");
    }

    @Test
    void getAccount_WhenAccountExists_ShouldReturnAccount() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act
        AccountResponse response = accountService.getAccount(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testAccount.getId());
        verify(accountRepository).findById(1L);
    }

    @Test
    void getAccount_WhenAccountDoesNotExist_ShouldThrowException() {
        // Arrange
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.getAccount(999L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void deposit_WithValidAmount_ShouldIncreaseBalance() {
        // Arrange
        BigDecimal depositAmount = BigDecimal.valueOf(500);
        Account updatedAccount = new Account(
                testAccount.getId(),
                testAccount.getOwnerName(),
                testAccount.getBalance().add(depositAmount),
                testAccount.getStatus()
        );
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // Act
        AccountResponse response = accountService.deposit(1L, depositAmount);

        // Assert
        assertThat(response.getBalance())
                .isEqualTo(updatedAccount.getBalance()); // Fix: use updatedAccount instead of calculation
        verify(accountRepository).save(any(Account.class));
    }


    @Test
    void withdrawal_WithSufficientFunds_ShouldDecreaseBalance() {
        // Arrange
        BigDecimal withdrawalAmount = BigDecimal.valueOf(500);
        Account updatedAccount = new Account(
                testAccount.getId(),
                testAccount.getOwnerName(),
                testAccount.getBalance().subtract(withdrawalAmount),
                testAccount.getStatus()
        );
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // Act
        AccountResponse response = accountService.withdrawal(1L, withdrawalAmount);

        // Assert
        assertThat(response.getBalance())
                .isEqualTo(updatedAccount.getBalance()); // Fix: use updatedAccount instead of calculation
        verify(accountRepository).save(any(Account.class));
    }


    @Test
    void withdrawal_WithInsufficientFunds_ShouldThrowException() {
        // Arrange
        BigDecimal withdrawalAmount = BigDecimal.valueOf(2000);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThatThrownBy(() -> accountService.withdrawal(1L, withdrawalAmount))
                .isInstanceOf(ActionNotAllowedException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void transfer_WithValidRequest_ShouldTransferFunds() {
        // Arrange
        Account fromAccount = new Account(1L, "John Doe", BigDecimal.valueOf(1000), AccountStatus.ACTIVE);
        Account toAccount = new Account(2L, "Jane Doe", BigDecimal.valueOf(500), AccountStatus.ACTIVE);
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(500), UUID.randomUUID().toString());

        // Create a transfer record with an ID for the mock response
        TransferRecord transferRecord = new TransferRecord();
        transferRecord.setId(1L); // Set an ID to avoid NPE
        transferRecord.setFromAccountId(fromAccount.getId());
        transferRecord.setToAccountId(toAccount.getId());
        transferRecord.setAmount(request.getAmount());
        transferRecord.setStatus(TransferStatus.PENDING);
        transferRecord.setIdempotencyKey(request.getIdempotencyKey());

        when(accountRepository.findByIdsForUpdate(any())).thenReturn(Arrays.asList(fromAccount, toAccount));
        when(transferRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(transferRecordRepository.save(any(TransferRecord.class))).thenReturn(transferRecord);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Mock Kafka send operation
        CompletableFuture<SendResult<String, TransactionCompletedEvent>> future = new CompletableFuture<>();
        future.complete(new SendResult<>(null, null));
        when(kafkaTemplate.send(anyString(), anyString(), any(TransactionCompletedEvent.class)))
                .thenReturn(future);

        // Act
        TransferResponse response = accountService.transfer(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTransferId()).isEqualTo(transferRecord.getId().toString());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transferRecordRepository, times(2)).save(any(TransferRecord.class));
        verify(kafkaTemplate).send(
                eq(KafkaTopicsConfiguration.TRANSACTION_TOPIC),
                any(String.class),
                any(TransactionCompletedEvent.class)
        );
    }

    @Test
    void transfer_WithInsufficientFunds_ShouldThrowException() {
        // Arrange
        Account fromAccount = new Account(1L, "John Doe", BigDecimal.valueOf(100), AccountStatus.ACTIVE);
        Account toAccount = new Account(2L, "Jane Doe", BigDecimal.valueOf(500), AccountStatus.ACTIVE);
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(500), UUID.randomUUID().toString());

        when(accountRepository.findByIdsForUpdate(any())).thenReturn(Arrays.asList(fromAccount, toAccount));
        when(transferRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(transferRecordRepository.save(any())).thenReturn(new TransferRecord());

        // Act & Assert
        assertThatThrownBy(() -> accountService.transfer(request))
                .isInstanceOf(ActionNotAllowedException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void changeAccountStatus_ToClosedWithZeroBalance_ShouldSucceed() {
        // Arrange
        Account account = new Account(1L, "John Doe", BigDecimal.ZERO, AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        AccountResponse response = accountService.changeAccountStatus(1L, AccountStatus.CLOSED);

        // Assert
        assertThat(response.getStatus()).isEqualTo(AccountStatus.CLOSED);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void changeAccountStatus_ToClosedWithNonZeroBalance_ShouldThrowException() {
        // Arrange
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThatThrownBy(() -> accountService.changeAccountStatus(1L, AccountStatus.CLOSED))
                .isInstanceOf(ActionNotAllowedException.class)
                .hasMessageContaining("Account balance must be 0 to close the account");
    }
}