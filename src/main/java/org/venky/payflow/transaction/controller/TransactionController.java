package org.venky.payflow.transaction.controller;

import org.springframework.web.bind.annotation.*;
import org.venky.payflow.transaction.dto.TransactionResponse;
import org.venky.payflow.transaction.service.TransactionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> getTransactions(@RequestParam UUID bankAccountId) {
        return transactionService.getMyTransactions(bankAccountId);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse getTransactionById(@PathVariable UUID transactionId) {
        return transactionService.getTransactionById(transactionId);
    }


}
