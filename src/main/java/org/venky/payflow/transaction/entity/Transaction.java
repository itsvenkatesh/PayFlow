package org.venky.payflow.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.venky.payflow.transaction.enums.TransactionStatus;
import org.venky.payflow.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID sourceBankAccountId;

    private UUID destinationBankAccountId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType  type;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
