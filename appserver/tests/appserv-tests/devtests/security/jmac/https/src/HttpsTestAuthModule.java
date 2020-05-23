/*
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

package com.sun.s1asdev.security.jmac.https;

import java.util.Map;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import javax.security.auth.x500.X500Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpsTestAuthModule implements ServerAuthModule {

    private CallbackHandler handler = null;

    public void initialize(MessagePolicy requestPolicy,
            MessagePolicy responsePolicy,
            CallbackHandler handler,
            Map options)
            throws AuthException {
        this.handler = handler;
    }

    public Class[] getSupportedMessageTypes() {
        return new Class[]{HttpServletRequest.class, HttpServletResponse.class};
    }

    public AuthStatus validateRequest(MessageInfo messageInfo,
            Subject clientSubject,
            Subject serviceSubject) throws AuthException {


        if (!isMandatory(messageInfo)) {
            return AuthStatus.SUCCESS;
        }

        X500Principal x500Principal = null;
        try {
            HttpServletRequest request =
                    (HttpServletRequest) messageInfo.getRequestMessage();
            X509Certificate certs[] =
                    (X509Certificate[]) request.getAttribute(
                    "jakarta.servlet.request.X509Certificate");
            if (certs == null || certs.length < 1) {
                System.out.println("javax...certs is null or empty");
                certs = (X509Certificate[]) request.getAttribute(
                        "org.apache.coyote.request.X509Certificate");
            }
            System.out.println("certs: " + certs);
            if (certs != null && certs.length > 0) {
                x500Principal = certs[0].getSubjectX500Principal();
                System.out.println("X500Principal = " + x500Principal);
            }

            CallerPrincipalCallback cpCallback =
                    new CallerPrincipalCallback(clientSubject, x500Principal);
            System.out.println("Subject before invoking callbacks: " + clientSubject);
            handler.handle(new Callback[]{cpCallback});
            System.out.println("Subject after invoking callbacks: " + clientSubject);

            request.setAttribute("MY_NAME", getClass().getName());
            System.out.println("login success: " + x500Principal);
            return AuthStatus.SUCCESS;
        } catch (Throwable t) {
            System.out.println("login fails: " + x500Principal);
            t.printStackTrace();
            return AuthStatus.SEND_FAILURE;
        }
    }

    public AuthStatus secureResponse(MessageInfo messageInfo,
            Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject)
            throws AuthException {
    }

    private boolean isMandatory(MessageInfo messageInfo) {
        return Boolean.valueOf((String) messageInfo.getMap().get(
                "jakarta.security.auth.message.MessagePolicy.isMandatory"));
    }
}
