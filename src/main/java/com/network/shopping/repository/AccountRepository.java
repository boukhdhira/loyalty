package com.network.shopping.repository;

import com.network.shopping.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByCreditCardsNumber(String number);

    Optional<Account> findByNumber(String number);
}
