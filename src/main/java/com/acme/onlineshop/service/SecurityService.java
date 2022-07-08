package com.acme.onlineshop.service;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.controller.SecurityController;
import com.acme.onlineshop.persistence.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class SecurityService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public SecurityService(UserService userService, JwtTokenService jwtTokenService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public SecurityController.JWTTokens getCurrentTokens(String username, String password) throws UsernameNotFoundException, BadCredentialsException {
        User user = userService.loadUserByUsername(username);
        validatePassword(username, password);
        return new SecurityController.JWTTokens(user.getAccessToken(), user.getRefreshToken());
    }

    public String generateNewAccessToken(String refreshToken, long lifeTimeInHours) {
        Optional<Jws<Claims>> tokenOptional = jwtTokenService.decryptToken(refreshToken);
        if(tokenOptional.isPresent()) {
            Jws<Claims> token = tokenOptional.get();
            if(JwtTokenService.isNotExpired(token.getBody().getExpiration())) {
                if(JwtTokenService.claimRefresh.equals(token.getBody().get(JwtTokenService.claimType, String.class))) {
                    User user = userService.loadUserByUsername(token.getBody().getSubject());
                    String accessToken = jwtTokenService.generateAccessToken(user.getUsername(), user.getRole(), user.getPermissionsAsAuthorities(), lifeTimeInHours);
                    return userService.updateUserAccessToken(user.getUsername(), accessToken).getAccessToken();
                }
                throw new BadCredentialsException("Not a refresh token");
            }
            throw new BadCredentialsException("Token expired");
        }
        throw new BadCredentialsException("Token invalid");
    }

    public String generateNewRefreshToken(String username, String password, long lifeTimeInHours) {
        User user = userService.loadUserByUsername(username);
        validatePassword(username, password);
        String token = jwtTokenService.generateRefreshToken(user.getUsername(), user.getRole(), lifeTimeInHours);
        return userService.updateUserRefreshToken(user.getUsername(), token).getRefreshToken();
    }

    private void validatePassword(String username, String password) throws BadCredentialsException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private boolean validateRawPassword(String givenPassword, String dbPassword) {
        return passwordEncoder.matches(givenPassword, dbPassword);
    }

    private static LocalDateTime convert(Date date) {
        return date.toInstant().atZone(ApplicationConfiguration.getTimeZone()).toLocalDateTime();
    }
}
