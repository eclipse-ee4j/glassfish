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

import javax.naming.InitialContext;

/**
 * LifecycleEventContext interface exposes the server-wide runtime environment that is created by ApplicationServer.
 * This context has only accessors and is a subset of ServerContext used by the server runtime.
 */
public interface LifecycleEventContext {
    /**
     * Get the server command-line arguments
     */
    String[] getCmdLineArgs();

    /**
     * Get server install root
     */
    String getInstallRoot();

    /**
     * Get the server instance name
     */
    String getInstanceName();

    /**
     * Get the initial naming context.
     */
    InitialContext getInitialContext();

    /**
     * Writes the specified message to a server log file.
     *
     * @param message a <code>String</code> specifying the message to be written to the log file
     */
    void log(String message);

    /**
     * Writes an explanatory message and a stack trace for a given <code>Throwable</code> exception to the server log file.
     *
     * @param message a <code>String</code> that describes the error or exception
     *
     * @param throwable the <code>Throwable</code> error or exception
     */

    void log(String message, Throwable throwable);
}
