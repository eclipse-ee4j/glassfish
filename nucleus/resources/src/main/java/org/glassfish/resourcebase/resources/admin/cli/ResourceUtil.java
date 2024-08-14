/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resourcebase.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * @author Jagadish Ramu
 */
@Service
public class ResourceUtil {

    private static final Logger LOG = System.getLogger(ResourceUtil.class.getName());

    @Inject
    private Provider<Target> targetProvider;
    @Inject
    private Domain domain;
    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    public void createResourceRef(String name, String enabled, String target) throws TransactionFailure {
        LOG.log(Level.DEBUG, "createResourceRef(name={0}, enabled={1}, target={2})", name, enabled, target);
        if (CommandTarget.TARGET_DOMAIN.equals(target)) {
            throw new IllegalArgumentException("Target " + target + " is not allowed for creating resource refs!");
        }
        SimpleJndiName jndiName = SimpleJndiName.of(name);
        Config config = domain.getConfigNamed(target);
        if (config != null) {
            if (!config.isResourceRefExists(jndiName)) {
                config.createResourceRef(enabled, jndiName);
            }
        }

        Server server = configBeansUtilities.getServerNamed(target);
        if (server != null) {
            if (!server.isResourceRefExists(jndiName)) {
                // create new ResourceRef as a child of Server
                server.createResourceRef(enabled, jndiName);
            }
        } else {
            Cluster cluster = domain.getClusterNamed(target);
            if(cluster != null){
                if (!cluster.isResourceRefExists(jndiName)) {
                    // create new ResourceRef as a child of Cluster
                    cluster.createResourceRef(enabled, jndiName);

                    // create new ResourceRef for all instances of Cluster
                    Target tgt = targetProvider.get();
                    List<Server> instances = tgt.getInstances(target);
                    for (Server svr : instances) {
                        if (!svr.isResourceRefExists(jndiName)) {
                            svr.createResourceRef(enabled, jndiName);
                        }
                    }
                }
            }
        }
    }

    /**
     * When <i>enabled=false</i> for <i>create-***-resource</i> (a resource that will have <i>resource-ref</i>)
     * and the --target is not <i>domain</i> or <i>config</i>, <i>enabled</i> value for <i>resource</i>
     * should be true and <i>enabled</i> value for the <i>resource-ref</i> should be false.
     * @param enabledValue enabled
     * @param target target
     * @return computed value for <i>enabled</i>
     */
    public String computeEnabledValueForResourceBasedOnTarget(String enabledValue, String target) {
        boolean enabled = Boolean.parseBoolean(enabledValue);
        if (!isNonResourceRefTarget(target) && !enabled) {
            return Boolean.toString(!enabled);
        }
        return enabledValue;
    }


    /**
     * Determines whether the target is of type "domain" or "config"
     * where resource-ref will not be created.
     *
     * @param target target-name
     * @return boolean
     */
    private boolean isNonResourceRefTarget(String target){
        if (CommandTarget.TARGET_DOMAIN.equals(target)) {
            return true;
        }
        if (domain.getConfigNamed(target) != null) {
            return true;
        }
        return false;
    }


    /**
     * @param refName must not be null.
     */
    public boolean isResourceRefInTarget(SimpleJndiName refName, String target){
        Set<String> targets = getTargetsReferringResourceRef(refName);
        for (String refTarget : targets) {
            if (refTarget.equals(target)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param refName must not be null.
     */
    public Set<String> getTargetsReferringResourceRef(SimpleJndiName refName) {
        Set<String> targets = new HashSet<>();
        List<Server> servers = domain.getServers().getServer();
        for (Server server : servers) {
            if (server.getResourceRef(refName) != null) {
                if (server.getCluster() != null) {
                    targets.add(server.getCluster().getName());
                } else if (server.isDas()) {
                    targets.add(CommandTarget.TARGET_SERVER);
                } else if (server.isInstance()) {
                    targets.add(server.getName());
                }
            }
        }
        return targets;
    }


    public void deleteResourceRef(SimpleJndiName jndiName, String target) throws TransactionFailure {
        LOG.log(Level.DEBUG, "deleteResourceRef(jndiName={0}, target={1})", jndiName, target);
        if (CommandTarget.TARGET_DOMAIN.equals(target)) {
            throw new IllegalArgumentException("Target " + target + " is not allowed for deleting resource refs!");
        }
        Config config = domain.getConfigNamed(target);
        if (config != null) {
            config.deleteResourceRef(jndiName);
        } else {
            Server server = configBeansUtilities.getServerNamed(target);
            if (server != null) {
                if (server.isResourceRefExists(jndiName)) {
                    // delete ResourceRef for Server
                    server.deleteResourceRef(jndiName);
                }
            } else {
                Cluster cluster = domain.getClusterNamed(target);
                if (cluster != null) {
                    if (cluster.isResourceRefExists(jndiName)) {
                        // delete ResourceRef of Cluster
                        cluster.deleteResourceRef(jndiName);

                        // delete ResourceRef for all instances of Cluster
                        Target tgt = targetProvider.get();
                        List<Server> instances = tgt.getInstances(target);
                        for (Server svr : instances) {
                            if (svr.isResourceRefExists(jndiName)) {
                                svr.deleteResourceRef(jndiName);
                            }
                        }
                    }
                }
            }
        }
    }
}
