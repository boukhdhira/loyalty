package com.network.shopping.security.jwt;

import com.network.shopping.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * @TokenProvider is responsible for performing JWT operations like
 * creation and validation token.
 */
@Component
@Slf4j
public class TokenProvider implements Serializable {

    private static final long serialVersionUID = -718545319933355976L;
    @Value("${jwt.token.validity}")
    private long jwtTokenValidity;
    @Value("${jwt.token.remember.validity}")
    private long jctTokenRememberValidity;
    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Authentication authentication, boolean rememberMe) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date validity;
        validity = rememberMe ?
                new Date(System.currentTimeMillis() + this.jctTokenRememberValidity * 1000)
                : new Date(System.currentTimeMillis() + this.jwtTokenValidity * 1000);

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(this.secret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}