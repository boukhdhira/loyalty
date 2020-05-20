package com.network.shopping.service.impl;

import com.network.shopping.dto.AccountDTO;
import com.network.shopping.dto.MailRequest;
import com.network.shopping.model.ConfirmationToken;
import com.network.shopping.model.User;
import com.network.shopping.repository.ConfirmationTokenRepository;
import com.network.shopping.service.AccountService;
import com.network.shopping.service.event.OnRegistrationCompleteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.network.shopping.config.Constants.*;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;

@Component
@Slf4j
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private static final String USER_NAME = "name";
    private final ConfirmationTokenRepository tokenRepository;
    private final MailClient mailClient;
    private final AccountService accountService;

    @Autowired
    public RegistrationListener(final ConfirmationTokenRepository confirmationTokenRepository, final MailClient mailClient
            , final AccountService accountService) {
        this.tokenRepository = confirmationTokenRepository;
        this.mailClient = mailClient;
        this.accountService = accountService;
    }

    @Override
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        final User user = event.getUser();
        final String token = UUID.randomUUID().toString();
        this.generateActivationToken(user, token);
        this.sendActivationMail(user, token);
        final AccountDTO account = this.accountService.createAccount(user.getUsername());
        log.debug("A new account was successfully created {}", account);
    }

    /**
     * Save validation token for user in params
     *
     * @param user  user entity
     * @param token UUID token
     */
    private void generateActivationToken(final User user, final String token) {
        final ConfirmationToken confirmationToken = new ConfirmationToken().setToken(token).setUser(user)
                .setExpiryDate(this.fetchExpiryDate(now()));
        this.tokenRepository.save(confirmationToken);
        log.debug("Save confirmation token for user {}", user);
    }

    private LocalDateTime fetchExpiryDate(final LocalDateTime creationDate) {
        return creationDate.plus(TOKEN_EXPIRATION_MINUTES, MINUTES);
    }

    /**
     * Send activation mail to user that contain activation key
     * in order to activate their account.
     * When user email is wrong address then user can never get 4
     * his activation key.
     *
     * @param user  user entity
     * @param token activation key
     */
    private void sendActivationMail(final User user, final String token) {
        final MailRequest request = new MailRequest();
        request.setRecipient(user.getEmail());
        final Map<String, Object> props = new HashMap<>();
        props.put(USER_NAME, user.getLastName());
        props.put(ACTIVATION_KEY, token);
        props.put(TOKEN_EXPIRATION, TOKEN_EXPIRATION_MINUTES);
        request.setProps(props);
        try {
            this.mailClient.prepareAndSendActivation(request);
        } catch (final MessagingException e) {
            //Todo: implement retray michanism
            log.error("[email not sent] Invalid mail address {}", user.getEmail(), e);
        } catch (final IOException e) {
            log.error("[Technical error] Unable to load mail resources {}", e.getMessage());
        }
    }
}
