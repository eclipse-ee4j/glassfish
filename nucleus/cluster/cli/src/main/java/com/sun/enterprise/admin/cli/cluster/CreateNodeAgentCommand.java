/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.admin.cli.CLICommand;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;

/**
 * This is a local command that creates a node agent.
 */
// XXX - make this command disappear until we actually implement it
//@Service(name = "create-node-agent")
@PerLookup
public final class CreateNodeAgentCommand extends CLICommand {

    /*
    @Param(name = "agentdir", optional = true)
    private String agentDir;

    @Param(name = "agentport", optional = true)
    private String agentPort;

    @Param(name = "agentproperties", optional = true, separator = ':')
    private String agentProperties;     // XXX - should it be a Properties?

    @Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    private boolean saveMasterPassword = false;

    @Param(name = "filesystemonly", optional = true, defaultValue = "false")
    private boolean filesystemOnly = false;

    @Param(name = "nodeagent_name", primary = true)
    private String nodeAgentName;

    private File agentsDir;             // the parent dir of all node agents
    */

    /**
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException  {

        /*
        if (ok(agentDir)) {
            agentsDir = new File(agentDir);
        } else {
            String agentRoot = getSystemProperty(
                                SystemPropertyConstants.AGENT_ROOT_PROPERTY);
            // AS_DEF_NODES_PATH might not be set on upgraded domains
            if (agentRoot != null)
                agentsDir = new File(agentRoot);
            else
                agentsDir = new File(new File(getSystemProperty(
                                SystemPropertyConstants.INSTALL_ROOT_PROPERTY)),
                                "nodes");
        }
        */

        // XXX - validate lots more...
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException, CommandValidationException {

        throw new CommandException("Not implemented");
    }
}
