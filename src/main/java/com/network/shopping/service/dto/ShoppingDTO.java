package com.network.shopping.service.dto;

import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ShoppingDTO {
    @NotNull(message = "Shopping amount is mandatory")
    private BigDecimal amount;
    @NotNull(message = "credit card used in the shopping transaction is mandatory")
    @CreditCardNumber(ignoreNonDigitCharacters = true)
    private String creditCardNumber;
    @NotNull
    @NotBlank
    @Size(max = 10, min = 10)
    private String merchantNumber;
    @NotNull
    //@DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate date;
//    @JsonIgnore
//    private final LocalDateTime editedAt = LocalDateTime.now();
}
