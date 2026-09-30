package org.venky.payflow.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.venky.payflow.transaction.enums.TransactionStatus;
import org.venky.payflow.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private UUID transactionId;
    private TransactionStatus status;
    private TransactionType type;
    private BigDecimal amount;
    private UUID sourceBankAccountId;
    private UUID destinationBankAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
