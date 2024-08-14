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

package com.sun.enterprise.v3.server;

import com.sun.appserv.server.LifecycleEventContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.glassfish.internal.api.ServerContext;
import org.glassfish.kernel.KernelLoggerInfo;

public class LifecycleEventContextImpl implements LifecycleEventContext {

    private ServerContext ctx;

    private static final Logger logger = KernelLoggerInfo.getLogger();

    /**
     * public constructor
     */
    public LifecycleEventContextImpl(ServerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Get the server command-line arguments
     */
    @Override
    public String[] getCmdLineArgs() {
        return ctx.getCmdLineArgs();
    }

    /**
     * Get server installation root
     */
    @Override
    public String getInstallRoot() {
        return ctx.getInstallRoot().getPath();
    }

    /**
     * Get the server instance name
     */
    @Override
    public String getInstanceName() {
        return ctx.getInstanceName();
    }

    /**
     * Get the initial naming context.
     */
    @Override
    public InitialContext getInitialContext() {
        return ctx.getInitialContext();
    }

    /**
     * Writes the specified message to a server log file.
     *
     * @param message a <code>String</code> specifying the
     *            message to be written to the log file
     */
    @Override
    public void log(String message) {
        logger.info(message);
    }

    /**
     * Writes an explanatory message and a stack trace
     * for a given <code>Throwable</code> exception
     * to the server log file.
     *
     * @param message a <code>String</code> that
     *            describes the error or exception
     * @param throwable the <code>Throwable</code> error
     *            or exception
     */
    @Override
    public void log(String message, Throwable throwable) {
        logger.log(Level.INFO, message, throwable);
    }
}
