package com.network.shopping.service.dto;

import com.network.shopping.service.utils.NotEmptyFields;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
public class AccountDTO {
    @NotEmptyFields(message = "Account must have at least one credit card")
    Set<String> creditCards = new HashSet<>();
    Set<BeneficiaryDTO> beneficiaries = new HashSet<>();
    @NotBlank(message = "account number is mandatory")
    @Pattern(regexp = "^[0-9]{9}$", message = "account number must contain 9 digits")
    private String number;
    @NotBlank(message = "account name is mandatory")
    @Size(max = 50, message = "Account name too large")
    private String name;
}
