package com.acme.onlineshop.utils;

/**
 * This enum holds relative paths from files within this application. These files should be loaded by {@link FileLoader}
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public enum FileLocation {

    ICON_TRAY_ELECTRIC_PYLON("/static/images/png/electricity-pylon.png"),
    ICON_TRAY_LIGHT_BULB_OFF("/static/images/png/lightbulb-off.png"),
    ICON_TRAY_LIGHT_BULB_ON("/static/images/png/lightbulb-on.png"),
    ICON_TRAY_NETWORK("/static/images/png/network.png"),
    LOGGING_DIRECTORY("/logging"),
    PID_FILE("/onlineshop.pid"),
    SETTINGS("/settings.properties");

    public final String location;

    FileLocation(String location) {
        this.location = location;
    }

}
