package org.venky.payflow.bankaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceChangeResponse  {
    private BigDecimal amount;

    private BigDecimal balance;
}
