package com.acme.onlineshop.persistence.user;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.security.PermissionFunction;
import com.acme.onlineshop.security.PermissionOperation;
import com.acme.onlineshop.security.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.nio.file.Path;
import java.util.*;

/**
 * <p>ATTENTION: Do NOT change this class recklessly!!! It is heavily used by the template engine Thymeleafe.</p>
 * <p>Adding something is always possible. But if something is taken away or changed, the HTML code must always be
 * tested again!</p>
 */
@Entity(name = "ONLINE_SHOP_USER")
public class User implements UserDetails {

    @Serial
    private static final long serialVersionUID = 3115895344156369657L;

    public final static int maxTokenSize = 2047;
    private final static String identifier = """
            User {
                ID = %d,
                Username = %s,
                Password = %s,
                Email = %s,
                Enabled = %b,
                Account expired = %b,
                Account locked = %b,
                Credentials expired = %b,
                Role = %s,
                Permissions = %s,
                JWT = %s
            }
            """;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(hidden = true)
    private long userId;
    @NotEmpty(message = "Username is required")
    @Column(unique = true)
    @NotBlank
    private String username;
    @NotEmpty(message = "Password is required")
    private String password;
    @NotEmpty(message = "Email is required")
    @Column(unique=true)
    private String email;
    @Schema(description = "Indicates whether the user's account has expired. An expired account cannot be authenticated.")
    private boolean accountNonExpired;
    @Schema(description = "Indicates whether the user's credentials (password) has expired. Expired credentials prevent authentication.")
    private boolean credentialsNonExpired;
    @Schema(description = "Indicates whether the user is locked or unlocked. A locked user cannot be authenticated.")
    private boolean accountNonLocked;
    @Schema(description = "Indicates whether the user is enabled or disabled. A disabled user cannot be authenticated.")
    private boolean enabled;
    @Schema(description = "A specific authentication user role")
    @NotNull(message = "A authentication role is required")
    @Enumerated(EnumType.STRING)
    private Role role;
    @Schema(description = "All permissions belonging to an user")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "permissionUser", orphanRemoval = true, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    private Set<UserPermission> permissions;
    @Schema(hidden = true)
    @NotNull
    @Column(length = maxTokenSize)
    private String accessToken;
    @NotNull
    @Column(length = maxTokenSize)
    private String refreshToken;
    @OneToOne(fetch = FetchType.LAZY)
    private Profile profile;

    public User(String username, String password, String email, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, boolean enabled, Role role, Set<UserPermission> permissions, String accessToken, String refreshToken) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
        this.role = role;
        this.permissions = permissions;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public User(User user) {
        this(user.username, user.password, user.email, user.accountNonExpired, user.credentialsNonExpired, user.accountNonLocked, user.enabled, user.role, user.permissions, user.accessToken, user.refreshToken);
        this.userId = user.userId;
    }

    public User(String userName, String password, String email, Role role) {
        this(userName, password, email, true, true, true, true, role, new HashSet<>(), "", "");
    }

    public User() {
        this("", "", "", Role.USER);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> result = new HashSet<>();
        result.add(new SimpleGrantedAuthority(role.getHierarchicalName()));
        for(UserPermission permission: permissions){
            permission.getPermissions().forEach(perm -> result.add(new SimpleGrantedAuthority(perm)));
        }
        return result;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<UserPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UserPermission> permissions) {
        this.permissions = permissions;
    }

    /**
     * ATTENTION: It looks like this method is not used anywhere. This is wrong.
     * It is used by the template engine Thymeleaf via HTML call. See
     * (.../i3de-meter-communication/src/main/resources/templates/fragments/main-users-change.html) call.
     *
     * @param operation {@link PermissionOperation} to be checked
     * @param function {@link PermissionFunction} to be checked
     * @return <code>TRUE</code> if user has permission for given operation &amp; function, <code>FALSE</code> otherwise
     */
    public boolean hasPermission(PermissionOperation operation, PermissionFunction function) {
        for(UserPermission permission: this.permissions) {
            if(permission.getOperation() == operation && permission.getFunctions().contains(function)) {
                return true;
            }
        }
        return false;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String jwt) {
        this.accessToken = jwt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshJwt) {
        this.refreshToken = refreshJwt;
    }

    public Set<String> getPermissionsAsAuthorities() {
        Set<String> result = new HashSet<>();
        for(UserPermission permission: permissions){
            result.addAll(permission.getPermissions());
        }
        return result;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Path getMediaDirectory() {
        return Path.of(ApplicationConfiguration.getMediaRootDirectory().toString(), this.username.toLowerCase(Locale.getDefault()));
    }

    public void addPermission(PermissionOperation function, PermissionFunction operation) {
        for(UserPermission permission: permissions) {
            if(permission.getOperation() == function) {
                permission.getFunctions().add(operation);
                return;
            }
        }
        Set<PermissionFunction> newOperations = new HashSet<>();
        newOperations.add(operation);
        permissions.add(new UserPermission(this, function, newOperations));
    }

    public void addPermissions(PermissionOperation function, Collection<PermissionFunction> operations) {
        for(UserPermission permission: permissions) {
            if(permission.getOperation() == function) {
                permission.getFunctions().addAll(operations);
                return;
            }
        }
        permissions.add(new UserPermission(this, function, new HashSet<>(operations)));
    }

    public void removePermission(PermissionOperation function, PermissionFunction operation) {
        for(UserPermission permission: permissions) {
            if(permission.getOperation() == function) {
                permission.getFunctions().remove(operation);
                return;
            }
        }
    }

    public void removePermissions(PermissionOperation function, Collection<PermissionFunction> operations) {
        for(UserPermission permission: permissions) {
            if(permission.getOperation() == function) {
                permission.getFunctions().removeAll(operations);
                return;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || User.class != obj.getClass()) {
            return false;
        } else {
            User other = (User) obj;
            return this == other || other.userId == this.userId && other.username.equals(this.username);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        String jwt = (this.accessToken.isBlank()) ? "- Empty -" : this.accessToken;
        return identifier.formatted(userId, username, password, email, enabled, !accountNonExpired, !accountNonLocked, !credentialsNonExpired, role, permissions, jwt);
    }
}
