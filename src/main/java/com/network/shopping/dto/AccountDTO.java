package com.network.shopping.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.network.shopping.service.utils.CreditCards;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

import static com.network.shopping.config.Constants.ACCOUNT_NUMBER_REGEX;

@Data
@Accessors(chain = true)
@ApiModel(description = "All details about an account. ")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {
    @ApiModelProperty(notes = "The account reattached credit card list")
    @CreditCards(message = "Account must have only a valid credit card")
    Set<String> creditCards = new HashSet<>();
    @ApiModelProperty(notes = "The account reattached list of beneficiaries")
    Set<BeneficiaryDTO> beneficiaries = new HashSet<>();
    @NotBlank(message = "account number is mandatory")
    @Pattern(regexp = ACCOUNT_NUMBER_REGEX, message = "account number must contain 9 digits")
    @ApiModelProperty(notes = "The account identifier number")
    private String number;
    @NotBlank(message = "account name is mandatory")
    @Size(max = 50, message = "Account name too large")
    @ApiModelProperty(notes = "The account entitled name")
    private String name;
}
