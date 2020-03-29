package com.network.shopping.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.network.shopping.security.LoginVM;
import com.network.shopping.security.UserDetailsImpl;
import com.network.shopping.security.UserDetailsServiceImpl;
import com.network.shopping.security.jwt.JwtFilter;
import com.network.shopping.security.jwt.TokenProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.util.stream.Collectors.toList;

/**
 * User Sign-up & Login with username & password using JWT Authentication
 */
@RestController
//@CrossOrigin
@RequestMapping("/api")
@Slf4j
@Api("Manager user authentication/logout request")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, TokenProvider tokenProvider
            , UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @ApiOperation("login an account")
    @PostMapping(value = "/authenticate")
    public ResponseEntity createAuthenticationToken(@RequestBody @Valid LoginVM authenticationRequest) {
        log.debug("Attempt to login user by user name = {}", authenticationRequest.getUsername());
        Authentication authentication = this.authenticate(authenticationRequest.getUsername()
                , authenticationRequest.getPassword());
//        UserDetails userDetails = this.userDetailsService
//                .loadUserByUsername(authenticationRequest.getUsername());
        String token = this.tokenProvider.generateToken(authentication
                , (authenticationRequest.isRememberMe() == null) ? FALSE : authenticationRequest.isRememberMe());
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(toList());
        return new ResponseEntity<>(JWTToken.builder().idToken(token).username(authenticationRequest.getUsername())
                .roles(roles).build()
                , this.getAuthenticationResponseHeaders(token), HttpStatus.OK);
    }

    private HttpHeaders getAuthenticationResponseHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token);
        return httpHeaders;
    }

    private Authentication authenticate(String username, String password) {
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
//        } catch (DisabledException e) {
//            log.error("User account is actually disabled.");
//        } catch (AuthenticationException e) {
//            log.error("invalid credentials: " + e.getMessage());
//        }
    }


    /**
     * Object to return as body in JWT Authentication.
     */
    @Data
    @Builder
    static class JWTToken {

        private String idToken;
        private String username;
        private List<String> roles;

        @JsonProperty("id_token")
        String getIdToken() {
            return this.idToken;
        }

    }
}