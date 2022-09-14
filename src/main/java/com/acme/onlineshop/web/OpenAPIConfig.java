package com.acme.onlineshop.web;

import com.acme.onlineshop.Constants;
import com.acme.onlineshop.filters.JWTFilter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * <p>Configuration for REST endpoint overview OpenAPI 3.0 (a.k.a. Swagger v3)</p>
 *
 * @see <a href="https://springdoc.org/#how-do-i-add-authorization-header-in-requests">Spring DOC with Spring Security</a>
 */
@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "OnlineShop REST API",
                version = "1.0",
                description = "This is an overview of all REST endpoints of this application",
                contact = @io.swagger.v3.oas.annotations.info.Contact(name = "OnlineShop", url = Constants.ONLINE_SHOP_WEB_ADDRESS, email = Constants.ONLINE_SHOP_SUPPORT_EMAIL_ADDRESS)
        )
)
public class OpenAPIConfig {
    public static final String BEARER = (Constants.OPEN_API_JWT) ? JWTFilter.BEARER.toLowerCase() : "";
    public static final String BEARER_KEY = (Constants.OPEN_API_JWT) ? "bearer-key" : "";
    public static final String ERROR_CODE_MAPPER = "ErrorCode-Mapper";

    @Bean
    public OpenApiCustomiser openApiCustomiser() {
        return openApi -> {
            Components components = openApi.getComponents();
            // Custom schemas
            for(Schema<?> schema: buildCustomSchemas()) {
                components.addSchemas(schema.getName(), schema);
            }
            // OpenAPI & JWT
            if(Constants.OPEN_API_JWT) {
                components.addSecuritySchemes(OpenAPIConfig.BEARER_KEY, new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(OpenAPIConfig.BEARER).bearerFormat("JWT"));
            }
        };
    }

    private static List<Schema<?>> buildCustomSchemas() {
        ArrayList<Schema<?>> result = new ArrayList<>();

        Schema<?> integerStringMap = new Schema<Map<Integer, String>>()
                .name(ERROR_CODE_MAPPER)
                .type("object")
                .addProperty("error code", new StringSchema().example("Error errorMessage belonging to the error code")).example(getErrorCodeExample());
        result.add(integerStringMap);
        // Build more custom schemas...
        return result;
    }

    private static Map<Integer, String> getErrorCodeExample() {
        Map<Integer, String> example = new WeakHashMap<>();
        example.put(666, "Oh no..., the devil himself showed up and stopped your request");
        return example;
    }
}
