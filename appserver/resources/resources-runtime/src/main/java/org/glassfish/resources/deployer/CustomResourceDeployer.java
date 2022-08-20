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

import java.util.Collection;
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
import org.glassfish.resources.api.JavaEEResource;
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
public class CustomResourceDeployer implements ResourceDeployer {

    @Inject
    private BindableResourcesHelper bindableResourcesHelper;

    @Inject
    private ResourceNamingService cns;
    /**
     * logger for this deployer
     */
    private static final Logger LOG = LogDomains.getLogger(CustomResourceDeployer.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deployResource(Object resource, String applicationName, String moduleName)
        throws Exception {
        CustomResource customResource = (CustomResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(customResource.getJndiName(), applicationName, moduleName);
        deployResource(resource, resourceInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deployResource(Object resource) throws Exception {
        CustomResource customResource = (CustomResource) resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(customResource);
        deployResource(customResource, resourceInfo);
    }


    private void deployResource(Object resource, ResourceInfo resourceInfo) {
        CustomResource customRes = (CustomResource) resource;

        // converts the config data to j2ee resource
        JavaEEResource j2eeResource = toCustomJavaEEResource(customRes, resourceInfo);

        // installs the resource
        installCustomResource((org.glassfish.resources.beans.CustomResource) j2eeResource, resourceInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        CustomResource customResource = (CustomResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(customResource.getJndiName(), applicationName, moduleName);
        deleteResource(customResource, resourceInfo);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void undeployResource(Object resource) throws Exception {
        CustomResource customResource = (CustomResource) resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(customResource);
        deleteResource(customResource, resourceInfo);
    }


    private void deleteResource(CustomResource customResource, ResourceInfo resourceInfo) throws NamingException {
        // converts the config data to j2ee resource
        // JavaEEResource j2eeResource = toCustomJavaEEResource(customRes, resourceInfo);
        // removes the resource from jndi naming
        cns.unpublishObject(resourceInfo, resourceInfo.getName());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(Object resource) {
        return resource instanceof CustomResource;
    }

    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    @Override
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void redeployResource(Object resource)
            throws Exception {

        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

    /**
     * Installs the given custom resource. It publishes the resource as a
     * javax.naming.Reference with the naming manager (jndi). This method gets
     * called during server initialization and custom resource deployer to
     * handle custom resource events.
     *
     * @param customRes custom resource
     */
    public void installCustomResource(org.glassfish.resources.beans.CustomResource customRes, ResourceInfo resourceInfo) {

        try {
            LOG.log(Level.FINE, "installCustomResource by jndi-name : {0}", resourceInfo);

            // bind a Reference to the object factory
            Reference ref = new Reference(customRes.getResType(), customRes.getFactoryClass(), null);

            // add resource properties as StringRefAddrs
            for (ResourceProperty prop : customRes.getProperties()) {
                ref.add(new StringRefAddr(prop.getName(), prop.getValue()));
            }

            // publish the reference
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
    public static JavaEEResource toCustomJavaEEResource(CustomResource rbean, ResourceInfo resourceInfo) {
        org.glassfish.resources.beans.CustomResource jr
            = new org.glassfish.resources.beans.CustomResource(resourceInfo);

        //jr.setDescription(rbean.getDescription()); // FIXME: getting error

        // sets the enable flag
        jr.setEnabled(Boolean.valueOf(rbean.getEnabled()));

        // sets the resource type
        jr.setResType(rbean.getResType());

        // sets the factory class name
        jr.setFactoryClass(rbean.getFactoryClass());

        // sets the properties
        List<Property> properties = rbean.getProperty();
        if (properties != null) {
            for (Property property : properties) {
                ResourceProperty rp = new ResourcePropertyImpl(property.getName(), property.getValue());
                jr.addProperty(rp);
            }
        }
        return jr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        if (handles(resource)) {
            if (!postApplicationDeployment) {
                return true;
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources)
        throws ResourceConflictException {
        // do nothing.
    }
}
