package com.poalim.mybank.account;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String ownerName;
    private BigDecimal balance;
    private AccountStatus status;
}
