/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;


/**
 * Remote AdminCommand to create a config node.  This command is run only on DAS.
 * Register the config node on DAS
 *
 * @author Carla Mott
 */
@Service(name = "delete-node-ssh")
@I18n("delete.node.ssh")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Nodes.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-node-ssh",
        description="Delete Node SSH")
})
public class DeleteNodeSshCommand extends DeleteNodeRemoteCommand {
    @Override
    public final void execute(AdminCommandContext context) {
        executeInternal(context);
    }
    /**
     * Get list of password file entries
     * @return List
     */
    @Override
    protected final List<String> getPasswords() {
        List<String> list = new ArrayList<>();
        list.add("AS_ADMIN_SSHPASSWORD=" + SSHLauncher.expandPasswordAlias(remotepassword));
        if (sshkeypassphrase != null) {
            list.add("AS_ADMIN_SSHKEYPASSPHRASE=" + SSHLauncher.expandPasswordAlias(sshkeypassphrase));
        }
        return list;
    }

    @Override
    protected String getUninstallCommandName() {
        return "uninstall-node-ssh";
    }

    @Override
    final protected void setTypeSpecificOperands(List<String> command, ParameterMap map) {
        command.add("--sshport");
        command.add(map.getOne(NodeUtils.PARAM_REMOTEPORT));

        command.add("--sshuser");
        command.add(map.getOne(NodeUtils.PARAM_REMOTEUSER));

        String key = map.getOne(NodeUtils.PARAM_SSHKEYFILE);

        if (key != null) {
            command.add("--sshkeyfile");
            command.add(key);
        }
    }
}
