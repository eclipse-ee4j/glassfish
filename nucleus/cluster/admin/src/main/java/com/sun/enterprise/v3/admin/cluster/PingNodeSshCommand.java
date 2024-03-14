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
import org.glassfish.api.I18n;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

/**
 * Remote AdminCommand to validate the connection to an SSH node.
 *
 * @author Joe Di Pol
 */
@Service(name = "ping-node-ssh")
@I18n("ping.node.ssh")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Node.class,
        opType=RestEndpoint.OpType.GET,
        path="ping-node-ssh",
        description="ping-node-ssh",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class PingNodeSshCommand extends PingNodeRemoteCommand {
    @Override
    public void execute(AdminCommandContext context) {
        executeInternal(context);
    }

    /**
     *
     * @param node the node of interest
     * @return null if all-OK, otherwise return an error message
     */
    @Override
    protected String validateSubType(Node node) {
        if (!NodeUtils.isSSHNode(node)) {
            return Strings.get("notSshNode", name);
        }
        return null;
    }
}
