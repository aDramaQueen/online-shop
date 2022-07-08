package com.acme.onlineshop.security;

import java.util.List;

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
public enum PermissionOperation {
    // ATTENTION: If you rename this Enum class, you also have to edit following HTML files
    //      .../i3de-meter-communication/src/main/resources/templates/fragments/main-users-add.html
    //      .../i3de-meter-communication/src/main/resources/templates/fragments/main-users-change.html

    USER, STATISTIC, SYSTEM, TIME_SERIES_DATABASE, ONLINE, METER, MODBUS, BACNET, MBUS;

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
        return List.of(USER, STATISTIC);
    }

    public static List<PermissionOperation> getRestEndpointPermissionOperations() {
        return List.of(SYSTEM, TIME_SERIES_DATABASE, ONLINE, METER, MODBUS, BACNET, MBUS);
    }

}
