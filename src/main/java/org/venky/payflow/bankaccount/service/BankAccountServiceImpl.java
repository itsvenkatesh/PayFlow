package org.venky.payflow.bankaccount.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.venky.payflow.bankaccount.dto.*;
import org.venky.payflow.bankaccount.entity.BankAccount;
import org.venky.payflow.bankaccount.enums.BankAccountStatus;
import org.venky.payflow.bankaccount.mapper.BankAccountMapper;
import org.venky.payflow.bankaccount.repository.BankAccountRepository;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.common.security.CurrentUserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BankAccountServiceImpl implements BankAccountService{

    private final BankAccountRepository bankAccountRepository;
    private final CurrentUserService currentUserService;
    private final BankAccountMapper  bankAccountMapper;

    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository, CurrentUserService currentUserService, BankAccountMapper bankAccountMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.currentUserService = currentUserService;
        this.bankAccountMapper = bankAccountMapper;
    }

    @Override
    public BankAccountResponse createBankAccount(CreateBankAccountRequest bankAccountRequest) {
        UUID loggedInUserId = currentUserService.extractUserIdFromAuthentication();

        String bankAccountNumber = generateAccountNumber();
        while(bankAccountRepository.existsByBankAccountNumber(bankAccountNumber)){
            bankAccountNumber = generateAccountNumber();
        }

        LocalDateTime now = LocalDateTime.now();

        BankAccount bankAccount = bankAccountMapper.toEntity(bankAccountRequest);

        bankAccount.setBankAccountNumber(bankAccountNumber);
        bankAccount.setUserId(loggedInUserId);
        bankAccount.setCreatedAt(now);
        bankAccount.setUpdatedAt(now);
        bankAccount.setBalance(BigDecimal.ZERO);
        bankAccount.setStatus(BankAccountStatus.ACTIVE);

        BankAccount dbBankAccount = bankAccountRepository.save(bankAccount);

        return bankAccountMapper.toResponse(dbBankAccount);

    }

    @Override
    public BankAccountResponse getBankAccountById(UUID accountId) {
        UUID loggedInUserId = currentUserService.extractUserIdFromAuthentication();

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + accountId));

        if (!bankAccount.getUserId().equals(loggedInUserId)) {
            throw new AccessDeniedException("You do not have access to this bank account");
        }

        return bankAccountMapper.toResponse(bankAccount);
    }

    @Override
    public List<BankAccountResponse> getMyBankAccounts() {
        UUID loggedInUserId = currentUserService.extractUserIdFromAuthentication();
        List<BankAccount> myBankAccounts = bankAccountRepository.findByUserId(loggedInUserId);

        return bankAccountMapper.toResponseList(myBankAccounts);
    }

    @Override
    @Transactional
    public BalanceChangeResponse deposit(UUID bankAccountId , BalanceChangeRequest depositRequest) {
        UUID loggedInUserId = currentUserService.extractUserIdFromAuthentication();

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        if (!bankAccount.getUserId().equals(loggedInUserId)) {
            throw new AccessDeniedException("You do not have access to this bank account");
        }

        if (bankAccount.getStatus() != BankAccountStatus.ACTIVE) {
            throw new IllegalStateException("Bank account is not active");
        }

        bankAccount.setBalance(bankAccount.getBalance().add(depositRequest.getAmount()));
        bankAccount.setUpdatedAt(LocalDateTime.now());

        bankAccountRepository.save(bankAccount);

        return new BalanceChangeResponse(depositRequest.getAmount(), bankAccount.getBalance());
    }

    @Override
    @Transactional
    public BalanceChangeResponse withdraw(UUID bankAccountId, BalanceChangeRequest withdrawRequest) {
        UUID loggedInUserId = currentUserService.extractUserIdFromAuthentication();

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() ->  new ResourceNotFoundException("Bank account not found with id: " + bankAccountId) );

        if (!bankAccount.getUserId().equals(loggedInUserId)) {
            throw new AccessDeniedException("You do not have access to this bank account");
        }

        if (bankAccount.getStatus() != BankAccountStatus.ACTIVE) {
            throw new IllegalStateException("Bank account is not active");
        }

        if (bankAccount.getBalance().compareTo(withdrawRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        bankAccount.setBalance(bankAccount.getBalance().subtract(withdrawRequest.getAmount()));
        bankAccount.setUpdatedAt(LocalDateTime.now());
        bankAccountRepository.save(bankAccount);

        return new BalanceChangeResponse(withdrawRequest.getAmount(), bankAccount.getBalance());
    }

    @Override
    public BalanceResponse balance(UUID bankAccountId) {
        UUID loggedInUserId = currentUserService.extractUserIdFromAuthentication();

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId).orElseThrow(()-> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        if  (!bankAccount.getUserId().equals(loggedInUserId)) {
            throw new AccessDeniedException("You do not have access to this bank account");
        }

        return new BalanceResponse(bankAccount.getBankAccountNumber(), bankAccount.getBalance());
    }

    @Override
    @Transactional
    public TransferMoneyResponse transferMoney(UUID bankAccountId, TransferMoneyRequest moneyTransferRequest) {
        UUID loggedInUserId = currentUserService.extractUserIdFromAuthentication();

        if (bankAccountId.equals(moneyTransferRequest.getDestinationBankAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        BankAccount sourceBankAccount = bankAccountRepository.findById(bankAccountId).orElseThrow(()-> new ResourceNotFoundException("Bank account not found with id: " + bankAccountId));

        BankAccount destinationBankAccount = bankAccountRepository.findById(moneyTransferRequest.getDestinationBankAccountId()).orElseThrow(()-> new ResourceNotFoundException("Bank account not found with id: " + moneyTransferRequest.getDestinationBankAccountId()));

        if (!sourceBankAccount.getUserId().equals(loggedInUserId)) {
            throw new AccessDeniedException("You do not have access to this bank account");
        }

        if (sourceBankAccount.getStatus() != BankAccountStatus.ACTIVE) {
            throw new IllegalStateException("Source Bank account is not active");
        }

        if (destinationBankAccount.getStatus() != BankAccountStatus.ACTIVE) {
            throw new IllegalStateException("Destination Bank account is not active");
        }

        if (sourceBankAccount.getBalance().compareTo(moneyTransferRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        sourceBankAccount.setBalance(sourceBankAccount.getBalance().subtract(moneyTransferRequest.getAmount()));
        sourceBankAccount.setUpdatedAt(LocalDateTime.now());

        destinationBankAccount.setBalance(destinationBankAccount.getBalance().add(moneyTransferRequest.getAmount()));
        destinationBankAccount.setUpdatedAt(LocalDateTime.now());

        bankAccountRepository.save(sourceBankAccount);
        bankAccountRepository.save(destinationBankAccount);

        UUID transactionId = UUID.randomUUID();

        TransferMoneyResponse transferMoneyResponse = new TransferMoneyResponse();
        transferMoneyResponse.setTransactionId(transactionId);
        transferMoneyResponse.setAmount(moneyTransferRequest.getAmount());
        transferMoneyResponse.setSourceBankAccountId(bankAccountId);
        transferMoneyResponse.setDestinationBankAccountId(moneyTransferRequest.getDestinationBankAccountId());
        transferMoneyResponse.setSourceBalance(sourceBankAccount.getBalance());
        transferMoneyResponse.setStatus("Success");

        return transferMoneyResponse;
    }

    private String generateAccountNumber() {
        long number = ThreadLocalRandom.current()
                .nextLong(100_000_000_000L, 1_000_000_000_000L);

        return String.valueOf(number);
    }
}
