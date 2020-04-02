package com.network.shopping.service.impl;

import com.network.shopping.domain.User;
import com.network.shopping.repository.UserRepository;
import com.network.shopping.service.UserService;
import com.network.shopping.service.dto.MailRequest;
import com.network.shopping.service.dto.UserDTO;
import com.network.shopping.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final MailClient mailClient;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, MailClient mailClient) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.mailClient = mailClient;
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
        this.sendActivationMail(user);
        //TODO: mail mandatry + add account;
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

    private void sendActivationMail(User user) {
        MailRequest request = new MailRequest();
        request.setRecipient(user.getEmail());
        Map<String, Object> props = new HashMap<>();
        props.put("name", user.getLastName());
        //TODO: generate key for each new client
        props.put("activationKey", "DODKDSL5SSS");
        request.setProps(props);
        try {
            this.mailClient.sendActivation(request);
        } catch (MessagingException e) {
            log.error("Invalid mail address {}", user.getEmail(), e);
        } catch (IOException e) {
            log.error("Unable to load mail resource {}", e.getMessage());
        }
    }
}
