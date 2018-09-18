/*
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

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import java.io.File;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.kernel.KernelLoggerInfo;

/**
 * A class to house identical code for stopping instances and DAS
 * @author Byron Nevins
 */
public class StopServer {

    /**
     * Shutdown of the server :
     *
     * All running services are stopped.
     * LookupManager is flushed.
     */
    protected final void doExecute(ServiceLocator habitat, ServerEnvironment env, boolean force) {
        try {
            KernelLoggerInfo.getLogger().info(KernelLoggerInfo.serverShutdownInit);
            // Don't shutdown GlassFishRuntime, as that can bring the OSGi framework down which is wrong
            // when we are embedded inside an existing runtime. So, just stop the glassfish instance that
            // we are supposed to stop. Leave any cleanup to some other code.

            // get the GlassFish object - we have to wait in case startup is still in progress
            // This is a temporary work-around until HK2 supports waiting for the service to
            // show up in the ServiceLocator.
            GlassFish gfKernel = habitat.getService(GlassFish.class);
            while (gfKernel == null) {
                Thread.sleep(1000);
                gfKernel = habitat.getService(GlassFish.class);
            }
            // gfKernel is absolutely positively for-sure not null.
            gfKernel.stop();
        }
        catch (Throwable t) {
            // ignore
        }


        if (force) {
            System.exit(0);
        }
        else {
            deletePidFile(env);
        }
    }

    private final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(StopServer.class);

    /**
     * It is **Essential** to delete this file!  Other code will assume the server
     * is running if it exists.
     * Any old App is currently (10/10/10) allowed to add a shutdownhook with a System.exit()
     * which is GUARANTEED to prevent the shutdown hook for deleting the pidfile to run.
     * So -- we always do it BEFORE trying to exit.
     */
    private void deletePidFile(ServerEnvironment env) {
        File pidFile = new File(env.getConfigDirPath(), "pid");

        if (pidFile.isFile()) {
            FileUtils.deleteFileNowOrLater(pidFile);
        }
    }
}
