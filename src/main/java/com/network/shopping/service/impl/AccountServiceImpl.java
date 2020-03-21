package com.network.shopping.service.impl;

import com.network.shopping.exception.ResourceNotFoundException;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.service.AccountService;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
@Transactional
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;
    private AccountMapper accountMapper;

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
}
