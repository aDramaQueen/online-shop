package com.acme.onlineshop.dto;

import com.acme.onlineshop.controller.errors.ErrorResponseCodes;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;

/**
 * <b>D</b>ata <b>A</b>ccess <b>O</b>bject (<b>DAO</b>) which shall be consumed from a {@link ResponseEntity}. It holds an error code &amp; a errorMessage
 */
public record ErrorResponse(
        @Schema(description = "Error status code") int errorCode,
        @Schema(description = "Message with more specific error information") String errorMessage) {

    public ErrorResponse(ErrorResponseCodes errorCode, String errorMessage) {
        this(errorCode.errorCode, errorMessage);
    }
}
