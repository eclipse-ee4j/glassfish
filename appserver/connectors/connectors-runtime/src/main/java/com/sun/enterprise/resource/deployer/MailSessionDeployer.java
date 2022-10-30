/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.deployer;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.MailSessionDescriptor;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.javaee.services.CommonResourceProxy;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resources.mail.config.MailResource;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.deriveResourceName;

/**
 * Handle deployment of resources defined by @MailSessionDefinition
 * and represented by a MailSessionDescriptor.
 */

@Service
@ResourceDeployerInfo(MailSessionDescriptor.class)
public class MailSessionDeployer implements ResourceDeployer<MailSessionDescriptor> {

    private static final Logger LOG = LogDomains.getLogger(MailSessionDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private Provider<org.glassfish.resourcebase.resources.util.ResourceManagerFactory> resourceManagerFactoryProvider;
    @Inject
    private Provider<CommonResourceProxy> mailSessionProxyProvider;
    @Inject
    private Provider<org.glassfish.resourcebase.resources.naming.ResourceNamingService> resourceNamingServiceProvider;
    @Inject
    private ConnectorRuntime runtime;


    @Override
    public void deployResource(MailSessionDescriptor resource, String applicationName, String moduleName) throws Exception {
        //do nothing
    }

    @Override
    public void deployResource(MailSessionDescriptor resource) throws Exception {
        SimpleJndiName resourceName = deriveResourceName(resource.getResourceId(), resource.getJndiName(), resource.getResourceType());
        MailResource mailResource = new MyMailResource(resource, resourceName);
        getDeployer(mailResource).deployResource(mailResource);
        LOG.log(Level.FINE, "Mail-Session resource is deployed having resource-name [" + resource.getName() + "]");

    }

    @Override
    public void undeployResource(MailSessionDescriptor resource) throws Exception {
        SimpleJndiName resourceName = deriveResourceName(resource.getResourceId(), resource.getJndiName(), resource.getResourceType());
        MailResource mailResource = new MyMailResource(resource, resourceName);
        getDeployer(mailResource).undeployResource(mailResource);
        LOG.log(Level.FINE, "Mail-Session resource is undeployed having resource-name [" + resource.getName() + "]");
    }

    @Override
    public void undeployResource(MailSessionDescriptor resource, String applicationName, String moduleName) throws Exception {
        //do nothing
    }

    @Override
    public void enableResource(MailSessionDescriptor resource) throws Exception {
        throw new UnsupportedOperationException("enable() not supported for mail-session type");
    }

    @Override
    public void disableResource(MailSessionDescriptor resource) throws Exception {
        throw new UnsupportedOperationException("disable() not supported for mail-session type");
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof MailSessionDescriptor;
    }

    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources) throws ResourceConflictException {
    }

    private ResourceDeployer getDeployer(Object resource) {
        return resourceManagerFactoryProvider.get().getResourceDeployer(resource);
    }

    public void registerMailSessions(com.sun.enterprise.deployment.Application application) {
        String appName = application.getAppName();
        Set<BundleDescriptor> bundles = application.getBundleDescriptors();
        for (BundleDescriptor bundle : bundles) {
            registerMailSessionDefinitions(appName, bundle);
            Collection<RootDeploymentDescriptor> dds = bundle.getExtensionsDescriptors();
            if (dds != null) {
                for (RootDeploymentDescriptor dd : dds) {
                    registerMailSessionDefinitions(appName, dd);
                }
            }
        }
    }

    private void registerMailSessionDefinitions(String appName, Descriptor descriptor) {

        if (descriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment env = (JndiNameEnvironment) descriptor;
            for (Descriptor msd : env.getResourceDescriptors(JavaEEResourceType.MSD)) {
                registerMSDReferredByApplication(appName, (MailSessionDescriptor)msd);
            }
        }

        //ejb descriptor
        if (descriptor instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbDesc = (EjbBundleDescriptor) descriptor;
            Set<EjbDescriptor> ejbDescriptors = (Set<EjbDescriptor>) ejbDesc.getEjbs();
            for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                for (Descriptor msd : ejbDescriptor.getResourceDescriptors(JavaEEResourceType.MSD)) {
                    registerMSDReferredByApplication(appName, (MailSessionDescriptor)msd);
                }
            }
            //ejb interceptors
            Set<EjbInterceptor> ejbInterceptors = ejbDesc.getInterceptors();
            for (EjbInterceptor ejbInterceptor : ejbInterceptors) {
                for (Descriptor msd : ejbInterceptor.getResourceDescriptors(JavaEEResourceType.MSD)) {
                    registerMSDReferredByApplication(appName, (MailSessionDescriptor)msd);
                }
            }
        }

        if (descriptor instanceof BundleDescriptor) {
            // managed bean descriptors
            Set<ManagedBeanDescriptor> managedBeanDescriptors = ((BundleDescriptor) descriptor).getManagedBeans();
            for (ManagedBeanDescriptor mbd : managedBeanDescriptors) {
                for (Descriptor msd : mbd.getResourceDescriptors(JavaEEResourceType.MSD)) {
                    registerMSDReferredByApplication(appName, (MailSessionDescriptor)msd);
                }
            }
        }
    }


    private void registerMSDReferredByApplication(String appName, MailSessionDescriptor msd) {
        if (!msd.isDeployed()) {
            CommonResourceProxy proxy = mailSessionProxyProvider.get();
            org.glassfish.resourcebase.resources.naming.ResourceNamingService resourceNamingService = resourceNamingServiceProvider.get();
            proxy.setDescriptor(msd);

            if (msd.getJndiName().isJavaApp()) {
                msd.setResourceId(appName);
            }

            if (msd.getJndiName().isJavaGlobal() || msd.getJndiName().isJavaApp()) {
                ResourceInfo resourceInfo = new ResourceInfo(msd.getJndiName(), appName);
                try {
                    resourceNamingService.publishObject(resourceInfo, proxy, true);
                    msd.setDeployed(true);
                } catch (NamingException e) {
                    Object params[] = new Object[] {appName, msd.getName(), e};
                    LOG.log(Level.WARNING, "exception while registering mail-session ", params);
                }
            }
        }
    }


    public void unRegisterMailSessions(com.sun.enterprise.deployment.Application application) {
        Set<BundleDescriptor> bundles = application.getBundleDescriptors();
        for (BundleDescriptor bundle : bundles) {
            unRegisterMailSessions(bundle);
            Collection<RootDeploymentDescriptor> dds = bundle.getExtensionsDescriptors();
            if (dds != null) {
                for (RootDeploymentDescriptor dd : dds) {
                    unRegisterMailSessions(dd);
                }
            }
        }
    }

    private void unRegisterMailSessions(Descriptor descriptor) {
        if (descriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment env = (JndiNameEnvironment) descriptor;
            for (Descriptor msd : env.getResourceDescriptors(JavaEEResourceType.MSD)) {
                unRegisterMSDReferredByApplication((MailSessionDescriptor)msd);
            }
        }

        //ejb descriptor
        if (descriptor instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbDesc = (EjbBundleDescriptor) descriptor;
            Set<EjbDescriptor> ejbDescriptors = (Set<EjbDescriptor>) ejbDesc.getEjbs();
            for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                for (Descriptor msd : ejbDescriptor.getResourceDescriptors(JavaEEResourceType.MSD)) {
                    unRegisterMSDReferredByApplication((MailSessionDescriptor)msd);
                }
            }
            //ejb interceptors
            Set<EjbInterceptor> ejbInterceptors = ejbDesc.getInterceptors();
            for (EjbInterceptor ejbInterceptor : ejbInterceptors) {
                for (Descriptor msd : ejbInterceptor.getResourceDescriptors(JavaEEResourceType.MSD)) {
                    unRegisterMSDReferredByApplication((MailSessionDescriptor)msd);
                }
            }
        }

        // managed bean descriptors
        if (descriptor instanceof BundleDescriptor) {
            Set<ManagedBeanDescriptor> managedBeanDescriptors = ((BundleDescriptor) descriptor).getManagedBeans();
            for (ManagedBeanDescriptor mbd : managedBeanDescriptors) {
                for (Descriptor msd : mbd.getResourceDescriptors(JavaEEResourceType.MSD)) {
                    unRegisterMSDReferredByApplication((MailSessionDescriptor)msd);
                }
            }
        }
    }

    private void unRegisterMSDReferredByApplication(MailSessionDescriptor msd) {
        try {
            if (msd.isDeployed()) {
                undeployResource(msd);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "exception while unregistering mail-session [ " + msd.getName() + " ]", e);
        }
    }

    abstract class FakeConfigBean implements ConfigBeanProxy {
        @Override
        public ConfigBeanProxy deepCopy(ConfigBeanProxy parent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConfigBeanProxy getParent() {
            return null;
        }

        @Override
        public <T extends ConfigBeanProxy> T getParent(Class<T> tClass) {
            return null;
        }

        @Override
        public <T extends ConfigBeanProxy> T createChild(Class<T> tClass) throws TransactionFailure {
            return null;
        }
    }

    class MailSessionProperty extends FakeConfigBean implements Property {

        private String name;
        private String value;
        private String description;

        MailSessionProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String value) throws PropertyVetoException {
            this.name = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String value) throws PropertyVetoException {
            this.value = value;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public void setDescription(String value) throws PropertyVetoException {
            this.description = value;
        }

        public void injectedInto(Object o) {
            //do nothing
        }
    }

    /**
     * A "fake" config bean with the same information as a <mail-resource>
     * config bean.  The information for this fake config bean comes from
     * a MailSessionDescriptor, which represents a @MailSessionDefinition
     * annotation.
     */
    class MyMailResource extends FakeConfigBean implements MailResource {

        private final MailSessionDescriptor desc;
        private final SimpleJndiName name;

        public MyMailResource(MailSessionDescriptor desc, SimpleJndiName name) {
            this.desc = desc;
            this.name = name;
        }

        @Override
        public String getStoreProtocol() {
            return desc.getStoreProtocol();
        }

        @Override
        public void setStoreProtocol(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getStoreProtocolClass() {
            return desc.getProperty("mail." + getStoreProtocol() + ".class");
        }

        @Override
        public void setStoreProtocolClass(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getTransportProtocol() {
            return desc.getTransportProtocol();
        }

        @Override
        public void setTransportProtocol(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getTransportProtocolClass() {
            return desc.getProperty("mail." + getTransportProtocol() + ".class");
        }

        @Override
        public void setTransportProtocolClass(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getHost() {
            return desc.getHost();
        }

        @Override
        public void setHost(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getUser() {
            return desc.getUser();
        }

        @Override
        public void setUser(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getFrom() {
            return desc.getFrom();
        }

        @Override
        public void setFrom(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getDebug() {
            return String.valueOf(true);
        }

        @Override
        public void setDebug(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getJndiName() {
            return name.toString();
        }

        @Override
        public void setJndiName(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getEnabled() {
            return String.valueOf(true);
        }

        @Override
        public void setEnabled(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getDescription() {
            return desc.getDescription();
        }

        @Override
        public void setDescription(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public List<Property> getProperty() {
            // make a copy in the required format
            List<Property> props = new ArrayList<>();
            for (String key : desc.getProperties().stringPropertyNames()) {
                props.add(new MailSessionProperty(key, desc.getProperty(key)));
            }
            return props;
        }

        @Override
        public Property getProperty(String name) {
            return new MailSessionProperty(name, desc.getProperty(name));
        }

        @Override
        public String getPropertyValue(String name) {
            return desc.getProperty(name);
        }

        @Override
        public String getPropertyValue(String name, String defaultValue) {
            String v = desc.getProperty(name);
            return v != null ? v : defaultValue;
        }

        @Override
        public String getObjectType() {
            return null;
        }

        @Override
        public void setObjectType(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getIdentity() {
            return name.toString();
        }

        @Override
        public String getDeploymentOrder() {
            return null;
        }

        @Override
        public void setDeploymentOrder(String value) {
            //do nothing
        }

        @Override
        public Property addProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property lookupProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Property removeProperty(Property prprt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
