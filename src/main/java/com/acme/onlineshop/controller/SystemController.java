package com.acme.onlineshop.controller;

import com.acme.onlineshop.controller.errors.ErrorResponseCodes;
import com.acme.onlineshop.dto.ErrorResponse;
import com.acme.onlineshop.dto.SystemInfoDTO;
import com.acme.onlineshop.filters.ErrorCodeFilter;
import com.acme.onlineshop.service.SystemService;
import com.acme.onlineshop.utils.Profile;
import com.acme.onlineshop.web.OpenAPIConfig;
import com.acme.onlineshop.web.RESTVersionURL;
import com.acme.onlineshop.web.URL;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tomcat.util.net.openssl.ciphers.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.List;
import java.util.Map;

import static com.acme.onlineshop.web.OpenAPIConfig.ERROR_CODE_MAPPER;

@RestController
@RequestMapping(value = URL.Path.REST_SYSTEM)
@Tag(name = "System", description = "Endpoints to control some application behavior")
public class SystemController {

    // private static final Logger LOGGER = LoggerFactory.getLogger(SystemController.class);

    private final static String newBehavior = "The application will follow new behavior rule.";
    private final static String metersGroupedByProtocol = "All meter devices of specified protocol";
    private final SystemService systemService;

    @Hidden
    @ExceptionHandler(IOException.class)
    private ResponseEntity<ErrorResponse> handleIOError(HttpServletResponse response, IOException exc) {
        response.setIntHeader(ErrorCodeFilter.ERROR_CODE_FIELD, ErrorResponseCodes.COMMON_ERROR.errorCode);
        ErrorResponse error = new ErrorResponse(ErrorResponseCodes.COMMON_ERROR.errorCode, exc.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Autowired
    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    //------------------------------------------------------ GET -------------------------------------------------------

    @Operation(summary = "Returns currently active profiles", security = { @SecurityRequirement(name = OpenAPIConfig.BEARER_KEY) })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns currently active profile",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = Profile.class))}
            )
    })
    @GetMapping(RESTVersionURL.URL.V_01 + "/current-profiles")
    public String getCurrentProfiles() {
        return systemService.getCurrentProfile().prettyName;
    }

    @Operation(summary = "Returns time zone currently in use", security = { @SecurityRequirement(name = OpenAPIConfig.BEARER_KEY) })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns current time zone",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = String.class), examples = @ExampleObject(value = "Singapore"))}
            )
    })
    @GetMapping(RESTVersionURL.URL.V_01 + "/time-zone")
    public String getTimeZone() {
        return systemService.getTimeZone();
    }

    @Operation(summary = "Gives a list with all possible time zones and their associated offsets with respect to UTC", security = { @SecurityRequirement(name = OpenAPIConfig.BEARER_KEY) })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns list of all known time zones.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = String.class)))}
            )
    })
    @GetMapping(RESTVersionURL.URL.V_01 + "/all-time-zones")
    public List<String> getAllTimeZonesWithOffset() {
        return systemService.getAllTimeZonesWithOffset();
    }

    @Operation(summary = "This endpoint returns a list of system attributes with their respective names and current values. All memory/space values are in GB.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns list of current state of system attributes.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SystemInfoDTO.class)))}
            )
    })
    @GetMapping(RESTVersionURL.URL.V_01 + "/current-info")
    public List<SystemInfoDTO> currentSystemInfo() {
        return SystemService.getCurrentSystemInfo();
    }

    @Operation(summary = "This endpoint returns the name of the new HTTP header field that is used  for error handling for all REST endpoints.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns header field name for error codes.",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = String.class))}
            )
    })
    @GetMapping(RESTVersionURL.URL.V_01 + "/error-code-header")
    public String getErrorCodeHeaderFild() {
        return ErrorCodeFilter.ERROR_CODE_FIELD;
    }

    @Operation(summary = "This endpoint returns a list of system error codes, that can occur during processing requests.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns map of all system error codes mapping to their messages",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(ref = "#/components/schemas/"+ ERROR_CODE_MAPPER))}
            )
    })
    @GetMapping(RESTVersionURL.URL.V_01 + "/error-codes")
    public Map<Integer, String> listErrorCodes() {
        return ErrorResponseCodes.getAllErrorCodes();
    }

    //------------------------------------------------------ PUT -------------------------------------------------------

    @Operation(summary = "Changes the time zone in use", security = { @SecurityRequirement(name = OpenAPIConfig.BEARER_KEY) })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Changes to new time zone.",
                    content = {@Content(schema = @Schema(hidden = true))}
            )
    })
    @PutMapping(RESTVersionURL.URL.V_01 + "/time-zone")
    public void setTimeZone(@Parameter(description = "New zone identifier", example = "Australia/Sydney") @RequestParam @NotBlank String timeZone) {
        systemService.setTimeZone(ZoneId.of(timeZone));
    }

    //----------------------------------------------------- POST -------------------------------------------------------

    @Operation(summary = "Updates token key for generating JSON Web Tokens (JWTs) with given one. ATTENTION: This mean all previous generated JWTs become invalid, since they will NOT match against the new key!!!", security = { @SecurityRequirement(name = OpenAPIConfig.BEARER_KEY) })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Updates token key for JSON Web Token (JWT) generation",
                    content = {@Content(schema = @Schema(hidden = true))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "If given token key is invalid",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))}
            )
    })
    @PostMapping(RESTVersionURL.URL.V_01 + "/token-key")
    public void updateTokenKey(@Parameter(description = "New key that should be used for JSON Web Token (JWT) validation") @RequestParam @NotBlank String newKey) {
        systemService.updateTokenKey(newKey);
    }

    @Operation(summary = "Updates token key for generating JSON Web Tokens (JWTs) with a newly generated one. ATTENTION: This mean all previous generated JWTs become invalid, since they will NOT match against the new key!!!", security = { @SecurityRequirement(name = OpenAPIConfig.BEARER_KEY) })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Generates & updates token key for JSON Web Token (JWT) generation",
                    content = {@Content(schema = @Schema(hidden = true))}
            )
    })
    @PostMapping(RESTVersionURL.URL.V_01 + "/token-key/generate")
    public void generateNewTokenKey() {
        systemService.generateNewTokenKey();
    }

    @Operation(summary = "By calling this endpoint, the complete application is shut down, therefore no further communication is possible until the application reboots again.", security = { @SecurityRequirement(name = OpenAPIConfig.BEARER_KEY) })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Shutting system down.",
                    content = {@Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = String.class, example = "Shutting system down. Bye, bye..."))}
            )
    })
    @PostMapping(RESTVersionURL.URL.V_01 + "/shutdown")
    public String shutdown() {
        systemService.shutDown();
        return "Shutting system down. Bye, bye...";
    }

}