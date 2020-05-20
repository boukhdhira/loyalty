package com.network.shopping.service.impl;

import com.network.shopping.dto.MailRequest;
import com.network.shopping.model.ConfirmationToken;
import com.network.shopping.model.User;
import com.network.shopping.repository.ConfirmationTokenRepository;
import com.network.shopping.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;

import static com.network.shopping.config.Constants.*;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class RenewalKeyJobService {

    private final UserRepository userRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final MailClient mailClient;

    @Autowired
    public RenewalKeyJobService(final UserRepository userRepository, final ConfirmationTokenRepository confirmationTokenRepository
            , final MailClient mailClient) {
        this.userRepository = userRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.mailClient = mailClient;
    }

    /**
     * second, minute, hour, day, month, weekday: cron pattern
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void job() {
        log.info("Begin scheduled job to renewal account activation key ...");
        final List<User> blockedUsers = this.userRepository.findOneByCredentialsExpiredTrue();
        if (isEmpty(blockedUsers)) {
            log.debug("Empty blocked user account");
            return;
        }
        log.debug("Attempt to renewal activation key for accounts : [{}]", blockedUsers);
        final List<ConfirmationToken> tokens = new ArrayList<>();
        blockedUsers.forEach(user -> {
            final String newKey = UUID.randomUUID().toString();
            final ConfirmationToken tokenData = this.confirmationTokenRepository.findByUser(user);
            tokenData.setToken(newKey).setExpiryDate(now().plus(RENEWAL_TOKEN_EXPIRATION_HOURS, HOURS));
            user.setCredentialsExpired(false);
            tokens.add(tokenData);
            this.sendActivationMail(user, newKey);
        });
        this.userRepository.saveAll(blockedUsers);
        this.confirmationTokenRepository.saveAll(tokens);
    }

    /**
     * sending activation key by mail will make it execute in a separate thread i.e. the caller will
     * not wait for the completion of the called method.
     *
     * @param user  user information
     * @param token renewal token
     */
    @Async
    private void sendActivationMail(final User user, final String token) {
        final MailRequest request = new MailRequest();
        request.setRecipient(user.getEmail());
        final Map<String, Object> props = new HashMap<>();
        props.put("name", user.getLastName());
        props.put(ACTIVATION_KEY, token);
        props.put(TOKEN_EXPIRATION, RENEWAL_TOKEN_EXPIRATION_HOURS);
        props.put("unit", HOURS.name());
        request.setProps(props);
        try {
            this.mailClient.prepareAndSendActivation(request);
        } catch (final MessagingException e) {
            log.error("[email not sent] Invalid mail address {}", user.getEmail(), e);
        } catch (final IOException e) {
            log.error("[Technical error] Unable to load mail resources {}", e.getMessage());
        }
    }
}