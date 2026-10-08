package org.venky.payflow.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public TransactionResponse createTransferTransaction(String sourceBankAccountNumber, String destinationBankAccountNumber, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();

        transaction.setSourceBankAccountNumber(sourceBankAccountNumber);
        transaction.setDestinationBankAccountNumber(destinationBankAccountNumber);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    public TransactionResponse createWithdrawalTransaction(String sourceBankAccountNumber, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();

        transaction.setSourceBankAccountNumber(sourceBankAccountNumber);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        transaction.setType(TransactionType.WITHDRAW);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    public TransactionResponse createDepositTransaction(String destinationBankAccountNumber, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = new Transaction();

        transaction.setDestinationBankAccountNumber(destinationBankAccountNumber);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        transaction.setType(TransactionType.DEPOSIT);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    public TransactionResponse getTransactionById(UUID transactionId) {
        UUID loggedInUser = currentUserService.extractUserIdFromAuthentication();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction not found"));

        boolean hasAccess = false;

        if (transaction.getSourceBankAccountNumber() != null) {
            BankAccount sourceAccount = bankAccountRepository
                    .findByBankAccountNumber(transaction.getSourceBankAccountNumber())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Source bank account not found"));

            if (sourceAccount.getUserId().equals(loggedInUser)) {
                hasAccess = true;
            }
        }

        if (transaction.getDestinationBankAccountNumber() != null) {
            BankAccount destinationAccount = bankAccountRepository
                    .findByBankAccountNumber(transaction.getDestinationBankAccountNumber())
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
    public Page<TransactionResponse> getMyTransactions(String bankAccountNumber, Pageable pageable) {
        UUID loggedInUser = currentUserService.extractUserIdFromAuthentication();

        BankAccount bankAccount = bankAccountRepository.findByBankAccountNumber(bankAccountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bank account not found"));

        if (!bankAccount.getUserId().equals(loggedInUser)) {
            throw new AccessDeniedException("You do not have access to this bank account");
        }

        Page<Transaction> transactions = transactionRepository.findBySourceBankAccountNumberOrDestinationBankAccountNumberOrderByCreatedAtDesc(bankAccountNumber, bankAccountNumber, pageable);

        return transactions.map(transactionMapper::toResponse);
    }

}
