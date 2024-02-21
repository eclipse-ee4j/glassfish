/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.cluster.RemoteType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Remote AdminCommand to create and ssh node.  This command is run only on DAS.
 * Register the node with SSH info on DAS
 *
 * @author Carla Mott
 */
@Service(name = "create-node-ssh")
@I18n("create.node.ssh")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Nodes.class,
        opType=RestEndpoint.OpType.POST,
        path="create-node-ssh",
        description="Create Node SSH")
})
public class CreateNodeSshCommand extends CreateRemoteNodeCommand {
    @Param(name = "sshport", optional = true, defaultValue = NodeUtils.NODE_DEFAULT_SSH_PORT)
    private String sshport;
    @Param(name = "sshuser", optional = true, defaultValue = NodeUtils.NODE_DEFAULT_REMOTE_USER)
    private String sshuser;
    @Param(name = "sshpassword", optional = true, password = true)
    private String sshpassword;
    @Param(name = "sshkeyfile", optional = true)
    private String sshkeyfile;
    @Param(name = "sshkeypassphrase", optional = true, password = true)
    private String sshkeypassphrase;

    @Override
    public final void execute(AdminCommandContext context) {
        populateBaseClass();
        executeInternal(context);
    }

    @Override
    protected void initialize() {
        // nothing to do...
    }

    @Override
    protected void validate() throws CommandValidationException {
        // nothing to do
    }

    /**
     * Sometimes the console passes an empty string for a parameter. This
     * makes sure those are defaulted correctly.
     */
    @Override
    protected final void checkDefaults() {
        super.checkDefaults();

        if (!StringUtils.ok(remotePort)) {
            remotePort = NodeUtils.NODE_DEFAULT_SSH_PORT;
        }
    }

    @Override
    protected final RemoteType getType() {
        return RemoteType.SSH;
    }

    /**
     * We can't put these values into the base class simply to get the names that
     * the user sees correct.  I.e. "ssh" versus "dcom" versus future types...
     *
     */
    @Override
    protected void populateBaseClass() {
        remotePort = sshport;
        remoteUser = sshuser;
        remotePassword = sshpassword;
    }

    @Override
    protected final void populateParameters(ParameterMap pmap) {
        pmap.add(NodeUtils.PARAM_SSHKEYFILE, sshkeyfile);
        pmap.add(NodeUtils.PARAM_SSHKEYPASSPHRASE, sshkeypassphrase);
    }

    @Override
    protected final void populateCommandArgs(List<String> args) {
        if (sshkeyfile == null) {
            File file = SSHUtil.getExistingKeyFile();
            if (file != null) {
                sshkeyfile = file.getAbsolutePath();
            }
        }

        if (sshkeyfile != null) {
            args.add("--sshkeyfile");
            args.add(sshkeyfile);
        }

        args.add("--sshuser");
        args.add(remoteUser);
        args.add("--sshport");
        args.add(remotePort);
    }

    /**
     * Get list of password file entries
     * @return List
     */
    @Override
    protected List<String> getPasswords() {
        List<String> list = new ArrayList<>();
        NodeUtils nUtils = new NodeUtils(habitat, logger);
        list.add("AS_ADMIN_SSHPASSWORD=" + nUtils.sshL.expandPasswordAlias(remotePassword));

        if (sshkeypassphrase != null) {
            list.add("AS_ADMIN_SSHKEYPASSPHRASE=" + nUtils.sshL.expandPasswordAlias(sshkeypassphrase));
        }
        return list;
    }

    @Override
    protected String getInstallNodeCommandName() {
        return "install-node-ssh";
    }
}
