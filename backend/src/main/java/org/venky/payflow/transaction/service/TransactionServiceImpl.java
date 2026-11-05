package org.venky.payflow.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.venky.payflow.bankaccount.entity.BankAccount;
import org.venky.payflow.bankaccount.repository.BankAccountRepository;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.common.security.CurrentUserService;
import org.venky.payflow.transaction.dto.TransactionFilter;
import org.venky.payflow.transaction.dto.TransactionResponse;
import org.venky.payflow.transaction.entity.Transaction;
import org.venky.payflow.transaction.enums.TransactionDirection;
import org.venky.payflow.transaction.enums.TransactionStatus;
import org.venky.payflow.transaction.enums.TransactionType;
import org.venky.payflow.transaction.mapper.TransactionMapper;
import org.venky.payflow.transaction.repository.TransactionRepository;
import org.venky.payflow.transaction.specification.TransactionSpecifications;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public Page<TransactionResponse> getMyTransactions(
            TransactionFilter filter,
            Pageable pageable
    ) {
        UUID loggedInUserId =
                currentUserService.extractUserIdFromAuthentication();

        List<BankAccount> userAccounts =
                bankAccountRepository.findByUserId(loggedInUserId);

        List<String> userAccountNumbers = userAccounts.stream()
                .map(BankAccount::getBankAccountNumber)
                .toList();

        if (userAccountNumbers.isEmpty()) {
            return Page.empty(pageable);
        }

        List<String> selectedAccountNumbers = null;

        if (filter.getBankAccountNumbers() != null
                && !filter.getBankAccountNumbers().isEmpty()) {

            // Only accept account numbers owned by this user.
            selectedAccountNumbers = filter.getBankAccountNumbers()
                    .stream()
                    .distinct()
                    .toList();

            if (!userAccountNumbers.containsAll(selectedAccountNumbers)) {
                throw new ResourceNotFoundException(
                        "One or more bank accounts were not found"
                );
            }
        }

        var specification =
                TransactionSpecifications.filterTransactions(
                        userAccountNumbers,
                        selectedAccountNumbers,
                        filter
                );

        return transactionRepository.findAll(specification, pageable)
                .map(transaction -> {
                    TransactionResponse response =
                            transactionMapper.toResponse(transaction);

                    boolean sentByUser = userAccountNumbers.contains(
                            transaction.getSourceBankAccountNumber()
                    );

                    boolean receivedByUser = userAccountNumbers.contains(
                            transaction.getDestinationBankAccountNumber()
                    );

                    if (sentByUser && !receivedByUser) {
                        response.setDirection(TransactionDirection.SENT);
                    } else if (receivedByUser && !sentByUser) {
                        response.setDirection(TransactionDirection.RECEIVED);
                    } else {
                        // Both accounts belong to the user.
                        // Direction is ambiguous for an internal transfer.
                        response.setDirection(null);
                    }

                    return response;
                });
    }
}
