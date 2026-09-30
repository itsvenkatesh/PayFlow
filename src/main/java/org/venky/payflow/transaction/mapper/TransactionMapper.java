package org.venky.payflow.transaction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.venky.payflow.transaction.dto.TransactionResponse;
import org.venky.payflow.transaction.entity.Transaction;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "id", target = "transactionId")
    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}