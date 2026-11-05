package org.venky.payflow.transaction.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.venky.payflow.transaction.dto.TransactionFilter;
import org.venky.payflow.transaction.entity.Transaction;
import org.venky.payflow.transaction.enums.TransactionDirection;

import java.util.ArrayList;
import java.util.List;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<Transaction> filterTransactions(
            List<String> userAccountNumbers,
            List<String> selectedAccountNumbers,
            TransactionFilter filter
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory ownership filter.
            Predicate sentByUser = root
                    .get("sourceBankAccountNumber")
                    .in(userAccountNumbers);

            Predicate receivedByUser = root
                    .get("destinationBankAccountNumber")
                    .in(userAccountNumbers);

            predicates.add(cb.or(sentByUser, receivedByUser));

            // Optional account filter.
            if (selectedAccountNumbers != null
                    && !selectedAccountNumbers.isEmpty()) {

                predicates.add(cb.or(
                        root.get("sourceBankAccountNumber")
                                .in(selectedAccountNumbers),
                        root.get("destinationBankAccountNumber")
                                .in(selectedAccountNumbers)
                ));
            }

            // Optional direction filter.
            if (filter.getDirections() != null
                    && !filter.getDirections().isEmpty()) {

                List<Predicate> directionPredicates = new ArrayList<>();

                if (filter.getDirections()
                        .contains(TransactionDirection.SENT)) {
                    directionPredicates.add(sentByUser);
                }

                if (filter.getDirections()
                        .contains(TransactionDirection.RECEIVED)) {
                    directionPredicates.add(receivedByUser);
                }

                predicates.add(cb.or(
                        directionPredicates.toArray(new Predicate[0])
                ));
            }

            // Optional transaction types.
            if (filter.getTypes() != null
                    && !filter.getTypes().isEmpty()) {
                predicates.add(
                        root.get("type").in(filter.getTypes())
                );
            }

            // Optional transaction statuses.
            if (filter.getStatuses() != null
                    && !filter.getStatuses().isEmpty()) {
                predicates.add(
                        root.get("status").in(filter.getStatuses())
                );
            }

            // Optional start date (inclusive).
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        filter.getFromDate().atStartOfDay()
                ));
            }

            // Optional end date (inclusive).
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThan(
                        root.get("createdAt"),
                        filter.getToDate().plusDays(1).atStartOfDay()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}