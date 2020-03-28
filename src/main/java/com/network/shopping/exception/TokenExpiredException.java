package com.network.shopping.exception;

import lombok.NonNull;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class TokenExpiredException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 898944764493186401L;

    public TokenExpiredException() {
        super(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri()
                , "Token validity duration has been expired", Status.UNAUTHORIZED);
    }

    public TokenExpiredException(@NonNull String message) {
        super(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri(), message, Status.UNAUTHORIZED);
    }
}
