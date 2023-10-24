/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.iiop.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;

/**
 * This class that implements ConnectionExecutionContext that gets stored in Thread Local Storage. If the current thread
 * creates child threads, the context info that is stored in the current thread is automatically propogated to the child
 * threads.
 *
 * Two class methods serve as a convinient way to set/get the Context information within the current thread.
 *
 * Thread Local Storage is a concept introduced in JDK1.2. So, it will not work on earlier releases of JDK.
 *
 * @see java.lang.ThreadLocal
 * @see java.lang.InheritableThreadLocal
 *
 */
public class ConnectionExecutionContext {

    public static final String IIOP_CLIENT_PER_THREAD_FLAG = "com.sun.appserv.iiopclient.perthreadauth";
    private static final boolean isPerThreadAuth;

    static {
        PrivilegedAction<Boolean> action = () -> Boolean.getBoolean(IIOP_CLIENT_PER_THREAD_FLAG);
        isPerThreadAuth = AccessController.doPrivileged(action).booleanValue();
    }

    // private static final InheritableThreadLocal connCurrent= new InheritableThreadLocal();
    private static final ThreadLocal connCurrent = (isPerThreadAuth) ? new ThreadLocal() : new InheritableThreadLocal();

    // XXX: Workaround for non-null connection object ri for local invocation.
    private static final ThreadLocal<Long> ClientThreadID = new ThreadLocal<>();

    public static Long readClientThreadID() {
        Long ID = ClientThreadID.get();
        // ClientThreadID.remove();
        return ID;
    }

    public static void setClientThreadID(Long ClientThreadID) {
        ConnectionExecutionContext.ClientThreadID.set(ClientThreadID);
    }

    public static void removeClientThreadID() {
        ClientThreadID.remove();
    }

    /**
     * This method can be used to add a new hashtable for storing the Thread specific context information. This method is
     * useful to add a deserialized Context information that arrived over the wire.
     *
     * @param ctxTable hashtable that stores the current thread's context information.
     */
    public static void setContext(Hashtable ctxTable) {
        if (ctxTable != null) {
            connCurrent.set(ctxTable);
        } else {
            connCurrent.set(new Hashtable());
        }
    }

    /**
     * This method returns the hashtable that stores the thread specific Context information.
     *
     * @return The Context object stored in the current TLS. It always returns a non null value;
     */
    public static Hashtable getContext() {
        if (connCurrent.get() == null) {
            setContext(null); // Create a new one...
        }
        return (Hashtable) connCurrent.get();
    }
}
