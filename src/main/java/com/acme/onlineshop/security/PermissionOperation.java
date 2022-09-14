package com.acme.onlineshop.security;

import java.util.List;

/**
 * <p>Permission split into 2 parts:</p>
 * <ul>
 *   <li>Application functions describe functionality parts of this application
 *   <ul>
 *      <li>Item management</li>
 *      <li>User management</li>
 *      <li>Statistics of running application</li>
 *      <li>...</li>
 *   </ul>
 *   <li>Permissions inside that application functions form single operations (following <b>CRUD</b> scheme)
 *   <dl>
 *       <dt><b>C</b>reate</dt>
 *       <dd>Add new users, or add new item, ...</dd>
 *       <dt><b>R</b>ead</dt>
 *       <dd>Look up currently existing users, or look up item, ...</dd>
 *       <dt><b>U</b>pdate</dt>
 *       <dd>Modify users (e.g. permissions), or modify items, ...</dd>
 *       <dt><b>D</b>elete</dt>
 *       <dd>Delete users, or delete items, ...</dd>
 *   </dl>
 * </ul>
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public enum PermissionOperation {

    ITEM, USER, STATISTIC, SYSTEM;

    public String getPermission(PermissionFunction function) {
        return "%s_%s".formatted(this.name(), function.name());
    }

    public static PermissionOperation getPermissionOperation(String operation) {
        return getPermissionOperation(operation, true);
    }

    public static PermissionOperation getPermissionOperation(String operation, boolean ignoreCase) {
        String temp;
        if(ignoreCase) {
            temp = operation.toUpperCase();
        } else {
            temp = operation;
        }
        for(PermissionOperation op: PermissionOperation.values()) {
            if (op.name().equals(temp)) {
                return op;
            }
        }
        throw new IllegalArgumentException("%s is no valid permission operation".formatted(operation));
    }

    public static List<PermissionOperation> getNoneRestEndpointPermissionOperations() {
        return List.of(ITEM, USER, STATISTIC, SYSTEM);
    }

    public static List<PermissionOperation> getRestEndpointPermissionOperations() {
        return List.of(ITEM, STATISTIC);
    }

}
