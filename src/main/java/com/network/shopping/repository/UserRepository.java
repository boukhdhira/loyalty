package com.network.shopping.repository;

import com.network.shopping.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneWithRolesByEmailIgnoreCase(String login);

    Optional<User> findOneWithRolesByUsername(String username);

    Optional<User> findOneByUsername(String username);

    Optional<User> findOneByEmailIgnoreCase(String email);
}
