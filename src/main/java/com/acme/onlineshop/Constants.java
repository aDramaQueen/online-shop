package com.acme.onlineshop;

/**
 * A constant class that only holds constant value for the entire application
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public final class Constants {

    private Constants() { }

    /**
     * Activates/Deactivates development mode
     * <p>
     * For development mode, should be <code>TRUE</code>
     * </p><br><p>
     * For production mode, should be <code>FALSE</code>
     * </p>
     */
    public final static boolean DEBUG = true;

    public final static boolean WEB_SECURITY = !DEBUG;
    public final static boolean WEB_SECURITY_DEBUG = false;

    /**
     * Enforces secure HTTP (HTTPS) in production mode
     */
    public final static boolean ENFORCE_HTTPS = true;

    public final static boolean OPEN_API = true; // = DEBUG;
    public final static boolean OPEN_API_JWT = WEB_SECURITY;

    public final static String DEFAULT_ADMIN_USERNAME = "admin";
    public final static String DEFAULT_ADMIN_PASSWORD = "password";
    public final static String DEFAULT_ADMIN_EMAIL = "admin@online-shop.com";

    public final static String ONLINE_SHOP_WEB_ADDRESS = "www.online-shop.com";
    public final static String ONLINE_SHOP_SUPPORT_EMAIL_ADDRESS = "support@online-shop.com";

    public final static String GIT_HUB_REPOSITORY = "https://github.com/aDramaQueen/online-shop";

    /**
     * Deletes <b>ALL</b> old logging files by start of application
     */
    public final static boolean DELETE_OLD_LOGS = DEBUG;

    /**
     * <p>Max size definition in "application.properties" file -> File management</p>
     * <br>
     * <p>ATTENTION: if you change this value higher than 2048KB, you have to chnage the "application.properties" file as well</p>
     */
    public final static int MAX_FILE_SIZE_IN_KB = 2048;
}

