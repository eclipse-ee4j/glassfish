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
 * ServerContainerCallbackHandler.java
 *
 * Created on September 14, 2004, 12:56 PM
 */

package com.sun.enterprise.security.ee.authentication.jakarta.callback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.auth.login.common.LoginException;

import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.CertStoreCallback;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.auth.message.callback.PrivateKeyCallback;
import jakarta.security.auth.message.callback.SecretKeyCallback;
import jakarta.security.auth.message.callback.TrustStoreCallback;

/**
 * Callback Handler for ServerContainer
 *
 * @author Harpreet Singh
 * @author Shing Wai Chan
 */
final public class ServerContainerCallbackHandler extends GlassFishBaseCallbackHandler {

    private String realmName;

    public ServerContainerCallbackHandler() {}

    public ServerContainerCallbackHandler(String realmName) {
        this.realmName = realmName;
    }

    @Override
    protected void handleSupportedCallbacks(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            processCallback(callback);
        }
    }

    @Override
    protected boolean isSupportedCallback(Callback callback) {
        boolean isSupported = false;
        if (callback instanceof CertStoreCallback || callback instanceof PasswordValidationCallback
                || callback instanceof CallerPrincipalCallback || callback instanceof GroupPrincipalCallback
                || callback instanceof SecretKeyCallback || callback instanceof PrivateKeyCallback
                || callback instanceof TrustStoreCallback) {

            isSupported = true;
        }
        return isSupported;
    }

    @Override
    protected void processPasswordValidation(PasswordValidationCallback pwdCallback) {
        String username = pwdCallback.getUsername();
        char[] password = pwdCallback.getPassword();

        try {
            LoginContextDriver.jmacLogin(pwdCallback.getSubject(), username, password, realmName);
            ditchPassword(password);

            pwdCallback.setResult(true);
        } catch (LoginException le) {
            // Login failed
            pwdCallback.setResult(false);
        }
    }

    private void ditchPassword(char[] passwd) {
        // Explicitly ditch the password
        if (passwd != null) {
            for (int i = 0; i < passwd.length; i++) {
                passwd[i] = ' ';
            }
        }
    }
}
