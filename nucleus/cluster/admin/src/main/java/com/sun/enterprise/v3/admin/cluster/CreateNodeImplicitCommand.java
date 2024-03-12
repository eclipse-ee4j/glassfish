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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Nodes;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.hk2.api.PerLookup;

import jakarta.inject.Inject;


import org.jvnet.hk2.annotations.Service;

/**
 * Remote AdminCommand to create a config node.  This command is run only on DAS.
 *  Register the config node on DAS
 *
 * @author Carla Mott
 */
@Service(name = "_create-node-implicit")
@I18n("create.node.implicit")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_create-node-implicit",
        description="_create-node-implicit")
})
public class CreateNodeImplicitCommand implements AdminCommand {

    @Inject
    Nodes nodes;

    @Inject
    private CommandRunner cr;

    @Param(name="name", optional= true)
    String name;

    @Param(name="nodedir", optional = true)
    String nodedir;

    @Param(name="nodehost",  primary = true)
    String nodehost;

    @Param(name = "installdir")
    String installdir;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        if (name == null)
            name = nodehost;

        if (nodes.getNode(name) != null) {
            //already created nothing to do here
            return;
        }
        CommandInvocation ci = cr.getCommandInvocation("_create-node", report, context.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);
        map.add(NodeUtils.PARAM_NODEDIR, nodedir);
        map.add(NodeUtils.PARAM_INSTALLDIR, installdir);
        map.add(NodeUtils.PARAM_NODEHOST, nodehost);
        map.add(NodeUtils.PARAM_TYPE,"CONFIG");

        ci.parameters(map);
        ci.execute();


    }

}
