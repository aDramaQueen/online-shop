package com.acme.onlineshop.utils.validators;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.Constants;
import com.acme.onlineshop.persistence.configuration.ApplicationConfig;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import com.acme.onlineshop.service.JwtTokenService;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * "Spring Boot" validator that checks the JSON Web Token (JWT) properties for this application on boot-up time.
 */
@Validated
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties implements BootValidation {

    private final static String KEY_IDENTIFIER = "key";
    private final ApplicationConfigRepository applicationConfigRepo;
    private String key = "";

    @Autowired
    public JwtProperties(ApplicationConfigRepository applicationConfigRepo) {
        this.applicationConfigRepo = applicationConfigRepo;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return JwtProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (Constants.WEB_SECURITY) {
            Optional<ApplicationConfig> oldConfig = applicationConfigRepo.findById(ApplicationConfiguration.APPLICATION_CONFIG_ID);
            if(oldConfig.isEmpty()) {
                validateKey(key, errors);
            } else {
                validateKey(oldConfig.get().getJwtKey(), errors);
                if(errors.hasErrors()) {
                    // If for any reasons the key on database is invalid, delete it.
                    applicationConfigRepo.deleteById(ApplicationConfiguration.APPLICATION_CONFIG_ID);
                }
            }
        }
    }

    private void validateKey(String key, Errors errors) {
        if (key.isEmpty()) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, KEY_IDENTIFIER, "", NOT_EMPTY);
        } else {
            try {
                JwtTokenService.testNewKey(key);
            } catch (WeakKeyException exc) {
                errors.rejectValue(KEY_IDENTIFIER, "", exc.getMessage());
            }
        }
    }
}
