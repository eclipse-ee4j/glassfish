/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import java.util.logging.Level;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.kernel.KernelLoggerInfo;

/**
 * A class to house identical code for stopping instances and DAS
 *
 * @author Byron Nevins
 */
public class StopServer {

    /**
     * Shutdown of the server:
     * <ol>
     * <li>All running services are stopped.
     * <li>LookupManager is flushed.
     * </ol>
     */
    protected final void doExecute(ServiceLocator serviceLocator, ServerEnvironment serverEnvironment, boolean force) {
        try {
            KernelLoggerInfo.getLogger().info(KernelLoggerInfo.serverShutdownInit);

            // Don't shutdown GlassFishRuntime, as that can bring the OSGi framework down which is wrong
            // when we are embedded inside an existing runtime. So, just stop the glassfish instance that
            // we are supposed to stop. Leave any cleanup to some other code.

            // get the GlassFish object - we have to wait in case startup is still in progress
            // This is a temporary work-around until HK2 supports waiting for the service to
            // show up in the ServiceLocator.
            GlassFish gfKernel = serviceLocator.getService(GlassFish.class);
            while (gfKernel == null) {
                Thread.yield();
                gfKernel = serviceLocator.getService(GlassFish.class);
            }

            // gfKernel is absolutely positively for-sure not null.
            gfKernel.stop();
        } catch (Throwable t) {
            KernelLoggerInfo.getLogger().log(Level.WARNING, "Server kernel stop failed.", t);
        }

        if (force) {
            System.exit(0);
        }
    }
}
