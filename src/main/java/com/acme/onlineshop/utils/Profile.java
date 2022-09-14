package com.acme.onlineshop.utils;

import java.util.Arrays;

/**
 * Enumeration of all known/allowed profiles for this application.
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/3.0.0-M1/reference/htmlsingle/#features.profiles">Spring Boot - Profiles</a>
 */
public enum Profile {

    DEVELOPMENT("Development", Tag.DEVELOPMENT),
    PRODUCTION("Production", Tag.PRODUCTION),
    TEST("Test", Tag.TEST),
    H2("H2", Tag.H2),
    HYPER_SQL("HyperSQL", Tag.HYPER_SQL),
    DERBY("Apache Derby", Tag.DERBY);

    public static class Tag {
        public final static String DEVELOPMENT = "development";
        public final static String PRODUCTION = "production";
        public final static String TEST = "test";
        public final static String H2 = "h2";
        public final static String HYPER_SQL = "hsql";
        public final static String DERBY = "derby";
    }

    public final String prettyName, tag;

    Profile(String prettyName, String tag) {
        this.prettyName = prettyName;
        this.tag = tag;
    }

    public static String[] getCodeProfiles() {
        Profile[] codeProfiles = new Profile[]{DEVELOPMENT, PRODUCTION, TEST};
        return getProfiles(codeProfiles);
    }

    public static String[] getDatabaseProfiles() {
        Profile[] databaseProfiles = new Profile[]{H2, HYPER_SQL};
        return getProfiles(databaseProfiles);
    }

    public static Profile getProfile(String name) throws IllegalArgumentException {
        for(Profile profile: Profile.values()) {
            if(profile.tag.equals(name)) {
                return profile;
            }
        }
        throw new IllegalArgumentException("Unknown profile: %s\n\tKnown profiles: %s".formatted(name, Arrays.toString(Profile.values())));
    }

    public static boolean isCodeProfile(String tag) throws IllegalStateException {
        Profile profile = getProfile(tag);
        switch(profile) {
            case DEVELOPMENT, PRODUCTION, TEST -> {
                return true;
            }
            case H2, HYPER_SQL -> {
                return false;
            }
            default -> throw new IllegalStateException("Unknown profile: "+profile);
        }
    }

    public static boolean isDatabaseProfile(String tag) {
        return !isCodeProfile(tag);
    }

    private static String[] getProfiles(Profile[] profiles) {
        String[] result = new String[profiles.length];
        for(int i=0; i<profiles.length; i++){
            result[i] = profiles[i].tag;
        }
        return result;
    }
}
