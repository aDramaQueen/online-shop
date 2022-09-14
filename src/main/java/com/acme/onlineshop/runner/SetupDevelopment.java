package com.acme.onlineshop.runner;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.Constants;
import com.acme.onlineshop.service.DatabaseTableService;
import com.acme.onlineshop.service.SecurityService;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.web.URL;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This runner sets up the application for development mode
 */
@Component
@Order(2)
@Profile(com.acme.onlineshop.utils.Profile.Tag.DEVELOPMENT)
public class SetupDevelopment extends Setup implements ApplicationRunner {

    private final String databasesHandling;
    private final DatabaseTableService databaseTableService;
    private final UserService userService;
    private final SecurityService securityService;

    @Autowired
    public SetupDevelopment(@Value("${spring.jpa.hibernate.ddl-auto:none}") String databasesHandling, DatabaseTableService databaseTableService, UserService userService, SecurityService securityService) {
        this.databasesHandling = databasesHandling;
        this.databaseTableService = databaseTableService;
        this.userService = userService;
        this.securityService = securityService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        setCodeProfile(com.acme.onlineshop.utils.Profile.DEVELOPMENT);
        checkForAdminUser(userService);
        cleanTables(databasesHandling, databaseTableService, ApplicationConfiguration.getDatabaseProfile());

        // printLoggerStatus();

        System.out.printf("%n"+(starterText)+"%n", ApplicationConfiguration.getWebInterfaceURL(), ApplicationConfiguration.getWebInterfaceURL(), URL.OPEN_API_WEB.url);
        if(Constants.WEB_SECURITY) {
            String refresh = securityService.generateNewRefreshToken(Constants.DEFAULT_ADMIN_USERNAME, Constants.DEFAULT_ADMIN_PASSWORD, 1);
            System.out.printf("%n"+(starterTokenText)+"%n", refresh, securityService.generateNewAccessToken(refresh, 1));
        }
    }

    private static void printLoggerStatus() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        System.out.println("\n----------------------------------");
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders();
            Set<String> result = new HashSet<>();
            while (iterator.hasNext()) {
                result.add(iterator.next().getName());
            }
            if(!result.isEmpty()) {
                System.out.printf("%s%nAPPENDERS: %s%n----------------------------------%n", logger, String.join(", ", result));
            }
        }
        System.out.println();
    }

}
