package org.venky.payflow.bankaccount.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.venky.payflow.bankaccount.dto.BankAccountResponse;
import org.venky.payflow.bankaccount.dto.CreateBankAccountRequest;
import org.venky.payflow.bankaccount.entity.BankAccount;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    BankAccount toEntity(CreateBankAccountRequest bankAccountRequest);

    BankAccountResponse toResponse(BankAccount bankAccount);

    List<BankAccountResponse> toResponseList(List<BankAccount> bankAccounts);
}
