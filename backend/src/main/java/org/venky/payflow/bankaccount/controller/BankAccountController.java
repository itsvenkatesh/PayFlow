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

    @PostMapping("/{bankAccountNumber}/deposit")
    public TransactionResponse depositAmount(@PathVariable String bankAccountNumber, @Valid @RequestBody BalanceChangeRequest depositRequest){
        return bankAccountService.deposit(bankAccountNumber ,depositRequest);
    }

    @PostMapping("/{bankAccountNumber}/withdraw")
    public TransactionResponse withdrawAmount(@PathVariable String bankAccountNumber, @Valid @RequestBody BalanceChangeRequest withdrawRequest){
        return bankAccountService.withdraw(bankAccountNumber, withdrawRequest);
    }

    @GetMapping("/{bankAccountNumber}/balance")
    public BalanceResponse getBalance(@PathVariable String bankAccountNumber){
        return bankAccountService.balance(bankAccountNumber);
    }
}
