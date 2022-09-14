package com.acme.onlineshop;

import com.acme.onlineshop.persistence.configuration.ApplicationConfig;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import com.acme.onlineshop.utils.FileLoader;
import com.acme.onlineshop.utils.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZoneId;

/**
 * This class is a static utility class, that holds properties that are applicable for the entire application. These
 * properties must be initialized once after the Spring Framework booted up.
 * <ul>
 *     <li>In what mode, does this application currently run (development, production, test)?</li>
 *     <li>What database is currently in usage (HyperSQL, H2, Apache Derby)?</li>
 *     <li>Time zone for this application</li>
 *     <li>JSON Web Token (JWT) key</li>
 *     <li>Utility class, that is necessary to establish secure connections (HTTPS) to time series databases</li>
 * </ul>
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public class ApplicationConfiguration {

    private record DatabaseOperator(ApplicationConfigRepository applicationConfigRepo) {
        @Transactional
        private void save(ApplicationConfig config) {
            applicationConfigRepo.save(config);
        }
    }

    public final static int APPLICATION_CONFIG_ID = 1;
    private static String webInterfaceURL;
    private static Profile codeProfile = null;
    private static Profile databaseProfile = null;
    private static DatabaseOperator databaseOperator;
    private static ZoneId timeZone = ZoneId.systemDefault();
    private static Path mediaRootDirectory;

    private ApplicationConfiguration() { }

    public static void initializeStaticRepository(ApplicationConfigRepository applicationConfig, String jwtKey, String mediaRootDirectory) throws IOException {
        ApplicationConfiguration.databaseOperator = new DatabaseOperator(applicationConfig);
        setup(applicationConfig, jwtKey);
    }

    /**
     * Returns web interface root URL of this application
     *
     * @return Root URL, meaning the Website
     */
    public static String getWebInterfaceURL() {
        return webInterfaceURL;
    }

    public static void setWebInterfaceURL(String webInterfaceURL) {
        if(ApplicationConfiguration.webInterfaceURL == null || ApplicationConfiguration.webInterfaceURL.equals(webInterfaceURL)) {
            ApplicationConfiguration.webInterfaceURL = webInterfaceURL;
        } else {
            throw new IllegalStateException("Can't change web interface URL, once it is set.");
        }
    }

    /**
     * Returns currently used code {@link Profile}. Or in other words, the current execution mode: development,
     * production or test.
     *
     * @return Currently mode, this application is running on.
     * @see Profile#isCodeProfile(String)
     */
    public static Profile getCodeProfile() {
        return codeProfile;
    }

    public static void setCodeProfile(Profile codeProfile) {
        if(ApplicationConfiguration.codeProfile == null || ApplicationConfiguration.codeProfile == codeProfile) {
            ApplicationConfiguration.codeProfile = codeProfile;
        } else {
            throw new IllegalStateException("Can't change profile, once it is set.");
        }
    }

    /**
     * <p>
     * Returns currently used database {@link Profile}. Or in other words, what database is in usage.
     * </p>
     * Currently supported databases:
     * <ul>
     *     <li>H2</li>
     *     <li>HyperSQL</li>
     *     <li>Apache Derby</li>
     * </ul>
     *
     * @return Currently used database
     * @see Profile#isDatabaseProfile(String)
     */
    public static Profile getDatabaseProfile() {
        return databaseProfile;
    }

    public static void setDatabaseProfile(Profile databaseProfile) {
        if(ApplicationConfiguration.databaseProfile == null || ApplicationConfiguration.databaseProfile == databaseProfile) {
            ApplicationConfiguration.databaseProfile = databaseProfile;
        } else {
            throw new IllegalStateException("Can't change profile, once it is set.");
        }
    }

    /**
     * Returns time zone of this application
     *
     * @return Time zone
     */
    public static ZoneId getTimeZone() {
        return timeZone;
    }

    /**
     * Changes time zone for this application to given value
     *
     * @param timeZone New time zone
     */
    public static void setTimeZone(ZoneId timeZone) {
        ApplicationConfiguration.timeZone = timeZone;
        ApplicationConfigRepository test = databaseOperator.applicationConfigRepo;

        ApplicationConfig config = databaseOperator.applicationConfigRepo.getReferenceById(APPLICATION_CONFIG_ID);
        config.setTimeZone(timeZone.toString());
        databaseOperator.save(config);
    }

    /**
     * Changes JSON Web Token (JWT) key for this application to given value
     *
     * @param jwtKey New JWT key
     */
    public static void setJwtKey(String jwtKey) {
        ApplicationConfig config = databaseOperator.applicationConfigRepo.getReferenceById(APPLICATION_CONFIG_ID);
        config.setJwtKey(jwtKey);
        databaseOperator.save(config);
    }

    public static Path getMediaRootDirectory() {
        return mediaRootDirectory;
    }

    public static void setMediaRootDirectory(Path mediaRootDirectory) {
        if(ApplicationConfiguration.mediaRootDirectory == null) {
            ApplicationConfiguration.mediaRootDirectory = mediaRootDirectory.normalize();
        } else {
            throw new IllegalStateException("Can't media root directory, once it is set.");
        }
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
