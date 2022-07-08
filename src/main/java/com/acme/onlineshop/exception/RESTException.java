package com.acme.onlineshop.exception;

import com.acme.onlineshop.controller.errors.ErrorResponseCodes;

public class RESTException extends RuntimeException {

    protected ErrorResponseCodes errorCode = ErrorResponseCodes.COMMON_ERROR;

    public RESTException() {
        super();
    }

    public RESTException(String message) {
        super(message);
    }

    public RESTException(Throwable throwable) {
        super(throwable);
    }

    public RESTException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ErrorResponseCodes getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorResponseCodes errorCode) {
        this.errorCode = errorCode;
    }
}
