package com.network.shopping.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@SuppressWarnings({"unchecked", "rawtypes"})
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {
        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Errors on request validation {}", errors);
        final ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return this.handleExceptionInternal(
                ex, apiError, headers, apiError.getStatus(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<Object> handleResourceNotFoundException(final ResourceNotFoundException ex, final WebRequest request) {
        log.error("Requested resource was not found ");
        final ApiError apiError =
                new ApiError(NOT_FOUND, ex.getLocalizedMessage());
        return new ResponseEntity(apiError, NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<Object> handlePreconditionFailedException(final IllegalArgumentException ex, final WebRequest request) {
        log.error("Illegal requested argument: {} ", ex.getMessage());
        final ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
        return new ResponseEntity(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public final ResponseEntity<Object> handlePreconditionFailedException(final DataIntegrityViolationException ex, final WebRequest request) {
        log.error("Illegal requested argument: {} ", ex.getMessage());
        final ApiError apiError =
                new ApiError(HttpStatus.CONFLICT, ex.getLocalizedMessage());
        return new ResponseEntity(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity handleConstraintViolationException(final ConstraintViolationException constraintViolationException) {
        final Set<ConstraintViolation<?>> violations = constraintViolationException.getConstraintViolations();
        String errorMessage = "";
        final Map<String, String> details = new HashMap<>();
        if (!violations.isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            violations.forEach(violation -> {
                builder.append(" - " + violation.getMessage());
                details.put(((PathImpl) violation.getPropertyPath()).getLeafNode().asString(), violation.getMessage());
            });
            errorMessage = builder.toString();
        }
        log.error("Invalid requested data : {} ", errorMessage);
        return new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, errorMessage, details), HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(value
//            = { IllegalArgumentException.class, IllegalStateException.class })
//    protected ResponseEntity<Object> handleConflict(
//            RuntimeException ex, WebRequest request) {
//        String bodyOfResponse = "This should be application specific";
//        return handleExceptionInternal(ex, bodyOfResponse,
//                new HttpHeaders(), HttpStatus.CONFLICT, request);
//    }
}
