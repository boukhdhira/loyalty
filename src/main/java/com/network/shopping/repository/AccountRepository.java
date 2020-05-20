package com.network.shopping.repository;

import com.network.shopping.model.Account;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByCreditCardsNumber(String number);

    Optional<Account> findOneByNumber(String number);

    Optional<Account> findOneByClientId(String clientId);

    //Unlike condition, unless expressions are evaluated after the method has been called.
    //result still refers to Account and not Optional. As it might be null, we should use the safe navigation operator.
    @Cacheable(value = "accountCache", key = "#number", unless = "#result == null")
    Optional<Account> findOneByNumberAndClientId(String number, String clientId);
}
