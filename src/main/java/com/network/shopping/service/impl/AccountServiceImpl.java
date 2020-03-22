package com.network.shopping.service.impl;

import com.network.shopping.domain.Account;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
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
    public Page<AccountDTO> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(account -> accountMapper.toDto(account));
    }

    @Override
    public AccountDTO getUserAccountByNumber(String number) {
        return accountRepository.findByNumber(number)
                .map(account -> accountMapper.toDto(account))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid account number"))
                ;
    }

    @Override
    public AccountDTO addAccount(AccountDTO accountDTO) {
        if (accountRepository.findByNumber(accountDTO.getNumber()).isPresent()) {
            throw new IllegalArgumentException("Account number already exist !");
        }
        Set<String> cards = accountDTO.getCreditCards();
        if (!isEmpty(cards) && cards.stream().anyMatch(number -> accountRepository.findByCreditCardsNumber(number).isPresent())) {
            throw new IllegalArgumentException("Credit card is already used for an other account !");
        }
        accountRepository.saveAndFlush(accountMapper.toEntity(accountDTO));
        return accountDTO;
    }

    @Override
    public AccountDTO addBeneficiariesToAccount(String accountId, List<BeneficiaryDTO> beneficiaryDTOS) {
        Account accountData = retriveAccountDataById(accountId);
        accountData.getBeneficiaries().addAll(beneficiaryDTOS.stream()
                .filter(Objects::nonNull)
                .map(dto -> beneficiaryMapper.toEntity(dto))
                .collect(Collectors.toSet()));
        return accountMapper.toDto(accountRepository.save(accountData));
    }

    private Account retriveAccountDataById(String accountId) {
        Optional<Account> accountOptional = accountRepository.findByNumber(accountId);
        if (!accountOptional.isPresent()) {
            throw new IllegalArgumentException("Invalid account number: " + accountId);
        }
        return accountOptional.get();
    }

    @Override
    public AccountDTO addCreditCardToAccount(String accountId, String cardNumber) {
        Account account = retriveAccountDataById(accountId);
        if (account.getCreditCards().stream().anyMatch(card -> card.getNumber().equals(cardNumber))) {
            throw new IllegalArgumentException(
                    format("Credit card number %s is already used for account %s", cardNumber, accountId));
        }
        CreditCard card = new CreditCard();
        card.setNumber(cardNumber);
        account.getCreditCards().add(card);
        return accountMapper.toDto(accountRepository.save(account));
    }
}
