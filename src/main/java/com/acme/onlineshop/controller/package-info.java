/**
 * <p>This package holds all URL endpoints of the application, which in turn call the individual service modules.
 * All REST endpoints implement OpenAPI 3.0 schemes for an overview via Swagger.</p><br>
 * <b>REST Endpoint structure:</b>
 * <ul>
 *     <li>System</li>
 * </ul>
 * <p>The implementation follows standard practices, therefore the controller layer calls the service layer for each
 * operation, to do the real work. Therefore each controller class has at least one other
 * {@link com.acme.onlineshop.service} class.</p>
 *
 * @author Richard Saeuberlich
 * @version 1.0
 */
package com.acme.onlineshop.controller;