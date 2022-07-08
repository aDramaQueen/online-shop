package com.acme.onlineshop.security;

import java.util.Arrays;
import java.util.List;

/**
 * This enumeration defines specific user roles.
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public enum Role {
    /**
     * Normal user without any permissions has usually very restricted access to application
     */
    USER("User"),
    /**
     * Admin user is able to do everything, without any restrictions
     */
    ADMIN("Administrator");

    public final String humanReadableName;

    // ATTENTION: If you rename this Enum class, you also have to edit following HTML files
    //      .../i3de-meter-communication/src/main/resources/templates/fragments/main-users-add.html
    //      .../i3de-meter-communication/src/main/resources/templates/fragments/main-users-change.html
    Role(String humanReadableName) {
        this.humanReadableName = humanReadableName;
    }

    /**
     * Hierarchical role names start with "ROLE_"
     *
     * @return Authority string
     * @see <a href="https://docs.spring.io/spring-security/site/docs/5.2.11.RELEASE/reference/html/authorization.html#authz-role-voter">Spring Security - Role voter</a>
     */
    public String getHierarchicalName() {
        return "ROLE_"+this.name();
    }

    /**
     * Builds &amp; returns a string representing the role hierarchy
     *
     * @return The role hierarchy
     * @see org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
     */
    public static String getHierarchy() {
        return ADMIN.getHierarchicalName() + " > " + USER.getHierarchicalName();
    }

    public static List<String> getAllRoles() {
        Role[] allRoles = Role.values();
        String[] result = new String[allRoles.length];
        for(int i=0; i<allRoles.length; i++) {
            result[i] = allRoles[i].humanReadableName;
        }
        return Arrays.asList(result);
    }
}
