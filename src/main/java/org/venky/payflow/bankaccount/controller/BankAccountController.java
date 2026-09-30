package org.venky.payflow.bankaccount.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.venky.payflow.bankaccount.dto.*;
import org.venky.payflow.bankaccount.service.BankAccountService;
import org.venky.payflow.transaction.dto.TransactionResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bankaccount")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping()
    public BankAccountResponse createBankAccount(@Valid @RequestBody CreateBankAccountRequest createBankAccountRequest){
        System.out.println(">>> CONTROLLER REACHED");
        return bankAccountService.createBankAccount(createBankAccountRequest);
    }

    @GetMapping()
    public List<BankAccountResponse> getMyBankAccounts(){
        return bankAccountService.getMyBankAccounts();
    }

    @GetMapping("/{bankAccountId}")
    public BankAccountResponse getBankAccountById(@PathVariable UUID bankAccountId){
        return bankAccountService.getBankAccountById(bankAccountId);
    }

    @PostMapping("/{bankAccountId}/deposit")
    public TransactionResponse depositAmount(@PathVariable UUID bankAccountId, @Valid @RequestBody BalanceChangeRequest depositRequest){
        return bankAccountService.deposit(bankAccountId ,depositRequest);
    }

    @PostMapping("/{bankAccountId}/withdraw")
    public TransactionResponse withdrawAmount(@PathVariable UUID bankAccountId, @Valid @RequestBody BalanceChangeRequest depositRequest){
        return bankAccountService.withdraw(bankAccountId ,depositRequest);
    }

    @GetMapping("/{bankAccountId}/balance")
    public BalanceResponse getBalance(@PathVariable UUID bankAccountId){
        return bankAccountService.balance(bankAccountId);
    }

    @PostMapping("/{bankAccountId}/transfer")
    public TransactionResponse transferMoney(@PathVariable UUID bankAccountId , @Valid @RequestBody TransferMoneyRequest transferMoneyRequest){
        return bankAccountService.transferMoney(bankAccountId, transferMoneyRequest);
    }
}
