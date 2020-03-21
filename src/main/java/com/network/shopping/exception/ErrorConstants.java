package com.network.shopping.exception;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_INVALID_CARD_NUMBER = "error.cardFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "http://127.0.0.1/";
    public static final URI ENTITY_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/entity-not-found");

    private ErrorConstants() {
    }
}