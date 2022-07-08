package com.acme.onlineshop;

import com.acme.onlineshop.persistence.configuration.ApplicationConfig;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

public class ApplicationConfiguration {

    private static class DatabaseOperator {

        private final ApplicationConfigRepository applicationConfigRepo;

        public DatabaseOperator(ApplicationConfigRepository applicationConfigRepo) {
            this.applicationConfigRepo = applicationConfigRepo;
        }

        public ApplicationConfigRepository getApplicationConfigRepo() {
            return applicationConfigRepo;
        }

        @Transactional
        public void save(ApplicationConfig config) {
            applicationConfigRepo.save(config);
        }
    }

    public final static int APPLICATION_CONFIG_ID = 1;
    private static DatabaseOperator databaseOperator;
    private static ZoneId timeZone = ZoneId.systemDefault();

    private ApplicationConfiguration() { }

    public static void initialize(ApplicationConfigRepository applicationConfig, String jwtKey) {
        ApplicationConfiguration.databaseOperator = new DatabaseOperator(applicationConfig);
        setup(applicationConfig, jwtKey);
    }

    public static ZoneId getTimeZone() {
        return timeZone;
    }

    public static void setTimeZone(ZoneId timeZone) {
        ApplicationConfiguration.timeZone = timeZone;

        ApplicationConfig config = databaseOperator.applicationConfigRepo.getById(APPLICATION_CONFIG_ID);
        config.setTimeZone(timeZone.toString());
        databaseOperator.save(config);
    }

    public static void setJwtKey(String jwtKey) {
        ApplicationConfig config = databaseOperator.applicationConfigRepo.getById(APPLICATION_CONFIG_ID);
        config.setJwtKey(jwtKey);
        databaseOperator.save(config);
    }

    private static void setup(ApplicationConfigRepository applicationConfig, String jwtKey) {
        setupApplication(applicationConfig.findById(APPLICATION_CONFIG_ID).orElse(null), jwtKey);
    }

    private static void setupApplication(ApplicationConfig applicationConfig, String jwtKey) {
        if(applicationConfig == null) {
            ApplicationConfig config = new ApplicationConfig();
            config.setId(1);
            config.setTimeZone(timeZone.toString());
            config.setJwtKey(jwtKey);
            databaseOperator.save(config);
        } else {
            timeZone = ZoneId.of(applicationConfig.getTimeZone());
        }
    }
}
