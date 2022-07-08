package com.acme.onlineshop.utils.validators;

import com.acme.onlineshop.utils.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>Parent interface for constants, that most "Spring Boot" property validator need.</p>
 *
 * <p>All validators will check their dedicated properties at boot-up time!</p>
 *
 * @see Validator
 */
public interface BootValidation extends Validator {

    String NOT_EMPTY = "Property mustn't be empty string";

    /**
     * Utility class that holds the current active "Spring Boot" profiles
     *
     * @see Profile
     */
    @Component
    class CurrentProfiles {

        private String activeProfile;
        private String defaultProfile;

        @Autowired
        public CurrentProfiles(@Value("${spring.profiles.active:}")String activeProfile, @Value("${spring.profiles.default:}")String defaultProfile) {
            this.activeProfile = activeProfile;
            this.defaultProfile = defaultProfile;
        }

        public String getActiveProfile() {
            return activeProfile;
        }

        public void setActiveProfile(String activeProfile) {
            this.activeProfile = activeProfile;
        }

        public String getDefaultProfile() {
            return defaultProfile;
        }

        public void setDefaultProfile(String defaultProfile) {
            this.defaultProfile = defaultProfile;
        }

        public Set<Profile> getCurrentProfiles() {
            Set<Profile> result = new HashSet<>();
            String profileString = (activeProfile.isEmpty()) ? defaultProfile : activeProfile;
            for(String profile: profileString.split(",")) {
                result.add(Profile.getProfile(profile.strip()));
            }
            return result;
        }
    }

}
