package com.network.shopping.service.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

@Data
public class BeneficiaryDTO {
    private String name;
    @Range(min = 0, max = 100)
    private BigDecimal percentage;

    private BigDecimal savings;
}
