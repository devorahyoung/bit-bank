package com.poalim.mybank.account;

import com.poalim.mybank.audit.Auditable;
import com.poalim.mybank.config.KafkaTopicsConfiguration;
import com.poalim.mybank.events.DepositCompletedEvent;
import com.poalim.mybank.events.TransactionCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransferRecordRepository transferRecordRepository;
    private final KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate;
    private final KafkaTemplate<String, DepositCompletedEvent> depositKafkaTemplate;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransferRecordRepository transferRecordRepository, KafkaTemplate<String, TransactionCompletedEvent> kafkaTemplate, KafkaTemplate<String, DepositCompletedEvent> depositKafkaTemplate) {
        this.accountRepository = accountRepository;
        this.transferRecordRepository = transferRecordRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.depositKafkaTemplate = depositKafkaTemplate;
    }

    @Transactional
    @Auditable(operationType = "CREATE_ACCOUNT")
    public AccountResponse createAccount(CreateAccountRequest request) {
        // Validate owner name
        if (request.getOwnerName() == null || request.getOwnerName().trim().length() < 3) {
            throw new IllegalArgumentException("Owner name must have at least 3 characters.");
        }

        // Validate initial balance
        if (request.getInitialBalance() == null || request.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance must be greater than or equal to 0.");
        }

        Account account = new Account(
                null,
                request.getOwnerName().trim(),
                request.getInitialBalance(),
                AccountStatus.ACTIVE
        );

        Account savedAccount = accountRepository.save(account);

        return new AccountResponse(
                savedAccount.getId(),
                savedAccount.getOwnerName(),
                savedAccount.getBalance(),
                savedAccount.getStatus()
        );
    }

    @Transactional
    @Auditable(operationType = "GET_ACCOUNT")
    public AccountResponse getAccount(Long id) throws AccountNotFoundException {
        return accountRepository.findById(id)
                .map(account -> new AccountResponse(
                        account.getId(),
                        account.getOwnerName(),
                        account.getBalance(),
                        account.getStatus()))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Auditable(operationType = "CHANGE_ACCOUNT_STATUS")
    public AccountResponse changeAccountStatus(Long id, AccountStatus status) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (status == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ActionNotAllowedException(id, "Account balance must be 0 to close the account.");
        }

        account.setStatus(status);
        Account savedAccount = accountRepository.save(account);

        return new AccountResponse(savedAccount.getId(), savedAccount.getOwnerName(), savedAccount.getBalance(), savedAccount.getStatus());
    }

    @Transactional
    @Auditable(operationType = "DEPOSIT")
    public AccountResponse deposit(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ActionNotAllowedException(id, "Deposit amount must be positive.");
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ActionNotAllowedException(id, "Cannot deposit to a non-active account.");
        }

        account.setBalance(account.getBalance().add(amount));
        Account savedAccount = accountRepository.save(account);

        // Create and send the deposit event
        String transactionId = UUID.randomUUID().toString();
        DepositCompletedEvent event = new DepositCompletedEvent(
                transactionId,
                id.toString(),
                amount,
                LocalDateTime.now()
        );

        try {
            depositKafkaTemplate.send(KafkaTopicsConfiguration.DEPOSIT_TOPIC,
                            event.transactionId(),
                            event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send deposit event: {}", ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to send deposit event: {}", e.getMessage());
        }

        return new AccountResponse(
                savedAccount.getId(),
                savedAccount.getOwnerName(),
                savedAccount.getBalance(),
                savedAccount.getStatus()
        );
    }

    @Transactional
    @Auditable(operationType = "WITHDRAWAL")
    public AccountResponse withdrawal(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ActionNotAllowedException(id, "Withdrawal amount must be positive.");
        }

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ActionNotAllowedException(id, "Cannot withdraw from a non-active account.");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ActionNotAllowedException(id, "Insufficient funds for withdrawal.");
        }

        account.setBalance(newBalance);
        Account savedAccount = accountRepository.save(account);

        return new AccountResponse(
                savedAccount.getId(),
                savedAccount.getOwnerName(),
                savedAccount.getBalance(),
                savedAccount.getStatus()
        );
    }

    @Transactional
    @Auditable(operationType = "TRANSFER")
    public TransferResponse transfer(TransferRequest request) {
        // Handle idempotency
        if (request.getIdempotencyKey() != null) {
            var existingTransfer = transferRecordRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingTransfer.isPresent()) {
                TransferRecord record = existingTransfer.get();
                return new TransferResponse(
                        record.getId().toString(),
                        record.getFromAccountId(),
                        record.getToAccountId(),
                        record.getAmount(),
                        record.getCompletedAt() != null ? record.getCompletedAt() : record.getCreatedAt(),
                        record.getStatus().toString()
                );
            }
        }

        // Validate transfer request
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ActionNotAllowedException(null, "Transfer amount must be positive.");
        }

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new ActionNotAllowedException(request.getFromAccountId(), "Cannot transfer to the same account.");
        }

        // Create transfer record for idempotency
        String transferId = UUID.randomUUID().toString();
        TransferRecord transferRecord = new TransferRecord();
        transferRecord.setIdempotencyKey(request.getIdempotencyKey());
        transferRecord.setFromAccountId(request.getFromAccountId());
        transferRecord.setToAccountId(request.getToAccountId());
        transferRecord.setAmount(request.getAmount());
        transferRecord.setStatus(TransferStatus.PENDING);
        transferRecord = transferRecordRepository.save(transferRecord);

        try {
            // Lock accounts in consistent order to prevent deadlocks
            List<Long> accountIds = Arrays.asList(request.getFromAccountId(), request.getToAccountId());
            accountIds.sort(Long::compareTo);
            List<Account> lockedAccounts = accountRepository.findByIdsForUpdate(accountIds);

            if (lockedAccounts.size() != 2) {
                throw new AccountNotFoundException(null);
            }

            Account fromAccount = lockedAccounts.stream()
                    .filter(acc -> acc.getId().equals(request.getFromAccountId()))
                    .findFirst()
                    .orElseThrow(() -> new AccountNotFoundException(request.getFromAccountId()));

            Account toAccount = lockedAccounts.stream()
                    .filter(acc -> acc.getId().equals(request.getToAccountId()))
                    .findFirst()
                    .orElseThrow(() -> new AccountNotFoundException(request.getToAccountId()));

            // Validate account statuses
            if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new ActionNotAllowedException(request.getFromAccountId(), "Source account is not active.");
            }

            if (toAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new ActionNotAllowedException(request.getToAccountId(), "Destination account is not active.");
            }

            // Check for sufficient funds
            BigDecimal newFromBalance = fromAccount.getBalance().subtract(request.getAmount());
            if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new ActionNotAllowedException(request.getFromAccountId(), "Insufficient funds for transfer.");
            }

            // Perform the transfer
            fromAccount.setBalance(newFromBalance);
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Update transfer record
            transferRecord.setStatus(TransferStatus.COMPLETED);
            transferRecord.setCompletedAt(LocalDateTime.now());
            transferRecordRepository.save(transferRecord);

            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    transferRecord.getId().toString(),
                    request.getFromAccountId().toString(),
                    request.getToAccountId().toString(),
                    request.getAmount(),
                    LocalDateTime.now()
            );

            // Send the event
            try {
                kafkaTemplate.send(KafkaTopicsConfiguration.TRANSACTION_TOPIC,
                                event.transactionId(),
                                event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send transaction event: {}", ex.getMessage());
                            }
                        });
            } catch (Exception e) {
                log.error("Failed to send transaction event: {}", e.getMessage());
            }


            return new TransferResponse(
                    transferRecord.getId().toString(),
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    transferRecord.getCompletedAt(),
                    TransferStatus.COMPLETED.toString()
            );

        } catch (Exception e) {
            // Mark transfer as failed
            transferRecord.setStatus(TransferStatus.FAILED);
            transferRecord.setCompletedAt(LocalDateTime.now());
            transferRecordRepository.save(transferRecord);
            throw e;
        }
    }
}