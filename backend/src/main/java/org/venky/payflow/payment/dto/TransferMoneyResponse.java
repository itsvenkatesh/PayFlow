package org.venky.payflow.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.venky.payflow.transaction.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferMoneyResponse {

    private UUID transactionId;

    private String sourceBankAccountNumber;

    private String destinationBankAccountNumber;

    private BigDecimal amount;

    private String currency;

    private TransactionStatus status;

    private LocalDateTime createdAt;
}