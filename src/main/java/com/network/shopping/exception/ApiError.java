package com.network.shopping.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Runtime error data
 * status: the HTTP status code
 * message: the error message associated with exception
 * details: List of constructed error/validation messages
 */
@Data
@JsonInclude(content = JsonInclude.Include.NON_NULL)
public class ApiError {

    private HttpStatus status;
    private String message;
    private Map<String, String> details;

    public ApiError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiError(HttpStatus status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        details = errors;
    }
}

