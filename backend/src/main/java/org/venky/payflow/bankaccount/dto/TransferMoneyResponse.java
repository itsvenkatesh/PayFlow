package org.venky.payflow.bankaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferMoneyResponse {

    private UUID transactionId;
    private UUID sourceBankAccountId;
    private UUID destinationBankAccountId;
    private BigDecimal amount;
    private BigDecimal sourceBalance;
    private String status;
}