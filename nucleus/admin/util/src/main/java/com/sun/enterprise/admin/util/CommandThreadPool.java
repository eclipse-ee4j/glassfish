/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.InstanceCommand;
import org.glassfish.api.admin.InstanceCommandResult;
import org.glassfish.api.admin.ServerEnvironment;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This singleton service creates and holds the command pool used to execute commands
 *
 * @author Vijay Ramachandran
 */
@Service
@RunLevel(mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING, value = StartupRunLevel.VAL)
public class CommandThreadPool implements PostConstruct {

    @Inject
    private ServiceLocator habitat;

    @Inject
    private ServerEnvironment serverEnv;

    @Inject
    private Domain domain;

    @Inject
    private Logger logger;

    private ExecutorService svc = null;

    public CommandThreadPool() {
    }

    /**
     * Process the instance file if this is DAS and there are instances configured already in this domain
     */
    @Override
    public void postConstruct() {
        // If this is not the DAS, no need for this pool
        if (serverEnv.isInstance()) {
            return;
        }
        int poolSize = 5;
        Config svrConfig = domain.getConfigNamed("server-config");
        // I am doing this code instead of a simple svrConfig.getAdminListener() here because embedded tests are failing
        // during build; got to check the reason why later.
        if (svrConfig != null) {
            NetworkConfig nwc = svrConfig.getNetworkConfig();
            if (nwc != null) {
                List<NetworkListener> lss = nwc.getNetworkListeners().getNetworkListener();
                if ((lss != null) && (!lss.isEmpty())) {
                    for (NetworkListener ls : lss) {
                        if (ServerTags.ADMIN_LISTENER_ID.equals(ls.getName())) {
                            if (ls.findThreadPool() != null) {
                                poolSize = Integer.parseInt(ls.findThreadPool().getMaxThreadPoolSize());
                            }
                        }
                    }
                }
            }
        }
        svc = Executors.newFixedThreadPool(poolSize, new InstanceStateThreadFactory());
    }

    public Future<InstanceCommandResult> submitJob(InstanceCommand ice, InstanceCommandResult r) {
        FutureTask<InstanceCommandResult> t = new FutureTask<InstanceCommandResult>((Runnable) ice, r);
        return svc.submit(t, r);
    }

    private static class InstanceStateThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnableObj) {
            Thread t = new Thread(runnableObj);
            t.setDaemon(true);
            return t;
        }
    }
}
