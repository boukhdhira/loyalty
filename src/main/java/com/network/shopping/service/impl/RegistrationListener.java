package com.network.shopping.service.impl;

import com.network.shopping.domain.ConfirmationToken;
import com.network.shopping.domain.User;
import com.network.shopping.repository.ConfirmationTokenRepository;
import com.network.shopping.service.AccountService;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.MailRequest;
import com.network.shopping.service.dto.OnRegistrationCompleteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.network.shopping.config.Constants.ACTIVATION_KEY;

@Component
@Slf4j
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private static final String USER_NAME = "name";
    private final ConfirmationTokenRepository tokenRepository;
    private final MailClient mailClient;
    private final AccountService accountService;

    @Autowired
    public RegistrationListener(ConfirmationTokenRepository confirmationTokenRepository, MailClient mailClient
            , AccountService accountService) {
        this.tokenRepository = confirmationTokenRepository;
        this.mailClient = mailClient;
        this.accountService = accountService;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        this.createVerificationToken(user, token);
        this.sendActivationMail(user, token);
        AccountDTO account = this.accountService.createAccount(user.getUsername());
        log.debug("A new account was successfully created {}", account);
    }

    /**
     * Save validation token for user in params
     *
     * @param user  user entity
     * @param token UUID token
     */
    private void createVerificationToken(User user, String token) {
        ConfirmationToken confirmationToken = new ConfirmationToken(token, user);
        this.tokenRepository.save(confirmationToken);
        log.debug("Save confirmation token for user {}", user);
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
    private void sendActivationMail(User user, String token) {
        MailRequest request = new MailRequest();
        request.setRecipient(user.getEmail());
        Map<String, Object> props = new HashMap<>();
        props.put(USER_NAME, user.getLastName());
        props.put(ACTIVATION_KEY, token);
        request.setProps(props);
        try {
            this.mailClient.prepareAndSendActivation(request);
        } catch (MessagingException e) {
            log.error("[email not sent] Invalid mail address {}", user.getEmail(), e);
        } catch (IOException e) {
            log.error("[Technical error] Unable to load mail resources {}", e.getMessage());
        }
    }
}
