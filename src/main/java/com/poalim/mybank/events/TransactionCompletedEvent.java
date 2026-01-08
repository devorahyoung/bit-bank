package com.poalim.mybank.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record TransactionCompletedEvent(
    String transactionId,
    String sourceAccountId,
    String targetAccountId,
    BigDecimal amount,
    LocalDateTime timestamp
) {}
