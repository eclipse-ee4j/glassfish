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

package com.sun.enterprise.security.auth.login.common;

import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This is the default callback handler provided by the application client container. The container tries to use the application
 * specified callback handler (if provided). If there is no callback handler or if the handler cannot be instantiated then this
 * default handler is used.
 */
public class ServerLoginCallbackHandler implements CallbackHandler {
    private static final String GP_CB = "jakarta.security.auth.message.callback.GroupPrincipalCallback";
    private static final String GPCBH_UTIL = "com.sun.enterprise.security.jmac.callback.ServerLoginCBHUtil";
    private static final String GPCBH_UTIL_METHOD = "processGroupPrincipal";
    private String username = null;
    private char[] password = null;
    private String moduleID = null;

    public ServerLoginCallbackHandler(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    public ServerLoginCallbackHandler(String username, char[] password, String moduleID) {
        this.username = username;
        this.password = password;
        this.moduleID = moduleID;
    }

    public ServerLoginCallbackHandler() {
    }

    public void setUsername(String user) {
        username = user;
    }

    public void setPassword(char[] pass) {
        password = pass;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    /**
     * This is the callback method called when authentication data is required. It either pops up a dialog box to request
     * authentication data or use text input.
     *
     * @param the callback object instances supported by the login module.
     */
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback nme = (NameCallback) callbacks[i];
                nme.setName(username);
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pswd = (PasswordCallback) callbacks[i];
                pswd.setPassword(password);
            } else if (callbacks[i] instanceof CertificateRealm.AppContextCallback) {
                ((CertificateRealm.AppContextCallback) callbacks[i]).setModuleID(moduleID);
            } else if (GP_CB.equals(callbacks[i].getClass().getName())) {
                processGroupPrincipal(callbacks[i]);
            } else {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }

    private static void processGroupPrincipal(Callback callback) throws UnsupportedCallbackException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Class clazz = loader.loadClass(GPCBH_UTIL);
            Method meth = clazz.getMethod(GPCBH_UTIL_METHOD, Callback.class);
            meth.invoke(null, callback);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedCallbackException(callback);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedCallbackException(callback);
        } catch (InvocationTargetException ex) {
            throw new UnsupportedCallbackException(callback);
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedCallbackException(callback);
        } catch (SecurityException ex) {
            throw new UnsupportedCallbackException(callback);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedCallbackException(callback);
        }

    }

}
