package com.network.shopping.service.mapper;

import com.network.shopping.common.Percentage;
import com.network.shopping.domain.Beneficiary;
import com.network.shopping.service.dto.BeneficiaryDTO;
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
    default BigDecimal toDecimal(String percentage) {
        return Percentage.of(percentage).asBigDecimal();
    }
}
