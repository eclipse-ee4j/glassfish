/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.mail.deployer;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.enterprise.repository.ResourceProperty;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.mail.Session;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.glassfish.resources.api.GlobalResourceDeployer;
import org.glassfish.resources.mail.config.MailResource;
import org.glassfish.resources.mail.naming.MailNamingObjectFactory;
import org.glassfish.resources.naming.SerializableObjectRefAddr;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * Handles mail resource events in the server instance.
 * <p/>
 * The mail resource events from the admin instance are propagated
 * to this object.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author James Kong
 * @since JDK1.4
 */
@Service
@ResourceDeployerInfo(MailResource.class)
@Singleton
public class MailResourceDeployer extends GlobalResourceDeployer implements ResourceDeployer {

    private static final Logger LOG = LogDomains.getLogger(MailResourceDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private ResourceNamingService namingService;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        MailResource mailRes = (MailResource) resource;
        if (mailRes == null) {
            LOG.log(Level.INFO, "Error in resource deploy.");
        } else {
            ResourceInfo resourceInfo = new ResourceInfo(mailRes.getJndiName(), applicationName, moduleName);
            //registers the jsr77 object for the mail resource deployed
            /* TODO Not needed any more ?
            /*ManagementObjectManager mgr =
                getAppServerSwitchObject().getManagementObjectManager();
            mgr.registerMailResource(mailRes.getJndiName());*/

            installResource(mailRes, resourceInfo);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deployResource(Object resource) throws Exception {
        MailResource mailResource =
                (MailResource) resource;
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(mailResource);
        deployResource(resource, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }

    /**
     * Local method for calling the ResourceInstaller for installing
     * mail resource in runtime.
     *
     * @param mailResource The mail resource to be installed.
     * @throws Exception when not able to create a resource
     */
    void installResource(MailResource mailResource,
                         ResourceInfo resourceInfo) throws Exception {
        // Converts the config data to j2ee resource ;
        // retieves the resource installer ; installs the resource ;
        // and adds it to a collection in the installer
        org.glassfish.resources.api.JavaEEResource j2eeRes = toMailJavaEEResource(mailResource, resourceInfo);
        //ResourceInstaller installer = runtime.getResourceInstaller();
        installMailResource((org.glassfish.resources.mail.beans.MailResource) j2eeRes, resourceInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        MailResource mailRes =
                (MailResource) resource;
        // converts the config data to j2ee resource
        ResourceInfo resourceInfo = new ResourceInfo(mailRes.getJndiName(), applicationName, moduleName);
        deleteResource(mailRes, resourceInfo);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void undeployResource(Object resource) throws Exception {
        MailResource mailRes =
                (MailResource) resource;
        // converts the config data to j2ee resource
        ResourceInfo resourceInfo = ResourceUtil.getResourceInfo(mailRes);
        deleteResource(mailRes, resourceInfo);
    }

    private void deleteResource(MailResource mailRes, ResourceInfo resourceInfo)
            throws NamingException {
        //JavaEEResource javaEEResource = toMailJavaEEResource(mailRes, resourceInfo);
        // removes the resource from jndi naming
        namingService.unpublishObject(resourceInfo, mailRes.getJndiName());

        /* TODO Not needed any more ?
            ManagementObjectManager mgr =
                    getAppServerSwitchObject().getManagementObjectManager();
            mgr.unregisterMailResource(mailRes.getJndiName());
        */

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
    public boolean handles(Object resource) {
        return resource instanceof MailResource;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
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
     * Installs the given mail resource. This method gets called during server
     * initialization and from mail resource deployer to handle resource events.
     *
     * @param mailResource mail resource
     */
    public void installMailResource(org.glassfish.resources.mail.beans.MailResource mailResource, ResourceInfo resourceInfo) {
        try {
            MailConfiguration config = new MailConfiguration(mailResource);
            Reference ref = new Reference(Session.class.getName(), MailNamingObjectFactory.class.getName(), null);
            SerializableObjectRefAddr serializableRefAddr = new SerializableObjectRefAddr("jndiName", config);
            ref.add(serializableRefAddr);

            // Publish the object
            namingService.publishObject(resourceInfo, ref, true);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "mailrsrc.create_obj_error", resourceInfo);
            LOG.log(Level.SEVERE, "mailrsrc.create_obj_error_excp", ex);
        }
    }

    /**
     * Returns a new instance of j2ee mail resource from the given config bean.
     * <p/>
     * This method gets called from the mail resource deployer to convert mail
     * config bean into mail j2ee resource.
     *
     * @param mailResourceConfig mail-resource config bean
     * @return a new instance of j2ee mail resource
     */
    public static org.glassfish.resources.api.JavaEEResource toMailJavaEEResource(
            MailResource mailResourceConfig, ResourceInfo resourceInfo) {

        org.glassfish.resources.mail.beans.MailResource mailResource
                = new org.glassfish.resources.mail.beans.MailResource(resourceInfo);

        //jr.setDescription(rbean.getDescription()); // FIXME: getting error
        mailResource.setEnabled(Boolean.valueOf(mailResourceConfig.getEnabled()));
        mailResource.setStoreProtocol(mailResourceConfig.getStoreProtocol());
        mailResource.setStoreProtocolClass(mailResourceConfig.getStoreProtocolClass());
        mailResource.setTransportProtocol(mailResourceConfig.getTransportProtocol());
        mailResource.setTransportProtocolClass(mailResourceConfig.getTransportProtocolClass());
        mailResource.setMailHost(mailResourceConfig.getHost());
        mailResource.setUsername(mailResourceConfig.getUser());
        mailResource.setMailFrom(mailResourceConfig.getFrom());
        mailResource.setDebug(Boolean.valueOf(mailResourceConfig.getDebug()));

        // sets the properties
        List<Property> properties = mailResourceConfig.getProperty();
        if (properties != null) {
            for (Property property : properties) {
                ResourceProperty rp = new org.glassfish.resources.api.ResourcePropertyImpl(property.getName(), property.getValue());
                mailResource.addProperty(rp);
            }
        }
        return mailResource;
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
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
                                          Resources allResources)
            throws ResourceConflictException {
        //do nothing.
    }
}
