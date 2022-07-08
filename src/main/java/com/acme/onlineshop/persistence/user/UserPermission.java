package com.acme.onlineshop.persistence.user;

import com.acme.onlineshop.security.PermissionFunction;
import com.acme.onlineshop.security.PermissionOperation;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

@Entity
@IdClass(UserPermissionID.class)
public class UserPermission implements Serializable {

    @Serial
    private static final long serialVersionUID = -6709470390227459315L;

    @Id
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERMISSION_USER")
    @JsonIgnore
    private User permissionUser;
    @Id
    @NotNull
    @Enumerated(EnumType.STRING)
    private PermissionOperation operation;
    @Size(min = 1, message = "A permission without any operations makes no sense")
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<PermissionFunction> functions;

    public UserPermission() { }

    public UserPermission(User user, PermissionOperation operation, Set<PermissionFunction> functions) {
        this.permissionUser = user;
        this.operation = operation;
        this.functions = functions;
    }

    public User getPermissionUser() {
        return permissionUser;
    }

    public void setPermissionUser(User permissionUser) {
        this.permissionUser = permissionUser;
    }

    public PermissionOperation getOperation() {
        return operation;
    }

    public void setOperation(PermissionOperation permission) {
        this.operation = permission;
    }

    public Set<PermissionFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(Set<PermissionFunction> operations) {
        this.functions = operations;
    }

    public Set<String> getPermissions() {
        Set<String> result = new HashSet<>();
        for(PermissionFunction func: functions){
            result.add(operation.getPermission(func));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || UserPermission.class != obj.getClass()) {
            return false;
        } else {
            UserPermission other = (UserPermission) obj;
            return this == other || operation == other.operation && permissionUser.equals(other.permissionUser);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionUser, operation);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",\n\t\t\t", "{\n\t\t\t", "\n\t\t}");
        functions.forEach(op -> sj.add(op.name()));
        return "\n\t\t" + operation + " " + sj;
    }
}
