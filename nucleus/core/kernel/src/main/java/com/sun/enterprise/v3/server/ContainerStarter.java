/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.data.ContainerRegistry;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.module.Module;

/**
 * This class is responsible for starting containers.
 *
 * @author Jerome Dochez, Sanjeeb Sahoo
 */
@Service
public class ContainerStarter {

	@Inject
	ServiceLocator serviceLocator;
	
    @Inject
    ServiceLocator habitat;

    @Inject
    Logger logger;

    @Inject
    ServerEnvironmentImpl env;

    @Inject ContainerRegistry registry;

    public Collection<EngineInfo> startContainer(Sniffer sniffer) {

        assert sniffer!=null;
        String containerName = sniffer.getModuleType();
        assert containerName!=null;
        
        // I do the container setup first so the code has a chance to set up
        // repositories which would allow access to the container module.
        try {

            Module[] modules = sniffer.setup(null, logger);
            logger.logp(Level.FINE, "ContainerStarter", "startContainer", "Sniffer {0} set up following modules: {1}",
                    new Object[]{sniffer, modules != null ? Arrays.toString(modules): ""});
        } catch(FileNotFoundException fnf) {
            logger.log(Level.SEVERE, fnf.getMessage());
            return null;
        } catch(IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
            return null;

        }

        // first the right container from that module.
        Map<String, EngineInfo> containers = new HashMap<String, EngineInfo>();
        for (String name : sniffer.getContainersNames()) {
            ServiceHandle<Container> provider = serviceLocator.getServiceHandle(Container.class, name);
            if (provider == null) {
                logger.severe("Cannot find Container named " + name + ", so unable to start " + sniffer.getModuleType() + " container");
                return null;
            }
            EngineInfo info = new EngineInfo(provider, sniffer, null /* never used */);
            containers.put(name, info);
        }
        // Now that we have successfully created all containers, let's register them as well.
        for (Map.Entry<String, EngineInfo> entry : containers.entrySet()) {
            registry.addContainer(entry.getKey(), entry.getValue());
        }
        return containers.values();
    }


}
