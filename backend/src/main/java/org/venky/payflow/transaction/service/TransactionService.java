package org.venky.payflow.transaction.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.venky.payflow.transaction.dto.TransactionResponse;
import org.venky.payflow.transaction.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    TransactionResponse createTransferTransaction(String sourceBankAccountNumber, String destinationBankAccountNumber, BigDecimal amount);

    TransactionResponse createWithdrawalTransaction(String sourceBankAccountNumber,BigDecimal amount);

    TransactionResponse createDepositTransaction(String sourceBankAccountNumber, BigDecimal amount);

    TransactionResponse getTransactionById(UUID transactionId);

    Page<TransactionResponse> getMyTransactions(String sourceBankAccountNumber, Pageable pageable);


}
