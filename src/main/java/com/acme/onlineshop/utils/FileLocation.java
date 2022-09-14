package com.acme.onlineshop.utils;

/**
 * This enum holds relative paths from files within this application. These files should be loaded by {@link FileLoader}
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public enum FileLocation {

    STATIC_DIRECTORY("static"),
    MEDIA_DIRECTORY("media"),
    IMAGE_DIRECTORY(MEDIA_DIRECTORY.location +"/images"),
    LOGGING_DIRECTORY("/logging"),
    PID_FILE("/online_shop.pid"),
    SETTINGS("/settings.properties");

    public final String location;

    FileLocation(String location) {
        this.location = location;
    }

}
