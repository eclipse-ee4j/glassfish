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

package com.sun.enterprise.v3.admin.cluster;


import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.v3.admin.StopServer;
import org.glassfish.api.Async;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * AdminCommand to stop the instance
 * server.
 * Shutdown of an instance.
 * This is the Async command running on the instance.
 *
 * note: This command is asynchronous.  We can't return anything so we just
 * log errors and return

 * @author Byron Nevins
 */
@Service(name = "_stop-instance")
@Async
@PerLookup
@CommandLock(CommandLock.LockType.NONE) // allow stop-instance always
@ExecuteOn(RuntimeType.INSTANCE)
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_stop-instance",
        description="_stop-instance")
})
public class StopInstanceInstanceCommand extends StopServer implements AdminCommand {

    @Inject
    private ServerEnvironment env;
    @Inject
    private ServiceLocator habitat;
    @Param(optional = true, defaultValue = "true")
    private Boolean force = true;

    public void execute(AdminCommandContext context) {

        if (!env.isInstance()) {
            String msg = Strings.get("stop.instance.notInstance",
                    env.getRuntimeType().toString());

            context.getLogger().warning(msg);
            return;
        }

        doExecute(habitat, env, force);
    }
}
