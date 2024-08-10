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

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.beans.PropertyVetoException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.connectors.config.SecurityMap;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.deriveResourceName;
import static org.glassfish.deployment.common.JavaEEResourceType.CFDPOOL;

/**
 * @author Dapeng Hu
 */
@Service
@ResourceDeployerInfo(ConnectionFactoryDefinitionDescriptor.class)
public class ConnectionFactoryDefinitionDeployer implements ResourceDeployer<ConnectionFactoryDefinitionDescriptor> {

    private static final Logger LOG = System.getLogger(ConnectionFactoryDefinitionDeployer.class.getName());
    private static final String PROPERTY_PREFIX = "org.glassfish.connector-connection-pool.";

    @Inject
    private Provider<org.glassfish.resourcebase.resources.util.ResourceManagerFactory> resourceManagerFactoryProvider;


    @Override
    public void deployResource(ConnectionFactoryDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        //TODO ASR
    }

    @Override
    public void deployResource(ConnectionFactoryDefinitionDescriptor resource) throws Exception {
        SimpleJndiName poolName = deriveResourceName(resource.getResourceId(), resource.getJndiName(), CFDPOOL);
        SimpleJndiName resourceName = deriveResourceName(resource.getResourceId(), resource.getJndiName(), resource.getResourceType());

        LOG.log(Level.INFO, "Deploying resource [{0}] with pool [{1}].", new Object[] {resourceName, poolName});
        ConnectorConnectionPool connectorCp = new MyConnectorConnectionPool(resource, poolName);
        getDeployer(connectorCp).deployResource(connectorCp);
        ConnectorResource connectorResource = new MyConnectorResource(poolName, resourceName);
        getDeployer(connectorResource).deployResource(connectorResource);
    }


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

    private ConnectionFactoryProperty convertProperty(String name, String value) {
        return new ConnectionFactoryProperty(name, value);
    }

    @Override
    public void undeployResource(ConnectionFactoryDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        //TODO ASR
    }

    @Override
    public void undeployResource(ConnectionFactoryDefinitionDescriptor resource) throws Exception {
        SimpleJndiName poolName = deriveResourceName(resource.getResourceId(), resource.getJndiName(), CFDPOOL);
        SimpleJndiName resourceName = deriveResourceName(resource.getResourceId(), resource.getJndiName(), resource.getResourceType());
        LOG.log(Level.INFO, "Undeploying resource [{0}] with pool [{1}].", new Object[] {resourceName, poolName});
        ConnectorResource connectorResource = new MyConnectorResource(poolName, resourceName);
        getDeployer(connectorResource).undeployResource(connectorResource);
        ConnectorConnectionPool connectorCp = new MyConnectorConnectionPool(resource, poolName);
        getDeployer(connectorCp).undeployResource(connectorCp);
    }

    @Override
    public void enableResource(ConnectionFactoryDefinitionDescriptor resource) throws Exception {
        throw new UnsupportedOperationException("enable() not supported for connection-factory-definition type");
    }

    @Override
    public void disableResource(ConnectionFactoryDefinitionDescriptor resource) throws Exception {
        throw new UnsupportedOperationException("disable() not supported for connection-factory-definition type");
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof ConnectionFactoryDefinitionDescriptor;
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

    class ConnectionFactoryProperty extends FakeConfigBean implements Property {

        private String name;
        private String value;
        private String description;

        ConnectionFactoryProperty(String name, String value) {
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

    class MyConnectorResource extends FakeConfigBean implements ConnectorResource {

        private SimpleJndiName poolName;
        private SimpleJndiName jndiName;

        MyConnectorResource(SimpleJndiName poolName, SimpleJndiName jndiName) {
            this.poolName = poolName;
            this.jndiName = jndiName;
        }

        @Override
        public String getPoolName() {
            return poolName.toString();
        }

        @Override
        public void setPoolName(String value) throws PropertyVetoException {
            this.poolName = new SimpleJndiName(value);
        }

        @Override
        public String getObjectType() {
            return null;
        }

        @Override
        public void setObjectType(String value) throws PropertyVetoException {
        }

        @Override
        public String getIdentity() {
            return jndiName.toString();
        }

        @Override
        public String getEnabled() {
            return String.valueOf(true);
        }

        @Override
        public void setEnabled(String value) throws PropertyVetoException {
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void setDescription(String value) throws PropertyVetoException {
        }

        @Override
        public List<Property> getProperty() {
            return null;
        }

        @Override
        public Property getProperty(String name) {
            return null;
        }

        @Override
        public String getPropertyValue(String name) {
            return null;
        }

        @Override
        public String getPropertyValue(String name, String defaultValue) {
            return null;
        }

        public void injectedInto(Object o) {
        }

        @Override
        public String getJndiName() {
            return jndiName.toString();
        }

        @Override
        public void setJndiName(String value) throws PropertyVetoException {
            this.jndiName = new SimpleJndiName(value);
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
    class MyConnectorConnectionPool extends FakeConfigBean implements ConnectorConnectionPool {

        private final ConnectionFactoryDefinitionDescriptor desc;
        private final SimpleJndiName name;

        public MyConnectorConnectionPool(ConnectionFactoryDefinitionDescriptor desc, SimpleJndiName name) {
            this.desc = desc;
            this.name = name;
        }

        @Override
        public String getObjectType() {
            return "user";
        }

        @Override
        public void setObjectType(String value) throws PropertyVetoException {
        }

        @Override
        public String getIdentity() {
            return name.toString();
        }

        @Override
        public String getSteadyPoolSize() {
            int minPoolSize = desc.getMinPoolSize();
            return minPoolSize < 0 ? "8" : Integer.toString(minPoolSize);
        }

        @Override
        public void setSteadyPoolSize(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMaxPoolSize() {
            int maxPoolSize = desc.getMaxPoolSize();
            return maxPoolSize < 0 ? "32" : Integer.toString(maxPoolSize);
        }

        @Override
        public void setMaxPoolSize(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMaxWaitTimeInMillis() {
            String maxWaitTimeInMillis = desc.getProperty(PROPERTY_PREFIX + "max-wait-time-in-millis");
            return maxWaitTimeInMillis == null || maxWaitTimeInMillis.isEmpty() ? "60000" : maxWaitTimeInMillis;
        }

        @Override
        public void setMaxWaitTimeInMillis(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getPoolResizeQuantity() {
            String poolResizeQuantity = desc.getProperty(PROPERTY_PREFIX + "pool-resize-quantity");
            return poolResizeQuantity == null || poolResizeQuantity.isEmpty() ? "2" : poolResizeQuantity;
        }

        @Override
        public void setPoolResizeQuantity(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getIdleTimeoutInSeconds() {
            String timeoutInSeconds = desc.getProperty(PROPERTY_PREFIX + "idle-timeout-in-seconds");
            return timeoutInSeconds == null || timeoutInSeconds.isEmpty() ? "300" : timeoutInSeconds;
        }

        @Override
        public void setIdleTimeoutInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getIsConnectionValidationRequired() {
            String isRequired = desc.getProperty(PROPERTY_PREFIX + "is-connection-validation-required");
            return isRequired == null || isRequired.isEmpty() ? "false" : isRequired;
        }

        @Override
        public void setIsConnectionValidationRequired(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getResourceAdapterName() {
            return desc.getResourceAdapter();
        }

        @Override
        public void setResourceAdapterName(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionDefinitionName() {
            return desc.getInterfaceName();
        }

        @Override
        public void setConnectionDefinitionName(String value)  throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getFailAllConnections() {
            String failAllConnections = desc.getProperty(PROPERTY_PREFIX + "fail-all-connections");
            return failAllConnections == null || failAllConnections.isEmpty() ? "false" : failAllConnections;
        }

        @Override
        public void setFailAllConnections(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getTransactionSupport() {
            return desc.getTransactionSupport();
        }

        @Override
        public void setTransactionSupport(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getValidateAtmostOncePeriodInSeconds() {
            String timeInSeconds = desc.getProperty(PROPERTY_PREFIX + "validate-at-most-once-period-in-seconds");
            return timeInSeconds == null || timeInSeconds.isEmpty() ? "0" : timeInSeconds;
        }

        @Override
        public void setValidateAtmostOncePeriodInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionLeakTimeoutInSeconds() {
            String timeInSeconds = desc.getProperty(PROPERTY_PREFIX + "connection-leak-timeout-in-seconds");
            return timeInSeconds == null || timeInSeconds.isEmpty() ? "0" : timeInSeconds;
        }

        @Override
        public void setConnectionLeakTimeoutInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionLeakReclaim() {
            String conLeakReclaim = desc.getProperty(PROPERTY_PREFIX + "connection-leak-reclaim");
            return conLeakReclaim == null || conLeakReclaim.isEmpty() ? "0" : conLeakReclaim;
        }

        @Override
        public void setConnectionLeakReclaim(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionCreationRetryAttempts() {
            String attemptCount = desc.getProperty(PROPERTY_PREFIX + "connection-creation-retry-attempts");
            return attemptCount == null || attemptCount.isEmpty() ? "0" : attemptCount;
        }

        @Override
        public void setConnectionCreationRetryAttempts(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionCreationRetryIntervalInSeconds() {
            String interval = desc.getProperty(PROPERTY_PREFIX + "connection-creation-retry-interval-in-seconds");
            return interval == null || interval.isEmpty() ? "0" : interval;
        }

        @Override
        public void setConnectionCreationRetryIntervalInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getLazyConnectionEnlistment() {
            String lazy = desc.getProperty(PROPERTY_PREFIX + "lazy-connection-enlistment");
            return lazy == null || lazy.isEmpty() ? "false" : lazy;
        }

        @Override
        public void setLazyConnectionEnlistment(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getLazyConnectionAssociation() {
            String lazy = desc.getProperty(PROPERTY_PREFIX + "lazy-connection-association");
            return lazy == null || lazy.isEmpty() ? "false" : lazy;
        }

        @Override
        public void setLazyConnectionAssociation(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getAssociateWithThread() {
            String associateWithThread = desc.getProperty(PROPERTY_PREFIX + "associate-with-thread");
            return associateWithThread == null || associateWithThread.isEmpty() ? "false" : associateWithThread;
        }

        @Override
        public void setAssociateWithThread(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getPooling() {
            String pooling = desc.getProperty(PROPERTY_PREFIX + "pooling");
            return pooling == null || pooling.isEmpty() ? "true" : pooling;
        }

        @Override
        public void setPooling(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMatchConnections() {
            String matchConn = desc.getProperty(PROPERTY_PREFIX + "match-connections");
            return matchConn == null || matchConn.isEmpty() ? "true" : matchConn;
        }

        @Override
        public void setMatchConnections(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMaxConnectionUsageCount() {
            String count = desc.getProperty(PROPERTY_PREFIX + "max-connection-usage-count");
            return count == null || count.isEmpty() ? "0" : count;
        }

        @Override
        public void setMaxConnectionUsageCount(String value) throws PropertyVetoException {
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
            Properties p = desc.getProperties();
            List<Property> connectionFactoryProperties = new ArrayList<>();
            for (Entry<Object, Object> entry : p.entrySet()) {
                String key = (String) entry.getKey();
                if(key.startsWith(PROPERTY_PREFIX)){
                    continue;
                }
                String value = (String) entry.getValue();
                ConnectionFactoryProperty dp = convertProperty(key, value);
                connectionFactoryProperties.add(dp);
            }

            return connectionFactoryProperties;
        }


        @Override
        public Property getProperty(String name) {
            String value = desc.getProperty(name);
            return new ConnectionFactoryProperty(name, value);
        }

        @Override
        public String getPropertyValue(String name) {
            return desc.getProperty(name);
        }

        @Override
        public String getPropertyValue(String name, String defaultValue) {
            String value = desc.getProperty(name);
            return value == null ? defaultValue : value;
        }

        public void injectedInto(Object o) {
            //do nothing
        }

        @Override
        public String getName() {
            return name.toString();
        }

        @Override
        public void setName(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getPing() {
            String ping = desc.getProperty(PROPERTY_PREFIX + "ping");
            return ping == null || ping.isEmpty() ? "false" : ping;
        }

        @Override
        public void setPing(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public List<SecurityMap> getSecurityMap() {
            return new ArrayList<>(0);
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
