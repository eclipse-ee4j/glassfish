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

import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.util.StringUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import jakarta.inject.Inject;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * Remote AdminCommand to validate the connection to an SSH node.
 * @author Joe Di Pol
 * @author Byron Nevins
 */
public abstract class PingNodeRemoteCommand implements AdminCommand {
    @Inject
    ServiceLocator habitat;
    @Inject
    private Nodes nodes;
    @Param(name = "name", primary = true)
    protected String name;
    @Param(optional = true, name = "validate", shortName = "v", alias = "full", defaultValue = "false")
    private boolean validate;
    private static final String NL = System.getProperty("line.separator");
    private Logger logger = null;
    protected abstract String validateSubType(Node node);

    protected final void executeInternal(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        StringBuilder msg = new StringBuilder();
        Node theNode = null;

        logger = context.getLogger();
        NodeUtils nodeUtils = new NodeUtils(habitat, logger);

        // Make sure Node is valid
        theNode = nodes.getNode(name);
        if (theNode == null) {
            String m = Strings.get("noSuchNode", name);
            logger.warning(m);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(m);
            return;
        }

        String err = validateSubType(theNode);
        if (err != null) {
            logger.warning(err);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(err);
            return;
        }

        try {
            String version = "";
            if (validate) {
                // Validates all parameters
                nodeUtils.validate(theNode);
                version = Strings.get("ping.glassfish.version",
                        theNode.getInstallDir(),
                        nodeUtils.getGlassFishVersionOnNode(theNode, context));
            }
            else {
                // Just does a basic connection check
                nodeUtils.pingRemoteConnection(theNode);
            }
            String m1 = Strings.get("ping.node.success", name,
                    theNode.getNodeHost(), theNode.getType());
            if (StringUtils.ok(version)) {
                m1 = m1 + NL + version;
            }
            report.setMessage(m1);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        }
        catch (CommandValidationException e) {
            String m1 = Strings.get("ping.node.failure", name,
                    theNode.getNodeHost(), theNode.getType());
            msg.append(StringUtils.cat(NL, m1, e.getMessage()));
            report.setMessage(msg.toString());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }
}
