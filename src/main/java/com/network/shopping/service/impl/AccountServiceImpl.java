package com.network.shopping.service.impl;

import com.network.shopping.common.Percentage;
import com.network.shopping.dto.AccountDTO;
import com.network.shopping.dto.BeneficiaryDTO;
import com.network.shopping.exception.ResourceNotFoundException;
import com.network.shopping.model.Account;
import com.network.shopping.model.Beneficiary;
import com.network.shopping.model.CreditCard;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.service.AccountService;
import com.network.shopping.service.mapper.AccountMapper;
import com.network.shopping.service.mapper.BeneficiaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
    public void setBeneficiaryMapper(final BeneficiaryMapper beneficiaryMapper) {
        this.beneficiaryMapper = beneficiaryMapper;
    }

    /**
     * Setter for property 'accountMapper'.
     *
     * @param accountMapper Value to set for property 'accountMapper'.
     */
    @Autowired
    public void setAccountMapper(final AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * Setter for property 'accountRepository'.
     *
     * @param accountRepository Value to set for property 'accountRepository'.
     */
    @Autowired
    public void setAccountRepository(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @CacheEvict(value = "accountCache", key = "#accountId")
    public void deleteAccount(final String accountId) {
        this.accountRepository.findOneByNumber(accountId).ifPresent(account -> {
            this.accountRepository.delete(account);
            log.debug("Deleted account: {}", account);
        });
    }

    @Override
    public Page<AccountDTO> getAllAccounts(final Pageable pageable) {
        return this.accountRepository.findAll(pageable).map(account -> this.accountMapper.toDto(account));
    }

    @Override
    //TODO: handle admin role to load all accounts
    public AccountDTO getUserAccountByNumber(final String number, final String clientId) {
        return this.accountRepository.findOneByNumberAndClientId(number, clientId)
                .map(account -> this.accountMapper.toDto(account))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid account number or you are not agile to access"))
                ;
    }

    @Override
    public AccountDTO createAccount(final AccountDTO accountDTO) {
        if (!isEmpty(accountDTO.getBeneficiaries())) {
            this.validateBeneficiariesPercentage(accountDTO);
        }

        if (this.accountRepository.findOneByNumber(accountDTO.getNumber()).isPresent()) {
            throw new DataIntegrityViolationException("Account number already exist !");
        }
        final Set<String> cards = accountDTO.getCreditCards();
        if (!isEmpty(cards) && cards.stream().anyMatch(number -> this.accountRepository.findByCreditCardsNumber(number).isPresent())) {
            throw new DataIntegrityViolationException("Credit card is already used for an other account !");
        }

        if (!isEmpty(accountDTO.getBeneficiaries()) && accountDTO.getBeneficiaries().size() == 1) {
            accountDTO.getBeneficiaries().stream().findFirst().ifPresent(p -> p.setPercentage(Percentage.oneHundred().toString()));
        }

        this.accountRepository.save(this.accountMapper.toEntity(accountDTO));
        return accountDTO;
    }

    @Override
    public AccountDTO createAccount(final String clientId) {
        final Account account = new Account();
        account.setClientId(clientId);
        account.setNumber(random(9, false, true));
        account.setName(DEFAULT_ACCOUNT_NAME);
        return this.accountMapper.toDto(this.accountRepository.save(account));
    }


    @Override
    public AccountDTO addBeneficiariesToAccount(final String accountId, final List<BeneficiaryDTO> beneficiaryDTOS, final String clientId) {
        final Account accountData = this.retrieveAccountDataById(accountId, clientId);
        return this.accountMapper.toDto(this.getUpdatedAccountBeneficiaries(beneficiaryDTOS, accountData));
    }

    @CachePut(value = "accountCache", key = "#accountData.number")
    private Account getUpdatedAccountBeneficiaries(final List<BeneficiaryDTO> beneficiaryDTOS, final Account accountData) {
        this.computeBeneficiariesNewAllocationsPercentage(accountData, beneficiaryDTOS);
        accountData.getBeneficiaries().addAll(beneficiaryDTOS.stream()
                .filter(Objects::nonNull)
                .map(dto -> this.beneficiaryMapper.toEntity(dto))
                .collect(toSet()));
        return this.accountRepository.saveAndFlush(accountData);
    }

    private void computeBeneficiariesNewAllocationsPercentage(final Account account, final List<BeneficiaryDTO> beneficiaryDTOS) {
        if (isEmpty(account.getBeneficiaries())) {
            log.info("Empty beneficiaries list ...  attempts to add {} beneficiaries ", beneficiaryDTOS.size());
            if (beneficiaryDTOS.size() == 1 && !beneficiaryDTOS.get(0).getPercentage().equals(oneHundred().toString())) {
                beneficiaryDTOS.get(0).setPercentage(oneHundred().toString());
            }
            return;
        }
        final Percentage totalPercentage = this.computeAllocationPercentages(beneficiaryDTOS);
        final BigDecimal remainingPercentage = BigDecimal.ONE.subtract(totalPercentage.asBigDecimal());
        account.getBeneficiaries()
                .forEach(person -> person.setAllocationPercentage(person.getAllocationPercentage().multiply(remainingPercentage)));
    }

    private Percentage computeAllocationPercentages(final List<BeneficiaryDTO> beneficiaryDTOS) {
        return beneficiaryDTOS
                .stream()
                .filter(Objects::nonNull)
                .map(beneficiaryDTO -> Percentage.of(beneficiaryDTO.getPercentage()))
                .reduce(zero(), Percentage::add);
    }

    public Account retrieveAccountDataById(final String accountId, final String clientId) {
        final Optional<Account> accountOptional = this.accountRepository.findOneByNumberAndClientId(accountId, clientId);
        if (!accountOptional.isPresent()) {
            throw new IllegalArgumentException("Invalid account number: " + accountId);
        }
        return accountOptional.get();
    }

    @Override
    public AccountDTO addCreditCardToAccount(final String accountId, final String cardNumber, final String clientId) {
        final Account account = this.retrieveAccountDataById(accountId, clientId);
        if (account.getCreditCards().stream().anyMatch(card -> cardNumber.equals(card.getNumber()))) {
            throw new IllegalArgumentException(
                    format("Credit card number %s is already used for account %s", cardNumber, accountId));
        }
        this.updateAccountData(cardNumber, account);
        return this.accountMapper.toDto(this.accountRepository.save(account));
    }

    @CachePut(value = "accountCache", key = "#account.number")
    private void updateAccountData(final String cardNumber, final Account account) {
        final CreditCard card = new CreditCard().setNumber(cardNumber);
        account.getCreditCards().add(card);
    }

    @Override
    public void removeBeneficiary(final String accountId, final String beneficiaryName, final String clientId) {
        final Account account = this.retrieveAccountDataById(accountId, clientId);
        Set<Beneficiary> beneficiaries = account.getBeneficiaries();
        final Optional<Beneficiary> deletedBeneficiary = beneficiaries.stream()
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
            final BigDecimal p = deletedBeneficiary.get().getAllocationPercentage();
            final BigDecimal remaining = new BigDecimal(beneficiaries.size() - 1);
            final BigDecimal extra = p.divide(remaining);
            final BigDecimal extraAmount = deletedBeneficiary.get().getSavings().divide(remaining);

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
        this.accountRepository.save(account);
    }

    @Override
    public String getAccountIdByClient(final String clientId) {
        return this.accountRepository.findOneByClientId(clientId).map(
                Account::getNumber
        ).orElseThrow(() -> new DataIntegrityViolationException("Cannot find account for authenticated user"));
    }

    @Override
    @CachePut(value = "accountCache", key = "#account.number")
    public void updateUserAccount(final AccountDTO account, final String userId) {
        final String accountNumber = account.getNumber();
        final Account accountEntity = this.retrieveAccountDataById(accountNumber, userId);

        final Set<String> cards = account.getCreditCards();
        if (!isEmpty(cards) && cards.stream().anyMatch(number -> this.accountRepository.findByCreditCardsNumber(number).isPresent())) {
            throw new DataIntegrityViolationException("Credit card is already used for an other account !");
        }

        if (!isEmpty(account.getBeneficiaries()) && account.getBeneficiaries().size() == 1) {
            account.getBeneficiaries().stream().findFirst().ifPresent(p -> p.setPercentage(Percentage.oneHundred().toString()));
        }

        if (!isEmpty(account.getBeneficiaries())) {
            this.validateBeneficiariesPercentage(account);
            accountEntity.setBeneficiaries(this.beneficiaryMapper.toEntities(account.getBeneficiaries()));
        }

        if (!isEmpty(account.getCreditCards())) {
            accountEntity.setCreditCards(account.getCreditCards().stream().map(e -> {
                CreditCard c = new CreditCard();
                c.setNumber(e);
                return c;
            }).collect(toSet()));
        }

        if (!account.getName().equals(accountEntity.getNumber())) {
            accountEntity.setName(account.getName());
        }
        // to maintain entity version
        this.accountRepository.save(accountEntity);
    }

    @Override
    public void updateBeneficiaryPercentage(final String accountId, final String beneficiaryName, final BeneficiaryDTO beneficiary, final String clientId) {
        if (!beneficiary.getName().equals(beneficiaryName)) {
            throw new IllegalArgumentException("unrecognized beneficiary " + beneficiaryName);
        }
    }

    private void validateBeneficiariesPercentage(final AccountDTO account) {
        this.computeAllocationPercentages(new ArrayList<>(account.getBeneficiaries()));
    }
}
