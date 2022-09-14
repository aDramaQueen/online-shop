package com.acme.onlineshop.security;

import com.acme.onlineshop.Constants;
import com.acme.onlineshop.filters.JWTFilter;
import com.acme.onlineshop.web.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration for security measures with multiple HttpSecurity instances. To be precise 3 instances:
 *  <dl>
 *      <dt>Authentication for REST API endpoints:</dt>
 *      <dd><b>J</b>SON <b>W</b>eb <b>T</b>oken (<b>JWT</b>) based</dd>
 *      <dt>Authentication for REST API endpoints from Spring Actuator:</dt>
 *      <dd><b>J</b>SON <b>W</b>eb <b>T</b>oken (<b>JWT</b>) based</dd>
 *      <dt>Authentication for "normal" website endpoints:</dt>
 *      <dd>Session based</dd>
 * </dl>
 *
 * <p>The REST endpoint use <b>NO</b> CSRF, since they are pure REST endpoint without any use of cookies, there is no need
 * for any protection: <a href="https://security.stackexchange.com/questions/166724/should-i-use-csrf-protection-on-rest-api-endpoints">Should I use CSRF protection on Rest API endpoints?</a>
 * </p><br>
 *
 * <b>ATTENTION:</b> The order in these 3 configurations is important. The one with the lowest order is checked first,
 * then the one with the second lowest, then the third lowest, etc. etc.
 *
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/configuration/java.html">Spring Security - Configuration</a>
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/configuration/java.html#_multiple_httpsecurity">Spring Security - Configuration for multiple HttpSecurities</a>
 */
@Configuration
@EnableWebSecurity(debug = Constants.WEB_SECURITY_DEBUG)
public class SecurityConfig {

    private final static AuthenticationSuccessHandler successHandler = loginAuthenticationSuccessHandler();
    private final static AuthenticationFailureHandler failureHandler = loginAuthenticationFailureHandler();

    private static final String[] WHITELIST_RESOURCES = {
            "/css/**",
            "/js/**",
            "/images/**",
            "/**/favicon.ico"
    };
    private static final String[] WHITELIST_REST_ENDPOINTS = {
            URL.Path.REST_SYSTEM + "/current-info*",
            URL.Path.REST_SYSTEM + "/error-codes*",
            URL.Path.REST_SYSTEM + "/error-code-header*",
            URL.Path.REST_SECURITY + "*",
            URL.Path.REST_SECURITY + "/**"
    };
    private static final String[] WHITELIST_WEBSITES = {
            URL.INDEX.url,
            URL.LOGIN.url+"*",
            URL.OPEN_API_WEB.url+"*",
            URL.OPEN_API_JSON.url+"*",
            URL.OPEN_API_JSON.url+"/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };
    private final JWTFilter jwtFilter;

    @Autowired
    SecurityConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Configuration for REST endpoints: Authentication system based on <b>J</b>SON <b>W</b>eb <b>T</b>okens (<b>JWT</b>)
     */
    @Order(1)
    @Bean
    protected SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        if(Constants.WEB_SECURITY) {
            // Everything that follow is just valid for endpoints beyond "/api/..."
            http.antMatcher("%s/**".formatted(URL.REST_API.url))
                    // For a pure REST endpoint, there is no need for CSRF tokens
                    .csrf().disable()
                    .authorizeRequests()
                    // Allows OpenAPI 3.0 & some chosen single endpoints without any security barrier
                    .antMatchers(WHITELIST_REST_ENDPOINTS).permitAll()
                    // TODO: Add some REST endpoints
                    .antMatchers(HttpMethod.DELETE, "%s/*".formatted(URL.REST_SECURITY.url)).hasAuthority(PermissionFunction.DELETE.getPermission(PermissionOperation.SYSTEM))
                    .antMatchers(HttpMethod.POST, "%s/*".formatted(URL.REST_SECURITY.url)).hasAuthority(PermissionFunction.CREATE.getPermission(PermissionOperation.SYSTEM))
                    .antMatchers(HttpMethod.PUT, "%s/*".formatted(URL.REST_SECURITY.url)).hasAuthority(PermissionFunction.CREATE_UPDATE.getPermission(PermissionOperation.SYSTEM))
                    .antMatchers(HttpMethod.GET, "%s/*".formatted(URL.REST_SECURITY.url)).hasAuthority(PermissionFunction.READ.getPermission(PermissionOperation.SYSTEM))
                    // All other requests have to be from any authenticated user
                    .anyRequest().authenticated()
                    // Stateless session for JWT
                    .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    // Add JWT BEFORE standard user authentication
                    .and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);//.addFilterAfter(errorCodeFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            //Allow every request from any (unauthorized) user & disable CSRF tokens
            http.antMatcher("%s/**".formatted(URL.REST_API.url)).authorizeRequests().anyRequest().permitAll().and().csrf().disable();
        }
        return http.build();
    }

    /**
     * Configuration for "Spring Boot Actuator" REST endpoints: Authentication system based on <b>J</b>SON <b>W</b>eb <b>T</b>okens (<b>JWT</b>)
     */
    @Order(2)
    @Bean
    protected SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        if(Constants.WEB_SECURITY) {
            // Everything that follow is just valid for endpoints beyond "/actuator/..."
            http.antMatcher("%s/**".formatted(URL.REST_ACTUATOR.url))
                    // For a pure REST endpoint, there is no need for CSRF tokens
                    .csrf().disable()
                    .authorizeRequests()
                    // Allows OpenAPI 3.0 & some chosen single endpoints without any security barrier
                    .antMatchers(WHITELIST_REST_ENDPOINTS).permitAll()
                    // All requests have to be from any authenticated user
                    .anyRequest().authenticated()
                    // Stateless session for JWT
                    .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    // Add JWT BEFORE standard user authentication
                    .and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            //Allow every request from any (unauthorized) user & disable CSRF tokens
            http.antMatcher("%s/**".formatted(URL.REST_ACTUATOR.url)).authorizeRequests().anyRequest().permitAll().and().csrf().disable();
        }
        return http.build();
    }

    /**
     * Configuration for "normal" websites: Authentication system based on sessions
     */
    @Order(3)
    @Bean
    protected SecurityFilterChain websiteFilterChain(HttpSecurity http) throws Exception {
        if(Constants.WEB_SECURITY) {
            http.authorizeRequests()
                    // Allows home view, login view, OpenAPI & resources without any security barrier
                    .antMatchers(WHITELIST_RESOURCES).permitAll()
                    .antMatchers(WHITELIST_REST_ENDPOINTS).permitAll()
                    .antMatchers(WHITELIST_WEBSITES).permitAll()
                    // All "User" related sites are just accessible for admin users
                    .antMatchers(URL.USERS.url + "/**").hasRole(Role.ADMIN.name())
                    // All other requests (e.g. statistics, meters, modbus,...) have to be from any authenticated user
                    .anyRequest().authenticated()
                    // Login mechanism
                    .and().formLogin().loginPage(URL.Path.LOGIN).successHandler(successHandler).failureHandler(failureHandler)
                    // Logout mechanism
                    .and().logout().logoutUrl(URL.Path.LOGOUT).deleteCookies("JSESSIONID").logoutSuccessUrl(URL.Path.HOME).permitAll()
                    // Allows loading HTML <object> tag
                    .and().headers().frameOptions().sameOrigin()
                    // Allow "Basic Auth"
                    .and().httpBasic();
        } else {
            //Allow every request from any (unauthorized) user & disable CSRF tokens
            http.authorizeRequests().anyRequest().permitAll()
                    // Login mechanism (is actually not very useful, since anonymous users have already all rights...)
                    .and().formLogin().loginPage(URL.Path.LOGIN).successHandler(successHandler).failureHandler(failureHandler)
                    // Logout mechanism
                    .and().logout().logoutUrl(URL.Path.LOGOUT).deleteCookies("JSESSIONID").logoutSuccessUrl(URL.Path.HOME).permitAll()
                    // Allows loading HTML <object> tag
                    .and().headers().frameOptions().sameOrigin()
                    // Allow "Basic Auth"
                    .and().httpBasic();
        }
        return http.build();
    }

    /**
     * {@link RoleVoter} for custom hierarchy of {@link Role}s
     *
     * @return {@link RoleHierarchyVoter}
     * @see <a href="https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html#authz-hierarchical-roles">Spring Security - Hierarchical Roles</a>
     */
    @Bean
    public RoleHierarchyVoter hierarchyVoter() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(Role.getHierarchy());
        return new RoleHierarchyVoter(roleHierarchy);
    }

    /**
     * Returns the {@link AuthenticationManager} for this application
     *
     * @param authenticationConfiguration Autowired authentication configuration
     * @return Authentication manager that handles all users
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private static AuthenticationSuccessHandler loginAuthenticationSuccessHandler() {
        return new RedirectionAuthenticationSuccessHandler(URL.Path.HOME);
    }

    private static AuthenticationFailureHandler loginAuthenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler(URL.Path.LOGIN + "?error=true");
    }
}
