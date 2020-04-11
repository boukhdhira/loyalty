package com.network.shopping.config;

import com.network.shopping.security.jwt.JwtAuthAccessDeniedEntryPoint;
import com.network.shopping.security.jwt.JwtAuthEntryPoint;
import com.network.shopping.security.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile("!test")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService jwtUserDetailsService;

    private final JwtFilter jwtRequestFilter;

    private final JwtAuthEntryPoint unauthorizedHandler;

    private final JwtAuthAccessDeniedEntryPoint accessDeniedHandler;

    public SecurityConfig(final UserDetailsService jwtUserDetailsService
            , final JwtFilter jwtRequestFilter, final JwtAuthEntryPoint unauthorizedHandler, final JwtAuthAccessDeniedEntryPoint accessDeniedHandler) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * configure AuthenticationManager so that it knows from
     * where to load user for matching credentials
     * Use BCryptPasswordEncoder to encode user password
     */
    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.jwtUserDetailsService)
                .passwordEncoder(this.passwordEncoder());
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(this.unauthorizedHandler)
                .accessDeniedHandler(this.accessDeniedHandler)
                .and()
                // don't authenticate this particular request
                .authorizeRequests()
                .antMatchers("/api/v1/management/**").hasAuthority("ROLE_ADMIN")
                .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**").permitAll()
                .antMatchers("/api/v1/signup").permitAll()
                .antMatchers("/api/v1/activate").permitAll()
                .antMatchers("/api/v1/bonus").permitAll()
                .antMatchers("/api/signin").permitAll()
                .anyRequest().authenticated()
                // make sure we use stateless session; session won't be used to store user's state.
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // Add a filter to validate the tokens with every request
        http.addFilterBefore(this.jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
