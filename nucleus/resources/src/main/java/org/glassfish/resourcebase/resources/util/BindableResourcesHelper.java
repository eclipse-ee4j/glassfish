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

package org.glassfish.resourcebase.resources.util;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Service;


/**
 * @author Jagadish Ramu
 */
@Service
public class BindableResourcesHelper {

    @LogMessagesResourceBundle
    public static final String LOGMESSAGE_RESOURCE = "org.glassfish.resourcebase.resources.LogMessages";

    @LoggerInfo(subsystem="RESOURCE", description="Nucleus Resource", publish=true)
    public static final String LOGGER = "jakarta.enterprise.resources.util";
    private static final Logger LOG = Logger.getLogger(LOGGER, LOGMESSAGE_RESOURCE);
    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(
        BindableResourcesHelper.class);
    private static final String DOMAIN = "domain";

    @Inject
    ServiceLocator habitat;

    @Inject
    private ServerEnvironment environment;

    private Server server;


    public boolean resourceExists(String jndiName, String target) {
        boolean exists = false;
        Domain domain = habitat.getService(Domain.class);
        if (target.equals(DOMAIN)) {
            // if target is "domain", as long as the resource is present in "resources" section,
            // it is valid.
            exists = true;
        } else {
            SimpleJndiName simpleJndiName = SimpleJndiName.of(jndiName);
            if (habitat.<ConfigBeansUtilities> getService(ConfigBeansUtilities.class)
                .getServerNamed(target) != null) {
                Server server = habitat.<ConfigBeansUtilities> getService(ConfigBeansUtilities.class)
                    .getServerNamed(target);
                exists = server.isResourceRefExists(simpleJndiName);
            } else if (domain.getClusterNamed(target) != null) {
                Cluster cluster = domain.getClusterNamed(target);
                exists = cluster.isResourceRefExists(simpleJndiName);
            } else {
                // if target is "CONFIG", as long as the resource is present in "resources" section,
                // it is valid.
                for (Config config : domain.getConfigs().getConfig()) {
                    if (config.getName().equals(target)) {
                        exists = true;
                        break;
                    }
                }
            }
        }
        return exists;
    }


    /**
     * Checks whether duplicate resource exists or resource is already created but not resource-ref
     * or resource-ref already exists.
     *
     * @param resources resources
     * @param jndiName resource-name
     * @param validateResourceRef whether to validate resource-ref
     * @param target target instance/cluster/domain
     * @param resourceTypeToValidate type of resource
     * @return ResourceStatus indicating Success or Failure
     */
    public ResourceStatus validateBindableResourceForDuplicates(Resources resources, final String jndiName,
                                                          boolean validateResourceRef, String target,
                                                          Class<? extends BindableResource> resourceTypeToValidate){
        // ensure we don't already have one of this name
        SimpleJndiName simpleJndiName = SimpleJndiName.of(jndiName);
        BindableResource duplicateResource = ResourceUtil.getBindableResourceByName(resources, jndiName);
        if (duplicateResource == null) {
            return new ResourceStatus(ResourceStatus.SUCCESS, "Validation Successful");
        }
        final String msg;
        if (validateResourceRef && getResourceByClass(duplicateResource).equals(resourceTypeToValidate)) {
            if ("domain".equals(target)) {
                msg = localStrings.getLocalString("duplicate.resource.found", "A {0} by name {1} already exists.",
                    getResourceTypeName(duplicateResource), jndiName);
            } else if (habitat.<org.glassfish.resourcebase.resources.admin.cli.ResourceUtil> getService(
                org.glassfish.resourcebase.resources.admin.cli.ResourceUtil.class)
                .getTargetsReferringResourceRef(simpleJndiName).contains(target)) {
                msg = localStrings.getLocalString("duplicate.resource.found.in.target",
                    "A {0} by name {1} already exists with resource-ref in target {2}.",
                    getResourceTypeName(duplicateResource), jndiName, target);
            } else {
                msg = localStrings.getLocalString("duplicate.resource.need.to.create.resource.ref",
                    "A {0} named {1} already exists. If you are trying to create the existing resource"
                        + "configuration in target {2}, please use 'create-resource-ref' command "
                        + "(or create resource-ref using admin console).",
                    getResourceTypeName(duplicateResource), jndiName, target);
            }
        } else {
            msg = localStrings.getLocalString("duplicate.resource.found", "A {0} by name {1} already exists.",
                getResourceTypeName(duplicateResource), jndiName);
        }
        return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
    }


    @SuppressWarnings("unchecked")
    public Class<? extends BindableResource> getResourceByClass(BindableResource resource) {
        Class<? extends BindableResource> type = BindableResource.class;
        if (Proxy.isProxyClass(resource.getClass())) {
            Class<?>[] interfaces = resource.getClass().getInterfaces();
            if (interfaces != null) {
                for (Class<?> clz : interfaces) {
                    if (BindableResource.class.isAssignableFrom(clz)) {
                        return (Class<? extends BindableResource>) clz;
                    }
                }
            }
        }
        return type;
    }


    public String getResourceTypeName(BindableResource resource){
        Class<?> resourceType = getResourceByClass(resource);
        return resourceType == null ? "Resource" : resourceType.getSimpleName();
    }

    public boolean isBindableResourceEnabled(BindableResource br){
        boolean resourceRefEnabled = false;
        ResourceRef ref = getServer().getResourceRef(SimpleJndiName.of(br.getJndiName()));
        if (ref == null) {
            LOG.log(Level.FINEST, "ResourceRef {0} was not found.", br.getJndiName());
        } else {
            resourceRefEnabled = Boolean.parseBoolean(ref.getEnabled());
        }
        boolean resourceEnabled = Boolean.parseBoolean(br.getEnabled());
        return resourceEnabled && resourceRefEnabled;
    }

    private Server getServer(){
        if(server == null){
            server = habitat.<Domain>getService(Domain.class).getServerNamed(environment.getInstanceName());
        }
        return server;
    }

}
