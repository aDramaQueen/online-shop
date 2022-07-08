package com.acme.onlineshop.controller;

import com.acme.onlineshop.Util;
import com.acme.onlineshop.persistence.configuration.ApplicationConfigRepository;
import com.acme.onlineshop.security.PermissionFunction;
import com.acme.onlineshop.security.PermissionOperation;
import com.acme.onlineshop.security.Role;
import com.acme.onlineshop.service.JwtTokenService;
import com.acme.onlineshop.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

public abstract class RestApiTest {

    protected static String Prefix = "Bearer ";
    protected static String tokenAllPrivileges, tokenNoPrivileges;
    protected static Optional<Jws<Claims>> allPrivileges;
    protected static Optional<Jws<Claims>> noPrivileges;

    @MockBean
    protected ApplicationConfigRepository applicationConfigRepository;
    @MockBean
    protected UserService userService;
    @MockBean
    protected JwtTokenService jwtTokenService;

    @BeforeAll
    public static void jwtInitialization() throws IOException {
        JwtTokenService jwtTokenService = new JwtTokenService(null, getKey());
        String all = jwtTokenService.generateAccessToken("admin", Role.ADMIN, getAllPermissions(), 1);
        String no = jwtTokenService.generateAccessToken("user", Role.USER, new HashSet<>(), 1);
        allPrivileges = jwtTokenService.decryptToken(all);
        noPrivileges = jwtTokenService.decryptToken(no);
        tokenAllPrivileges = Prefix + all;
        tokenNoPrivileges = Prefix + no;
    }

    @BeforeEach
    public void jwtSetUp() {
        when(jwtTokenService.decryptToken(tokenAllPrivileges.substring(Prefix.length()))).thenReturn(allPrivileges);
        when(jwtTokenService.decryptToken(tokenNoPrivileges.substring(Prefix.length()))).thenReturn(noPrivileges);
    }

    private static String getKey() throws IOException {
        String fileText = Util.readFile(Util.FileName.JWT_ENCRYPTION_KEY);
        return fileText.split("=")[1].replace("\"", "").strip();
    }

    private static Set<String> getAllPermissions() {
        Set<String> result = new HashSet<>();
        Set<PermissionFunction> operations = EnumSet.allOf(PermissionFunction.class);
        for (PermissionOperation function : PermissionOperation.values()) {
            operations.forEach(op -> result.add(function.getPermission(op)));
        }
        return result;
    }
}
