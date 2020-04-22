package com.network.shopping.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShoppingDTO {
    public static final String MIN_AMOUNT = "0.0";
    @NotNull(message = "Shopping amount is mandatory")
    @DecimalMin(value = MIN_AMOUNT, inclusive = false, message = "Invalid amount")
    private BigDecimal amount;
    @NotNull(message = "credit card used in the shopping transaction is mandatory")
    @CreditCardNumber(ignoreNonDigitCharacters = true, message = "Invalid credit card number")
    private String creditCardNumber;
    @NotBlank(message = "merchant number is mandatory")
    @Pattern(regexp = "[0-9]{10}", message = "Invalid merchant number")
    private String merchantNumber;
    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String date;
//    @JsonIgnore
//    private final LocalDateTime editedAt = LocalDateTime.now();
}
