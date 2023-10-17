/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.jmac.http.servlet.form;

import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.System.Logger;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

public class HttpServletFormTestAuthModule implements ServerAuthModule {
    private static final Logger LOG = System.getLogger(HttpServletFormTestAuthModule.class.getName());

    private static final String SAVED_REQUEST = "Saved_Request";
    private static final String SAVED_SUBJECT = "Saved_Subject";
    private CallbackHandler handler;
    private String pc;

    @Override
    public void initialize(final MessagePolicy requestPolicy, final MessagePolicy responsePolicy,
        final CallbackHandler handler, final Map options) throws AuthException {
        LOG.log(DEBUG, "initialize(requestPolicy={0}, responsePolicy={1}, handler={2}, options={3})", requestPolicy,
            responsePolicy, handler, options);
        this.handler = handler;
        if (options != null) {
            this.pc = (String) options.get("jakarta.security.jacc.PolicyContext");
        }
    }


    @Override
    public Class[] getSupportedMessageTypes() {
        return new Class[] {HttpServletRequest.class, HttpServletResponse.class};
    }


    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject)
        throws AuthException {
        final HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        final HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

        LOG.log(INFO, "The request URI: {0}", request.getRequestURI());
        if (!isMandatory(messageInfo) && !request.getRequestURI().endsWith("/j_security_check")) {
            return AuthStatus.SUCCESS;
        }

        String username = null;
        String password = null;
        try {
            HttpSession session = request.getSession(false);
            LOG.log(INFO, "Session: {0}", session);
            if (session != null) {
                final Subject savedClientSubject = (Subject) session.getAttribute(SAVED_SUBJECT);
                if (savedClientSubject != null) {
                    LOG.log(INFO, "Already has saved subject");
                    // just copy principals for testing
                    clientSubject.getPrincipals().addAll(savedClientSubject.getPrincipals());
                    request.setAttribute("MY_NAME", getClass().getName());
                    request.setAttribute("PC", pc);
                    return AuthStatus.SUCCESS;
                }
            }

            username = request.getParameter("j_username");
            password = request.getParameter("j_password");
            LOG.log(INFO, "Credentials used: {0} with password {1}", username, password);

            if (username == null || password == null) {
                LOG.log(INFO, "Forwarding to login form, creating the session.");
                if (session == null) {
                    session = request.getSession(true);
                }
                session.setAttribute(SAVED_REQUEST, new SavedRequest(request));
                final RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
                rd.forward(request, response);
                return AuthStatus.SEND_CONTINUE;
            }

            final PasswordValidationCallback pwdCallback = new PasswordValidationCallback(clientSubject, username,
                password.toCharArray());
            final CallerPrincipalCallback cpCallback = new CallerPrincipalCallback(clientSubject, username);
            LOG.log(INFO, "Subject before invoking callbacks: {0}", clientSubject);
            handler.handle(new Callback[] {pwdCallback, cpCallback});
            LOG.log(INFO, "Subject after invoking callbacks: {0}", clientSubject);

            if (!pwdCallback.getResult()) {
                LOG.log(WARNING, "Login failed for {0}", username);
                final RequestDispatcher rd = request.getRequestDispatcher("error.html");
                rd.forward(request, response);
                return AuthStatus.SEND_FAILURE;
            }
            LOG.log(INFO, "Login succeeded for {0}", username);
            final SavedRequest sreq;
            if (session == null) {
                sreq = null;
            } else {
                sreq = (SavedRequest) session.getAttribute(SAVED_REQUEST);
                // for testing only as Subject is not Serializable
                session.setAttribute(SAVED_SUBJECT, clientSubject);
            }
            LOG.log(INFO, "Found saved request: {0}", sreq);
            if (sreq == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return AuthStatus.SEND_FAILURE;
            }
            final StringBuilder sb = new StringBuilder(sreq.getRequestURI());
            if (sreq.getQueryString() != null) {
                sb.append('?');
                sb.append(sreq.getQueryString());
            }
            response.sendRedirect(response.encodeRedirectURL(sb.toString()));
            return AuthStatus.SEND_CONTINUE;
        } catch (final Exception e) {
            LOG.log(ERROR, "Login crashed for " + username, e);
            final RequestDispatcher rd = request.getRequestDispatcher("error.html");
            try {
                rd.forward(request, response);
            } catch (final Exception ex) {
                throw new AuthException(ex);
            }
            return AuthStatus.SEND_FAILURE;
        }
    }


    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }


    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
    }


    private boolean isMandatory(final MessageInfo messageInfo) {
        return Boolean
            .parseBoolean((String) messageInfo.getMap().get("jakarta.security.auth.message.MessagePolicy.isMandatory"));
    }
}
