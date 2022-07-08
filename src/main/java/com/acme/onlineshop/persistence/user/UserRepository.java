package com.acme.onlineshop.persistence.user;

import com.acme.onlineshop.security.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Database user {@link Repository}
 *
 * @see <a href="https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods">JPA - Query methods</a>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Gets {@link User} by ID and loads {@link User} itself in {@link UserPermission} children eagerly
     *
     * @param userID Unique ID from desired {@link User}
     * @return {@link User} belonging to given ID
     */
    @EntityGraph(attributePaths = {"permissions.permissionUser"}, type = EntityGraph.EntityGraphType.LOAD)
    User getEagerByUserId(long userID) throws EntityNotFoundException;

    /**
     * Searches the database for a specific {@link User} with given username
     *
     * @param username Unique username
     * @return The {@link User} belonging to this username
     */
    Optional<User> findByUsername(String username);

    /**
     * Returns the user defined by username, with eager loaded {@link UserPermission}s
     *
     * @param username Name for desired {@link User}
     * @return Desired {@link User}
     */
    @EntityGraph(attributePaths = {"permissions.permissionUser"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findEagerByUsername(String username);

    /**
     * Returns boolean, if user exists ot not.
     *
     * @param username Username of user in question
     * @return <code>True</code> if user exists, <code>false</code> otherwise
     */
    boolean existsUserByUsername(String username);

    /**
     * Find first user on database with specified {@link Role}
     *
     * @param role The {@link Role}, a {@link User} has to have
     * @return {@link User} with specified {@link Role}
     */
    Optional<User> getFirstByRole(Role role);

    /**
     * Find all users on database with specified {@link Role}
     *
     * @param role The {@link Role}, a {@link User} has to have
     * @return {@link List} of {@link User}s with specified {@link Role}
     */
    List<User> findAllByRole(Role role);

    /**
     * Count users on database with specified {@link Role}
     *
     * @param role The {@link Role}, the {@link User} has to have
     * @return Number of {@link User}s with specified {@link Role}
     */
    long countByRole(Role role);

    long countByRoleAndUsernameIn(Role role, Collection<String> username);

    /**
     * Deletes a {@link User}, if the given username matches to any existing {@link User}
     *
     * @param username Name of {@link User} which should be deleted
     */
    void deleteByUsername(String username);

    /**
     * Deletes a {@link Collection} of {@link User}s, if the give usernames matches to any existing {@link User}s
     *
     * @param usernames {@link Collection} of name of {@link User}s which should be deleted
     */
    void deleteByUsernameIn(Collection<String> usernames);
}
