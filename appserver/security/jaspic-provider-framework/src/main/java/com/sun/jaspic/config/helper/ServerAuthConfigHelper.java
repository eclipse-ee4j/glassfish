/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jaspic.config.helper;

import com.sun.jaspic.config.delegate.MessagePolicyDelegate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.config.ServerAuthConfig;
import jakarta.security.auth.message.config.ServerAuthContext;
import jakarta.security.auth.message.module.ServerAuthModule;

/**
 *
 * @author Ron Monzillo
 */
public class ServerAuthConfigHelper extends AuthConfigHelper implements ServerAuthConfig {

    final static AuthStatus[] vR_SuccessValue = {AuthStatus.SUCCESS, AuthStatus.SEND_SUCCESS};
    final static AuthStatus[] sR_SuccessValue = {AuthStatus.SEND_SUCCESS};
    HashMap<String, HashMap<Integer, ServerAuthContext>> contextMap;
    AuthContextHelper acHelper;

    protected ServerAuthConfigHelper(String loggerName, EpochCarrier providerEpoch,
            AuthContextHelper acHelper, MessagePolicyDelegate mpDelegate,
            String layer, String appContext, CallbackHandler cbh)
            throws AuthException {
        super(loggerName, providerEpoch, mpDelegate, layer, appContext, cbh);
        this.acHelper = acHelper;
        this.mpDelegate = mpDelegate;
    }

    protected void initializeContextMap() {
        contextMap = new HashMap<String, HashMap<Integer, ServerAuthContext>>();
    }

    protected void refreshContextHelper() {
        acHelper.refresh();
    }

    protected ServerAuthContext createAuthContext(final String authContextID,
            final Map properties) throws AuthException {

        if (!acHelper.isProtected(new ServerAuthModule[0], authContextID)) {
            return null;
        }

        // need to coordinate calls to CallerPrincipalCallback; expecially optional
        // modules that might reset the result of a required module
        return new ServerAuthContext() {

            ServerAuthModule[] module = init();

            ServerAuthModule[] init() throws AuthException {
                ServerAuthModule[] m;
                try {
                    m = acHelper.getModules(new ServerAuthModule[0], authContextID);
                } catch (AuthException ae) {
                    logIfLevel(Level.SEVERE, ae,
                            "ServerAuthContext: ", authContextID,
                            "of AppContext: ", getAppContext(),
                            "unable to load server auth modules");
                    throw ae;
                }

                MessagePolicy requestPolicy =
                        mpDelegate.getRequestPolicy(authContextID, properties);
                MessagePolicy responsePolicy =
                        mpDelegate.getResponsePolicy(authContextID, properties);

                boolean noModules = true;
                for (int i = 0; i < m.length; i++) {
                    if (m[i] != null) {
                        if (isLoggable(Level.FINE)) {
                            logIfLevel(Level.FINE, null,
                                    "ServerAuthContext: ", authContextID,
                                    "of AppContext: ", getAppContext(),
                                    "initializing module");
                        }
                        noModules = false;
                        checkMessageTypes(m[i].getSupportedMessageTypes());
                        m[i].initialize(requestPolicy, responsePolicy,
                                cbh, acHelper.getInitProperties(i, properties));
                    }
                }
                if (noModules) {
                    logIfLevel(Level.WARNING, null,
                            "ServerAuthContext: ", authContextID,
                            "of AppContext: ", getAppContext(),
                            "contains no Auth Modules");
                }
                return m;
            }

            public AuthStatus validateRequest(MessageInfo arg0, Subject arg1, Subject arg2) throws AuthException {
                AuthStatus[] status = new AuthStatus[module.length];
                for (int i = 0; i < module.length; i++) {
                    if (module[i] == null) {
                        continue;
                    }
                    if (isLoggable(Level.FINE)) {
                        logIfLevel(Level.FINE, null,
                                "ServerAuthContext: ", authContextID,
                                "of AppContext: ", getAppContext(),
                                "calling vaidateRequest on module");
                    }
                    status[i] = module[i].validateRequest(arg0, arg1, arg2);
                    if (acHelper.exitContext(vR_SuccessValue, i, status[i])) {
                        return acHelper.getReturnStatus(vR_SuccessValue,
                                AuthStatus.SEND_FAILURE, status, i);
                    }
                }
                return acHelper.getReturnStatus(vR_SuccessValue,
                        AuthStatus.SEND_FAILURE, status, status.length - 1);
            }

            public AuthStatus secureResponse(MessageInfo arg0, Subject arg1) throws AuthException {
                AuthStatus[] status = new AuthStatus[module.length];
                for (int i = 0; i < module.length; i++) {
                    if (module[i] == null) {
                        continue;
                    }
                    if (isLoggable(Level.FINE)) {
                        logIfLevel(Level.FINE, null,
                                "ServerAuthContext: ", authContextID,
                                "of AppContext: ", getAppContext(),
                                "calling secureResponse on module");
                    }
                    status[i] = module[i].secureResponse(arg0, arg1);
                    if (acHelper.exitContext(sR_SuccessValue, i, status[i])) {
                        return acHelper.getReturnStatus(sR_SuccessValue,
                                AuthStatus.SEND_FAILURE, status, i);
                    }
                }
                return acHelper.getReturnStatus(sR_SuccessValue,
                        AuthStatus.SEND_FAILURE, status, status.length - 1);
            }

            public void cleanSubject(MessageInfo arg0, Subject arg1) throws AuthException {
                for (int i = 0; i < module.length; i++) {
                    if (module[i] == null) {
                        continue;
                    }
                    if (isLoggable(Level.FINE)) {
                        logIfLevel(Level.FINE, null,
                                "ServerAuthContext: ", authContextID,
                                "of AppContext: ", getAppContext(),
                                "calling cleanSubject on module");
                    }
                    module[i].cleanSubject(arg0, arg1);
                }
            }
        };
    }

    public ServerAuthContext getAuthContext(String authContextID,
            Subject subject, final Map properties) throws AuthException {
        return super.getContext(contextMap, authContextID, subject, properties);
    }

    public boolean isProtected() {
        return (!acHelper.returnsNullContexts() || mpDelegate.isProtected());
    }
}
