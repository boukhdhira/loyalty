package com.network.shopping.service.impl;

import com.network.shopping.domain.User;
import com.network.shopping.repository.UserRepository;
import com.network.shopping.service.UserService;
import com.network.shopping.service.dto.UserDTO;
import com.network.shopping.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (this.userRepository.findOneByUsername(userDTO.getUsername().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException(userDTO.getUsername() + ": user name already used");
        }

        if (this.userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException();
        }

        User user = this.userRepository.save(this.userMapper.toEntity(userDTO));
        log.debug("Created Information for User: {}", user);
        //TODO: mailService.sendCreationEmail(newUser);
        return this.userMapper.toDto(user);
    }
}
