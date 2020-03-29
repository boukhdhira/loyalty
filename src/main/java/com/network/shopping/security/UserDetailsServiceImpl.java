package com.network.shopping.security;

import com.network.shopping.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.debug("Fetching user login into database {}", login);


        if (new EmailValidator().isValid(login, null)) {
            return this.userRepository.findOneWithRolesByEmailIgnoreCase(login)
                    .map(UserDetailsImpl::build)
                    .orElseThrow(() -> new UsernameNotFoundException("Oups ! your username is incorrect: " + login));
        }

        String lowercaseLogin = login.toLowerCase();
        return this.userRepository.findOneWithRolesByUsername(lowercaseLogin)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("Oups ! your username is incorrect: " + login));
    }
}

