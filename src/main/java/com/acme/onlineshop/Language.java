package com.acme.onlineshop;

/**
 * Enumeration scheme is following ISO 639-1 codes
 *
 * @see <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">Wikipedia - List of ISO 639-1 codes</a>
 * @author Richard Saeuberlich
 * @version 1.0
 */
public enum Language {

    ENGLISH("English", "en"),
    FRENCH("French", "fr"),
    GERMAN("German", "de"),
    SPANISH("Spanish", "es");

    final public String prettyName, iso_639_1_code;

    Language(String prettyName, String iso_639_1_code) {
        this.prettyName = prettyName;
        this.iso_639_1_code = iso_639_1_code;
    }
}
