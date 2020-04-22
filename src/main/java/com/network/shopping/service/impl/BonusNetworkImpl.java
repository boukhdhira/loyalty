package com.network.shopping.service.impl;

import com.network.shopping.dto.BonusConfirmationDTO;
import com.network.shopping.dto.ShoppingDTO;
import com.network.shopping.model.Account;
import com.network.shopping.model.Bonus;
import com.network.shopping.model.Store;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.repository.BonusRepository;
import com.network.shopping.repository.StoreRepository;
import com.network.shopping.service.BonusNetwork;
import com.network.shopping.service.event.OnBonusComputedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class BonusNetworkImpl implements BonusNetwork {
    private final StoreRepository storeRepository;
    private final BonusRepository bonusRepository;
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public BonusNetworkImpl(final StoreRepository storeRepository, final BonusRepository bonusRepository
            , final AccountRepository accountRepository, final ApplicationEventPublisher eventPublisher
            , final Clock clock) {
        this.storeRepository = storeRepository;
        this.bonusRepository = bonusRepository;
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public BonusConfirmationDTO bonusAccountFor(final ShoppingDTO shopping) {
        final String transactionCard = shopping.getCreditCardNumber().replaceAll("\\s+", "");
        final Optional<Account> optAccount = this.accountRepository.findByCreditCardsNumber(transactionCard);
        return optAccount.map(account -> {
            Optional<Store> optStore = this.storeRepository.findByMerchantNumber(shopping.getMerchantNumber());
            if (!optStore.isPresent()) {
                throw new IllegalArgumentException("Unrecognized merchant " + shopping.getMerchantNumber());
            }
            final Store store = optStore.get();
            BigDecimal benefitAmount = this.computeBenefit(store, shopping.getAmount());
            this.makeContribution(account, benefitAmount);

            return this.saveBonusOperation(shopping, account, benefitAmount);
        }).orElseThrow(() -> new IllegalArgumentException("Account not found: Invalid credit card or it's not registered "
                + shopping.getCreditCardNumber()));
    }

    /**
     * Submit bonus operation.
     */
    private BonusConfirmationDTO saveBonusOperation(final ShoppingDTO shopping, final Account account, final BigDecimal benefitAmount) {
        final String confirmationNumber = RandomStringUtils.random(10, false, true);
        final Bonus bonusData = new Bonus();
        bonusData.setBonusAmount(benefitAmount);
        bonusData.setAccountNumber(account.getNumber());
        bonusData.setProductNumber(shopping.getMerchantNumber());
        bonusData.setShoppingDate(LocalDate.parse(shopping.getDate()));
        bonusData.setShoppingAmount(shopping.getAmount());
        bonusData.setConfirmationNumber(confirmationNumber);
        bonusData.setBonusDate(LocalDate.now(this.clock));
        this.bonusRepository.save(bonusData);
        this.notifyClient(bonusData);
        log.debug("A new bonus request has been submitted {} ", bonusData);
        return BonusConfirmationDTO.builder().confirmationNumber(confirmationNumber).build();
    }

    /**
     * Compute a product benefits
     *
     * @param store  store
     * @param dining dining amount
     * @return benefit amount
     */
    private BigDecimal computeBenefit(final Store store, final BigDecimal dining) {
        log.info("Computing benefits for product {} ", store.getMerchantNumber());
        return store.getBenefitsPercentage().multiply(dining);
    }

    /**
     * Distribute benefits amount to an account beneficiaries
     * according to their percentage.
     *
     * @param account       account beneficiary
     * @param benefitAmount dining amount
     */
    private void makeContribution(final Account account, final BigDecimal benefitAmount) {
        account.getBeneficiaries().forEach(beneficiary -> {
            final BigDecimal saving = beneficiary.getSavings();
            beneficiary.setSavings(saving.add(benefitAmount.multiply(beneficiary.getAllocationPercentage())));
        });
        this.accountRepository.save(account);
        log.info("Making contribution to account {} by {} €", account.getNumber(), benefitAmount);
    }

    private void notifyClient(final Bonus bonusData) {
        this.eventPublisher.publishEvent(new OnBonusComputedEvent(bonusData));
    }
}
