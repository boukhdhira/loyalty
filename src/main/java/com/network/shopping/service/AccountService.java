package com.network.shopping.service;

import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {
    void deleteAccount(String accountId);

    Page<AccountDTO> getAllAccounts(Pageable pageable);

    AccountDTO getUserAccountByNumber(String number);

    AccountDTO addAccount(AccountDTO accountDTO);

    AccountDTO addBeneficiariesToAccount(String accountId, List<BeneficiaryDTO> beneficiaryDTOS);

    AccountDTO addCreditCardToAccount(String accountId, String cardNumber);
}

