/*
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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.glassfish.admin.rest.utils.ResourceUtil;
import org.glassfish.api.ActionReport;
import org.glassfish.common.util.admin.RestSessionManager;

/**
 * Represents a session with GlassFish Rest service
 *
 * @author Mitesh Meswani
 */
public class SessionResource {

    private String sessionId;
    private HttpHeaders requestHeaders;
    private UriInfo uriInfo;

    RestSessionManager sessionManager;

    public SessionResource(RestSessionManager sessionManager, String sessionId, HttpHeaders requestHeaders, UriInfo uriInfo) {
        this.sessionManager = sessionManager;
        this.sessionId = sessionId;
        this.requestHeaders = requestHeaders;
        this.uriInfo = uriInfo;
    }

    @DELETE
    public Response delete() {
        Response.Status status;
        ActionReport.ExitCode exitCode;
        String message;
        if (!sessionManager.deleteSession(sessionId)) {
            status = Response.Status.BAD_REQUEST;
            exitCode = ActionReport.ExitCode.FAILURE;
            message = "Session with id " + sessionId + " does not exist";
        } else {
            status = Response.Status.OK;
            exitCode = ActionReport.ExitCode.SUCCESS;
            message = "Session with id " + sessionId + " deleted";
        }

        return Response.status(status).entity(ResourceUtil.getActionReportResult(exitCode, message, requestHeaders, uriInfo)).build();

    }

}
