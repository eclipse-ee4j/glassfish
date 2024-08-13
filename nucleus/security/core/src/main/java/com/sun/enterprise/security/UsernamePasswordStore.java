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

package com.sun.enterprise.security;

import com.sun.enterprise.security.common.ClientSecurityContext;

import java.util.Arrays;

/**
 * This class is used to share information between either of the following scenarios 1. Different points of execution of a single
 * thread 2. Different threads that wish to share the username and password information
 *
 * Which of the above two condition is applicable depends upon the system property key
 * "com.sun.appserv.iiopclient.perthreadauth"; When set to true, scenario #1 above applies and the username/password information
 * is not shared between threads. When set to false, scenario #2 above applies and the username/password information stored by
 * one thread is global and visible to all threads.
 */
public final class UsernamePasswordStore {
    private static final boolean isPerThreadAuth = Boolean.getBoolean(ClientSecurityContext.IIOP_CLIENT_PER_THREAD_FLAG);

    private static ThreadLocal<UsernamePasswordStore> localUpc = isPerThreadAuth ? new ThreadLocal<>() : null;
    private static UsernamePasswordStore sharedUpc;

    private final String username;
    private final char[] password;

    /**
     * This creates a new UsernamePasswordStore object. The constructor is marked as private.
     *
     * @param username
     * @param password
     */
    private UsernamePasswordStore(String username, char[] password) {
        // Copy the password to another reference before storing it to the instance field.
        char[] passwordCopy = password == null ? null : Arrays.copyOf(password, password.length);
        this.password = passwordCopy;
        this.username = username;
    }

    /**
     * This method returns a UsernamePasswordStore, that is either thread-local or global depending on the system property
     * IIOP_PER_THREAD_CLIENT_FLAG. This method is marked as private.
     *
     * @return The current UsernamePasswordStore
     */
    private static UsernamePasswordStore get() {
        if (isPerThreadAuth) {
            return localUpc.get();
        }

        synchronized (UsernamePasswordStore.class) {
            return sharedUpc;
        }
    }

    /**
     * This method sets the username and password as thread-local or global variable
     *
     * @param username
     * @param password
     */
    public static void set(String username, char[] password) {
        if (isPerThreadAuth) {
            localUpc.set(new UsernamePasswordStore(username, password));
        } else {
            synchronized (UsernamePasswordStore.class) {
                sharedUpc = new UsernamePasswordStore(username, password);
            }
        }
    }

    /**
     * Clears the username and password, that might have been previously stored, either globally or locally to each thread.
     */
    public static void reset() {
        if (isPerThreadAuth) {
            localUpc.set(null);
        } else {
            synchronized (UsernamePasswordStore.class) {
                sharedUpc = null;
            }
        }
    }

    /**
     * Clears the username and password only is they were stored locally to each thread
     */
    public static void resetThreadLocalOnly() {
        if (isPerThreadAuth) {
            localUpc.set(null);
        }
    }

    /**
     * Returns the username, that was previously stored.
     *
     * @return The username set previously or null if not set
     */
    public static String getUsername() {
        UsernamePasswordStore ups = UsernamePasswordStore.get();
        if (ups == null) {
            return null;
        }

        return ups.username;
    }

    /**
     * Returns the password, that was previously stored.
     *
     * @return The password set previously or null if not set
     */
    public static char[] getPassword() {
        UsernamePasswordStore ups = UsernamePasswordStore.get();
        if (ups == null) {
            return null;
        }

        // Copy the password to another reference before returning it
        return ups.password == null ? null : Arrays.copyOf(ups.password, ups.password.length);
    }

}
