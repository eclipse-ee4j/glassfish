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

/*
 * ClientContainerCallbackHandler.java
 *
 * Created on September 14, 2004, 12:20 PM
 */

package com.sun.enterprise.security.ee.jmac.callback;

import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.UsernamePasswordStore;

import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.CertStoreCallback;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.auth.message.callback.PrivateKeyCallback;
import jakarta.security.auth.message.callback.SecretKeyCallback;
import jakarta.security.auth.message.callback.TrustStoreCallback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Appclient side Callback Handler for WSS.
 *
 * @author Harpreet Singh
 * @author Shing Wai Chan
 */
public final class ClientContainerCallbackHandler extends GlassFishBaseCallbackHandler {

    private static final String LOGIN_NAME = "j2eelogin.name";
    private static final String LOGIN_PASSWORD = "j2eelogin.password";

    public ClientContainerCallbackHandler() {
    }

    // TODO:V3 trying to read system properties here
    @Override
    protected void handleSupportedCallbacks(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        // this variable is set to true if we have used the older jaas
        // mechanisms to process the callbacks - and we will not need
        // to process further as the inside loop, just takes care
        // of processing all callbacks
        boolean processedSomeAppclientCallbacks = false;

        int i = 0;
        while (i < callbacks.length) {
            if (!processedSomeAppclientCallbacks) {
                if (callbacks[i] instanceof NameCallback || callbacks[i] instanceof PasswordCallback
                        || callbacks[i] instanceof ChoiceCallback) {

                    String loginName = UsernamePasswordStore.getUsername();
                    char[] password = UsernamePasswordStore.getPassword();
                    boolean doSet = false;
                    if (loginName == null) {
                        loginName = System.getProperty(LOGIN_NAME);
                        doSet = true;
                    }
                    if (password == null) {
                        password = System.getProperty(LOGIN_PASSWORD).toCharArray();
                        doSet = true;
                    }
                    if (doSet) {
                        UsernamePasswordStore.set(loginName, password);
                    }
                    // TODO: V3 CallbackHandler callbackHandler = AppContainer.getCallbackHandler();
                    CallbackHandler callbackHandler = SecurityServicesUtil.getInstance().getCallbackHandler();
                    if (loginName != null && password != null) {
                        // username/password set already
                        for (Callback callback : callbacks) {
                            if (callback instanceof NameCallback) {
                                NameCallback nc = (NameCallback) callback;
                                nc.setName(loginName);
                            } else if (callback instanceof PasswordCallback) {
                                PasswordCallback pc = (PasswordCallback) callback;
                                pc.setPassword(password);
                            }
                        }
                    } else {
                        // once this is called all callbacks will be handled by
                        // callbackHandler and then we dont have to check for
                        // NameCallback PasswordCallback and ChoiceCallback
                        // again.
                        // Let control flow to the callback processors
                        callbackHandler.handle(callbacks);
                    }
                    processedSomeAppclientCallbacks = true;
                    break;
                }
            }
            processCallback(callbacks[i]);
            i++;
        }
    }

    @Override
    protected boolean isSupportedCallback(Callback callback) {
        boolean supported = false;
        if (callback instanceof NameCallback || callback instanceof PasswordCallback || callback instanceof ChoiceCallback
                || callback instanceof CallerPrincipalCallback || callback instanceof GroupPrincipalCallback
                || callback instanceof CertStoreCallback || callback instanceof PasswordValidationCallback
                || callback instanceof SecretKeyCallback || callback instanceof PrivateKeyCallback
                || callback instanceof TrustStoreCallback) {
            supported = true;
        }
        return supported;
    }

}
