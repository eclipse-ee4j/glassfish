/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.cluster.NodeInfo;
import com.sun.enterprise.util.cluster.RemoteType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ListNodesHelper {

    private static final String EOL = "\n";

    private final Servers servers;
    private final RemoteType listType;
    private final boolean long_opt;
    private final boolean terse;
    private final Nodes nodes;


    public ListNodesHelper(Logger _logger, Servers servers, Nodes nodes, RemoteType type, boolean long_opt, boolean terse) {
        this.listType = type;
        this.long_opt = long_opt;
        this.terse = terse;
        this.servers = servers;
        this.nodes = nodes;
    }

    public String getNodeList() {
        List<NodeInfo> infos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean firstNode = true;

        for (Node n : nodes.getNode()) {

            String name = n.getName();
            String nodeType = n.getType();
            String host = n.getNodeHost();
            String installDir = n.getInstallDir();

            if (listType != null && !listType.name().equals(nodeType)) {
                continue;
            }

            if (firstNode) {
                firstNode = false;
            } else {
                sb.append(EOL);
            }

            if (terse) {
                sb.append(name);
            } else if (!long_opt) {
                sb.append(name).append("  ").append(nodeType).append("  ").append(host);
            }

            if (long_opt){
                List<Server> serversOnNode = servers.getServersOnNode(n);
                StringBuilder instanceList = new StringBuilder();
                if (serversOnNode.size() > 0) {
                    int i = 0;
                    for (Server server: serversOnNode){
                        if (i > 0) {
                            instanceList.append(", ");
                        }
                        instanceList.append(server.getName());
                        i++;
                    }
                }
                infos.add(new NodeInfo(name, host, installDir, nodeType, instanceList.toString()));
            }
        }
        if (long_opt) {
            return NodeInfo.format(infos);
        }
        return sb.toString();
    }
}
