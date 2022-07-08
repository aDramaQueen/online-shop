package com.acme.onlineshop.service;

import com.acme.onlineshop.exception.EnablingException;
import com.acme.onlineshop.exception.PasswordStrengthException;
import com.acme.onlineshop.exception.RESTException;
import com.acme.onlineshop.exception.RoleChangeException;
import com.acme.onlineshop.persistence.user.User;
import com.acme.onlineshop.persistence.user.UserPermission;
import com.acme.onlineshop.persistence.user.UserRepository;
import com.acme.onlineshop.security.PasswordValidator;
import com.acme.onlineshop.security.PermissionFunction;
import com.acme.onlineshop.security.PermissionOperation;
import com.acme.onlineshop.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean atLeastOneAdmin() {
        return userRepository.countByRole(Role.ADMIN) > 0;
    }

    @Transactional
    public User updateUserAccessToken(String username, String accessToken) throws EntityNotFoundException {
        checkTokenSize(accessToken);
        User user = getUserEager(username);
        user.setAccessToken(accessToken);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRefreshToken(String username, String refreshToken) throws EntityNotFoundException {
        checkTokenSize(refreshToken);
        User user = getUserEager(username);
        user.setRefreshToken(refreshToken);
        return userRepository.save(user);
    }

    @Transactional
    public User addNewUser(User user, boolean validatePassword) {
        setUserForUserPermissions(user);
        if(validatePassword) {
            PasswordValidator validator = new PasswordValidator();
            if(!validator.isValid(user.getPassword())) {
                throw new PasswordStrengthException(validator.getErrorMessages());
            }
        }
        if(userRepository.countByRole(Role.ADMIN) < 1) {
            // If not even one admin user, make new user to admin user
            user.setRole(Role.ADMIN);
        }
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        if(user.getRole() == Role.ADMIN) {
            addMissingAdminPermissions(user);
            user.setEnabled(true);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(User user) {
        if(user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) < 1) {
            throw new IllegalStateException("Cannot delete last admin user");
        }
        userRepository.delete(user);
    }

    @Transactional
    public void deleteUsers(List<String> usernames) {
        if(nbrOfAdminUsersIsValid(usernames)) {
            userRepository.deleteByUsernameIn(usernames);
        } else {
            throw new IllegalStateException("Cannot delete last admin users");
        }
    }

    @Transactional(readOnly = true)
    public User loadUserByID(long id) throws EntityNotFoundException {
        return userRepository.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RESTException("User %s does NOT exist".formatted(username));
        } else {
            return user.get();
        }
    }

    @Transactional(readOnly = true)
    public boolean doesUserExist(String username) {
        return userRepository.existsUserByUsername(username);
    }

    @Transactional
    public User updateUser(User originalUser, User modifiedUser) throws RoleChangeException {
        if(originalUser.getRole()==Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            if(modifiedUser.getRole()!=Role.ADMIN) {
                throw new RoleChangeException("Can't change last admin user to none admin user");
            }
            if(!modifiedUser.isEnabled()) {
                throw new EnablingException("Can't disable last admin user");
            }
        }
        setUserForUserPermissions(modifiedUser);
        if(modifiedUser.getRole() == Role.ADMIN) {
            addMissingAdminPermissions(modifiedUser);
        }
        modifiedUser.setUserId(originalUser.getUserId());
        modifiedUser.setPassword(originalUser.getPassword());
        return userRepository.save(modifiedUser);
    }

    public static String generateRandomPassword() {
        return PasswordValidator.generateValidPassword();
    }

    @Transactional
    public User updateUserPassword(String username, String oldPassword, String newPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User \""+username+"\" does NOT exist");
        } else {
            User newPasswordUser = user.get();
            if(passwordEncoder.matches(oldPassword, newPasswordUser.getPassword())) {
                PasswordValidator validator = new PasswordValidator();
                if(validator.isValid(newPassword)) {
                    newPasswordUser.setPassword(passwordEncoder.encode(newPassword));
                    return userRepository.save(newPasswordUser);
                } else {
                    throw new PasswordStrengthException(validator.getErrorMessages());
                }
            } else {
                throw new BadCredentialsException("Invalid Password");
            }
        }
    }

    /**
     * This step is necessary, since the construction of user permissions was done without the user itself.
     *
     * @param user User with NOT linked {@link UserPermission}s
     * @see com.acme.onlineshop.persistence.user.UserPermissionConverter
     */
    private void setUserForUserPermissions(User user) {
        for(UserPermission permission: user.getPermissions()) {
            permission.setPermissionUser(user);
        }
    }

    /**
     * If you delete {@link User}s, first check if there is at least 1 admin user left after the transaction
     *
     * @param usernames {@link List} of usernames which should be deleted
     * @return <code>TRUE</code> if at least 1 user is left after the transaction, <code>FALSE</code> otherwise
     */
    private boolean nbrOfAdminUsersIsValid(List<String> usernames) {
        long nbrAdminUsers = userRepository.countByRole(Role.ADMIN);
        long nbrAdminUsersToDelete = userRepository.countByRoleAndUsernameIn(Role.ADMIN, usernames);
        return nbrAdminUsers > nbrAdminUsersToDelete;
    }

    private void checkTokenSize(String token) {
        if (token.length() > User.maxTokenSize) {
            throw new PersistenceException("Size of JWT too big for internal database. Max size: %d".formatted(User.maxTokenSize));
        }
    }

    private User getUserEager(String username) {
        Optional<User> userOptional = userRepository.findEagerByUsername(username);
        if(userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new EntityNotFoundException("User with username %s does NOT exist".formatted(username));
        }
    }

    private void addMissingAdminPermissions(User user) {
        for(UserPermission permission: getAdminPermissions(user)) {
            Optional<UserPermission> existingPermission = user.getPermissions().stream().filter(userPermission -> userPermission.getOperation() == permission.getOperation()).findAny();
            if(existingPermission.isPresent()) {
                existingPermission.get().getFunctions().addAll(permission.getFunctions());
            } else {
                user.getPermissions().add(permission);
            }
        }
    }

    private Set<UserPermission> getAdminPermissions(User user) {
        Set<UserPermission> result = new HashSet<>();
        Set<PermissionFunction> operations = EnumSet.allOf(PermissionFunction.class);
        for (PermissionOperation function : PermissionOperation.values()) {
            result.add(new UserPermission(user, function, operations));
        }
        return result;
    }
}
