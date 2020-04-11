package com.network.shopping.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@ApiModel(description = "All details about an account beneficiary.")
// disable override/serialisation on "saving" property idem @JsonIgnore to setter and @JsonProperty to getter
@JsonIgnoreProperties(value = "savings", allowGetters = true)
public class BeneficiaryDTO {
    @NotBlank(message = "Beneficiary name is mandatory")
    @ApiModelProperty(notes = "Beneficiary name")
    private String name;
    @ApiModelProperty(notes = "Beneficiary allocation percentage")
    @NotBlank(message = "allocation percentage is mandatory")
    @Pattern(regexp = "^[0-9]{1,3}%$", message = "Invalid allocation percentage")
    private String percentage;
    @ApiModelProperty(notes = "Beneficiary savings amount")
    private BigDecimal savings = BigDecimal.ZERO;
}
