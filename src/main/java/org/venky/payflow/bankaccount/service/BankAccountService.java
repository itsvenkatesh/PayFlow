package org.venky.payflow.bankaccount.service;

import org.venky.payflow.bankaccount.dto.*;

import java.util.List;
import java.util.UUID;

public interface BankAccountService {

    BankAccountResponse createBankAccount(CreateBankAccountRequest bankAccountRequest);

    BankAccountResponse getBankAccountById(UUID accountId);

    List<BankAccountResponse> getMyBankAccounts();

    BalanceChangeResponse deposit(UUID bankAccountId, BalanceChangeRequest depositRequest);

    BalanceChangeResponse withdraw(UUID bankAccountId, BalanceChangeRequest withdrawRequest);

    BalanceResponse balance(UUID bankAccountId);

    TransferMoneyResponse transferMoney(UUID bankAccountId , TransferMoneyRequest moneyTransferRequest);

}
