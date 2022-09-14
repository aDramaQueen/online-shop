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
    REST_ACTUATOR("", Path.REST_ACTUATOR),
    REST_API("", Path.REST_API),
    REST_SECURITY("", Path.REST_SECURITY),
    REST_SYSTEM("", Path.REST_SYSTEM),

    // ------------- Websites -------------
    ABOUT("about", Path.ABOUT),
    ADMIN("admin", Path.ADMIN),
    ADMIN_CATEGORIES("admin_categories", Path.ADMIN_CATEGORIES),
    ADMIN_CUSTOMERS("admin_customers", Path.ADMIN_CUSTOMERS),
    ADMIN_INVENTORY("admin_inventory", Path.ADMIN_INVENTORY),
    ADMIN_OVERVIEW("admin_overview", Path.ADMIN_OVERVIEW),
    BASKET("basket", Path.BASKET),
    CATEGORY("category", Path.CATEGORY),
    CONTACT("contact", Path.CONTACT),
    HOME("home", Path.HOME),
    INDEX("index", Path.INDEX),
    LOGIN("login", Path.LOGIN),
    LOGOUT("logout", Path.LOGOUT),
    PROFILE("profile", Path.PROFILE),
    UPLOAD_FILE("uploadForm", Path.UPLOAD_FILE),
    USERS("users", Path.USERS),
    USERS_CHANGE("users-change", Path.USERS_CHANGE),
    USERS_EDIT_ATTRIBUTES("", Path.USERS_EDIT_ATTRIBUTES),
    USERS_EDIT_PASSWORD("", Path.USERS_EDIT_PASSWORD),
    USERS_ADD("users-add", Path.USERS_ADD),
    REGISTER("", Path.REGISTER),
    REGISTER_ERROR("register_error", Path.REGISTER),
    REGISTER_SUCCESS("register_success", Path.REGISTER),
    SEARCH("search", Path.SEARCH),
    SETTINGS("settings", Path.SETTINGS);

    public final static class Path {
        // ------------- OPEN API -------------
        public final static String OPEN_API_WEB = "/open-api";
        public final static String OPEN_API_JSON = OPEN_API_WEB+"-json";

        // --------------- REST ---------------
        public final static String REST_ACTUATOR = "/actuator";
        public final static String REST_API = "/api";
        public final static String REST_SECURITY = REST_API + "/security";
        public final static String REST_SYSTEM = REST_API + "/system";

        // ------------- Websites -------------
        public final static String ABOUT = "/about";
        public final static String ADMIN = "/admin";
        public final static String ADMIN_CATEGORIES = ADMIN+"/categories";
        public final static String ADMIN_CUSTOMERS = ADMIN+"/customers";
        public final static String ADMIN_INVENTORY = ADMIN+"/inventory";
        public final static String ADMIN_OVERVIEW = ADMIN+"/overview";
        public final static String BASKET = "/basket";
        public final static String CATEGORY = "/category";
        public final static String CONTACT = "/contact";
        public final static String HOME = "/home";
        public final static String INDEX = "/";
        public final static String LOGIN = "/login";
        public final static String LOGOUT = "/logout";
        public final static String PROFILE = "/profile";
        public final static String UPLOAD_FILE = "/upload";
        public final static String USERS = "/users";
        public final static String USERS_CHANGE = USERS+"/change";
        public final static String USERS_EDIT_ATTRIBUTES = USERS+"/edit-attributes";
        public final static String USERS_EDIT_PASSWORD = USERS+"/edit-password";
        public final static String USERS_ADD = USERS+"/add";
        public final static String REGISTER = "/register";
        public final static String SEARCH = "/browse";
        public final static String SETTINGS = "/settings";
    }

    public final String html, url;

    URL(String html, String url) {
        this.html = html;
        this.url = url;
    }
}
