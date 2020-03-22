package com.network.shopping.service.mapper;

import com.network.shopping.domain.Beneficiary;
import com.network.shopping.service.dto.BeneficiaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface BeneficiaryMapper {
    Set<Beneficiary> toEntitys(Set<BeneficiaryDTO> dtos);

    @Mapping(target = "allocationPercentage", source = "percentage")
    Beneficiary toEntity(BeneficiaryDTO dto);
}
