package com.network.shopping.service.mapper;

import com.network.shopping.domain.User;
import com.network.shopping.service.dto.UserDTO;
import com.network.shopping.service.utils.AuthorityMapper;
import com.network.shopping.service.utils.AuthorityMapping;
import com.network.shopping.service.utils.EncodedMapping;
import com.network.shopping.service.utils.PasswordEncoderMapper;
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
