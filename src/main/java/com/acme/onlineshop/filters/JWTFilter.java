package com.acme.onlineshop.filters;

import com.acme.onlineshop.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final static Logger LOGGER = LoggerFactory.getLogger(JWTFilter.class);
    public final static String BEARER = "Bearer ";
    public final static int BEARER_LENGTH = BEARER.length();

    private final JwtTokenService jwtTokenService;

    @Autowired
    public JWTFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * This filter is meant to be used for REST endpoints, since he is <b>NOT</b> continuing the filter chain if the
     * request has no authorization header.
     *
     * @param request {@link HttpServletRequest} The (incoming) request
     * @param response {@link HttpServletResponse} The response for given (incoming) request
     * @param filterChain {@link FilterChain} The chain, that only will be continued if validation was successful
     * @throws IOException If something went horribly wrong...
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(SecurityContextHolder.getContext().getAuthentication() == null && authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            String jwt = authorizationHeader.substring(BEARER_LENGTH);
            Optional<Jws<Claims>> tokenOptional = jwtTokenService.decryptToken(jwt);
            if(tokenOptional.isPresent()) {
                Jws<Claims> token = tokenOptional.get();
                // Check if token is expired
                if(JwtTokenService.isNotExpired(token.getBody().getExpiration()) && JwtTokenService.claimAccess.equals(token.getBody().get(JwtTokenService.claimType, String.class))) {
                    // Add Role authority for current request
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(token.getBody().get(JwtTokenService.claimRole, String.class)));
                    // Add Permission authorities for current request
                    @SuppressWarnings({"unchecked"})List<String> permissions = token.getBody().get(JwtTokenService.claimPermissions, List.class);
                    permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
                    // Set authorities for current request
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(token.getBody().getSubject(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            LOGGER.warn("Invalid token from: %s".formatted(getIP(request)));
        }
        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    private static String getIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forward-For");
        return (ipAddress == null) ? request.getRemoteAddr() : ipAddress;
    }
}
