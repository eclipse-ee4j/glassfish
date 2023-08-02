/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.web.security;

import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.web.integration.WebPrincipal;
import com.sun.enterprise.security.web.integration.WebProgrammaticLogin;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.logging.Logger;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;

/**
 * Internal implementation for servlet programmatic login.
 *
 * @see com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin
 *
 */
@Service
public class WebProgrammaticLoginImpl implements WebProgrammaticLogin {

    // Used for the auth-type string.
    public static final String WEBAUTH_PROGRAMMATIC = "PROGRAMMATIC";

    private static final Logger LOG = Logger.getLogger(WebProgrammaticLoginImpl.class.getName());

    /**
     * Login and set up principal in request and session. This implements programmatic login for servlets.
     *
     * <P>
     * Due to a number of bugs in RI the security context is not shared between web container and ejb container. In order for an
     * identity established by programmatic login to be known to both containers, it needs to be set not only in the security context
     * but also in the current request and, if applicable, the session object. If a session does not exist this method does not
     * create one.
     *
     * <P>
     * See bugs 4646134, 4688449 and other referenced bugs for more background.
     *
     * <P>
     * Note also that this login does not hook up into SSO.
     *
     * @param user User name to login.
     * @param password User password.
     * @param request HTTP request object provided by caller application. It should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It should be an instance of HttpServletResponse. This is
     * not used currently.
     * @param realm the realm name to be authenticated to. If the realm is null, authentication takes place in default realm
     * @returns A Boolean object; true if login succeeded, false otherwise.
     * @see com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin
     * @throws Exception on login failure.
     *
     */
    @Override
    public Boolean login(String user, char[] password, String realm, HttpServletRequest request, HttpServletResponse response) {
        // Need real request object not facade

        Request coyoteRequest = getUnwrappedCoyoteRequest(request);
        if (coyoteRequest == null) {
            return false;
        }

        // Try to login - this will set up security context on success
        LoginContextDriver.login(user, password, realm);

        // Create a WebPrincipal for tomcat and store in current request
        // This will allow programmatic authorization later in this request
        // to work as expected.

        SecurityContext secCtx = SecurityContext.getCurrent();
        assert (secCtx != null); // since login succeeded above

        WebPrincipal principal = new WebPrincipal(user, password, secCtx);
        coyoteRequest.setUserPrincipal(principal);
        coyoteRequest.setAuthType(WEBAUTH_PROGRAMMATIC);

        LOG.log(FINE, "Programmatic login set principal in http request to: {0}", user);

        // Try to retrieve a Session object (not the facade); if it exists
        // store the principal there as well. This will allow web container
        // authorization to work in subsequent requests in this session.

        Session realSession = getSession(coyoteRequest);
        if (realSession != null) {
            realSession.setPrincipal(principal);
            realSession.setAuthType(WEBAUTH_PROGRAMMATIC);
            LOG.log(FINE, "Programmatic login set principal in session.");
        } else {
            LOG.log(FINE, "Programmatic login: No session available.");
        }

        return true;
    }

    /**
     * Return the unwrapped <code>CoyoteRequest</code> object.
     */
    private static Request getUnwrappedCoyoteRequest(HttpServletRequest request) {
        Request coyoteRequest = null;
        ServletRequest servletRequest = request;
        try {

            ServletRequest prevRequest = null;
            while (servletRequest != prevRequest && servletRequest instanceof ServletRequestWrapper) {
                prevRequest = servletRequest;
                servletRequest = ((ServletRequestWrapper) servletRequest).getRequest();
            }

            if (servletRequest instanceof RequestFacade) {
                coyoteRequest = ((RequestFacade) servletRequest).getUnwrappedCoyoteRequest();
            }

        } catch (AccessControlException ex) {
            LOG.log(FINE, "Programmatic login faiied to get request", ex);
        }

        return coyoteRequest;
    }

    /**
     * Logout and remove principal in request and session.
     *
     * @param request HTTP request object provided by caller application. It should be an instance of HttpRequestFacade.
     * @param response HTTP response object provided by called application. It should be an instance of HttpServletResponse. This is
     * not used currently.
     * @returns A Boolean object; true if login succeeded, false otherwise.
     * @see com.sun.enterprise.security.ee.auth.login.ProgrammaticLogin
     * @throws Exception any exception encountered during logout operation
     */
    @Override
    public Boolean logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Need real request object not facade

        Request coyoteRequest = getUnwrappedCoyoteRequest(request);
        if (coyoteRequest == null) {
            return false;
        }

        // Logout - clears out security context

        LoginContextDriver.logout();
        // Remove principal and auth type from request

        coyoteRequest.setUserPrincipal(null);
        coyoteRequest.setAuthType(null);
        LOG.log(FINE, "Programmatic logout removed principal from request.");

        // Remove from session if possible.

        Session realSession = getSession(coyoteRequest);
        if (realSession != null) {
            realSession.setPrincipal(null);
            realSession.setAuthType(null);
            LOG.log(FINE, "Programmatic logout removed principal from session.");
        }

        return true;
    }

    /**
     * Returns the underlying Session object from the request, if one is available, or null.
     *
     */
    private static Session getSession(Request request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Context context = request.getContext();
            if (context != null) {
                Manager manager = context.getManager();
                if (manager != null) {
                    // need to locate the real Session obj
                    String sessionId = session.getId();
                    try {
                        return manager.findSession(sessionId);
                    } catch (IOException e) {
                    }
                }
            }
        }

        return null;
    }
}
