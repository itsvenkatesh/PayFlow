package org.venky.payflow.transaction.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.venky.payflow.transaction.enums.TransactionDirection;
import org.venky.payflow.transaction.enums.TransactionStatus;
import org.venky.payflow.transaction.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

@Data
public class TransactionFilter {

    private List<String> bankAccountNumbers;

    private List<TransactionDirection> directions;

    private List<TransactionType> types;

    private List<TransactionStatus> statuses;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}