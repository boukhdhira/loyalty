package com.network.shopping.exception;

import lombok.NonNull;

//@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1105850725177644962L;

    public BadRequestException(@NonNull String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException() {
        super();
    }
}
