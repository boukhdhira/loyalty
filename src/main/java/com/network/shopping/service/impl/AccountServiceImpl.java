package com.network.shopping.service.impl;

import com.network.shopping.common.Percentage;
import com.network.shopping.domain.Account;
import com.network.shopping.domain.Beneficiary;
import com.network.shopping.domain.CreditCard;
import com.network.shopping.exception.ResourceNotFoundException;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.service.AccountService;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import com.network.shopping.service.mapper.AccountMapper;
import com.network.shopping.service.mapper.BeneficiaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.network.shopping.common.Percentage.oneHundred;
import static com.network.shopping.common.Percentage.zero;
import static com.network.shopping.config.Constants.DEFAULT_ACCOUNT_NAME;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private BeneficiaryMapper beneficiaryMapper;
    private AccountRepository accountRepository;
    private AccountMapper accountMapper;

    /**
     * Setter for property 'beneficiaryMapper'.
     *
     * @param beneficiaryMapper Value to set for property 'beneficiaryMapper'.
     */
    @Autowired
    public void setBeneficiaryMapper(BeneficiaryMapper beneficiaryMapper) {
        this.beneficiaryMapper = beneficiaryMapper;
    }

    /**
     * Setter for property 'accountMapper'.
     *
     * @param accountMapper Value to set for property 'accountMapper'.
     */
    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * Setter for property 'accountRepository'.
     *
     * @param accountRepository Value to set for property 'accountRepository'.
     */
    @Autowired
    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void deleteAccount(String accountId) {
        this.accountRepository.findOneByNumber(accountId).ifPresent(account -> {
            this.accountRepository.delete(account);
            log.debug("Deleted account: {}", account);
        });
    }

    @Override
    public Page<AccountDTO> getAllAccounts(Pageable pageable) {
        return this.accountRepository.findAll(pageable).map(account -> this.accountMapper.toDto(account));
    }

    @Override
    public AccountDTO getUserAccountByNumber(String number) {
        return this.accountRepository.findOneByNumber(number)
                .map(account -> this.accountMapper.toDto(account))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid account number"))
                ;
    }

    @Override
    public AccountDTO createAccount(AccountDTO accountDTO) {
        if (!isEmpty(accountDTO.getBeneficiaries())) {
            this.validateBeneficiariesPercentage(accountDTO);
        }

        if (this.accountRepository.findOneByNumber(accountDTO.getNumber()).isPresent()) {
            throw new DataIntegrityViolationException("Account number already exist !");
        }
        Set<String> cards = accountDTO.getCreditCards();
        if (!isEmpty(cards) && cards.stream().anyMatch(number -> this.accountRepository.findByCreditCardsNumber(number).isPresent())) {
            throw new DataIntegrityViolationException("Credit card is already used for an other account !");
        }

        if (!isEmpty(accountDTO.getBeneficiaries()) && accountDTO.getBeneficiaries().size() == 1) {
            accountDTO.getBeneficiaries().stream().findFirst().ifPresent(p -> p.setPercentage(Percentage.oneHundred().toString()));
        }

        this.accountRepository.saveAndFlush(this.accountMapper.toEntity(accountDTO));
        return accountDTO;
    }

    @Override
    public AccountDTO createAccount(String clientId) {
        Account account = new Account();
        account.setClientId(clientId);
        account.setNumber(random(9, false, true));
        account.setName(DEFAULT_ACCOUNT_NAME);
        return this.accountMapper.toDto(this.accountRepository.saveAndFlush(account));
    }


    private void validateBeneficiariesPercentage(AccountDTO account) {
        this.computeAllocationPercentages(new ArrayList<>(account.getBeneficiaries()));
    }

    @Override
    public AccountDTO addBeneficiariesToAccount(String accountId, List<BeneficiaryDTO> beneficiaryDTOS) {
        Account accountData = this.retrieveAccountDataById(accountId);
        this.computeBeneficiariesNewAllocationsPercentage(accountData, beneficiaryDTOS);
        accountData.getBeneficiaries().addAll(beneficiaryDTOS.stream()
                .filter(Objects::nonNull)
                .map(dto -> this.beneficiaryMapper.toEntity(dto))
                .collect(toSet()));
        return this.accountMapper.toDto(this.accountRepository.save(accountData));
    }

    private void computeBeneficiariesNewAllocationsPercentage(Account account, List<BeneficiaryDTO> beneficiaryDTOS) {
        if (isEmpty(account.getBeneficiaries())) {
            log.info("Empty beneficiaries list ...  attempts to add {} beneficiaries ", beneficiaryDTOS.size());
            if (beneficiaryDTOS.size() == 1 && !beneficiaryDTOS.get(0).getPercentage().equals(oneHundred().toString())) {
                beneficiaryDTOS.get(0).setPercentage(oneHundred().toString());
            }
            return;
        }
        Percentage totalPercentage = this.computeAllocationPercentages(beneficiaryDTOS);
        BigDecimal remainingPercentage = BigDecimal.ONE.subtract(totalPercentage.asBigDecimal());
        account.getBeneficiaries()
                .forEach(person -> person.setAllocationPercentage(person.getAllocationPercentage().multiply(remainingPercentage)));
    }

    private Percentage computeAllocationPercentages(List<BeneficiaryDTO> beneficiaryDTOS) {
        return beneficiaryDTOS
                .stream()
                .filter(Objects::nonNull)
                .map(beneficiaryDTO -> Percentage.of(beneficiaryDTO.getPercentage()))
                .reduce(zero(), Percentage::add);
    }

    private Account retrieveAccountDataById(String accountId) {
        Optional<Account> accountOptional = this.accountRepository.findOneByNumber(accountId);
        if (!accountOptional.isPresent()) {
            throw new IllegalArgumentException("Invalid account number: " + accountId);
        }
        return accountOptional.get();
    }

    @Override
    public AccountDTO addCreditCardToAccount(String accountId, String cardNumber) {
        Account account = this.retrieveAccountDataById(accountId);
        if (account.getCreditCards().stream().anyMatch(card -> cardNumber.equals(card.getNumber()))) {
            throw new IllegalArgumentException(
                    format("Credit card number %s is already used for account %s", cardNumber, accountId));
        }
        CreditCard card = new CreditCard();
        card.setNumber(cardNumber);
        account.getCreditCards().add(card);
        return this.accountMapper.toDto(this.accountRepository.save(account));
    }

    @Override
    public void removeBeneficiary(String accountId, String beneficiaryName) {
        Account account = this.retrieveAccountDataById(accountId);
        Set<Beneficiary> beneficiaries = account.getBeneficiaries();
        Optional<Beneficiary> deletedBeneficiary = beneficiaries.stream()
                .filter(beneficiary -> beneficiary.getName().equalsIgnoreCase(beneficiaryName))
                .findFirst();
        if (!deletedBeneficiary.isPresent()) {
            throw new IllegalArgumentException("No such beneficiary with name " + beneficiaryName);
        }
        // If we are removing the only beneficiary or the beneficiary has an
        // allocation of zero we don't need to worry. Otherwise, need to share
        // out the benefit of the deleted beneficiary amongst all the others
        if (beneficiaries.size() == 1 || deletedBeneficiary.get()
                .getAllocationPercentage().equals(BigDecimal.ZERO)) {
            beneficiaries.removeIf(b -> b.getName().equalsIgnoreCase(beneficiaryName));
        } else {
            // This logic is very simplistic, doesn't account for rounding errors
            BigDecimal p = deletedBeneficiary.get().getAllocationPercentage();
            BigDecimal remaining = new BigDecimal(beneficiaries.size() - 1);
            BigDecimal extra = p.divide(remaining);
            BigDecimal extraAmount = deletedBeneficiary.get().getSavings().divide(remaining);

            beneficiaries = beneficiaries.stream()
                    .filter(beneficiary -> beneficiary != deletedBeneficiary.get())
                    .peek(beneficiary -> {
                        BigDecimal percentage = beneficiary.getAllocationPercentage();
                        BigDecimal saving = beneficiary.getSavings();
                        beneficiary.setAllocationPercentage(percentage.add(extra));
                        beneficiary.setSavings(saving.add(extraAmount));
                    }).collect(toSet());
        }
        account.setBeneficiaries(beneficiaries);
        this.accountRepository.saveAndFlush(account);
    }

    @Override
    public String getAccountIdByClient(String clientId) {
        return this.accountRepository.findOneByClientId(clientId).map(
                Account::getNumber
        ).orElseThrow(() -> new DataIntegrityViolationException("Cannot find account for authenticated user"));
    }
}
