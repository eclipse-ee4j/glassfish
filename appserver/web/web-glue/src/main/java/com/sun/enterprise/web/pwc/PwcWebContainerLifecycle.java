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

package com.sun.enterprise.web.pwc;

import com.sun.appserv.server.ServerLifecycleException;

import org.apache.catalina.Logger;

public interface PwcWebContainerLifecycle {

  public void onInitialization(String rootDir, String instanceName,
                                 boolean useNaming, Logger logger,
                               String embeddedClassName)
                        throws Exception;

    /**
     * Server is starting up applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onStartup()
                        throws Exception;

    /**
     * Server has complted loading the applications and is ready to serve requests.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onReady() throws Exception;

    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onShutdown()
                        throws Exception;

    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem.  This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onTermination()
                        throws Exception;
}
