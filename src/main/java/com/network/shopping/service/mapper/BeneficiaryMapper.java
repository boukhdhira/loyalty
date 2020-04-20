package com.network.shopping.service.mapper;

import com.network.shopping.common.Percentage;
import com.network.shopping.dto.BeneficiaryDTO;
import com.network.shopping.model.Beneficiary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface BeneficiaryMapper {
    Set<Beneficiary> toEntities(Set<BeneficiaryDTO> dtos);

    @Mapping(target = "allocationPercentage", source = "percentage", qualifiedByName = "toDecimal")
    Beneficiary toEntity(BeneficiaryDTO dto);

    @Named("toDecimal")
    default BigDecimal toDecimal(final String percentage) {
        return Percentage.of(percentage).asBigDecimal();
    }
}
