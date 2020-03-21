package com.network.shopping.exception;

import lombok.NonNull;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -1688754067556253652L;

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundException(@NonNull String message) {
        super(message);
    }
}
