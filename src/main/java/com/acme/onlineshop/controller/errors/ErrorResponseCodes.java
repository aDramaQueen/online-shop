package com.acme.onlineshop.controller.errors;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.Map;

@Schema(name = "Error Response Codes")
public enum ErrorResponseCodes {

    OK(0, "Request processed successfully."),
    COMMON_ERROR(1, "An error occurred during processing request."),
    NOT_IMPLEMENTED_ERROR(2, "This functionality is not supported."),
    JWT_ERROR(10, "An error occurred during processing a JSON web token request.");

    public final int errorCode;
    public final String errorMessage;

    ErrorResponseCodes(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static Map<Integer, String> getAllErrorCodes() {
        ErrorResponseCodes[] allCodes = values();
        Map<Integer, String> result = new HashMap<>(allCodes.length);
        for(ErrorResponseCodes code: values()){
            result.put(code.errorCode, code.errorMessage);
        }
        return result;
    }
}
