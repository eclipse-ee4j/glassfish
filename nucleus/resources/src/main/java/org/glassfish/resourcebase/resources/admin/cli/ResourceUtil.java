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

package org.glassfish.resourcebase.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.TransactionFailure;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jagadish Ramu
 */
@Service
public class ResourceUtil {

    private static final String DOMAIN = "domain";

    @Inject
    private Provider<Target> targetProvider;

    @Inject
    private Domain domain;
    
    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    //to initialize config-bean-utils in mvn test mode.
    @Inject
    private ConfigBeansUtilities configBeanUtilities;

    public void createResourceRef(String jndiName, String enabled, String target) throws TransactionFailure {

        if (target.equals(DOMAIN)) {
            return;
        }

        Config config = domain.getConfigNamed(target);
        if( config != null){
            if(!config.isResourceRefExists(jndiName)) {
                config.createResourceRef(enabled,jndiName);
            }
            //return;
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
        String result = enabledValue;
        boolean enabled = Boolean.valueOf(enabledValue);
        if(!isNonResourceRefTarget(target) && !enabled ){
            result = Boolean.toString(!enabled);
        }
        return result;
    }

    /**
     * Determines whether the target is of type "domain" or "config"
     * where resource-ref will not be created.
     * @param target target-name
     * @return boolean
     */
    private boolean isNonResourceRefTarget(String target){
        boolean isNonResourceRefTarget = false;
        if(DOMAIN.equals(target)){
            isNonResourceRefTarget = true;
        }else{
            if(domain.getConfigNamed(target)!=null){
                isNonResourceRefTarget = true;
            }
        }
        return isNonResourceRefTarget;
    }

    public boolean isResourceRefInTarget(String refName, String target){
        Set<String> targets = getTargetsReferringResourceRef(refName);
        boolean resourceRefInTarget = false;
        for(String refTarget : targets){
            if(refTarget.equals(target)){
                resourceRefInTarget = true;
                break;
            }
        }
        return resourceRefInTarget;
    }


    public Set<String> getTargetsReferringResourceRef(String refName) {
        Set<String> targets = new HashSet<String>();
        List<Server> servers = domain.getServers().getServer();
        for(Server server: servers){
            if(server.getResourceRef(refName) != null){
                if(server.getCluster() != null){
                    targets.add(server.getCluster().getName());
                }else if(server.isDas()){
                    targets.add(SystemPropertyConstants.DAS_SERVER_NAME);
                }else if(server.isInstance()){
                    targets.add(server.getName());
                }
            }
        }
        return targets;
    }


    public void deleteResourceRef(String jndiName, String target) throws TransactionFailure {

        if (target.equals(DOMAIN)) {
            return;
        }

        Config config = domain.getConfigNamed(target);
        if(config!=null) {
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
                if(cluster != null){
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
