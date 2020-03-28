package com.network.shopping.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.network.shopping.exception.AccountDisabledException;
import com.network.shopping.exception.InvalidCredentialsException;
import com.network.shopping.security.DomainUserDetailsService;
import com.network.shopping.security.LoginVM;
import com.network.shopping.security.jwt.JwtFilter;
import com.network.shopping.security.jwt.TokenProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static java.lang.Boolean.FALSE;

@RestController
//@CrossOrigin
@RequestMapping("/api")
@Slf4j
@Api("Manager user authentication/logout request")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private DomainUserDetailsService userDetailsService;

    @ApiOperation("Handle login attempt")
    @PostMapping(value = "/authenticate")
    public ResponseEntity createAuthenticationToken(@RequestBody @Valid LoginVM authenticationRequest) {
        log.debug("Attempt to login user by user name = {}", authenticationRequest.getUsername());
        this.authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        UserDetails userDetails = this.userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        String token = this.tokenProvider.generateToken(userDetails
                , (authenticationRequest.isRememberMe() == null) ? FALSE : authenticationRequest.isRememberMe());
        //String jwt = tokenProvider.createToken(authentication, rememberMe);
        return new ResponseEntity<>(new JWTToken(token), this.getAuthenticationResponseHeaders(token), HttpStatus.OK);
    }

    private HttpHeaders getAuthenticationResponseHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        return httpHeaders;
    }

    private void authenticate(String username, String password) {
        try {
            this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new AccountDisabledException("User account is actually disabled.");
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("INVALID CREDENTIALS: " + e.getMessage());
        }
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return this.idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}