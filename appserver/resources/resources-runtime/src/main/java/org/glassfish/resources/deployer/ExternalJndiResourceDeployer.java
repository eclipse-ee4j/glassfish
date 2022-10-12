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
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.InitialContextFactory;

import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.glassfish.resourcebase.resources.util.BindableResourcesHelper;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.glassfish.resources.api.JavaEEResource;
import org.glassfish.resources.api.ResourcePropertyImpl;
import org.glassfish.resources.config.ExternalJndiResource;
import org.glassfish.resources.naming.JndiProxyObjectFactory;
import org.glassfish.resources.naming.ProxyRefAddr;
import org.glassfish.resources.naming.SerializableObjectRefAddr;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * Handles external-jndi resource events in the server instance.
 * <p/>
 * The external-jndi resource events from the admin instance are propagated to this object.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author Nazrul Islam
 * @since JDK1.4
 */
@Service
@Singleton
@ResourceDeployerInfo(ExternalJndiResource.class)
public class ExternalJndiResourceDeployer implements ResourceDeployer {

    private static final Logger LOG = LogDomains.getLogger(ExternalJndiResourceDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private ResourceNamingService namingService;
    @Inject
    private BindableResourcesHelper bindableResourcesHelper;

    @Override
    public synchronized void deployResource(Object resource, String applicationName, String moduleName)
        throws Exception {
        ExternalJndiResource jndiRes = (ExternalJndiResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(jndiRes.getJndiName(), applicationName, moduleName);
        createExternalJndiResource(jndiRes, resourceInfo);
    }


    @Override
    public synchronized void deployResource(Object resource) throws Exception {
        ExternalJndiResource jndiRes = (ExternalJndiResource) resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(jndiRes);
        createExternalJndiResource(jndiRes, resourceInfo);
    }


    private void createExternalJndiResource(ExternalJndiResource jndiRes, ResourceInfo resourceInfo) {
        JavaEEResource j2eeRes = toExternalJndiJavaEEResource(jndiRes, resourceInfo);
        installExternalJndiResource((org.glassfish.resources.beans.ExternalJndiResource) j2eeRes, resourceInfo);
    }


    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ExternalJndiResource jndiRes = (ExternalJndiResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(jndiRes.getJndiName(), applicationName, moduleName);
        deleteResource(jndiRes, resourceInfo);
    }


    @Override
    public synchronized void undeployResource(Object resource) throws Exception {
        ExternalJndiResource jndiRes = (ExternalJndiResource) resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(jndiRes);
        deleteResource(jndiRes, resourceInfo);
    }


    private void deleteResource(ExternalJndiResource jndiResource, ResourceInfo resourceInfo) {
        JavaEEResource j2eeResource = toExternalJndiJavaEEResource(jndiResource, resourceInfo);
        uninstallExternalJndiResource(j2eeResource, resourceInfo);
    }

    @Override
    public synchronized void redeployResource(Object resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof ExternalJndiResource;
    }

    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    @Override
    public Class<?>[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }


    @Override
    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    @Override
    public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

    /**
     * Installs the given external jndi resource. This method gets called
     * during server initialization and from external jndi resource
     * deployer to handle resource events.
     *
     * @param extJndiRes external jndi resource
     */
    public void installExternalJndiResource(org.glassfish.resources.beans.ExternalJndiResource extJndiRes, ResourceInfo resourceInfo) {

        try {
            // create the external JNDI factory, its initial context and
            // pass them as references.
            String factoryClass = extJndiRes.getFactoryClass();
            String jndiLookupName = extJndiRes.getJndiLookupName();

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "installExternalJndiResources resourceName "
                        + resourceInfo + " factoryClass " + factoryClass
                        + " jndiLookupName = " + jndiLookupName);
            }


            Object factory = ResourceUtil.loadObject(factoryClass);
            if (factory == null) {
                LOG.log(Level.WARNING, "jndi.factory_load_error", factoryClass);
                return;

            } else if (!(factory instanceof javax.naming.spi.InitialContextFactory)) {
                LOG.log(Level.WARNING, "jndi.factory_class_unexpected", factoryClass);
                return;
            }

            // Get properties to create the initial naming context
            // for the target JNDI factory
            Hashtable<String, String> env = new Hashtable();
            for (ResourceProperty prop : extJndiRes.getProperties()) {
                env.put(prop.getName(), prop.getValue());
            }

            Context context = null;
            try {
                context = ((InitialContextFactory) factory).getInitialContext(env);
            } catch (NamingException ne) {
                LOG.log(Level.SEVERE, "jndi.initial_context_error", factoryClass);
                LOG.log(Level.SEVERE, "jndi.initial_context_error_excp", ne.getMessage());
            }

            if (context == null) {
                LOG.log(Level.SEVERE, "jndi.factory_create_error", factoryClass);
                return;
            }

            // Bind a Reference to the proxy object factory; set the
            // initial context factory.
            //JndiProxyObjectFactory.setInitialContext(bindName, context);

            Reference ref = new Reference(extJndiRes.getResType(),
                    "org.glassfish.resources.naming.JndiProxyObjectFactory",
                    null);

            // unique JNDI name within server runtime
            ref.add(new SerializableObjectRefAddr("resourceInfo", resourceInfo));

            // target JNDI name
            ref.add(new StringRefAddr("jndiLookupName", jndiLookupName));

            // target JNDI factory class
            ref.add(new StringRefAddr("jndiFactoryClass", factoryClass));

            // add Context info as a reference address
            ref.add(new ProxyRefAddr(extJndiRes.getResourceInfo().getName(), env));

            // Publish the reference
            namingService.publishObject(resourceInfo, ref, true);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "customrsrc.create_ref_error", resourceInfo);
            LOG.log(Level.SEVERE, "customrsrc.create_ref_error_excp", ex);
        }
    }

    /**
     * Un-installs the external jndi resource.
     *
     * @param resource external jndi resource
     */
    public void uninstallExternalJndiResource(JavaEEResource resource, ResourceInfo resourceInfo) {

        // removes the jndi context from the factory cache
        //String bindName = resource.getResourceInfo().getName();
        JndiProxyObjectFactory.removeInitialContext(resource.getResourceInfo());

        // removes the resource from jndi naming
        try {
            namingService.unpublishObject(resourceInfo, resourceInfo.getName());
            /* TODO V3 handle jms later
            //START OF IASRI 4660565
            if (((ExternalJndiResource)resource).isJMSConnectionFactory()) {
                nm.unpublishObject(IASJmsUtil.getXAConnectionFactoryName(resourceName));
            }
            //END OF IASRI 4660565
            */
        } catch (javax.naming.NamingException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Error while unpublishing resource: " + resourceInfo, e);
            }
        }
    }


    /**
     * Returns a new instance of j2ee external jndi resource from the given
     * config bean.
     * <p/>
     * This method gets called from the external resource
     * deployer to convert external-jndi-resource config bean into
     * external-jndi j2ee resource.
     *
     * @param rbean external-jndi-resource config bean
     * @return a new instance of j2ee external jndi resource
     */
    public static JavaEEResource toExternalJndiJavaEEResource(ExternalJndiResource rbean, ResourceInfo resourceInfo) {
        org.glassfish.resources.beans.ExternalJndiResource jr = new org.glassfish.resources.beans.ExternalJndiResource(
            resourceInfo);

        //jr.setDescription( rbean.getDescription() ); // FIXME: getting error

        // sets the enable flag
        jr.setEnabled(Boolean.valueOf(rbean.getEnabled()));

        // sets the jndi look up name
        jr.setJndiLookupName(rbean.getJndiLookupName());

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

    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        return handles(resource) && !postApplicationDeployment;
    }

    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
        // do nothing.
    }
}
