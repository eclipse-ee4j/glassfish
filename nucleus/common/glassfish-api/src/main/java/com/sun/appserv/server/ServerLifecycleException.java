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

package com.sun.appserv.server;

/**
 * Exception thrown by application server lifecycle modules and subsystems. These exceptions
 * are generally considered fatal to the operation of application server.
 */
public final class ServerLifecycleException extends Exception {

    /**
     * Construct a new LifecycleException with no other information.
     */
    public ServerLifecycleException() {
        super();
    }

    /**
     * Construct a new LifecycleException for the specified message.
     *
     * @param message Message describing this exception
     */
    public ServerLifecycleException(String message) {
        super(message);
    }

    /**
     * Construct a new LifecycleException for the specified throwable.
     *
     * @param rootCause Throwable that caused this exception
     */
    public ServerLifecycleException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * Construct a new LifecycleException for the specified message
     * and throwable.
     *
     * @param message Message describing this exception
     * @param rootCause Throwable that caused this exception
     */
    public ServerLifecycleException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}
