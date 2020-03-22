package com.network.shopping.service.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class BeneficiaryDTO {
    @NotBlank(message = "Beneficiary name is mandatory")
    private String name;
    @Range(min = 0, max = 100, message = "Invalid percentage")
    private BigDecimal percentage;

    private BigDecimal savings;
}
