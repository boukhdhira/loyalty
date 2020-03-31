package com.network.shopping.security.jwt;

import com.network.shopping.security.EntryPointResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * (@)ExceptionHandler will only work if the request is handled by the DispatcherServlet.
 * However this exception occurs before that as it is thrown by a Filter. So you will
 * never be able to handle this exception with an (@)ExceptionHandler.
 */
@Component
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        log.error("Unauthorized error: {}", e.getMessage());
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.getOutputStream().println(
                EntryPointResponse.builder().timestamp(new Timestamp(System.currentTimeMillis()).toString())
                        .status(HttpStatus.UNAUTHORIZED.getReasonPhrase()).code(HttpServletResponse.SC_UNAUTHORIZED + "")
                        .error(e.getMessage()).build().toString());
    }
}
