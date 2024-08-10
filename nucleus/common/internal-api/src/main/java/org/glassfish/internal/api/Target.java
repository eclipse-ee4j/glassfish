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

package org.glassfish.internal.api;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

@Service
public class Target {
    @Inject
    private Domain domain;

    @Inject
    private ServerEnvironment serverEnv;

    @Inject
    ServiceLocator habitat;

    /**
     * Lets caller to know if the caller is in DAS
     */
    public boolean isThisDAS() {
        return serverEnv.isDas();
    }

    /**
     * Lets caller to know if the caller is in an instance
     */
    public boolean isThisInstance() {
        return serverEnv.isInstance();
    }

    /**
     * Checks if a given target is cluster or nor
     * @param targetName the name of the target
     * @return true if the target represents a cluster; false otherwise
     */
    public boolean isCluster(String targetName) {
        return (domain.getClusterNamed(targetName) != null);
    }

    /**
     * Returns the Cluster element for a given cluster name
     * @param targetName the name of the target
     * @return Cluster element that represents the cluster
     */
    public Cluster getCluster(String targetName) {
        return domain.getClusterNamed(targetName);
    }

    /**
     * Returns the config element that represents a given cluster
     * @param targetName the name of the target
     * @return Config element representing the cluster
     */
    public Config getClusterConfig(String targetName) {
        Cluster cl = getCluster(targetName);
        if(cl == null)
            return null;
        return(domain.getConfigNamed(cl.getConfigRef()));
    }

    /**
     * Returns config element that represents a given server
     * @param targetName the name of the target
     * @return Config element representing the server instance
     */
    public Config getServerConfig(String targetName) {
        Server s = domain.getServerNamed(targetName);
        if(s == null)
            return null;
        return domain.getConfigNamed(s.getConfigRef());
    }

    /**
     * Given a name (of instance or cluster or config), returns the appropriate Config object
     * @param targetName name of target
     * @return Config element of this target
     */
    public Config getConfig(String targetName) {
        if(CommandTarget.CONFIG.isValid(habitat, targetName))
            return domain.getConfigNamed(targetName);
        if(CommandTarget.DAS.isValid(habitat, targetName))
            return getServerConfig(targetName);
        if(CommandTarget.STANDALONE_INSTANCE.isValid(habitat, targetName))
            return getServerConfig(targetName);
        if(CommandTarget.CLUSTER.isValid(habitat, targetName))
                return getClusterConfig(targetName);
        return null;
    }

    /**
     * Given an instance that is part of a cluster, returns the Cluster element of the cluster to which the
     * given instance belongs
     * @param targetName name of target
     * @return Cluster element to which this instance below
     */
    public Cluster getClusterForInstance(String targetName) {
        return domain.getClusterForInstance(targetName);
    }

    /**
     * Given a list instance names, get List<Server>
     */
    public List<Server> getInstances(List<String> names) {
        List<Server> instances = new ArrayList<Server>();
        for(String aName : names)
            instances.addAll(getInstances(aName));
        return instances;
    }

    public Node getNode(String targetName) {
        return domain.getNodeNamed(targetName);
    }

    /**
     * Given the name of a target, returns a list of Server objects. If given target is a standalone server,
     * then the server's Server element is returned in the list. If the target is a cluster, then the list of Server
     * elements that represent all server instances of that cluster is returned.
     * @param targetName the name of the target
     * @return list of Server elements that represent the target
     */
    public List<Server> getInstances(String targetName) {
        List<Server> instances = new ArrayList<Server>();
        if(CommandTarget.DOMAIN.isValid(habitat, targetName))
            return instances;
        if(CommandTarget.DAS.isValid(habitat, targetName))
            return instances;
        if(CommandTarget.STANDALONE_INSTANCE.isValid(habitat, targetName)) {
            instances.add(domain.getServerNamed(targetName));
        }
        if(CommandTarget.CLUSTER.isValid(habitat, targetName)) {
            instances = getCluster(targetName).getInstances();
        }
        if(CommandTarget.CONFIG.isValid(habitat, targetName)) {
            List<String> targets = domain.getAllTargets();
            for(String aTarget : targets) {
                if(CommandTarget.CLUSTER.isValid(habitat, aTarget) &&
                        getCluster(aTarget).getConfigRef().equals(targetName)) {
                    instances.addAll(getCluster(aTarget).getInstances());
                }
                if(CommandTarget.STANDALONE_INSTANCE.isValid(habitat, aTarget) &&
                        domain.getServerNamed(aTarget).getConfigRef().equals(targetName)) {
                    instances.add(domain.getServerNamed(aTarget));
                }
            }
        }
        if(CommandTarget.NODE.isValid(habitat, targetName)) {
            List<Server> allInstances = getAllInstances();
            for(Server s : allInstances) {
                if(targetName.equals(s.getNodeRef()))
                    instances.add(s);
            }
        }
        return instances;
    }

    /**
     * Gets all instances present in the domain
     * @return list of Server elements that represent all instances
     */
    public List<Server> getAllInstances() {
        List<Server> list = new ArrayList<Server>();
        for(Server s : domain.getServers().getServer()) {
            if(!CommandTarget.DAS.isValid(habitat, s.getName())) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Given name of a target verifies if it is valid
     * @param targetName name of the target
     * @return true if the target is a valid cluster or server instance or a config
     */
    public boolean isValid(String targetName) {
        if(isCluster(targetName))
            return true;
        if(getInstances(targetName).size() != 0)
            return true;
        if(domain.getConfigNamed(targetName) != null)
            return true;
        return false;
    }
}
