package com.acme.onlineshop.utils.validators;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import com.acme.onlineshop.utils.FileLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * "Spring Boot" validator that checks resource related properties for this application on boot-up time.
 */
@Validated
@ConfigurationProperties(prefix = "spring.web.resources")
public class ResourceProperties implements BootValidation {

    private final static String STATIC_LOCATION_IDENTIFIER = "static-locations";
    private final static Set<String> DEFAULT_CLASSPATH_LOCATIONS = Set.of(
            "classpath:/static", "classpath:/public", "classpath:/resources", "classpath:/META-INF/resources"
    );
    private String staticLocations = "";

    public String getStaticLocations() {
        return staticLocations;
    }

    public void setStaticLocations(String staticLocations) {
        this.staticLocations = staticLocations;
    }

    /**
     * This is just a dummy Bean, to force {@link com.acme.onlineshop.service.ImageService} to wait for this Bean.
     *
     * @return Always {@code true}
     */
    @Bean("ResourcePropertiesTest")
    public boolean ready() {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ResourceProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if(staticLocations.isBlank()) {
            errors.rejectValue(STATIC_LOCATION_IDENTIFIER, "", "Can't have empty static file location. This application needs an additional media directory for static files");
        } else {
            List<Path> resultLocations = new ArrayList<>();
            for (String location : staticLocations.split(",")) {
                if (!DEFAULT_CLASSPATH_LOCATIONS.contains(location)) {
                    try {
                        URL url_location = checkMediaRootDirectory(location);
                        Path path_location = buildMediaRootDirectory(url_location);
                        resultLocations.add(path_location);
                    } catch (IOException exc) {
                        errors.rejectValue(STATIC_LOCATION_IDENTIFIER, "", exc.getMessage());
                    }
                }
            }
            if (resultLocations.size() > 1) {
                errors.rejectValue(STATIC_LOCATION_IDENTIFIER, "", "Can't have more than one media file root directory");
            } else {
                ApplicationConfiguration.setMediaRootDirectory(resultLocations.get(0));
            }
        }
    }

    /**
     * Check if given property value is file system value
     *
     * @return File system location as URL
     */
    private static URL checkMediaRootDirectory(String location) throws MalformedURLException {
        URL url = new URL(location);
        if (!url.getProtocol().equals("file")) {
            throw new IllegalStateException("This application is only capable of serving media files with a local file system. You provided an other protocol: "+url.getProtocol());
        } else {
            return url;
        }
    }

    /**
     * Create directory of given location
     *
     * @param url File system location as URL
     * @return Path to (new) directory
     */
    private Path buildMediaRootDirectory(URL url) throws IOException {
        Path result = Path.of(FileLoader.getRootDirectory().toString(), url.getPath()).normalize();
        FileLoader.createDirectory(result);
        return result;
    }
}
