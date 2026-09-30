package org.venky.payflow.bankaccount.service;

import org.venky.payflow.bankaccount.dto.*;
import org.venky.payflow.transaction.dto.TransactionResponse;

import java.util.List;
import java.util.UUID;

public interface BankAccountService {

    BankAccountResponse createBankAccount(CreateBankAccountRequest bankAccountRequest);

    BankAccountResponse getBankAccountById(UUID accountId);

    List<BankAccountResponse> getMyBankAccounts();

    TransactionResponse deposit(UUID bankAccountId, BalanceChangeRequest depositRequest);

    TransactionResponse withdraw(UUID bankAccountId, BalanceChangeRequest withdrawRequest);

    BalanceResponse balance(UUID bankAccountId);

    TransactionResponse transferMoney(UUID bankAccountId , TransferMoneyRequest moneyTransferRequest);

}
