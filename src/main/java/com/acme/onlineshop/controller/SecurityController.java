package com.acme.onlineshop.controller;

import com.acme.onlineshop.controller.errors.ErrorResponseCodes;
import com.acme.onlineshop.dto.ErrorResponse;
import com.acme.onlineshop.filters.ErrorCodeFilter;
import com.acme.onlineshop.service.SecurityService;
import com.acme.onlineshop.web.RESTVersionURL;
import com.acme.onlineshop.web.URL;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.WeakKeyException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping(value = URL.Path.REST_SECURITY)
@Tag(name = "Security", description = "Endpoints to control user privileges/authentications")
public class SecurityController {

    @Schema
    public record JWTToken(
            @Schema(description = "JSON Web Token (JWT)") String token
    ) { }

    @Schema
    public record JWTTokens(
            @Schema(description = "(Short living) Access token") String accessToken,
            @Schema(description = "(Long living) Refresh token") String refreshToken
    ) { }

    @Schema(description = "Request body for new access token")
    private static class AccessTokenRequest {

        @Schema(description = "Valid refresh token", required = true, minLength = 1)
        private String refreshToken;
        @Schema(description = "Time to live (TTL) in hours", defaultValue = "24", required = true, minimum = "1")
        private long timeToLiveInHrs;

        public AccessTokenRequest(String refreshToken, long timeToLiveInHrs) {
            this.refreshToken = refreshToken;
            this.timeToLiveInHrs = timeToLiveInHrs;
        }

        public AccessTokenRequest() {
            this("", 24);
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(@NotBlank String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public long getTimeToLiveInHrs() {
            return timeToLiveInHrs;
        }

        public void setTimeToLiveInHrs(@Positive long timeToLiveInHrs) {
            this.timeToLiveInHrs = timeToLiveInHrs;
        }
    }

    @Schema(description = "Request body for new refresh token")
    private static class RefreshTokenRequest {

        @Schema(description = "Unique username, that must exist", required = true, minLength = 1)
        private String username;
        @Schema(description = "Valid password belonging to given username", required = true, minLength = 1)
        private String password;
        @Schema(description = "Time to live (TTL) in hours", defaultValue = "8760", required = true, minimum = "1")
        private long timeToLiveInHrs;

        public RefreshTokenRequest(String username, String password, long timeToLiveInHrs) {
            this.username = username;
            this.password = password;
            this.timeToLiveInHrs = timeToLiveInHrs;
        }

        public RefreshTokenRequest() {
            this("", "", 8760L);
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            if(username == null || username.isBlank()) {
                throw new IllegalArgumentException("Username mustn't be empty");
            } else {
                this.username = username;
            }
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            if(password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password mustn't be empty");
            } else {
                this.password = password;
            }
        }

        public long getTimeToLiveInHrs() {
            return timeToLiveInHrs;
        }

        public void setTimeToLiveInHrs(long timeToLiveInHrs) {
            if(timeToLiveInHrs < 1) {
                throw new IllegalArgumentException("Time to live must be greater than 0");
            } else {
                this.timeToLiveInHrs = timeToLiveInHrs;
            }
        }
    }

    // private static final Logger LOGGER = LoggerFactory.getLogger(SecurityController.class);
    private final SecurityService securityService;

    @Autowired
    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Hidden
    @ExceptionHandler({WeakKeyException.class, DecodingException.class})
    public ResponseEntity<ErrorResponse> handleGenerationFailure(HttpServletResponse response, JwtException exc) {
        response.setIntHeader(ErrorCodeFilter.ERROR_CODE_FIELD, ErrorResponseCodes.COMMON_ERROR.errorCode);
        ErrorResponse error = new ErrorResponse(ErrorResponseCodes.JWT_ERROR, exc.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Hidden
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleValidationFailure(HttpServletResponse response) {
        response.setIntHeader(ErrorCodeFilter.ERROR_CODE_FIELD, ErrorResponseCodes.COMMON_ERROR.errorCode);
        ErrorResponse error = new ErrorResponse(ErrorResponseCodes.COMMON_ERROR.errorCode, "Validation failed");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //------------------------------------------------------ GET -------------------------------------------------------

    @Operation(summary = "Returns last generated access & refresh JSON Web Tokens (JWTs) for given user [or an empty string if until now no token was generated], if user identified by its (unique) username is successfully validated")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns current access & refresh JSON Web Tokens (JWTs)",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JWTTokens.class))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "To minimize any given information for security reasons, this error is returned for any possible failures, e.g.: user does not exist, password wrong, etc. etc.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @GetMapping(value = RESTVersionURL.URL.V_01 + "/token")
    public JWTTokens getCurrentToken(
            @Parameter(description = "Name of desired user") @RequestParam @NotBlank String username,
            @Parameter(description = "Password belonging to user") @RequestParam @NotBlank String password) {
        return securityService.getCurrentTokens(username, password);
    }

    //----------------------------------------------------- POST -------------------------------------------------------

    @Operation(summary = "Generates and returns a new access JSON Web Token (JWT), if given refresh token is successfully validated")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Generates & returns a new access JSON Web Token (JWT)",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JWTToken.class))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "To minimize any given information for security reasons, this error is returned for any possible failures, e.g.: user does not exist, password wrong, token expired, etc. etc.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping(RESTVersionURL.URL.V_01 + "/token/access")
    public JWTToken generateNewAccessToken(@RequestBody AccessTokenRequest requestBody) {
        return new JWTToken(securityService.generateNewAccessToken(requestBody.getRefreshToken(), requestBody.getTimeToLiveInHrs()));
    }

    @Operation(summary = "Generates and returns a new refresh JSON Web Token (JWT), if user identified by its (unique) username is successfully validated")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Generates & returns a new refresh JSON Web Token (JWT)",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JWTToken.class))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "To minimize any given information for security reasons, this error is returned for any possible faults, e.g.: user does not exist, password wrong, token expired, etc. etc.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping(RESTVersionURL.URL.V_01 + "/token/refresh")
    public JWTToken generateNewRefreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return new JWTToken(securityService.generateNewRefreshToken(refreshTokenRequest.getUsername(), refreshTokenRequest.getPassword(), refreshTokenRequest.getTimeToLiveInHrs()));
    }
}
