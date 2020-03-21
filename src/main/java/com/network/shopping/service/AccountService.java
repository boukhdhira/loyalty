package com.network.shopping.service;

import com.network.shopping.service.dto.AccountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    Page<AccountDTO> getAllAccounts(Pageable pageable);

    AccountDTO getUserAccountByNumber(String number);

    AccountDTO addAccount(AccountDTO accountDTO);
}

