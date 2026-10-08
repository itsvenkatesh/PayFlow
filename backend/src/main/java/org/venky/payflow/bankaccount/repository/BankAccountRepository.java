package org.venky.payflow.bankaccount.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.venky.payflow.bankaccount.entity.BankAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    boolean existsByBankAccountNumber(String bankAccountNumber);

    Optional<BankAccount> findByBankAccountNumber(String bankAccountNumber);

    List<BankAccount> findByUserId(UUID userId);
}