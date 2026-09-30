package org.venky.payflow.transaction.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.venky.payflow.bankaccount.entity.BankAccount;
import org.venky.payflow.bankaccount.repository.BankAccountRepository;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.common.security.CurrentUserService;
import org.venky.payflow.transaction.dto.TransactionResponse;
import org.venky.payflow.transaction.entity.Transaction;
import org.venky.payflow.transaction.enums.TransactionStatus;
import org.venky.payflow.transaction.enums.TransactionType;
import org.venky.payflow.transaction.mapper.TransactionMapper;
import org.venky.payflow.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService{

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;
    private final BankAccountRepository bankAccountRepository;


    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper, CurrentUserService currentUserService, BankAccountRepository bankAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.currentUserService = currentUserService;
        this.bankAccountRepository = bankAccountRepository;
    }


    @Override
    public Transaction createTransferTransaction(UUID sourceBankAccountId, UUID destinationBankAccountId, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();

        transaction.setSourceBankAccountId(sourceBankAccountId);
        transaction.setDestinationBankAccountId(destinationBankAccountId);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        Transaction savedTransaction = transactionRepository.save(transaction);

        System.out.println("TRANSACTION ID AFTER SAVE = " + savedTransaction.getId());

        return savedTransaction;
//        return transactionRepository.save(transaction);
    }

    @Override
    public TransactionResponse getTransactionById(UUID transactionId) {
        UUID loggedInUser = currentUserService.extractUserIdFromAuthentication();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction not found"));

        boolean hasAccess = false;

        if (transaction.getSourceBankAccountId() != null) {
            BankAccount sourceAccount = bankAccountRepository
                    .findById(transaction.getSourceBankAccountId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Source bank account not found"));

            if (sourceAccount.getUserId().equals(loggedInUser)) {
                hasAccess = true;
            }
        }

        if (transaction.getDestinationBankAccountId() != null) {
            BankAccount destinationAccount = bankAccountRepository
                    .findById(transaction.getDestinationBankAccountId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Destination bank account not found"));

            if (destinationAccount.getUserId().equals(loggedInUser)) {
                hasAccess = true;
            }
        }

        if (!hasAccess) {
            throw new AccessDeniedException("You do not have access to this transaction");
        }

        return transactionMapper.toResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getMyTransactions(UUID bankAccountId) {
        UUID loggedInUser = currentUserService.extractUserIdFromAuthentication();

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bank account not found"));

        if (!bankAccount.getUserId().equals(loggedInUser)) {
            throw new AccessDeniedException("You do not have access to this bank account");
        }

        List<Transaction> transactions = transactionRepository.findBySourceBankAccountIdOrDestinationBankAccountId(bankAccountId,bankAccountId);

        List<TransactionResponse> responses = new ArrayList<>();

        for (Transaction transaction : transactions) {
            responses.add(transactionMapper.toResponse(transaction));
        }

        return responses;
    }

}
