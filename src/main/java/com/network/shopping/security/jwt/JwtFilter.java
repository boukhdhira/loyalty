package com.network.shopping.security.jwt;

import com.network.shopping.exception.TokenExpiredException;
import com.network.shopping.security.DomainUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * For any incoming request, this Filter class gets executed. It checks
 * if the request has a valid JWT token. If it has a valid JWT Token, then
 * it sets the authentication in context to specify that the current user
 * is authenticated.
 */
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final DomainUserDetailsService jwtUserDetailsService;

    private final TokenProvider tokenProvider;

    @Autowired
    public JwtFilter(DomainUserDetailsService jwtUserDetailsService, TokenProvider tokenProvider) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String username = null;
        String jwtToken = null;
        try {
            jwtToken = this.resolveToken(request);
            username = this.tokenProvider.getUsernameFromToken(jwtToken);
        } catch (IllegalArgumentException e) {
            log.error("JWT Token is not found");
            //throw new TokenInvalidException();
        } catch (ExpiredJwtException e) {
            log.error("JWT Token has been expired");
            throw new TokenExpiredException();
        }
        // Once we get the token validate it.
        if (isNotEmpty(username) && StringUtils.hasText(jwtToken)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
            // if token is valid configure Spring Security to manually set
            // authentication
            if (Boolean.TRUE.equals(this.tokenProvider.validateToken(jwtToken, userDetails))) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * JWT Token is in the form "Bearer token".
     * Remove Bearer word and get only the Token
     *
     * @param request HttpServletRequest
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        log.warn("JWT Token was not recognized");
        return null;
    }
}
