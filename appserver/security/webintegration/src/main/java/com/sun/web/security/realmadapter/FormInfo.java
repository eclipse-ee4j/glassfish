/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
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
package com.sun.web.security.realmadapter;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.deploy.LoginConfig;

import static com.sun.enterprise.util.Utility.isAllNull;
import static java.util.logging.Level.FINE;
import static org.apache.catalina.realm.Constants.FORM_ACTION;
import static org.apache.catalina.realm.Constants.FORM_METHOD;

public class FormInfo {

    private static final Logger LOG = Logger.getLogger(FormInfo.class.getName());

    private final Supplier<Context> contextSupplier;

    /*
     * the following fields are used to implement a bypass of FBL related targets
     */
    protected final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private boolean contextEvaluated = false;
    private String loginPage;
    private String errorPage;

    public FormInfo(Supplier<Context> contextSupplier) {
        this.contextSupplier = contextSupplier;
    }

    public boolean isRequestFormPage(HttpRequest request) {
        initFormPages();

        if (isAllNull(loginPage, errorPage)) {
            return false;
        }

        String requestURI = request.getRequestPathMB().toString();
        LOG.log(FINE, "requestURI: {0}, loginPage: {1}, errorPage: {2}",
            new Object[] {requestURI, loginPage, errorPage});

        if (loginPage != null && loginPage.equals(requestURI)) {
            LOG.log(FINE, "Allowed access to login page {0}", loginPage);
            return true;
        }

        if (errorPage != null && errorPage.equals(requestURI)) {
            LOG.log(FINE, "Allowed access to error page {0}", errorPage);
            return true;
        }

        if (requestURI.endsWith(FORM_ACTION)) {
            LOG.log(FINE, "Allowed access to username/password submission ({0})", FORM_ACTION);
            return true;
        }

        return false;
    }

    private void initFormPages() {
        // allow access to form login related pages and targets
        // and the "j_security_check" action
        boolean evaluated = false;
        try {
            rwLock.readLock().lock();
            evaluated = contextEvaluated;
        } finally {
            rwLock.readLock().unlock();
        }

        if (!evaluated) {
            try {
                rwLock.writeLock().lock();
                if (!contextEvaluated) {
                    // Use Context here as preAuthenticateCheck does not have it
                    // and our Container is always a Context
                    LoginConfig config = contextSupplier.get().getLoginConfig();
                    if (config != null && FORM_METHOD.equals(config.getAuthMethod())) {
                        loginPage = config.getLoginPage();
                        errorPage = config.getErrorPage();
                    }
                    contextEvaluated = true;
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

}
