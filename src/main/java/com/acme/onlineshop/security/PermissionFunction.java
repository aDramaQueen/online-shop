package com.acme.onlineshop.security;

/**
 * <p>Permission split into 2 parts:</p>
 * <ul>
 *   <li>Application functions describe functionality parts of this application
 *   <ul>
 *      <li>User management</li>
 *      <li>Modbus meter management</li>
 *      <li>BACnet meter management</li>
 *      <li>MBus meter management</li>
 *      <li>Statistics of running application</li>
 *      <li>...</li>
 *   </ul>
 *   <li>Permissions inside that application functions form single operations (following <b>CRUD</b> scheme)
 *   <dl>
 *       <dt><b>C</b>reate</dt>
 *       <dd>Add new users, or add new Modbus meters, ...</dd>
 *       <dt><b>R</b>ead</dt>
 *       <dd>Look up currently existing users, or look up statistics, ...</dd>
 *       <dt><b>U</b>pdate</dt>
 *       <dd>Modify users (e.g. permissions), or modify currently running BACnet meters, ...</dd>
 *       <dt><b>D</b>elete</dt>
 *       <dd>Delete users, or delete currently running MBus meters, ...</dd>
 *   </dl>
 * </ul>
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
public enum PermissionFunction {
    // ATTENTION: If you rename this Enum class, you also have to edit following HTML files
    //      .../i3de-meter-communication/src/main/resources/templates/fragments/main-users-add.html
    //      .../i3de-meter-communication/src/main/resources/templates/fragments/main-users-change.html

    CREATE_UPDATE("Create / Update"),
    READ("Read"),
    DELETE("Delete");

    public final String prettyName;

    PermissionFunction(String prettyName) {
        this.prettyName = prettyName;
    }

    public String getPermission(PermissionOperation operation) {
        return "%s_%s".formatted(operation.name(), this.name());
    }

    public static PermissionFunction getPermissionFunction(String function) {
        return getPermissionFunction(function, true);
    }

    public static PermissionFunction getPermissionFunction(String function, boolean ignoreCase) {
        String temp;
        if(ignoreCase) {
            temp = function.toUpperCase();
        } else {
            temp = function;
        }
        for(PermissionFunction op: PermissionFunction.values()) {
            if (op.name().equals(temp)) {
                return op;
            }
        }
        throw new IllegalArgumentException("%s is no valid permission function".formatted(function));
    }
}
