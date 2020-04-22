package com.network.shopping.service.mapper;

import com.network.shopping.common.Percentage;
import com.network.shopping.dto.StoreDTO;
import com.network.shopping.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    @Mapping(target = "benefitsPercentage", source = "benefitsPercentage", qualifiedByName = "toDecimal")
    Store toEntity(StoreDTO sto);

    @Named("toDecimal")
    default BigDecimal toDecimal(final String percentage) {
        return Percentage.of(percentage).asBigDecimal();
    }
}
