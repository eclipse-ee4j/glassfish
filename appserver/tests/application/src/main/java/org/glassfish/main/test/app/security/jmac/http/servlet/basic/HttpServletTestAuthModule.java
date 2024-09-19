/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.main.test.app.security.jmac.http.servlet.basic;

import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.lang.System.Logger;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import org.glassfish.common.util.HttpParser;
import org.glassfish.security.common.UserNameAndPassword;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class HttpServletTestAuthModule implements ServerAuthModule {
    private static final Logger LOG = System.getLogger(HttpServletTestAuthModule.class.getName());

    private CallbackHandler handler;
    private String pc;

    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler,
        Map options) throws AuthException {
        LOG.log(DEBUG, "initialize(requestPolicy={0}, responsePolicy={1}, handler={2}, options={3})",
            requestPolicy, responsePolicy, handler, options);
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
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject)
        throws AuthException {
        LOG.log(DEBUG, "validateRequest(messageInfo={0}, clientSubject={1}, serviceSubject={2})", messageInfo,
            clientSubject, serviceSubject);
        if (!isMandatory(messageInfo)) {
            return AuthStatus.SUCCESS;
        }

        try {
            HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
            HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
            UserNameAndPassword login = HttpParser.parseBasicAuthorizationHeader(request.getHeader("Authorization"));
            LOG.log(INFO, "REQUEST: Login: {0}", login);
            if (login == null) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"default\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                LOG.log(INFO, "login prompt for username/password");
                return AuthStatus.SEND_CONTINUE;
            }

            PasswordValidationCallback pwdCallback = new PasswordValidationCallback(clientSubject, login.getName(),
                login.getPassword());
            CallerPrincipalCallback cpCallback = new CallerPrincipalCallback(clientSubject, login.getName());
            LOG.log(DEBUG, "Subject before invoking callbacks: {0}", clientSubject);
            handler.handle(new Callback[] {pwdCallback, cpCallback});
            LOG.log(INFO, "Subject after invoking callbacks: {0}", clientSubject);

            if (!pwdCallback.getResult()) {
                LOG.log(INFO, "login fails for login {0}", login);
                return AuthStatus.SEND_FAILURE;
            }
            request.setAttribute("MY_NAME", getClass().getName());
            request.setAttribute("PC", pc);
            LOG.log(INFO, "login succeeded for login {0}", login);
            messageInfo.setResponseMessage(new MyHttpServletResponseWrapper(response));
            return AuthStatus.SUCCESS;
        } catch (Exception e) {
            LOG.log(ERROR, "Login failed.", e);
            return AuthStatus.SEND_FAILURE;
        }
    }

    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        LOG.log(DEBUG, "secureResponse(messageInfo={0}, serviceSubject={1})", messageInfo, serviceSubject);
        if (!isMandatory(messageInfo)) {
            return AuthStatus.SUCCESS;
        }

        try {
            HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
            request.setAttribute("SR", "true");
            MyHttpServletResponseWrapper response = (MyHttpServletResponseWrapper) messageInfo.getResponseMessage();
            int count = response.getAdjustedCount();
            PrintWriter writer = response.getWriter();
            writer.println("\nAdjusted count: " + count);
            messageInfo.setResponseMessage(response.getResponse());
            return AuthStatus.SUCCESS;
        } catch (Exception e) {
            LOG.log(ERROR, "Securing response failed.", e);
            return AuthStatus.FAILURE;
        }
    }


    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
    }


    private boolean isMandatory(MessageInfo messageInfo) {
        return Boolean
            .parseBoolean((String) messageInfo.getMap().get("jakarta.security.auth.message.MessagePolicy.isMandatory"));
    }
}
