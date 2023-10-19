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

package com.sun.enterprise.security.ee;

import java.net.SocketPermission;
// IASRI 4660742 START
// IASRI 4660742 END
import java.util.PropertyPermission;
import com.sun.enterprise.security.authorize.cache.CachedPermissionImpl;
import com.sun.enterprise.security.authorize.cache.PermissionCache;
import com.sun.enterprise.security.ssl.SSLUtils;

/**
 * Java 2 security manager that enforces code security.
 *
 * @author Harish Prabandham
 */
public class J2EESecurityManager extends java.rmi.RMISecurityManager {

    private CachedPermissionImpl connectPerm;

    private PermissionCache cache;

    private boolean cacheEnabled = false;

    public J2EESecurityManager() {
    }

    /*
     * public void checkAccess(ThreadGroup t) { Class[] clss = getClassContext(); for(int i=1; i < clss.length; ++i) { //
     * IASRI 4660742 System.out.println(clss[i] + " : " + clss[i].getProtectionDomain()); // START OF IASRI 4660742
     * _logger.log(Level.FINE,clss[i] + " : " + clss[i].getProtectionDomain()); // END OF IASRI 4660742 }
     *
     * System.out.flush();
     *
     * // JDK 1.1. implementation... Class[] clss = getClassContext(); for(int i=1; i < clss.length; ++i) {
     * checkIfInContainer(clss[i]); } }
     *
     * // JDK 1.1. implementation... private void checkIfInContainer(Class clazz) { Class[] parents =
     * clazz.getDeclaredClasses(); for(int i=0; i < parents.length; ++i) { if(parents[i] == com.sun.ejb.Container.class)
     * throw new SecurityException("Got it...."); } }
     */

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
        if (key.equals("javax.net.ssl.keyStorePassword") || key.equals("javax.net.ssl.trustStorePassword")) {
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
