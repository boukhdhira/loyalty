package com.network.shopping.repository;

import com.network.shopping.model.ConfirmationToken;
import com.network.shopping.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByToken(String token);

    ConfirmationToken findByUser(User user);
}
