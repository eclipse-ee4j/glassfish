/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.util.ResourceManagerFactory;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.AdministeredObjectDefinitionDescriptor;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * @author Dapeng Hu
 */
@Service
@ResourceDeployerInfo(AdministeredObjectDefinitionDescriptor.class)
public class AdministeredObjectDefinitionDeployer implements ResourceDeployer {

    @Inject
    private Provider<ResourceManagerFactory> resourceManagerFactoryProvider;

    private static Logger _logger = LogDomains.getLogger(AdministeredObjectDefinitionDeployer.class, LogDomains.RSR_LOGGER);
    final static String PROPERTY_PREFIX = "org.glassfish.admin-object.";

    @Override
    public void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        //TODO ASR
    }

    @Override
    public void deployResource(Object resource) throws Exception {

        final AdministeredObjectDefinitionDescriptor desc = (AdministeredObjectDefinitionDescriptor) resource;
        String resourceName = ConnectorsUtil.deriveResourceName(desc.getResourceId(), desc.getName(), desc.getResourceType());

        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "AdministeredObjectDefinitionDeployer.deployResource() : resource-name ["+resourceName+"]");
        }

        //deploy resource
        MyAdministeredObjectResource adminObjectResource = new MyAdministeredObjectResource(desc, resourceName);
        getDeployer(adminObjectResource).deployResource(adminObjectResource);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource){
        if(handles(resource)){
            if(!postApplicationDeployment){
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validatePreservedResource(com.sun.enterprise.config.serverbeans.Application oldApp,
                                          com.sun.enterprise.config.serverbeans.Application newApp,
                                          Resource resource,
                                          Resources allResources)
    throws ResourceConflictException {
        //do nothing.
    }


    private ResourceDeployer getDeployer(Object resource) {
        return resourceManagerFactoryProvider.get().getResourceDeployer(resource);
    }

    private AdministeredObjectProperty convertProperty(String name, String value) {
        return new AdministeredObjectProperty(name, value);
    }

    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        //TODO ASR
    }

    @Override
    public void undeployResource(Object resource) throws Exception {

        final AdministeredObjectDefinitionDescriptor desc = (AdministeredObjectDefinitionDescriptor) resource;

        String resourceName = ConnectorsUtil.deriveResourceName(desc.getResourceId(), desc.getName(), desc.getResourceType());

        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "AdministeredObjectDefinitionDeployer.undeployResource() : resource-name ["+resourceName+"]");
        }

        //undeploy resource
        MyAdministeredObjectResource adminObjectResource = new MyAdministeredObjectResource(desc, resourceName);
        getDeployer(adminObjectResource).undeployResource(adminObjectResource);

    }

    @Override
    public void redeployResource(Object resource) throws Exception {
        throw new UnsupportedOperationException("redeploy() not supported for administered-object-definition type");
    }

    @Override
    public void enableResource(Object resource) throws Exception {
        throw new UnsupportedOperationException("enable() not supported for administered-object-definition type");
    }

    @Override
    public void disableResource(Object resource) throws Exception {
        throw new UnsupportedOperationException("disable() not supported for administered-object-definition type");
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof AdministeredObjectDefinitionDescriptor;
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
    @SuppressWarnings("rawtypes")
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
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

    class AdministeredObjectProperty extends FakeConfigBean implements Property {

        private String name;
        private String value;
        private String description;

        AdministeredObjectProperty(String name, String value) {
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

    class MyAdministeredObjectResource extends FakeConfigBean implements AdminObjectResource {

        private AdministeredObjectDefinitionDescriptor desc;
        private String name;

        public MyAdministeredObjectResource(AdministeredObjectDefinitionDescriptor desc, String name) {
            this.desc = desc;
            this.name = name;
        }

        @Override
        public String getObjectType() {
            return "user";  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setObjectType(String value) throws PropertyVetoException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getIdentity() {
            return name;
        }

        @Override
        public String getResAdapter() {
            return desc.getResourceAdapter();
        }

        @Override
        public void setResAdapter(String value) throws PropertyVetoException {
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
        public String getJndiName() {
            return name;
        }

        @Override
        public void setJndiName(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getResType() {
            return desc.getInterfaceName();
        }

        @Override
        public void setResType(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getClassName() {
            return desc.getClassName();
        }

        @Override
        public void setClassName(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getEnabled() {
            return "true";
        }

        @Override
        public void setEnabled(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public List<Property> getProperty() {
            Properties p = desc.getProperties();
            List<Property> administeredObjectProperties = new ArrayList<>();
            for (Entry<Object, Object> entry : p.entrySet()) {
                String key = (String) entry.getKey();
                if(key.startsWith(PROPERTY_PREFIX)){
                    continue;
                }
                String value = (String) entry.getValue();
                AdministeredObjectProperty dp = convertProperty(key, value);
                administeredObjectProperties.add(dp);
            }

            return administeredObjectProperties;
        }

        @Override
        public Property getProperty(String name) {
            String value = desc.getProperty(name);
            return new AdministeredObjectProperty(name, value);
        }

        @Override
        public String getPropertyValue(String name) {
            return desc.getProperty(name);
        }

        @Override
        public String getPropertyValue(String name, String defaultValue) {
            String value = null;
            value = desc.getProperty(name);
            if (value != null) {
                return value;
            } else {
                return defaultValue;
            }
        }

        public void injectedInto(Object o) {
            //do nothing
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
