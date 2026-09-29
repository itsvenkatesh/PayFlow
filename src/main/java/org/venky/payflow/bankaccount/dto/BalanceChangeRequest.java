package org.venky.payflow.bankaccount.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceChangeRequest  {
    @NotNull
    @Positive
    private BigDecimal amount;
}
