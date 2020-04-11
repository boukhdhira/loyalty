package com.network.shopping.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class StoreDTO {

    @NotBlank(message = "Merchant number is mandatory")
    @Pattern(regexp = "[0-9]{10}", message = "Invalid merchant number")
    private String merchantNumber;

    @NotBlank(message = "Store name is mandatory")
    private String name;

    @NotBlank
    @Pattern(regexp = "^[0-9]{1,3}%$", message = "Invalid benefits percentage")
    private String benefitsPercentage;

    @NotBlank(message = "benefits policy is mandatory")
    private String benefitsAvailabilityPolicy;
}
