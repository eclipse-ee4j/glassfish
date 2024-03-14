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
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Nodes;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import jakarta.inject.Inject;


import org.jvnet.hk2.annotations.Service;
import java.util.logging.Logger;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

@Service(name = "list-nodes-config")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.nodes.config.command")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-nodes-config",
        description="list-nodes-config")
})
public class ListNodesConfigCommand implements AdminCommand{

    @Inject
    Servers servers;
    @Inject
    private Nodes nodes;

    @Param(optional = true, defaultValue = "false", name="long", shortName="l")
    private boolean long_opt;
    @Param(optional = true)
    private boolean terse;

    private ActionReport report;
    Logger logger;

    @Override
    public void execute(AdminCommandContext context) {

        report = context.getActionReport();

        logger = context.getLogger();

        ListNodesHelper lnh = new ListNodesHelper(logger, servers, nodes, "CONFIG", long_opt, terse);

        String nodeList = lnh.getNodeList();

        report.setMessage(nodeList);

        report.setActionExitCode(ExitCode.SUCCESS);

    }
}
