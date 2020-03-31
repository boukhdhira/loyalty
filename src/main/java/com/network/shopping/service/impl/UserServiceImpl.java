package com.network.shopping.service.impl;

import com.network.shopping.domain.User;
import com.network.shopping.repository.UserRepository;
import com.network.shopping.service.UserService;
import com.network.shopping.service.dto.UserDTO;
import com.network.shopping.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            throw new IllegalArgumentException(userDTO.getUsername() + ": user name  is already used for other account");
        }

        if (this.userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException(userDTO.getEmail() + ": user email is already used for other");
        }

        User user = this.userRepository.save(this.userMapper.toEntity(userDTO));
        log.debug("User registered successfully!: {}", user);
        //TODO: mailService.sendCreationEmail(newUser);
        return this.userMapper.toDto(user);
    }

    @Override
    public void deleteUser(String username) {
        this.userRepository.findOneByUsername(username)
                .ifPresent(user -> {
                    this.userRepository.delete(user);
                    log.debug("User identified by {}  was deleted ", username);
                });
    }

    @Override
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable).map(this.userMapper::toDto);
    }
}
