/*
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

package com.sun.enterprise.security.auth.login;

import com.sun.enterprise.security.GUILoginDialog;
import com.sun.enterprise.security.TextLoginDialog;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This is the default callback handler provided by the application client container. The container tries to use the application
 * specified callback handler (if provided). If there is no callback handler or if the handler cannot be instantiated then this
 * default handler is used.
 *
 * Note: User-defined Callback Handlers which intend to indicate cancel status must extend this class and set the ThreadLocal
 * cancelStatus.
 */
public class LoginCallbackHandler implements CallbackHandler {
    private boolean isGUI;
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(LoginCallbackHandler.class);
    protected ThreadLocal<Boolean> cancelStatus = new ThreadLocal<Boolean>();

    /**
     * Check whether the authentication was cancelled by the user.
     *
     * @return boolean indicating whether the authentication was cancelled.
     */
    public boolean getCancelStatus() {
        boolean cancelled = cancelStatus.get();
        cancelStatus.set(false);
        return cancelled;
    }

    public LoginCallbackHandler() {
        this(true);
    }

    public LoginCallbackHandler(boolean gui) {
        isGUI = gui;
        cancelStatus.set(false);
    }

    /**
     * This is the callback method called when authentication data is required. It either pops up a dialog box to request
     * authentication data or use text input.
     *
     * @param the callback object instances supported by the login module.
     */
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (isGUI) {
            String user = localStrings.getLocalString("login.user", "user");
            new GUILoginDialog(user, callbacks);
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    cancelStatus.set(((NameCallback) callbacks[i]).getName() == null);
                    break;
                }
            }
        } else {
            new TextLoginDialog(callbacks);
        }
    }
}
