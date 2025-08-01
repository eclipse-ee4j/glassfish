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

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.util.StringUtils;

import jakarta.inject.Inject;

import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Remote AdminCommand to update a config node.
 *
 * @author Joe Di Pol
 */
@Service(name = "update-node-config")
@I18n("update.node.config")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Node.class,
        opType=RestEndpoint.OpType.POST,
        path="update-node-config",
        description="Update Node Config",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class UpdateNodeConfigCommand implements AdminCommand  {

    @Inject
    private CommandRunner cr;

    @Inject
    ServiceLocator habitat;

    @Inject
    private Nodes nodes;

    @Param(name="name", primary = true)
    private String name;

    @Param(name="nodehost", optional=true)
    private String nodehost;

    @Param(name = "installdir", optional=true)
    private String installdir;

    @Param(name = "nodedir", optional=true)
    private String nodedir;

    private static final String NL = System.getProperty("line.separator");

    private Logger logger = null;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        StringBuilder msg = new StringBuilder();
        Node node = null;
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        logger = context.getLogger();

        // Make sure Node is valid
        node = nodes.getNode(name);
        if (node == null) {
            String m = Strings.get("noSuchNode", name);
            logger.warning(m);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(m);
            return;
        }

        if (node.isDefaultLocalNode()) {
            String m = Strings.get("update.node.config.defaultnode", name);
            logger.warning(m);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(m);
            return;
        }

        // After updating the config node it needs to have a host
        if (!StringUtils.ok(nodehost) && !StringUtils.ok(node.getNodeHost())) {
            String m = Strings.get("update.node.config.missing.attribute",
                    node.getName(), NodeUtils.PARAM_NODEHOST);
            logger.warning(m);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(m);
            return;
        }

        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);

        if (installdir != null) {
            map.add(NodeUtils.PARAM_INSTALLDIR, installdir);
        }
        if (nodehost != null) {
            map.add(NodeUtils.PARAM_NODEHOST, nodehost);
        }
        if (nodedir != null) {
            map.add(NodeUtils.PARAM_NODEDIR, nodedir);
        }

        map.add(NodeUtils.PARAM_TYPE, "CONFIG");

        if (map.size() > 1) {
            CommandInvocation ci = cr.getCommandInvocation("_update-node", report, context.getSubject());
            ci.parameters(map);
            ci.execute();

            if (StringUtils.ok(report.getMessage())) {
                if (msg.length() > 0) {
                    msg.append(NL);
                }
                msg.append(report.getMessage());
            }

            report.setMessage(msg.toString());
        }
    }
}
