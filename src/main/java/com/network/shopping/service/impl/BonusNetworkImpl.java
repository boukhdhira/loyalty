package com.network.shopping.service.impl;

import com.network.shopping.repository.AccountRepository;
import com.network.shopping.repository.BonusRepository;
import com.network.shopping.repository.StoreRepository;
import com.network.shopping.service.BonusNetwork;
import com.network.shopping.service.dto.BonusConfirmationDTO;
import com.network.shopping.service.dto.ShoppingDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BonusNetworkImpl implements BonusNetwork {
    private final StoreRepository storeRepository;
    private final BonusRepository bonusRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public BonusNetworkImpl(StoreRepository storeRepository, BonusRepository bonusRepository
            , AccountRepository accountRepository) {
        this.storeRepository = storeRepository;
        this.bonusRepository = bonusRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public BonusConfirmationDTO bonusAccountFor(ShoppingDTO shopping) {
        return new BonusConfirmationDTO();
    }
}
