/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.jmac.https;

import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.x500.X500Principal;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class HttpsTestAuthModule implements ServerAuthModule {

    private static final Logger LOG = System.getLogger(HttpsTestAuthModule.class.getName());

    private CallbackHandler handler;

    @Override
    public void initialize(final MessagePolicy requestPolicy, final MessagePolicy responsePolicy,
        final CallbackHandler handler, final Map<String, Object> options) throws AuthException {
        this.handler = handler;
    }


    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return new Class[] {HttpServletRequest.class, HttpServletResponse.class};
    }


    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject,
        final Subject serviceSubject) throws AuthException {
        LOG.log(Level.INFO, "validateRequest(messageInfo={0}, clientSubject={1}, serviceSubject={2})", messageInfo,
            clientSubject, serviceSubject);
        if (!isMandatory(messageInfo)) {
            return AuthStatus.SUCCESS;
        }

        try {
            final HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
            // Should be set by Catalina or Grizly
            final X509Certificate[] certs = (X509Certificate[]) request
                .getAttribute("jakarta.servlet.request.X509Certificate");
            LOG.log(INFO, "Request attributes: {0}", Collections.list(request.getAttributeNames()));
            LOG.log(INFO, "Certificates found in the request attribute: {0}", Arrays.toString(certs));

            if (certs == null || certs.length == 0) {
                return AuthStatus.SEND_FAILURE;
            }
            final X500Principal x500Principal = certs[0].getSubjectX500Principal();
            LOG.log(INFO, "User''s X500Principal={0}", x500Principal);
            final CallerPrincipalCallback cpCallback = new CallerPrincipalCallback(clientSubject, x500Principal);
            LOG.log(INFO, "Subject before invoking callbacks: {0}", clientSubject);
            handler.handle(new Callback[] {cpCallback});
            LOG.log(INFO, "Subject after invoking callbacks: {0}", clientSubject);

            request.setAttribute("MY_NAME", getClass().getName());
            LOG.log(INFO, "Login success: {0}", x500Principal);
            return AuthStatus.SUCCESS;
        } catch (final Exception e) {
            LOG.log(ERROR, "Login failed.", e);
            return AuthStatus.SEND_FAILURE;
        }
    }


    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SEND_SUCCESS;
    }


    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
    }


    private boolean isMandatory(final MessageInfo messageInfo) {
        return Boolean
            .parseBoolean((String) messageInfo.getMap().get("jakarta.security.auth.message.MessagePolicy.isMandatory"));
    }
}
