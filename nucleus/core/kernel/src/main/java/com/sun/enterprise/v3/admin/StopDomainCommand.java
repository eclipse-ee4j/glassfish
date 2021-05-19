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

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * AdminCommand to stop the domain execution which mean shuting down the application
 * server.
 *
 * @author Jerome Dochez
 */
@Service(name = "stop-domain")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@Async
@I18n("stop.domain.command")
@AccessRequired(resource="domain", action="stop")
@ExecuteOn(RuntimeType.DAS)
public class StopDomainCommand extends StopServer implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(StopDomainCommand.class);
    @Inject
    ServiceLocator habitat;
    @Inject
    ServerEnvironment env;
    @Param(optional = true, defaultValue = "true")
    Boolean force;

    /**
     * Shutdown of the application server :
     *
     * All running services are stopped.
     * LookupManager is flushed.
     */
    public void execute(AdminCommandContext context) {

        if (!env.isDas()) {
            // This command is asynchronous.  We can't return anything so we just
            // log the error and return
            String msg = localStrings.getLocalString("stop.domain.notDas",
                    "stop-domain only works with domains, this is a {0}",
                    env.getRuntimeType().toString());

            context.getLogger().warning(msg);
            return;
        }

        doExecute(habitat, env, force);
    }
}
