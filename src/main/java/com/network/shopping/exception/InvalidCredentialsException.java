package com.network.shopping.exception;

import lombok.NonNull;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class InvalidCredentialsException extends AbstractThrowableProblem {
    private static final long serialVersionUID = 8412741208203363793L;

    public InvalidCredentialsException() {
        super(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri()
                , "Incorrect password", Status.BAD_REQUEST);
    }

    public InvalidCredentialsException(@NonNull String message) {
        super(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri(), message, Status.BAD_REQUEST);
    }
}
