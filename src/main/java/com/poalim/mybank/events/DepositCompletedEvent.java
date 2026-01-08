package com.poalim.mybank.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositCompletedEvent(
    String transactionId,
    String accountId,
    BigDecimal amount,
    LocalDateTime timestamp
) {}