package com.network.shopping.exception;

import lombok.NonNull;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class AccountDisabledException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 970019377117725808L;

    public AccountDisabledException() {
        super(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri()
                , "Incorrect password", Status.BAD_REQUEST);
    }

    public AccountDisabledException(@NonNull String message) {
        super(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri(), message, Status.BAD_REQUEST);
    }
}
