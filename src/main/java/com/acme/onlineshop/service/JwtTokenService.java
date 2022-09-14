package com.acme.onlineshop.service;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.Constants;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import com.acme.onlineshop.persistence.user.User;
import com.acme.onlineshop.security.Role;
import com.acme.onlineshop.web.URL;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * <p>This service generates JWT for {@link URL} endpoints of this application.</p>
 *
 * <p><b>ATTENTION:</b> To work properly, this {@link Service} needs a key to generate JWTs. The key should be
 * <u>unique</u> for each instance of this application. Otherwise you could generate with a second instance keys that
 * would be valid for the other instance.</p>
 * <p>To load the key in this application, you have to set the variable "<code>jwt.key</code>" in a property file, or
 * as a environment variable.</p>
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config">Spring Boot - External Configuration</a>
 * @see <a href="https://github.com/jwtk/jjwt#java-jwt-json-web-token-for-java-and-android">JJWT - Java JSON Web Token</a>
 */
@Service
public class JwtTokenService implements ApplicationListener<ApplicationReadyEvent> {

    public final static String claimRole = "role";
    public final static String claimPermissions = "perm";
    public final static String claimType = "type";
    public final static String claimAccess = "access";
    public final static String claimRefresh = "refresh";

    /**
     * Algorithm for token generation
     *
     * @see <a href="https://github.com/jwtk/jjwt#signature-algorithms-keys">Signature Algorithms Keys</a>
     */
    private final static SignatureAlgorithm encryptingAlgorithm = SignatureAlgorithm.HS512;
    private final ApplicationConfigRepository applicationConfigRepo;
    private JwtParser parser;
    private Key key;

    @Autowired
    public JwtTokenService(ApplicationConfigRepository applicationConfigRepo, @Value("${jwt.key:}") String jwtKey) {
        this.applicationConfigRepo = applicationConfigRepo;
        updateKey(jwtKey);
    }

    /**
     * Method will be executed exactly one time, namely when the application is fully loaded.
     *
     * @param readyEvent The "boot up is finished" event
     * @see ApplicationReadyEvent
     */
    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent readyEvent) {
        String jwtKey = applicationConfigRepo.getReferenceById(ApplicationConfiguration.APPLICATION_CONFIG_ID).getJwtKey();
        updateKey(jwtKey);
    }

    public void updateKey(String jwtKey) {
        this.key = getKey(jwtKey, encryptingAlgorithm);
        this.parser = Jwts.parserBuilder().setSigningKey(this.key).build();
    }

    /**
     * Tests given JWT key for validity.
     *
     * @param jwtKey Key to be tested
     * @throws WeakKeyException if given key is invalid
     */
    public static void testNewKey(String jwtKey) throws WeakKeyException {
        JwtTokenService service = new JwtTokenService(null, jwtKey);
        service.generateRefreshToken(Constants.DEFAULT_ADMIN_USERNAME, Role.ADMIN, 1);
    }

    /**
     * If the signature of the token matches its payload, the payload is extracted and returned individually in
     * so-called {@link Claims}. These are just a kind of {@link Map} that can then be conveniently searched for
     * individual contents.
     *
     * @param encryptedToken The raw encrypted token string from the header, from the HTTP {@link HttpServletRequest}
     * @return Returns a {@link Optional} of {@link Jws} of {@link Claims}, if encryption is valid.
     *         Returns an empty {@link Optional} otherwise.
     */
    public Optional<Jws<Claims>> decryptToken(String encryptedToken) {
        try {
            return Optional.of(parser.parseClaimsJws(encryptedToken));
        } catch (JwtException exc) {
            return Optional.empty();
        }
    }

    public String generateAccessToken(String username, Role role, Set<String> permissions, long lifeTimeInHours) throws WeakKeyException {
        Map<String, Object> claims = getClaims(role, permissions);
        claims.put(claimType, claimAccess);
        return generateToken(username, claims, lifeTimeInHours);
    }

    public String generateRefreshToken(String username, Role role, long lifeTimeInHours) throws WeakKeyException {
        Map<String, Object> claims = Map.of(
                claimType,
                claimRefresh,
                claimRole,
                role.getHierarchicalName()
        );
        return generateToken(username, claims, lifeTimeInHours);
    }

    public static boolean isNotExpired(Date expirationDate) {
        if(expirationDate == null) {
            return true;
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiration = expirationDate.toInstant().atZone(ApplicationConfiguration.getTimeZone()).toLocalDateTime();
            return expiration.isAfter(now);
        }
    }

    private String generateToken(String username, Map<String, Object> claims, long lifeTimeInHours) throws WeakKeyException {
        LocalDateTime now = LocalDateTime.now();
        ZoneOffset offset = ApplicationConfiguration.getTimeZone().getRules().getOffset(now);
        LocalDateTime expirationDate = now.plusHours(lifeTimeInHours);

        return Jwts.builder()
                .addClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(now.toInstant(offset)))
                .setExpiration(Date.from(expirationDate.toInstant(offset)))
                .signWith(key)
                .compact();
    }

    /**
     * Creates &amp; returns a {@link Map} of {@link String}s/{@link List} of {@link String}s which represents all authorities belonging to a certain
     * {@link User}. In JWT jargon it is a so-called "claim".
     *
     * @param role {@link Role} belonging to a certain {@link User}
     * @param authorities {@link Collection} of {@link GrantedAuthority}s belonging to a certain user
     * @return {@link Map} of {@link String}s/{@link List} of {@link String}s, which represents the authorizations for a certain {@link User}
     * @see <a href="https://github.com/jwtk/jjwt#claims">JWT claims</a>
     */
    private static Map<String, Object> getClaims(Role role, Set<String> authorities) {
        Map<String, Object> result = new HashMap<>();
        result.put(claimRole, role.getHierarchicalName());
        result.put(claimPermissions, authorities);
        return result;
    }

    private static SecretKey getKey(String key, SignatureAlgorithm algorithm) {
        byte[] encodedKey = key.getBytes();
        return new SecretKeySpec(encodedKey,0,encodedKey.length, algorithm.getJcaName());
    }
}
