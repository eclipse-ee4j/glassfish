/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.deployer;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.repository.ResourceProperty;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.glassfish.resources.api.ResourcePropertyImpl;
import org.glassfish.resources.config.CustomResource;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * Handles custom resource events in the server instance.
 * <p/>
 * The custom resource events from the admin instance are propagated
 * to this object.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 * <p/>
 * <p/>
 * Note: Since a notification is not sent to the user of the custom
 * resources upon undeploy, it is possible that there would be
 * stale objects not being garbage collected. Future versions
 * should take care of this problem.
 *
 * @author Nazrul Islam
 * @since JDK1.4
 */
@Service
@ResourceDeployerInfo(CustomResource.class)
@Singleton
public class CustomResourceDeployer implements ResourceDeployer<CustomResource> {

    @Inject
    private BindableResourcesHelper bindableResourcesHelper;
    @Inject
    private ResourceNamingService cns;

    private static final Logger LOG = LogDomains.getLogger(CustomResourceDeployer.class, LogDomains.RSR_LOGGER);

    @Override
    public synchronized void deployResource(CustomResource resource, String applicationName, String moduleName)
        throws Exception {
        ResourceInfo resourceInfo = new ResourceInfo(resource.getJndiName(), applicationName, moduleName);
        deployResource(resource, resourceInfo);
    }


    @Override
    public synchronized void deployResource(CustomResource resource) throws Exception {
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(resource);
        deployResource(resource, resourceInfo);
    }


    private void deployResource(CustomResource resource, ResourceInfo resourceInfo) {
        org.glassfish.resources.beans.CustomResource j2eeResource = toCustomJavaEEResource(resource, resourceInfo);
        installCustomResource(j2eeResource, resourceInfo);
    }


    @Override
    public void undeployResource(CustomResource resource, String applicationName, String moduleName) throws Exception {
        ResourceInfo resourceInfo = new ResourceInfo(resource.getJndiName(), applicationName, moduleName);
        deleteResource(resource, resourceInfo);
    }


    @Override
    public synchronized void undeployResource(CustomResource resource) throws Exception {
        CustomResource customResource = resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(customResource);
        deleteResource(customResource, resourceInfo);
    }


    private void deleteResource(CustomResource customResource, ResourceInfo resourceInfo) throws NamingException {
        cns.unpublishObject(resourceInfo, resourceInfo.getName());
    }


    @Override
    public boolean handles(Object resource) {
        return resource instanceof CustomResource;
    }


    @Override
    public synchronized void enableResource(CustomResource resource) throws Exception {
        deployResource(resource);
    }


    @Override
    public synchronized void disableResource(CustomResource resource) throws Exception {
        undeployResource(resource);
    }


    /**
     * Installs the given custom resource. It publishes the resource as a
     * {@link javax.naming.Reference} with the naming manager (jndi).
     * This method gets called during server initialization and custom resource deployer to
     * handle custom resource events.
     *
     * @param customRes custom resource
     */
    public void installCustomResource(org.glassfish.resources.beans.CustomResource customRes, ResourceInfo resourceInfo) {
        try {
            LOG.log(Level.FINE, "installCustomResource by jndi-name : {0}", resourceInfo);
            Reference ref = new Reference(customRes.getResType(), customRes.getFactoryClass(), null);
            for (ResourceProperty prop : customRes.getProperties()) {
                ref.add(new StringRefAddr(prop.getName(), prop.getValue()));
            }
            cns.publishObject(resourceInfo, ref, true);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "customrsrc.create_ref_error", resourceInfo);
            LOG.log(Level.SEVERE, "customrsrc.create_ref_error_excp", ex);
        }
    }


    /**
     * Returns a new instance of j2ee custom resource from the given
     * config bean.
     * <p/>
     * This method gets called from the custom resource deployer
     * to convert custom-resource config bean into custom j2ee resource.
     *
     * @param rbean custom-resource config bean
     * @return new instance of j2ee custom resource
     */
    private static org.glassfish.resources.beans.CustomResource toCustomJavaEEResource(CustomResource rbean, ResourceInfo resourceInfo) {
        org.glassfish.resources.beans.CustomResource jr = new org.glassfish.resources.beans.CustomResource(resourceInfo);
        jr.setEnabled(Boolean.valueOf(rbean.getEnabled()));
        jr.setResType(rbean.getResType());
        jr.setFactoryClass(rbean.getFactoryClass());
        List<Property> properties = rbean.getProperty();
        if (properties != null) {
            for (Property property : properties) {
                ResourceProperty rp = new ResourcePropertyImpl(property.getName(), property.getValue());
                jr.addProperty(rp);
            }
        }
        return jr;
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
        // do nothing.
    }
}
