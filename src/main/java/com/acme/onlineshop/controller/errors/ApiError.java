package com.acme.onlineshop.controller.errors;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "API Error", description = "Error response with error code and specific error errorMessage")
public record ApiError(
        @Schema(name = "Error Code", description = "Look up endpoint for error codes, to have an overview of all possible error codes") int errorCode,
        @Schema(name = "Error Message", description = "Specific errorMessage(s), that describe(s) the error(s)") List<String> errors) {

    public ApiError(ErrorResponseCodes errorCode, String error) {
        this(errorCode.errorCode, List.of(error));
    }

    public ApiError(int errorCode, String error) {
        this(errorCode, List.of(error));
    }

    public ApiError(int errorCode, Exception exception) {
        this(errorCode, List.of(exception.getLocalizedMessage()));
    }

    public ApiError(ErrorResponseCodes errorCode, Exception exception) {
        this(errorCode.errorCode, List.of(exception.getLocalizedMessage()));
    }

    public ApiError(int errorCode, Iterable<Exception> exceptions) {
        this(errorCode, getErrors(exceptions));
    }

    public ApiError(ErrorResponseCodes errorCode, Iterable<Exception> exceptions) {
        this(errorCode.errorCode, getErrors(exceptions));
    }

    private static List<String> getErrors(Iterable<Exception> exceptions) {
        List<String> result = new ArrayList<>();
        exceptions.forEach(exc -> result.add(exc.getLocalizedMessage()));
        return result;
    }
}
