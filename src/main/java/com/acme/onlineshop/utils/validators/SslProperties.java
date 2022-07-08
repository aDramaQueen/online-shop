package com.acme.onlineshop.utils.validators;

import com.acme.onlineshop.Constants;
import com.acme.onlineshop.Main;
import com.acme.onlineshop.utils.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * <p>"Spring Boot" validator that checks the SSL properties for this application on boot-up time.</p>
 * <p>The SSL certificates are stored inside a key store in "PKC12" format. The validation makes sure, the
 * right store type is selected, the store itself exists and finally the store password opens the key store.</p>
 */
@Validated
@Configuration
@ConfigurationProperties(prefix = "server.ssl")
public class SslProperties implements BootValidation {

    private final static String[] SUPPORTED_SSL_KEY_STORES = new String[]{"PKCS12"};

    private final static String SSL_IDENTIFIER = "enabled";
    private final static String KEY_STORE_TYPE_IDENTIFIER = "keyStoreType";
    private final static String KEY_STORE_IDENTIFIER = "keyStore";
    private final static String KEY_STORE_PASSWORD_IDENTIFIER = "keyStorePassword";

    private boolean enabled;
    private String keyStoreType = "";
    private String keyStore = "";
    private String keyStorePassword = "";
    private final CurrentProfiles currentProfiles;

    @Autowired
    public SslProperties(CurrentProfiles currentProfiles) {
        this.currentProfiles = currentProfiles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return SslProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        enforceSSL(errors);
        checkEmptyValues(errors);
        Optional<KeyStore> store = (!keyStoreType.isEmpty()) ? checkType(errors) : Optional.empty();
        Optional<InputStream> file = (!keyStore.isEmpty()) ? checkFile(errors) : Optional.empty();
        if(!keyStorePassword.isBlank() && store.isPresent() && file.isPresent()) {
            checkPassword(store.get(), file.get(), errors);
        }
    }

    private void enforceSSL(Errors errors) {
        Set<Profile> profiles;
        try {
            profiles = currentProfiles.getCurrentProfiles();
        } catch (IllegalArgumentException exc) {
            errors.rejectValue("spring.profiles.active", "", exc.getMessage());
            return;
        }
        for(Profile profile: profiles) {
            if(!enabled && profile == Profile.PRODUCTION && Constants.ENFORCE_HTTPS) {
                errors.rejectValue(SSL_IDENTIFIER, "", "Can't run production without SSL/TLS.");
            }
        }
    }

    private void checkEmptyValues(Errors errors) {
        if(enabled) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, KEY_STORE_TYPE_IDENTIFIER, "", NOT_EMPTY);
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, KEY_STORE_IDENTIFIER, "", NOT_EMPTY);
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, KEY_STORE_PASSWORD_IDENTIFIER, "", NOT_EMPTY);
        }
    }

    private Optional<KeyStore> checkType(Errors errors) {
        try {
            for(String type: SUPPORTED_SSL_KEY_STORES){
                if(type.equalsIgnoreCase(keyStoreType)) {
                    return Optional.of(KeyStore.getInstance(keyStoreType));
                }
            }
            errors.rejectValue(KEY_STORE_TYPE_IDENTIFIER, "", SUPPORTED_SSL_KEY_STORES, "NOT supported key store type: %s%n\t\tMust be one of following types: %s".formatted(keyStoreType, Arrays.toString(SUPPORTED_SSL_KEY_STORES)));
        } catch (KeyStoreException exc) {
            errors.rejectValue(KEY_STORE_TYPE_IDENTIFIER, "", SUPPORTED_SSL_KEY_STORES, exc.getMessage());
        }
        return Optional.empty();
    }

    private Optional<InputStream> checkFile(Errors errors) {
        if (keyStore.toLowerCase(Locale.ROOT).startsWith("file:")) {
            String keyStorePath = keyStore.substring("file:".length()).strip();
            try {
                return Optional.of(new FileInputStream(keyStorePath));
            } catch (FileNotFoundException exc) {
                errors.rejectValue(KEY_STORE_IDENTIFIER, "", exc.getMessage());
                return Optional.empty();
            }
        } else if (keyStore.toLowerCase(Locale.ROOT).startsWith("classpath:")) {
            String keyStorePath = keyStore.substring("classpath:".length()).strip();
            InputStream result;
            if(keyStorePath.startsWith("/")) {
                result = Main.class.getResourceAsStream(keyStorePath);
            } else {
                result = Main.class.getResourceAsStream("/"+keyStorePath);
            }
            if(result == null) {
                errors.rejectValue(KEY_STORE_IDENTIFIER, "", "Couldn't load embedded key store with location: "+keyStore);
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } else {
            errors.rejectValue(KEY_STORE_IDENTIFIER, "", "Couldn't recognize key store location -> '%s'%n\tProperty has to be of form -> 'file:<path-to-file>'%n\t\tE.g.: 'file:./i3de.properties'".formatted(keyStore));
            return Optional.empty();
        }
    }

    private void checkPassword(KeyStore keyStore, InputStream inputStream, Errors errors) {
        try {
            keyStore.load(inputStream, keyStorePassword.toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException exc) {
            errors.rejectValue(KEY_STORE_PASSWORD_IDENTIFIER, "", exc.getMessage());
        }
    }
}
