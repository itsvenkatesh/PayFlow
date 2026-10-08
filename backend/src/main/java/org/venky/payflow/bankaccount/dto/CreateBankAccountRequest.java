package org.venky.payflow.bankaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBankAccountRequest {

    @NotBlank
    private String bankName;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;
}
