package com.network.shopping.security.jwt;

import com.network.shopping.security.UserDetailsServiceImpl;
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


/**
 * For any incoming request, this Filter class gets executed. It checks
 * if the request has a valid JWT token. If it has a valid JWT Token, then
 * it sets the authentication in context to specify that the current user
 * is authenticated.
 * <p>
 * makes a single execution for each request to our API. It provides a
 * doFilterInternal() method that we will implement parsing & validating JWT,
 * loading User details (using UserDetailsService), checking Authorizaion
 * (using UsernamePasswordAuthenticationToken)
 */
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final UserDetailsServiceImpl jwtUserDetailsService;

    private final TokenProvider tokenProvider;

    @Autowired
    public JwtFilter(UserDetailsServiceImpl jwtUserDetailsService, TokenProvider tokenProvider) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String jwt = this.resolveToken(request);
            if (jwt != null && this.tokenProvider.validateJwtToken(jwt)) {
                String username = this.tokenProvider.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            this.logger.error("Cannot set user authentication: {}", e);
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
