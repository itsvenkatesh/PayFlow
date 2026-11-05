package org.venky.payflow.transaction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.venky.payflow.transaction.entity.Transaction;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> , JpaSpecificationExecutor<Transaction> {
    Page<Transaction> findBySourceBankAccountNumberOrDestinationBankAccountNumberOrderByCreatedAtDesc(
            String sourceBankAccountNumber,
            String destinationBankAccountNumber,
            Pageable pageable
    );

}
