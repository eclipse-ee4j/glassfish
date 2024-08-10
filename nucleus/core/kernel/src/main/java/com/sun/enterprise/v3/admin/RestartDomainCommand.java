/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.module.ModulesRegistry;

import jakarta.inject.Inject;

import org.glassfish.api.Async;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * For non-verbose mode: Stop this server, spawn a new JVM that will wait for this JVM to die. The new JVM then starts
 * the server again.
 *
 * For verbose mode: We want the asadmin console itself to do the respawning -- so just return a 10 from System.exit().
 * This tells asadmin to restart.
 *
 * @author Byron Nevins
 */
@Service(name = "restart-domain")
@PerLookup
@Async
@I18n("restart.domain.command")
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.POST, path = "restart-domain", description = "restart-domain") })
@AccessRequired(resource = "domain", action = { "stop", "start" })
public class RestartDomainCommand extends RestartServer implements AdminCommand {

    @Inject
    private ModulesRegistry registry;
    // no default value! We use the Boolean as a tri-state.
    @Param(name = "debug", optional = true)
    private String debug;
    @Inject
    private ServerEnvironment env;

    /** version which will use injection */
    public RestartDomainCommand() {
    }

    /** version which will not use injection */
    public RestartDomainCommand(final ModulesRegistry registryIn) {
        registry = registryIn;
    }

    /**
     * Restart of the application server :
     *
     * All running services are stopped. LookupManager is flushed.
     *
     * Client code that started us should notice the return value of 10 and restart us.
     */
    @Override
    public void execute(AdminCommandContext context) {
        setRegistry(registry);
        setServerName(env.getInstanceRoot().getName());
        if (debug != null) {
            setDebug(Boolean.parseBoolean(debug));
        }

        doExecute(context);
    }
}
