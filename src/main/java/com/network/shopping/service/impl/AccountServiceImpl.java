package com.network.shopping.service.impl;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    BeneficiaryMapper beneficiaryMapper;
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
        accountRepository.findOneByNumber(accountId).ifPresent(account -> {
            accountRepository.delete(account);
            log.debug("Deleted account: {}", account);
        });
    }

    @Override
    public Page<AccountDTO> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(account -> accountMapper.toDto(account));
    }

    @Override
    public AccountDTO getUserAccountByNumber(String number) {
        return accountRepository.findOneByNumber(number)
                .map(account -> accountMapper.toDto(account))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid account number"))
                ;
    }

    @Override
    public AccountDTO addAccount(AccountDTO accountDTO) {
        if (accountRepository.findOneByNumber(accountDTO.getNumber()).isPresent()) {
            throw new DataIntegrityViolationException("Account number already exist !");
        }
        Set<String> cards = accountDTO.getCreditCards();
        if (!isEmpty(cards) && cards.stream().anyMatch(number -> accountRepository.findByCreditCardsNumber(number).isPresent())) {
            throw new DataIntegrityViolationException("Credit card is already used for an other account !");
        }
        accountRepository.saveAndFlush(accountMapper.toEntity(accountDTO));
        return accountDTO;
    }

    @Override
    public AccountDTO addBeneficiariesToAccount(String accountId, List<BeneficiaryDTO> beneficiaryDTOS) {
        Account accountData = retrieveAccountDataById(accountId);
        accountData.getBeneficiaries().addAll(beneficiaryDTOS.stream()
                .filter(Objects::nonNull)
                .map(dto -> beneficiaryMapper.toEntity(dto))
                .collect(toSet()));
        return accountMapper.toDto(accountRepository.save(accountData));
    }

    private Account retrieveAccountDataById(String accountId) {
        Optional<Account> accountOptional = accountRepository.findOneByNumber(accountId);
        if (!accountOptional.isPresent()) {
            throw new IllegalArgumentException("Invalid account number: " + accountId);
        }
        return accountOptional.get();
    }

    @Override
    public AccountDTO addCreditCardToAccount(String accountId, String cardNumber) {
        Account account = retrieveAccountDataById(accountId);
        if (account.getCreditCards().stream().anyMatch(card -> cardNumber.equals(card.getNumber()))) {
            throw new IllegalArgumentException(
                    format("Credit card number %s is already used for account %s", cardNumber, accountId));
        }
        CreditCard card = new CreditCard();
        card.setNumber(cardNumber);
        account.getCreditCards().add(card);
        return accountMapper.toDto(accountRepository.save(account));
    }

    @Override
    public void removeBeneficiary(String accountId, String beneficiaryName) {
        Account account = retrieveAccountDataById(accountId);
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
        accountRepository.saveAndFlush(account);
    }
}
