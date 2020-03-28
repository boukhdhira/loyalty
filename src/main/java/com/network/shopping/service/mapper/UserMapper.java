package com.network.shopping.service.mapper;

import com.network.shopping.domain.User;
import com.network.shopping.service.dto.UserDTO;
import com.network.shopping.service.utils.EncodedMapping;
import com.network.shopping.service.utils.PasswordEncoderMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PasswordEncoderMapper.class)
public interface UserMapper {

    @Mapping(target = "password", qualifiedBy = EncodedMapping.class)
    User toEntity(UserDTO dto);

    UserDTO toDto(User user);
}
