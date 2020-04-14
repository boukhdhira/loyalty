package com.network.shopping.service.impl;

import com.network.shopping.domain.Bonus;
import com.network.shopping.domain.User;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.repository.UserRepository;
import com.network.shopping.service.dto.MailRequest;
import com.network.shopping.service.event.OnBonusComputedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BonusNotifierListener implements ApplicationListener<OnBonusComputedEvent> {
    private static final String BONUS_AMOUNT = "bonusAmount";
    private static final String SHOPPING_AMOUNT = "shoppingAmount";
    private final MailClient mailClient;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public BonusNotifierListener(final MailClient mailClient, final AccountRepository repository
            , final UserRepository userRepository) {
        this.mailClient = mailClient;
        this.accountRepository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(final OnBonusComputedEvent event) {
        final Bonus bonusContribution = event.getBonus();
        final String recipientAddress = this.getReceptionAddress(bonusContribution.getAccountNumber());
        final MailRequest request = new MailRequest();
        request.setRecipient(recipientAddress);
        final Map<String, Object> props = new HashMap<>();
        props.put(BONUS_AMOUNT, bonusContribution.getBonusAmount());
        props.put(SHOPPING_AMOUNT, bonusContribution.getShoppingAmount());
        request.setProps(props);
        try {
            this.mailClient.prepareAndSendBonus(request);
        } catch (final MessagingException e) {
            log.error("[email not sent] Invalid mail address {}", recipientAddress, e);
        } catch (final IOException e) {
            log.error("[Technical error] Unable to load mail resources {}", e.getMessage());
        }
    }

    private String getReceptionAddress(final String accountId) {
        return this.accountRepository.findOneByNumber(accountId)
                .flatMap(account -> this.userRepository.findOneByUsername(account.getClientId()))
                .map(User::getEmail).orElseThrow(() -> new IllegalStateException("unrecognized account"));
    }
}
