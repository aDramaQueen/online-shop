package com.acme.onlineshop.utils.validators;

import com.acme.onlineshop.utils.Profile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>"Spring Boot" validator that checks the selected {@link Profile} for this application on boot-up time.</p>
 * <p>Only one profile can be selected at once. Further more, certain conditions may force you to select a specific
 * profile.</p>
 */
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.profiles")
public class ProfileProperties implements BootValidation {

    private final static String EXACTLY_TWO_PROFILES = "There must be exactly 2 profiles.%n\tOne for code execution mode: %s%n\tOne for embedded database selection: %s".formatted(Arrays.toString(Profile.getCodeProfiles()), Arrays.toString(Profile.getDatabaseProfiles()));
    private final static String PROFILE_ACTIVE_IDENTIFIER = "active";

    private String activeProfile = "";
    private String defaultProfile = "";

    public String getActive() {
        return activeProfile;
    }

    public void setActive(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public String getDefault() {
        return defaultProfile;
    }

    public void setDefault(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ProfileProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String profile = (activeProfile.isEmpty()) ? defaultProfile : activeProfile;
        Set<String> profiles = extractProfiles(profile);
        if(profiles.isEmpty()) {
            errors.rejectValue(PROFILE_ACTIVE_IDENTIFIER, "", NOT_EMPTY);
        } else if(profiles.size() < 2) {
            errors.rejectValue(PROFILE_ACTIVE_IDENTIFIER, "", "Can't load just 1 profile.\n\t"+EXACTLY_TWO_PROFILES);
        } else if(profiles.size() > 2) {
            errors.rejectValue(PROFILE_ACTIVE_IDENTIFIER, "", "Can't load more then 2 profiles.\n\t"+EXACTLY_TWO_PROFILES);
        } else {
            Iterator<String> it = profiles.iterator();
            String code = null, database = null, temp;
            while (it.hasNext()) {
                temp = it.next();
                try {
                    if (Profile.isCodeProfile(temp)) {
                        code = temp;
                    } else {
                        database = temp;
                    }
                } catch (IllegalStateException exc) {
                    errors.rejectValue(PROFILE_ACTIVE_IDENTIFIER, "", "Unknown profile -> '%s'\n\t%s".formatted(temp, EXACTLY_TWO_PROFILES));
                }
            }
            if(code == null){
                errors.rejectValue(PROFILE_ACTIVE_IDENTIFIER, "", "Did NOT found a code profile.\n\t"+EXACTLY_TWO_PROFILES);
            }else if(database == null){
                errors.rejectValue(PROFILE_ACTIVE_IDENTIFIER, "", "Did NOT found a database profile.\n\t"+EXACTLY_TWO_PROFILES);
            }
        }
    }

    private Set<String> extractProfiles(String profiles) {
        Set<String> result = new HashSet<>();
        if (!profiles.isEmpty()) {
            for (String profile : profiles.split(",")) {
                result.add(profile.strip());
            }
        }
        return result;
    }
}
