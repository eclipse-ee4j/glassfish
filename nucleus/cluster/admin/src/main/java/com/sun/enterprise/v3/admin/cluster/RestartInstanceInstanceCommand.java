/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.module.ModulesRegistry;
import org.glassfish.api.*;
import org.glassfish.api.admin.*;
import jakarta.inject.Inject;
import com.sun.enterprise.v3.admin.RestartServer;
import com.sun.enterprise.v3.admin.RestartServer;


import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 *
 * @author bnevins
 */
@Service(name = "_restart-instance")
@PerLookup
@CommandLock(CommandLock.LockType.NONE) // allow restart always
@Async
@I18n("restart.instance.command")
@ExecuteOn(RuntimeType.INSTANCE)
public class RestartInstanceInstanceCommand extends RestartServer implements AdminCommand {

    @Inject
    ModulesRegistry registry;
    @Inject
    private ServerEnvironment env;
    // no default value!  We use the Boolean as a tri-state.
    @Param(name = "debug", optional = true)
    private Boolean debug;

    @Override
    public void execute(AdminCommandContext context) {
        if (!env.isInstance()) {
            String msg = Strings.get("restart.instance.notInstance",
                    env.getRuntimeType().toString());

            context.getLogger().warning(msg);
            return;
        }
        setRegistry(registry);
        setServerName(env.getInstanceName());

        if (debug != null)
            setDebug(debug);

        doExecute(context);
    }
}
