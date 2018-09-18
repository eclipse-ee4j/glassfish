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

package com.sun.enterprise.resource.naming;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.internal.api.Globals;

/**
 * Utility class to bootstrap connector-runtime.<br>
 * Must be used only for ObjectFactory implementations of connector, only in CLIENT mode<br>
 */
public class ConnectorNamingUtils {

    private static Logger _logger =
    LogDomains.getLogger(ConnectorNamingUtils.class, LogDomains.RSR_LOGGER);

    private volatile static ConnectorRuntime runtime;

    static {
        //making sure that connector-runtime is always initialized.
        //This solves the issue of multiple threads doing resource lookup in STANDALONE mode.
        getRuntime();
    }

    public static ConnectorRuntime getRuntime() {
        try {
            if (runtime == null) {
                synchronized(ConnectorNamingUtils.class) {
                    if(runtime == null) {
                        runtime = ConnectorRuntime.getRuntime();
                    }
                }
            }
        } catch (Exception e) {
            // Assuming that connector runtime is always available in SERVER and APPCLIENT mode and
            // hence this is CLIENT mode
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "unable to get Connector Runtime due to the following exception, " +
                    "trying client mode", e);
            }
            runtime = getHabitat().getService(ConnectorRuntime.class);
        }
        return runtime;
    }

    static private ServiceLocator getHabitat() {
        ServiceLocator habitat = Globals.getStaticHabitat();
        StartupContext startupContext = new StartupContext();
        ServiceLocatorUtilities.addOneConstant(habitat, startupContext);
        ServiceLocatorUtilities.addOneConstant(habitat,
                new ProcessEnvironment(ProcessEnvironment.ProcessType.Other));

        return habitat;
    }
}
