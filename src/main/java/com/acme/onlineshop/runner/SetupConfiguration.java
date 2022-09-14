package com.acme.onlineshop.runner;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import com.acme.onlineshop.service.ImageService;
import com.acme.onlineshop.utils.IPLoader;
import liquibase.pro.packaged.V;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static com.acme.onlineshop.utils.Profile.*;

/**
 * This runner initialises the static {@link ApplicationConfiguration} class, by determining what code profile &amp;
 * what database profile is active. Also, it sets up the base URL for this application.
 */
@Configuration
@Order(0)
public class SetupConfiguration implements ApplicationRunner {

    private final String activeProfile, defaultProfile;
    private final ServerProperties serverProperties;
    private final ApplicationConfigRepository applicationConfigRepository;
    private final String jwtKey, mediaRootDirectory;

    @Autowired
    public SetupConfiguration(ApplicationConfigRepository applicationConfigRepository, ServerProperties serverProperties, @Value("${jwt.key}")String jwtKey, @Value("${spring.profiles.active:}")String activeProfile, @Value("${spring.profiles.default:}")String defaultProfile, @Value("${spring.web.resources.static-locations}")String mediaRootDirectory) {
        this.applicationConfigRepository = applicationConfigRepository;
        this.jwtKey = jwtKey;
        this.activeProfile = activeProfile;
        this.defaultProfile = defaultProfile;
        this.serverProperties = serverProperties;
        this.mediaRootDirectory = mediaRootDirectory;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String profiles = (activeProfile.isEmpty()) ? defaultProfile : activeProfile;
        for(String profile: profiles.split(",")) {
            if(isDatabaseProfile(profile)) {
                ApplicationConfiguration.setDatabaseProfile(getProfile(profile));
            } else if(isCodeProfile(profile)) {
                ApplicationConfiguration.setCodeProfile(getProfile(profile));
            } else {
                throw new IllegalArgumentException("Unknown profile: %s\nIt's neither code, nor database profile.".formatted(profile));
            }
        }

        if(!profiles.contains(com.acme.onlineshop.utils.Profile.TEST.tag)) {
            String rootURL = IPLoader.getRootURL(serverProperties);
            ApplicationConfiguration.setWebInterfaceURL(rootURL);
        }
        ApplicationConfiguration.initializeStaticRepository(applicationConfigRepository, jwtKey, mediaRootDirectory);
    }

}
