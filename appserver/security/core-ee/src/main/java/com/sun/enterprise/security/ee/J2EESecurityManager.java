/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.ee;

import com.sun.enterprise.security.ee.authorize.cache.CachedPermissionImpl;
import com.sun.enterprise.security.ee.authorize.cache.PermissionCache;
import com.sun.enterprise.security.ssl.SSLUtils;

import java.net.SocketPermission;
import java.util.PropertyPermission;

import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_PASSWORD;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_PASSWORD;

/**
 * Java 2 security manager that enforces code security.
 *
 * @author Harish Prabandham
 */
@Deprecated(since = "7.1.0", forRemoval = true)
public class J2EESecurityManager extends java.rmi.RMISecurityManager {

    private CachedPermissionImpl connectPerm;
    private PermissionCache cache;
    private boolean cacheEnabled;


    @Override
    public void checkAccess(ThreadGroup t) {
        super.checkAccess(t);
        checkPermission(new java.lang.RuntimePermission("modifyThreadGroup"));
    }

    @Override
    public void checkPackageAccess(final String pkgname) {
        // Remove this once 1.2.2 SecurityManager/ClassLoader bug is fixed.
        if (!pkgname.startsWith("sun.")) {
            super.checkPackageAccess(pkgname);
        }
    }

    @Override
    public void checkExit(int status) {
        // Verify exit permission
        super.checkExit(status);
    }

    @Override
    public void checkConnect(String host, int port) {
        if (checkConnectPermission()) {
            return;
        }
        super.checkConnect(host, port);
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if (checkConnectPermission()) {
            return;
        }
        super.checkConnect(host, port, context);
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (checkProperty(key)) {
            return;
        }
        super.checkPropertyAccess(key);
    }

    private boolean checkConnectPermission() {
        if (cacheEnabled()) {
            return connectPerm.checkPermission();
        }
        return false;
    }

    private boolean checkProperty(String key) {
        if (KEYSTORE_PASSWORD.getSystemPropertyName().equals(key)
            || TRUSTSTORE_PASSWORD.getSystemPropertyName().equals(key)) {
            SSLUtils.checkPermission(key);
        }
        if (cacheEnabled()) {
            return cache.checkPermission(new PropertyPermission(key, "read"));
        }
        return false;
    }

    public synchronized boolean cacheEnabled() {
        return cacheEnabled;
    }

    public synchronized void enablePermissionCache(PermissionCache c) {
        if (c != null) {
            cache = c;
            connectPerm = new CachedPermissionImpl(cache, new SocketPermission("*", "connect"));
            cacheEnabled = true;
        }
    }

}
