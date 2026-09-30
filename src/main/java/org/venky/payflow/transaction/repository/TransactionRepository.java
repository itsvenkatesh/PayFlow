package org.venky.payflow.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.venky.payflow.transaction.entity.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findBySourceBankAccountIdOrDestinationBankAccountId(
            UUID sourceBankAccountId,
            UUID destinationBankAccountId
    );

}
