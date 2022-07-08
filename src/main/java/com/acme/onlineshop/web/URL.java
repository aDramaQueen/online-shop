package com.acme.onlineshop.web;

import com.acme.onlineshop.security.SecurityConfig;

/**
 * <p>Enumeration of all available view URLs for all webpages of this application</p>
 * <br>
 * <p>The URL &amp; HTML strings are used to register the corresponding HTML file with URL via {@link SecurityConfig} &
 * in all {@link com.acme.onlineshop.controller}s</p>
 */
public enum URL {
    // ------------- OPEN API -------------
    OPEN_API_WEB("", Path.OPEN_API_WEB),
    OPEN_API_JSON("", Path.OPEN_API_JSON),

    // --------------- REST ---------------
    REST_API("", Path.REST_API),
    REST_SECURITY("", Path.REST_SECURITY),
    REST_SYSTEM("", Path.REST_SYSTEM),

    // ------------- Websites -------------
    INDEX("index", Path.INDEX),
    LOGIN("login", Path.LOGIN),
    LOGOUT("logout", Path.LOGOUT),
    USERS("users", Path.USERS),
    USERS_CHANGE("users-change", Path.USERS_CHANGE),
    USERS_EDIT_ATTRIBUTES("", Path.USERS_EDIT_ATTRIBUTES),
    USERS_EDIT_PASSWORD("", Path.USERS_EDIT_PASSWORD),
    USERS_ADD("users-add", Path.USERS_ADD),
    STATISTICS("statistics", Path.STATISTICS),
    GREETING("greeting", Path.GREETING);

    public final static class Path {
        // ------------- OPEN API -------------
        public final static String OPEN_API_WEB = "/open-api";
        public final static String OPEN_API_JSON = OPEN_API_WEB+"-json";

        // --------------- REST ---------------
        public final static String REST_API = "/api";
        public final static String REST_SECURITY = REST_API + "/security";
        public final static String REST_SYSTEM = REST_API + "/system";

        // ------------- Websites -------------
        public final static String INDEX = "/";
        public final static String LOGIN = "/login";
        public final static String LOGOUT = "/logout";
        public final static String USERS = "/users";
        public final static String USERS_CHANGE = USERS+"/change";
        public final static String USERS_EDIT_ATTRIBUTES = USERS+"/edit-attributes";
        public final static String USERS_EDIT_PASSWORD = USERS+"/edit-password";
        public final static String USERS_ADD = USERS+"/add";
        public final static String STATISTICS = "/statistics";
        public final static String GREETING = "/greeting";
    }

    public final String html, url;

    URL(String html, String url) {
        this.html = html;
        this.url = url;
    }
}
