package com.acme.onlineshop.runner;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.Constants;
import com.acme.onlineshop.persistence.user.User;
import com.acme.onlineshop.security.Role;
import com.acme.onlineshop.service.DatabaseTableService;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.utils.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Abstract parent class, with some static functions that every application mode runner may, or may not need. Runners
 * that inherit from this class:
 * <ul>
 *     <li>{@link SetupProduction}</li>
 *     <li>{@link SetupDevelopment}</li>
 *     <li>{@link SetupTest}</li>
 * </ul>
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
abstract class Setup {

    protected final static Logger LOGGER = LoggerFactory.getLogger(Setup.class);
    protected final static String starterText = """
            ---------------------------------------------------------------------------------
            Homepage: %s
            All endpoints with OpenAPI 3.0: %s%s
            ---------------------------------------------------------------------------------
            """;
    protected final static String starterTokenText = """
            ---------------------------------------------------------------------------------
            New Refresh Token: %s
            New Access Token: %s
            ---------------------------------------------------------------------------------
            """;

    protected static void setCodeProfile(Profile profile) {
        if(ApplicationConfiguration.getCodeProfile() == null || ApplicationConfiguration.getCodeProfile() == profile) {
            ApplicationConfiguration.setCodeProfile(profile);
        } else {
            throw new IllegalStateException("Try to set code profile with: %s, but profile was already set with other value: %s\nMultiple code profiles are NOT allowed!!!".formatted(profile, ApplicationConfiguration.getCodeProfile()));
        }
    }

    protected static void setDatabaseProfile(Profile profile) {
        if(ApplicationConfiguration.getDatabaseProfile() == null || ApplicationConfiguration.getDatabaseProfile() == profile) {
            ApplicationConfiguration.setDatabaseProfile(profile);
        } else {
            throw new IllegalStateException("Try to set database profile with: %s, but profile was already set with other value: %s\nMultiple database profiles are NOT allowed!!!".formatted(profile, ApplicationConfiguration.getDatabaseProfile()));
        }
    }

    protected static void setRootURL(String rootURL) {
        ApplicationConfiguration.setWebInterfaceURL(rootURL);
    }

    /**
     * If no admin user exists, create standard admin user.
     *
     * @param userService The service that adds new users
     */
    protected static void checkForAdminUser(UserService userService) throws IOException {
        if(!userService.atLeastOneAdmin()) {
            User adminUser = new User(Constants.DEFAULT_ADMIN_USERNAME, Constants.DEFAULT_ADMIN_PASSWORD, Constants.DEFAULT_ADMIN_EMAIL, Role.ADMIN);
            userService.addNewUser(adminUser, false);
            LOGGER.info("Created standard admin user");
        }
    }

    /**
     * Deletes special <i>"Liquibase"</i> tracking tables (@see <a href="https://docs.liquibase.com/concepts/tracking-tables/tracking-tables.html">Liquibase documentation: Tracking Table</a>).
     *
     * @param databasesHandling {@link String} property value, which indicates that tables should be dropped, or not
     * @param databaseTableService Service which actually deletes the tables
     * @see DatabaseTableService
     */
    protected static void cleanTables(String databasesHandling, DatabaseTableService databaseTableService, Profile databaseProfile) throws SQLException {
        if(databasesHandling.equals("create-drop")) {
            String[] liquibaseTables = new String[]{"DATABASECHANGELOG", "DATABASECHANGELOGLOCK"};
            int deletedTables = databaseTableService.dropTableIfExists(databaseProfile, liquibaseTables);
            LOGGER.warn("Deleted %d table(s).".formatted(deletedTables));
        }
    }

}
