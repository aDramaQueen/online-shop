package com.acme.onlineshop.persistence.user;

import com.acme.onlineshop.security.PermissionOperation;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class UserPermissionID implements Serializable {

    @Serial
    private static final long serialVersionUID = -3484920956657004637L;

    private User permissionUser;
    private PermissionOperation operation;

    public UserPermissionID() { }

    public UserPermissionID(User permissionUser, PermissionOperation operation) {
        this.permissionUser = permissionUser;
        this.operation = operation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || UserPermissionID.class != obj.getClass()) {
            return false;
        } else {
            UserPermissionID other = (UserPermissionID) obj;
            return this == other || other.permissionUser.getUsername().equals(this.permissionUser.getUsername()) && other.operation == this.operation;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionUser.getUsername(), operation);
    }
}
