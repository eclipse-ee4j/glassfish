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

import com.sun.enterprise.util.cluster.RemoteType;
import com.sun.enterprise.config.serverbeans.Node;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

import org.jvnet.hk2.annotations.Service;

/**
 * Remote AdminCommand to update an ssh node.  This command is run only on DAS.
 *
 * @author Joe Di Pol
 */
@Service(name = "update-node-ssh")
@I18n("update.node.ssh")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Node.class,
        opType=RestEndpoint.OpType.POST,
        path="update-node-ssh",
        description="Update Node",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class UpdateNodeSshCommand extends UpdateNodeRemoteCommand {
    @Param(name = "sshport", optional = true)
    private String sshportInSubClass;
    @Param(name = "sshuser", optional = true)
    private String sshuserInSubClass;
    @Param(name = "sshkeyfile", optional = true)
    private String sshkeyfileInSubClass;
    @Param(name = "sshpassword", optional = true, password = true)
    private String sshpasswordInSubClass;
    @Param(name = "sshkeypassphrase", optional = true, password = true)
    private String sshkeypassphraseInSubClass;

    @Override
    public void execute(AdminCommandContext context) {
        executeInternal(context);
    }

    @Override
    protected void populateParameters() {
        remotePort = sshportInSubClass;
        remoteUser = sshuserInSubClass;
        sshkeyfile = sshkeyfileInSubClass;
        remotepassword = sshpasswordInSubClass;
        sshkeypassphrase = sshkeypassphraseInSubClass;
    }

    @Override
    protected RemoteType getType() {
        return RemoteType.SSH;
    }

    @Override
    protected String getDefaultPort() {
        return NodeUtils.NODE_DEFAULT_SSH_PORT;
    }
}
