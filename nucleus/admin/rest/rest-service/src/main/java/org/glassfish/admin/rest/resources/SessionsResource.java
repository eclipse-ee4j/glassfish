/*
 * Copyright (c) 2024, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.admin.rest.resources;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.net.NetUtils;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.admin.restconnector.RestConfig;
import org.glassfish.common.util.admin.RestSessionManager;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.RemoteAdminAccessException;
import org.glassfish.jersey.internal.util.collection.Ref;

import static jakarta.ws.rs.core.Response.Status.FORBIDDEN;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * Represents sessions with GlassFish Rest service
 *
 * @author Mitesh Meswani
 */
@Path("/sessions")
public class SessionsResource extends AbstractResource {

    private static final Logger LOGGER = Logger.getLogger(SessionsResource.class.getName());

    @Inject
    private RestSessionManager sessionManager;

    @Inject
    private Ref<Request> request;

    /**
     * Get a new session with GlassFish Rest service If a request lands here when authentication has been turned on => it
     * has been authenticated.
     *
     * @return a new session with GlassFish Rest service
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    @Produces({ MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5", "text/html" })
    public Response create(HashMap<String, String> data) {
        if (data == null) {
            data = new HashMap<>();
        }
        final RestConfig restConfig = ResourceUtil.getRestConfig(locatorBridge.getRemoteLocator());

        Response.ResponseBuilder responseBuilder = Response.status(UNAUTHORIZED);
        RestActionReporter ar = new RestActionReporter();
        Request grizzlyRequest = request.get();

        // If the call flow reached here, the request has been authenticated by logic in RestAdapater
        // probably with an admin username and password.  The remoteHostName value
        // in the data object is the actual remote host of the end-user who is
        // using the console (or, conceivably, some other client).  We need to
        // authenticate here once again with that supplied remoteHostName to
        // make sure we enforce remote access rules correctly.
        String hostName = data.get("remoteHostName");
        boolean isAuthorized = false;
        boolean responseErrorStatusSet = false;
        Subject subject = null;
        try {
            subject = ResourceUtil.authenticateViaAdminRealm(locatorBridge.getRemoteLocator(), grizzlyRequest, hostName);
            isAuthorized = ResourceUtil.isAuthorized(locatorBridge.getRemoteLocator(), subject, "domain/rest-sessions/rest-session",
                    "create");
        } catch (RemoteAdminAccessException e) {
            responseBuilder.status(FORBIDDEN);
            responseErrorStatusSet = true;
        } catch (Exception e) {
            ar.setMessage("Error while authenticating " + e);
        }

        if (isAuthorized) {
            responseBuilder.status(OK);

            // Check to see if the username has been set (anonymous user case)
            String username = (String) grizzlyRequest.getAttribute("restUser");
            if (username != null) {
                ar.getExtraProperties().put("username", username);
            }
            ar.getExtraProperties().put("token",
                    sessionManager.createSession(grizzlyRequest.getRemoteAddr(), subject, chooseTimeout(restConfig, grizzlyRequest)));

        } else {
            if (!responseErrorStatusSet) {
                responseBuilder.status(UNAUTHORIZED);
            }
        }

        return responseBuilder.entity(new ActionReportResult(ar)).build();
    }

    private int chooseTimeout(final RestConfig restConfig, final Request grizzlyRequest) {
        // For requests originating from the admin console, tie the REST token
        // lifetime to the configured admin GUI session timeout (das-config
        // adminSessionTimeoutInMinutes, 0 = never) so that changing "Admin Session
        // Timeout" actually takes effect for the console. Without this the token
        // would always expire after the rest-config session-token-timeout (default
        // 30 minutes), regardless of the Admin Session Timeout setting. Other REST
        // clients (asadmin, scripts, ...) keep the rest-config default.
        // See issue #24982.
        if (isFromAdminConsole(grizzlyRequest)) {
            Integer adminSessionTimeout = getAdminSessionTimeout();
            if (adminSessionTimeout != null) {
                return adminSessionTimeout;
            }
        }
        int inactiveSessionLifeTime = 30 /*mins*/;
        if (restConfig != null) {
            inactiveSessionLifeTime = Integer.parseInt(restConfig.getSessionTokenTimeout());
        }
        return inactiveSessionLifeTime;
    }

    /**
     * The admin console always runs co-located with the DAS and is the only
     * client that sends the {@code X-GlassFish-Remote-Host} header. Restricting
     * the admin-session timeout to such requests means a remote REST client
     * cannot influence its own token lifetime. This mirrors the check in
     * {@code GenericAdminAuthenticator}.
     */
    private boolean isFromAdminConsole(final Request grizzlyRequest) {
        return NetUtils.isLocal(grizzlyRequest.getRemoteAddr())
                && grizzlyRequest.getHeader("X-GlassFish-Remote-Host") != null;
    }

    /**
     * @return the configured admin GUI session timeout in minutes (das-config
     *         {@code adminSessionTimeoutInMinutes}), or {@code null} if it cannot
     *         be determined or parsed.
     */
    private Integer getAdminSessionTimeout() {
        try {
            Domain domain = Globals.getDefaultBaseServiceLocator().getService(Domain.class);
            String value = domain.getServerNamed("server").getConfig().getAdminService()
                    .getDasConfig().getAdminSessionTimeoutInMinutes();
            return value == null ? null : Integer.valueOf(value.trim());
        } catch (RuntimeException ex) {
            LOGGER.log(Level.FINE, "Unable to read admin session timeout", ex);
            return null;
        }
    }

    @Path("{sessionId}/")
    public SessionResource getSessionResource(@PathParam("sessionId") String sessionId) {
        return new SessionResource(sessionManager, sessionId, requestHeaders, uriInfo);
    }
}
