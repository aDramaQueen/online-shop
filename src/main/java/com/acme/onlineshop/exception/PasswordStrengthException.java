package com.acme.onlineshop.exception;

import java.util.List;

public class PasswordStrengthException extends RuntimeException {

    private final List<String> errorMessages;

    public PasswordStrengthException(String message, List<String> errorMessages) {
        super(message);
        this.errorMessages = errorMessages;
    }

    public PasswordStrengthException(List<String> errorMessages) {
        this("Password not strong enough", errorMessages);
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
