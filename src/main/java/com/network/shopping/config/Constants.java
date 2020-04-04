package com.network.shopping.config;

public final class Constants {
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";
    public static final String USER_NAME_REGEX = "^[A-Za-z]*$";
    public static final String ACCOUNT_NUMBER_REGEX = "^[0-9]{9}$";
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final String ACTIVATION_KEY = "activationKey";
    public static final String DEFAULT_ACCOUNT_NAME = "LoyaltY";
    public static final int TOKEN_EXPIRATION = 60 * 24;
}
