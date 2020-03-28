package com.network.shopping.exception;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class TokenInvalidException extends AbstractThrowableProblem {

    private static final long serialVersionUID = -7845538559253492212L;

    public TokenInvalidException() {
        super(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri()
                , "Unrecognized token", Status.UNAUTHORIZED);
    }
}
