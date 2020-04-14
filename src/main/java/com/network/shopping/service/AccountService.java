package com.network.shopping.service;

import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {
    void deleteAccount(String accountId);

    Page<AccountDTO> getAllAccounts(Pageable pageable);

    AccountDTO getUserAccountByNumber(String number, String clientId);

    AccountDTO createAccount(AccountDTO accountDTO);

    AccountDTO createAccount(String clientId);

    AccountDTO addBeneficiariesToAccount(String accountId, List<BeneficiaryDTO> beneficiaryDTOS, String clientId);

    AccountDTO addCreditCardToAccount(String accountId, String cardNumber, String clientId);

    void removeBeneficiary(String accountId, String beneficiaryName, String clientId);

    String getAccountIdByClient(String clientId);

    void updateUserAccount(AccountDTO account, String userId);

    void updateBeneficiaryPercentage(String accountId, String beneficiaryName, BeneficiaryDTO beneficiary, String clientId);
}

