package com.network.shopping.security.jwt;

import com.network.shopping.security.EntryPointResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * Whenever a user attempts to access a page that is restricted to roles they do not have,
 * the application will return a status code of 403, which means Access Denied.
 */
@Component
@Slf4j
public class JwtAuthAccessDeniedEntryPoint implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        log.error("Access Denied error: {}", e.getMessage());
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        httpServletResponse.getOutputStream().println(
                EntryPointResponse.builder().timestamp(new Timestamp(System.currentTimeMillis()).toString())
                        .status(HttpStatus.FORBIDDEN.getReasonPhrase()).code(HttpServletResponse.SC_FORBIDDEN + "")
                        .error(e.getMessage()).build().toString());
    }
}
