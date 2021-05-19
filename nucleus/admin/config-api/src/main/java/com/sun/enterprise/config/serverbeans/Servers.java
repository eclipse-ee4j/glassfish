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

package com.sun.enterprise.config.serverbeans;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.Create;
import org.glassfish.config.support.Delete;
import org.glassfish.config.support.TypeAndNameResolver;
import org.glassfish.config.support.TypeResolver;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.DuckTyped;

import java.util.List;
import java.util.ArrayList;

/**
 * List of configured servers.
 */
@Configured
public interface Servers extends ConfigBeanProxy {

    /**
     * Return the list of currently configured server. Servers can be added or removed by using the returned
     * {@link java.util.List} instance
     *
     * @return the list of configured {@link Server}
     */
    @Element
    // example below on how to annotate a CRUD command with cluster specific data.
    @Create(value = "_register-instance", resolver = TypeResolver.class, decorator = Server.CreateDecorator.class, cluster = @org.glassfish.api.admin.ExecuteOn(value = RuntimeType.DAS), i18n = @I18n("_register.instance.command"))
    @Delete(value = "_unregister-instance", resolver = TypeAndNameResolver.class, decorator = Server.DeleteDecorator.class, cluster = @org.glassfish.api.admin.ExecuteOn(value = {
            RuntimeType.DAS, RuntimeType.INSTANCE }), i18n = @I18n("_unregister.instance.command"))
    public List<Server> getServer();

    /**
     * Return the server with the given name, or null if no such server exists.
     *
     * @param name the name of the server
     * @return the Server object, or null if no such server
     */
    @DuckTyped
    public Server getServer(String name);

    /**
     * Return the list of Servers that reference a Node
     *
     * @param node Node to get servers that reference
     * @return List of Server objects that reference the passed node. List will be of length 0 if no servers reference node.
     */
    @DuckTyped
    public List<Server> getServersOnNode(Node node);

    class Duck {
        public static Server getServer(Servers instance, String name) {
            for (Server server : instance.getServer()) {
                if (server.getName().equals(name)) {
                    return server;
                }
            }
            return null;
        }

        public static List<Server> getServersOnNode(Servers servers, Node node) {
            List<Server> serverList = servers.getServer();
            List<Server> serverListOnNode = new ArrayList<Server>();
            Server instance = null;
            String nodeName = node.getName();
            if (serverList.size() > 0) {
                for (Server server : serverList) {
                    if (nodeName.equals(server.getNodeRef())) {
                        serverListOnNode.add(server);
                    }
                }
            }
            return serverListOnNode;
        }

    }
}
