package org.venky.payflow.transaction.service;


import org.venky.payflow.transaction.dto.TransactionResponse;
import org.venky.payflow.transaction.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionResponse createTransferTransaction(UUID sourceBankAccountId, UUID destinationBankAccountId,BigDecimal amount);

    TransactionResponse createWithdrawalTransaction(UUID sourceBankAccountId,BigDecimal amount);

    TransactionResponse createDepositTransaction(UUID destinationBankAccountId, BigDecimal amount);

    TransactionResponse getTransactionById(UUID transactionId);

    List<TransactionResponse> getMyTransactions(UUID bankAccountId);


}
