package org.venky.payflow.bankaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.venky.payflow.bankaccount.enums.BankAccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountResponse {

    private UUID id;

    private String bankAccountNumber;

    private String bankName;

    private BigDecimal balance;

    private String currency;

    private BankAccountStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
