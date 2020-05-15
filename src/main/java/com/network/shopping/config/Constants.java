package com.network.shopping.config;

public final class Constants {
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";
    public static final String USER_NAME_REGEX = "^[A-Za-z]*$";
    public static final String ACCOUNT_NUMBER_REGEX = "^[0-9]{9}$";
    public static final String CREDIT_CARD_NUMBER_REGEX = "^(?:(?<visa>4[0-9]{12}(?:[0-9]{3})?)|" +
            "(?<mastercard>5[1-5][0-9]{14})|" +
            "(?<discover>6(?:011|5[0-9]{2})[0-9]{12})|" +
            "(?<amex>3[47][0-9]{13})|" +
            "(?<diners>3(?:0[0-5]|[68][0-9])?[0-9]{11})|" +
            "(?<jcb>(?:2131|1800|35[0-9]{3})[0-9]{11}))$";
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final String ACTIVATION_KEY = "activationKey";
    public static final String DEFAULT_ACCOUNT_NAME = "LoyaltY";
    public static final int TOKEN_EXPIRATION_MINUTES = 30;
    public static final String TOKEN_EXPIRATION = "tokenExpiration";

    //To disable the public one
    private Constants() {
    }
}
