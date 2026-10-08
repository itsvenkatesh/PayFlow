package org.venky.payflow.transaction.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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

//    @GetMapping
//    public List<TransactionResponse> getTransactions(@RequestParam UUID bankAccountId) {
//        return transactionService.getMyTransactions(bankAccountId, );
//    }

    @GetMapping("/{bankAccountNumber}")
    public ResponseEntity<Page<TransactionResponse>> getMyTransactions(
            @PathVariable String bankAccountNumber,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                transactionService.getMyTransactions(
                        bankAccountNumber,
                        pageable
                )
        );
    }

    @GetMapping("/bank-accounts/transactions/{transactionId}")
    public TransactionResponse getTransactionById(@PathVariable UUID transactionId,
        @PageableDefault(
            size = 10,
            sort = "createdAt",
            direction = Sort.Direction.DESC
        )
        Pageable pageable
    ) {
        return transactionService.getTransactionById(transactionId);
    }
}
