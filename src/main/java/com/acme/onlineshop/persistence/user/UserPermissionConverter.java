package com.acme.onlineshop.persistence.user;

import com.acme.onlineshop.security.PermissionFunction;
import com.acme.onlineshop.security.PermissionOperation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserPermissionConverter implements Converter<String, Set<UserPermission>> {

    public final static String separator = "-$-";

    @Override
    public Set<UserPermission> convert(String source) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<PermissionOperation, Set<PermissionFunction>> rawMap;
        try {
            rawMap = objectMapper.readValue(source, new TypeReference<>() { } );
        } catch (JsonProcessingException exc) {
            throw new IllegalArgumentException(exc.getMessage());
        }
        return buildPermissions(rawMap);
    }

    private Set<UserPermission> buildPermissions(Map<PermissionOperation, Set<PermissionFunction>> permissions) {
        Set<UserPermission> result = new HashSet<>();
        for(Map.Entry<PermissionOperation, Set<PermissionFunction>> entry : permissions.entrySet()) {
            result.add(new UserPermission(null, entry.getKey(), entry.getValue()));
        }
        return result;
    }
}