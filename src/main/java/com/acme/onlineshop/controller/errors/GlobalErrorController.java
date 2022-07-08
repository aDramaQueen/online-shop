package com.acme.onlineshop.controller.errors;

import com.acme.onlineshop.dto.ErrorResponse;
import com.acme.onlineshop.exception.RESTException;
import com.acme.onlineshop.filters.ErrorCodeFilter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalErrorController extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorController.class);

    /**
     * Handles all direct {@link Exception}s thrown by any controller which are <b>NOT</b> caught on controller level
     *
     * @param response  {@link HttpServletResponse} to manipulate HTTP response beyond the body
     * @param exc       Exception that was thrown during execution of a called method
     * @return          A new response entity that holds an error code &amp; an error error message
     * @see             <a href="https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc">Exception Handling in Spring MVC</a>
     */
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "400",
                    description = "Error on application level occurred, most likely due malformed request on clientside.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponse> handleRESTException(HttpServletResponse response, Exception exc) throws Exception {
        LOGGER.error("Unsuspected error occurred...", exc);
        // If the exception is annotated with @ResponseStatus rethrow it and let the framework handle it
        if (AnnotationUtils.findAnnotation(exc.getClass(), ResponseStatus.class) != null) {
            throw exc;
        } else {
            response.setIntHeader(ErrorCodeFilter.ERROR_CODE_FIELD, ErrorResponseCodes.COMMON_ERROR.errorCode);
            ErrorResponse error = new ErrorResponse(ErrorResponseCodes.COMMON_ERROR.errorCode, exc.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles all direct {@link RESTException}s thrown by any controller which are <b>NOT</b> caught on controller
     * level
     *
     * @param response  {@link HttpServletResponse} to manipulate HTTP response beyond the body
     * @param exc       Exception that was thrown during execution of a called method
     * @return          A new response entity that holds an error code &amp; an error error message
     * @see             <a href="https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc">Exception Handling in Spring MVC</a>
     */
    @ExceptionHandler(value = {RESTException.class})
    private ResponseEntity<ErrorResponse> handleBACnetError(HttpServletResponse response, RESTException exc) {
        // If the exception is annotated with @ResponseStatus rethrow it and let the framework handle it
        if (AnnotationUtils.findAnnotation(exc.getClass(), ResponseStatus.class) != null) {
            throw exc;
        } else {
            response.setIntHeader(ErrorCodeFilter.ERROR_CODE_FIELD, exc.getErrorCode().errorCode);
            ErrorResponse error = new ErrorResponse(exc.getErrorCode(), exc.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
