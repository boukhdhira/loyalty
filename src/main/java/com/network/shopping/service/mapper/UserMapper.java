package com.network.shopping.service.mapper;

import com.network.shopping.dto.UserDTO;
import com.network.shopping.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PasswordEncoderMapper.class, AuthorityMapper.class})
public interface UserMapper {

    @Mapping(target = "password", qualifiedBy = EncodedMapping.class)
    @Mapping(target = "roles", source = "administrator", qualifiedBy = AuthorityMapping.class)
    User toEntity(UserDTO dto);

    UserDTO toDto(User user);

    List<UserDTO> toDtos(List<User> user);
}
