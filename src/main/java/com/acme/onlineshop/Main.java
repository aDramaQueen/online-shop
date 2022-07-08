package com.acme.onlineshop;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.acme.onlineshop.controller.SystemController;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import com.acme.onlineshop.persistence.user.User;
import com.acme.onlineshop.security.Role;
import com.acme.onlineshop.service.DatabaseTableService;
import com.acme.onlineshop.service.SecurityService;
import com.acme.onlineshop.service.UserService;
import com.acme.onlineshop.utils.FileLoader;
import com.acme.onlineshop.utils.FileLocation;
import com.acme.onlineshop.utils.IPLoader;
import com.acme.onlineshop.web.SwaggerConfiguration;
import com.acme.onlineshop.web.URL;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>Entry &amp; exit point for this (Spring) application.</p>
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config">Spring Boot - External Configuration</a>
 * @author Richard Saeuberlich
 * @version 1.7
 */
@ServletComponentScan
@SpringBootApplication
@PropertySource(value = "classpath:secrets/ssl.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:secrets/jwt.properties", ignoreResourceNotFound = true)
@ConfigurationPropertiesScan("com.acme.onlineshop.utils.validators")
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static String rootURL;
	private static int additionalPort;
	private static ApplicationContext context;
	private static com.acme.onlineshop.utils.Profile profile;

	/**
	 * Starting point for this application.
	 *
	 * @param args Additional program/application arguments
	 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/environment/cmdLineArgs.html">Java - Command line arguments</a>
	 * @see <a href="https://www.baeldung.com/spring-boot-command-line-arguments">Spring Boot - Command line arguments</a>
	 */
	public static void main(String[] args) {
		if (Constants.DELETE_OLD_LOGS) {
			cleanOldLoggingFiles();
		}
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class).web(WebApplicationType.SERVLET);
		builder.headless(Constants.HEADLESS);
		SpringApplication app = builder.build();

		//Write PID (Process ID) to file in root directory
		app.addListeners(new ApplicationPidFileWriter(FileLoader.getRootDirectory() + FileLocation.PID_FILE.location));
		app.addListeners(new SwaggerConfiguration());

		context = builder.run(args);
	}

	private static int getPort(String port) {
		return Integer.parseInt(port);
	}

	public static com.acme.onlineshop.utils.Profile getCurrentProfile() {
		return profile;
	}

	public static String getRootURL() {
		return rootURL;
	}

	public static int getAdditionalPort() {
		return additionalPort;
	}

	/**
	 * Returns a bean, which has given class. This is obviously only unique if there is only one instance of the bean
	 * in the entire Spring framework.
	 *
	 * @param clazz {@link Class} of {@link Bean} which should be returned
	 * @return A Spring {@link Bean}
	 * @throws org.springframework.beans.BeansException If not one bean with given class exists
	 * @see ApplicationContext#getBean(Class)
	 */
	public static <T> T getBean(Class<T> clazz) {
		return context.getBean(clazz);
	}

	/**
	 * This method is called automatically when the program is terminated. Be it from outside (using SIGTERM)
	 * or from inside using REST endpoint via {@link SystemController}.
	 */
	@PreDestroy
	public void tearDown() {
		LOGGER.error("Shutting System Down.....");
	}

	/**
	 * <p>
	 *     This method finishes the entire application gracefully &amp; returns the given exit code to the operation
	 *     system.
	 * </p>
	 * <p>
	 *     Closing a {@link SpringApplication} basically means closing the underlying {@link ApplicationContext}.
	 * </p>
	 *
	 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.application-exit">Spring - Application Exit</a>
	 */
	public static void exit(int exitCode) {
		SpringApplication.exit(context, () -> exitCode);
	}

	/**
	 * Finishes application with exit code 0
	 *
	 * @see Main#exit(int)
	 */
	public static void exit() {
		exit(0);
	}

	@Bean
	@Order(0)
	CommandLineRunner setupApplication(@Value("${spring.profiles.active:}")String activeProfile, @Value("${spring.profiles.default:}")String defaultProfile, @Value("${server.additionalPort:}")String additionalPort, ServerProperties serverProperties) {
		return args -> {
			String profile = (activeProfile.isEmpty()) ? defaultProfile : activeProfile;
			if(!profile.contains(com.acme.onlineshop.utils.Profile.TEST.tag)) {
				rootURL = IPLoader.getRootURL(serverProperties);
			}
			if(additionalPort.isEmpty()) {
				Main.additionalPort = -1;
			} else {
				Main.additionalPort = Integer.parseInt(additionalPort);
			}
		};
	}

	@Bean
	@Order(1)
	CommandLineRunner setupConfiguration(ApplicationConfigRepository applicationConfigRepository, @Value("${jwt.key}")String jwtKey) {
		return args -> ApplicationConfiguration.initialize(applicationConfigRepository, jwtKey);
	}

	@Bean
	@Profile(com.acme.onlineshop.utils.Profile.Tag.DEVELOPMENT)
	@Order(2)
	CommandLineRunner startUpDevelopment(@Value("${spring.jpa.hibernate.ddl-auto:none}") String databasesHandling, DatabaseTableService databaseTableService, UserService userService, SecurityService securityService) {
		cleanTables(databasesHandling, databaseTableService);
		profile = com.acme.onlineshop.utils.Profile.DEVELOPMENT;
		checkForAdminUser(userService);
		return args -> {
			System.out.println("\n---------------------------------------------------------------------------------\n");
			System.out.printf("Current profile: %s\n", profile.prettyName);
			System.out.printf("Homepage: %s\n", rootURL);
			System.out.printf("All endpoints with OpenAPI 3.0: %s%s\n", rootURL, URL.OPEN_API_WEB.url);
			if(Constants.WEB_SECURITY) {
				String refresh = securityService.generateNewRefreshToken(Constants.DEFAULT_ADMIN_USERNAME, Constants.DEFAULT_ADMIN_PASSWORD, 1);
				System.out.printf("New Refresh Token: %s\n", refresh);
				System.out.printf("New Access Token: %s\n", securityService.generateNewAccessToken(refresh, 1));
			}
			System.out.println("\n---------------------------------------------------------------------------------\n");
		};
	}

	@Bean
	@Profile(com.acme.onlineshop.utils.Profile.Tag.PRODUCTION)
	@Order(3)
	CommandLineRunner startUpProduction(UserService userService, DatabaseTableService databaseTableService, SecurityService securityService) {
		profile = com.acme.onlineshop.utils.Profile.PRODUCTION;
		checkForAdminUser(userService);
		return args -> {
			System.out.println("\n---------------------------------------------------------------------------------\n");
			System.out.printf("Homepage: %s\n", rootURL);
			System.out.printf("All endpoints with OpenAPI 3.0: %s%s\n", rootURL, URL.OPEN_API_WEB.url);
			System.out.println("\n---------------------------------------------------------------------------------\n");
		};
	}

	@Bean
	@Profile(com.acme.onlineshop.utils.Profile.Tag.TEST)
	@Order(4)
	CommandLineRunner startUpTest() {
		profile = com.acme.onlineshop.utils.Profile.TEST;
		return args -> {};
	}

	/**
	 * If no admin user exists, create standard admin user.
	 *
	 * @param userService The service that adds new users
	 */
	private static void checkForAdminUser(UserService userService) {
		if(!userService.atLeastOneAdmin()) {
			User adminUser = new User(Constants.DEFAULT_ADMIN_USERNAME, Constants.DEFAULT_ADMIN_PASSWORD, Role.ADMIN);
			userService.addNewUser(adminUser, false);
			LOGGER.info("Created standard admin user");
		}
	}

	/**
	 * <p>Deletes logging directory: ./logs</p><br>
	 *
	 * <b>ATTENTION:</b>
	 * <p>This function is called before Spring framework boots. Therefore don't use any logger inside this method.</p>
	 */
	private static void cleanOldLoggingFiles() {
		Path root = FileLoader.getRootDirectory();
		Path loggingDir = Paths.get(root.toString(), FileLocation.LOGGING_DIRECTORY.location);
		if (Files.exists(loggingDir)) {
			try {
				FileUtils.cleanDirectory(loggingDir.toFile());
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
	}

	/**
	 * Deletes special <i>"Liquibase"</i> tracking tables (@see <a href="https://docs.liquibase.com/concepts/tracking-tables/tracking-tables.html">Liquibase documentation: Tracking Table</a>).
	 *
	 * @param databasesHandling {@link String} property value, which indicates that tables should be dropped, or not
	 * @param databaseTableService Service which actually deletes the tables
	 * @see DatabaseTableService
	 */
	private static void cleanTables(String databasesHandling, DatabaseTableService databaseTableService) {
		if(databasesHandling.equals("create-drop")) {
			String[] liquibaseTables = new String[]{"DATABASECHANGELOG", "DATABASECHANGELOGLOCK"};
			int deletedTables = databaseTableService.dropTableIfExists(liquibaseTables);
			LOGGER.warn("Deleted %d table(s).".formatted(deletedTables));
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
