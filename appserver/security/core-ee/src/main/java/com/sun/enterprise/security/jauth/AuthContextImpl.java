/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.jauth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.AppConfigurationEntry;

/**
 * Shared logic from Client and ServerAuthContext reside here.
 */
final class AuthContextImpl {

    static final String INIT = "initialize";
    static final String DISPOSE_SUBJECT = "disposeSubject";

    static final String SECURE_REQUEST = "secureRequest";
    static final String VALIDATE_RESPONSE = "validateResponse";

    static final String VALIDATE_REQUEST = "validateRequest";
    static final String SECURE_RESPONSE = "secureResponse";

    // managesSessions method is implemented by looking for
    // corresponding option value in module configuration
    static final String MANAGES_SESSIONS = "managesSessions";
    static final String MANAGES_SESSIONS_OPTION = "managessessions";

    private final ConfigFile.Entry[] entries;
    private final Logger logger;

    AuthContextImpl(ConfigFile.Entry[] entries, Logger logger) throws AuthException {

        this.entries = entries;
        this.logger = logger;
    }

    /**
     * Invoke modules according to configuration
     */
    Object[] invoke(final String methodName, final Object[] args) throws AuthException {

        // invoke modules in a doPrivileged
        final Object rValues[] = new Object[entries.length];

        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Void>) () -> {
                invokePriv(methodName, args, rValues);
                return null;
            });
        } catch (java.security.PrivilegedActionException pae) {
            if (pae.getException() instanceof AuthException) {
                throw (AuthException) pae.getException();
            }
            throw new AuthException("Could not execute method " + methodName, pae.getException());
        }
        return rValues;
    }

    void invokePriv(String methodName, Object[] args, Object[] rValues) throws AuthException {

        // special treatment for managesSessions until the module
        // interface can be extended.
        if (methodName.equals(MANAGES_SESSIONS)) {
            for (int i = 0; i < entries.length; i++) {
                Map<String, ?> options = entries[i].getOptions();
                String mS = (String) options.get(MANAGES_SESSIONS_OPTION);
                rValues[i] = Boolean.valueOf(mS);
            }
            return;
        }

        boolean success = false;
        AuthException firstRequiredError = null;
        AuthException firstError = null;

        // XXX no way to reverse module invocation

        for (int i = 0; i < entries.length; i++) {

            // get initialized module instance

            Object module = entries[i].module;

            // invoke the module

            try {
                Method[] mArray = module.getClass().getMethods();
                for (Method element : mArray) {
                    if (element.getName().equals(methodName)) {

                        // invoke module
                        rValues[i] = element.invoke(module, args);

                        // success -
                        // return if SUFFICIENT and no previous REQUIRED errors

                        if (firstRequiredError == null
                                && entries[i].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT) {

                            if (logger != null && logger.isLoggable(Level.FINE)) {
                                logger.fine(entries[i].getLoginModuleName() + "." + methodName + " SUFFICIENT success");
                            }

                            return;
                        }

                        if (logger != null && logger.isLoggable(Level.FINE)) {
                            logger.fine(entries[i].getLoginModuleName() + "." + methodName + " success");
                        }

                        success = true;
                        break;
                    }
                }

                if (!success) {
                    // PLEASE NOTE:
                    // this exception will be thrown if any module
                    // in the context does not support the method.
                    NoSuchMethodException nsme = new NoSuchMethodException(
                            "module " + module.getClass().getName() + " does not implement " + methodName);
                    AuthException ae = new AuthException();
                    ae.initCause(nsme);
                    throw ae;
                }
            } catch (IllegalAccessException iae) {
                AuthException ae = new AuthException();
                ae.initCause(iae);
                throw ae;
            } catch (InvocationTargetException ite) {

                // failure cases

                AuthException ae;

                if (ite.getCause() instanceof AuthException) {
                    ae = (AuthException) ite.getCause();
                } else {
                    ae = new AuthException();
                    ae.initCause(ite.getCause());
                }

                if (entries[i].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.REQUISITE) {

                    if (logger != null && logger.isLoggable(Level.FINE)) {
                        logger.fine(entries[i].getLoginModuleName() + "." + methodName + " REQUISITE failure");
                    }

                    // immediately throw exception

                    if (firstRequiredError != null) {
                        throw firstRequiredError;
                    }
                    throw ae;

                }
                if (entries[i].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.REQUIRED) {

                    if (logger != null && logger.isLoggable(Level.FINE)) {
                        logger.fine(entries[i].getLoginModuleName() + "." + methodName + " REQUIRED failure");
                    }

                    // save exception and continue

                    if (firstRequiredError == null) {
                        firstRequiredError = ae;
                    }

                } else {

                    if (logger != null && logger.isLoggable(Level.FINE)) {
                        logger.fine(entries[i].getLoginModuleName() + "." + methodName + " OPTIONAL failure");
                    }

                    // save exception and continue

                    if (firstError == null) {
                        firstError = ae;
                    }
                }
            }
        }

        // done invoking entire stack of modules

        if (firstRequiredError != null) {
            throw firstRequiredError;
        }
        if (firstError != null && !success) {
            throw firstError;
        }

        // if no errors, return gracefully
        if (logger != null && logger.isLoggable(Level.FINE)) {
            logger.fine("overall " + methodName + " success");
        }
    }
}
