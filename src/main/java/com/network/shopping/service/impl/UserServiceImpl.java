package com.network.shopping.service.impl;

import com.network.shopping.domain.ConfirmationToken;
import com.network.shopping.domain.User;
import com.network.shopping.repository.ConfirmationTokenRepository;
import com.network.shopping.repository.UserRepository;
import com.network.shopping.service.UserService;
import com.network.shopping.service.dto.OnRegistrationCompleteEvent;
import com.network.shopping.service.dto.UserDTO;
import com.network.shopping.service.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static com.network.shopping.config.Constants.TOKEN_EXPIRATION;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final MailClient mailClient;

    private final ApplicationEventPublisher eventPublisher;

    private final ConfirmationTokenRepository tokenRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, MailClient mailClient
            , ApplicationEventPublisher eventPublisher, ConfirmationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.mailClient = mailClient;
        this.eventPublisher = eventPublisher;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Save user account and send activation key throw an email
     * Generate activation key and store also send email are
     * considered a parallel treatment (Async) and it will be
     * handled throw async event.
     * <b>All type of users needs to be activated on creation.</b>
     *
     * @param userDTO user data information
     * @return created entity
     */
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
        this.eventPublisher.publishEvent(new OnRegistrationCompleteEvent
                (user));
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

    @Override
    public void activateRegistration(String key) {
        Optional<ConfirmationToken> tokenRecord = this.tokenRepository.findByToken(key);
        User user = tokenRecord.map(token -> {
            if (this.isExpired(token)) {
                throw new DataIntegrityViolationException("Token is expired");
            }
            return token.getUser();
        }).orElseThrow(() -> new IllegalArgumentException("Invalid activation key"));
        user.setEnabled(true);
        this.userRepository.save(user);
        log.debug("user is enabled now {}", user);
    }

    private boolean isExpired(ConfirmationToken token) {
        return this.calculateExpiryDate(token.getCreatedDate()).before(new Date());
    }

    private Date calculateExpiryDate(Date creationDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(creationDate);
        cal.add(Calendar.MINUTE, TOKEN_EXPIRATION);
        return new Date(cal.getTime().getTime());
    }
}
