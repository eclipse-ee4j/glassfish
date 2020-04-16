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

package test.provider;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("rawtypes")
public class TestHttpServletServerAuthModule implements ServerAuthModule {

    /**
     * This is the URI of the action which will trigger a redirect.
     */
    private static final String LOGIN_ACTION_URI = "/samlogin";

    /**
     * This must point to a JSF Facelets page to trigger the bug.
     */
    private static final String REDIRECT_FACES_URI = "/message.xhtml";

    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    }

    @Override
    public void initialize(final MessagePolicy reqPolicy, final MessagePolicy resPolicy, final CallbackHandler cBH,
            final Map opts) throws AuthException {
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject)
            throws AuthException {
        try {
            final HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
            final HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

            if (!request.getRequestURI().endsWith(LOGIN_ACTION_URI)) {
                return AuthStatus.SUCCESS;
            }

            request.getRequestDispatcher(REDIRECT_FACES_URI).forward(request, response);
            return AuthStatus.SEND_CONTINUE;
        } catch (Throwable e) {
            AuthException authException = new AuthException();
            authException.initCause(e);
            throw authException;
        }
    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SEND_SUCCESS;
    }

}
