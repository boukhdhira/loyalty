package com.network.shopping.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel(description = "All details about an account beneficiary.")
// disable override saving property
@JsonIgnoreProperties(value = "savings", allowGetters = true)
public class BeneficiaryDTO {
    @NotBlank(message = "Beneficiary name is mandatory")
    @ApiModelProperty(notes = "Beneficiary name")
    private String name;
    @Range(min = 0, max = 100, message = "Invalid percentage")
    @ApiModelProperty(notes = "Beneficiary allocation percentage")
    private BigDecimal percentage;
    @ApiModelProperty(notes = "Beneficiary savings amount")
    private BigDecimal savings = BigDecimal.ZERO;
}
