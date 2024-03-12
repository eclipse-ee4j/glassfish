/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyVetoException;
import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.*;


/**
 * Change the node-agent element to use V3 mechanism
 * @author Carla Mott
 */

@Service
@PerLookup
public class NodeAgentConfigUpgrade implements ConfigurationUpgrade, PostConstruct {
    @Inject
    Domain domain;

    @Inject
    Servers servers;

    public void postConstruct() {

        final NodeAgents nodeAgents = domain.getNodeAgents();
        if (nodeAgents == null) {
            createDefaultNodeList();
            return;
        }

        final List<NodeAgent> agList= nodeAgents.getNodeAgent();
        if (agList.size() == 0) {
            createDefaultNodeList();
            return;
        }
        try {
            ConfigSupport.apply(new SingleConfigCode<Domain>() {
                public Object run(Domain d) throws PropertyVetoException, TransactionFailure {

                    Nodes nodes=d.createChild(Nodes.class);
                    Transaction t = Transaction.getTransaction(d);
                    if (t==null)
                        return null;

                    for( NodeAgent na: agList){
                        String host=null;
                        Node node = nodes.createChild(Node.class);

                        node.setName(na.getName());
                        node.setType("CONFIG");
                        JmxConnector jc = na.getJmxConnector();
                        if (jc != null){
                            List<Property> agentProp =jc.getProperty();  //get the properties and see if host name is specified
                            for ( Property p : agentProp)  {
                                String name = p.getName();
                                if (name.equals("client-hostname")) {
                                    node.setNodeHost(p.getValue()); //create the node with a host name
                                    node.setInstallDir("${com.sun.aas.productRoot}");
                                }
                            }
                        }
                        nodes.getNode().add(node);
                    }

                    // Now add the builtin localhost node
                    createDefaultNode(d, nodes);

                    d.setNodes(nodes);

                    List<Server> serverList=servers.getServer();
                    if (serverList.size() <= 0)
                        return null;

                    for (Server s: serverList){
                        s = t.enroll(s);
                        s.setNodeRef(s.getNodeAgentRef());
                        s.setNodeAgentRef(null);
                    }
                    //remove the node-agent element
//                    d.getNodeAgents().getNodeAgent().clear();
                    d.setNodeAgents(null);
                    return null;
                }

        }, domain);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                "Failure while upgrading node-agent from V2 to V3", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * If the domain.xml has no node agents, then create the default node list
     * with the localhost node.
     */

    private void createDefaultNodeList() {
        try {
            ConfigSupport.apply(new SingleConfigCode<Domain>() {
                public Object run(Domain d) throws PropertyVetoException, TransactionFailure {

                    Nodes nodes=d.createChild(Nodes.class);
                    Transaction t = Transaction.getTransaction(d);
                    if (t==null)
                        return null;

                    createDefaultNode(d, nodes);
                    d.setNodes(nodes);
                    return null;
                }
        }, domain);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                "Failure while creating default localhost node during V2 to V3 upgrade.", e);
            throw new RuntimeException(e);
        }

    }

    private void createDefaultNode(Domain d, Nodes nodes)
            throws TransactionFailure, PropertyVetoException {
        Property domainProp = d.getProperty("administrative.domain.name");
        String domainName = domainProp.getValue();
        Node node = nodes.createChild(Node.class);
        node.setName("localhost" + "-" + domainName);
        node.setType("CONFIG");
        node.setNodeHost("localhost");
        node.setInstallDir("${com.sun.aas.productRoot}");
        nodes.getNode().add(node);
    }
}
