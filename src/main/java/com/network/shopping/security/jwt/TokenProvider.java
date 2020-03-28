package com.network.shopping.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @TokenProvider is responsible for performing JWT operations like
 * creation and validation token.
 */
@Component
public class TokenProvider implements Serializable {

    private static final long serialVersionUID = -718545319933355976L;
    @Value("${jwt.token.validity}")
    private long jwtTokenValidity;
    @Value("${jwt.token.remember.validity}")
    private long jctTokenRememberValidity;
    @Value("${jwt.secret}")
    private String secret;

    public TokenProvider() {
    }

    /**
     * Retrieve username from jwt token
     *
     * @param token given web token
     * @return user name
     */
    public String getUsernameFromToken(String token) {
        return this.getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return this.getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = this.getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * for retrieving any information from token we will need the secret key
     *
     * @param token token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
    }

    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        Date expiration = this.getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * generate token for user
     */
    public String generateToken(UserDetails userDetails, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        return this.doGenerateToken(claims, userDetails.getUsername(), rememberMe);
    }

    /**
     * while creating the token :
     * 1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
     * 2. Sign the JWT using the HS512 algorithm and secret key.
     * 3. According to JWS Compact Serialization compaction of the JWT to a URL-safe string
     * {@link } https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
     */
    private String doGenerateToken(Map<String, Object> claims, String subject, boolean rememberMe) {
        Date validity;
        validity = rememberMe ?
                new Date(System.currentTimeMillis() + this.jctTokenRememberValidity * 1000)
                : new Date(System.currentTimeMillis() + this.jwtTokenValidity * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, this.secret)
                .compact();
    }

    /**
     * validate token
     *
     * @param token       given token
     * @param userDetails user credentials
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = this.getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !this.isTokenExpired(token));
    }
}