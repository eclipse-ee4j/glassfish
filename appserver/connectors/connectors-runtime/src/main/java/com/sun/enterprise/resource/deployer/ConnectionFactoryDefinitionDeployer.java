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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.ConnectionFactoryDefinitionDescriptor;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import static com.sun.logging.LogDomains.RSR_LOGGER;
import static org.glassfish.deployment.common.JavaEEResourceType.CFDPOOL;

/**
 * @author Dapeng Hu
 */
@Service
@ResourceDeployerInfo(ConnectionFactoryDefinitionDescriptor.class)
public class ConnectionFactoryDefinitionDeployer implements ResourceDeployer<ConnectionFactoryDefinitionDescriptor> {

    @Inject
    private Provider<org.glassfish.resourcebase.resources.util.ResourceManagerFactory> resourceManagerFactoryProvider;

    private static final Logger LOG = LogDomains.getLogger(ConnectionFactoryDefinitionDeployer.class, RSR_LOGGER);
    static final String PROPERTY_PREFIX = "org.glassfish.connector-connection-pool.";

    @Override
    public void deployResource(ConnectionFactoryDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        //TODO ASR
    }

    @Override
    public void deployResource(ConnectionFactoryDefinitionDescriptor resource) throws Exception {

        String poolName = ConnectorsUtil.deriveResourceName(resource.getResourceId(), resource.getName(), CFDPOOL);
        String resourceName = ConnectorsUtil.deriveResourceName(resource.getResourceId(), resource.getName(),resource.getResourceType());

        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "ConnectionFactoryDefinitionDeployer.deployResource() : pool-name ["+poolName+"], " +
                    " resource-name ["+resourceName+"]");
        }

        ConnectorConnectionPool connectorCp = new MyConnectorConnectionPool(resource, poolName);

        //deploy pool
        getDeployer(connectorCp).deployResource(connectorCp);

        //deploy resource
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

        String poolName = ConnectorsUtil.deriveResourceName(resource.getResourceId(), resource.getName(), CFDPOOL);
        String resourceName = ConnectorsUtil.deriveResourceName(resource.getResourceId(), resource.getName(),resource.getResourceType());

        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "ConnectionFactoryDefinitionDeployer.undeployResource() : pool-name ["+poolName+"], " +
                    " resource-name ["+resourceName+"]");
        }

        //undeploy resource
        ConnectorResource connectorResource = new MyConnectorResource(poolName, resourceName);
        getDeployer(connectorResource).undeployResource(connectorResource);

        //undeploy pool
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

        private String poolName;
        private String jndiName;

        MyConnectorResource(String poolName, String jndiName) {
            this.poolName = poolName;
            this.jndiName = jndiName;
        }

        @Override
        public String getPoolName() {
            return poolName;
        }

        @Override
        public void setPoolName(String value) throws PropertyVetoException {
            this.poolName = value;
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
            return jndiName;
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
            return jndiName;
        }

        @Override
        public void setJndiName(String value) throws PropertyVetoException {
            this.jndiName = value;
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
        private final String name;

        public MyConnectorConnectionPool(ConnectionFactoryDefinitionDescriptor desc, String name) {
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
        public String getSteadyPoolSize() {
            int minPoolSize = desc.getMinPoolSize();
            if(minPoolSize >= 0){
                return Integer.toString(minPoolSize);
            }else{
                return "8";
            }
        }

        @Override
        public void setSteadyPoolSize(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMaxPoolSize() {
            int maxPoolSize = desc.getMaxPoolSize();
            if (maxPoolSize >= 0) {
                return Integer.toString(maxPoolSize);
            }else{
                return "32";
            }
        }

        @Override
        public void setMaxPoolSize(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMaxWaitTimeInMillis() {
            String maxWaitTimeInMillis = desc.getProperty(PROPERTY_PREFIX+"max-wait-time-in-millis");
            if (maxWaitTimeInMillis != null && !maxWaitTimeInMillis.equals("")) {
                return maxWaitTimeInMillis;
            }else{
                return "60000";
            }
        }

        @Override
        public void setMaxWaitTimeInMillis(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getPoolResizeQuantity() {
            String poolResizeQuantity = desc.getProperty(PROPERTY_PREFIX+"pool-resize-quantity");
            if (poolResizeQuantity != null && !poolResizeQuantity.equals("")) {
                return poolResizeQuantity;
            }else{
                return "2";
            }
        }

        @Override
        public void setPoolResizeQuantity(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getIdleTimeoutInSeconds() {
            String idleTimeoutInSeconds = desc.getProperty(PROPERTY_PREFIX+"idle-timeout-in-seconds");
            if (idleTimeoutInSeconds != null && !idleTimeoutInSeconds.equals("")) {
                return idleTimeoutInSeconds;
            }else{
                return "300";
            }
        }

        @Override
        public void setIdleTimeoutInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getIsConnectionValidationRequired() {
            String isConnectionValidationRequired = desc.getProperty(PROPERTY_PREFIX+"is-connection-validation-required");
            if (isConnectionValidationRequired != null && !isConnectionValidationRequired.equals("")) {
                return isConnectionValidationRequired;
            }else{
                return "false";
            }
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
            String failAllConnections = desc.getProperty(PROPERTY_PREFIX+"fail-all-connections");
            if (failAllConnections != null && !failAllConnections.equals("")) {
                return failAllConnections;
            }else{
                return "false";
            }
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
            String validateAtmostOncePeriodInSeconds = desc.getProperty(PROPERTY_PREFIX+"validate-at-most-once-period-in-seconds");
            if (validateAtmostOncePeriodInSeconds != null && !validateAtmostOncePeriodInSeconds.equals("")) {
                return validateAtmostOncePeriodInSeconds;
            }else{
                return "0";
            }
        }

        @Override
        public void setValidateAtmostOncePeriodInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionLeakTimeoutInSeconds() {
            String connectionLeakTimeoutInSeconds = desc.getProperty(PROPERTY_PREFIX+"connection-leak-timeout-in-seconds");
            if (connectionLeakTimeoutInSeconds != null && !connectionLeakTimeoutInSeconds.equals("")) {
                return connectionLeakTimeoutInSeconds;
            }else{
                return "0";
            }
        }

        @Override
        public void setConnectionLeakTimeoutInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionLeakReclaim() {
            String connectionLeakReclaim = desc.getProperty(PROPERTY_PREFIX+"connection-leak-reclaim");
            if (connectionLeakReclaim != null && !connectionLeakReclaim.equals("")) {
                return connectionLeakReclaim;
            }else{
                return "0";
            }
        }

        @Override
        public void setConnectionLeakReclaim(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionCreationRetryAttempts() {
            String connectionCreationRetryAttempts = desc.getProperty(PROPERTY_PREFIX+"connection-creation-retry-attempts");
            if (connectionCreationRetryAttempts != null && !connectionCreationRetryAttempts.equals("")) {
                return connectionCreationRetryAttempts;
            }else{
                return "0";
            }
        }

        @Override
        public void setConnectionCreationRetryAttempts(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getConnectionCreationRetryIntervalInSeconds() {
            String connectionCreationRetryIntervalInSeconds = desc.getProperty(PROPERTY_PREFIX+"connection-creation-retry-interval-in-seconds");
            if (connectionCreationRetryIntervalInSeconds != null && !connectionCreationRetryIntervalInSeconds.equals("")) {
                return connectionCreationRetryIntervalInSeconds;
            }else{
                return "0";
            }
        }

        @Override
        public void setConnectionCreationRetryIntervalInSeconds(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getLazyConnectionEnlistment() {
            String lazyConnectionEnlistment = desc.getProperty(PROPERTY_PREFIX+"lazy-connection-enlistment");
            if (lazyConnectionEnlistment != null && !lazyConnectionEnlistment.equals("")) {
                return lazyConnectionEnlistment;
            }else{
                return "false";
            }
        }

        @Override
        public void setLazyConnectionEnlistment(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getLazyConnectionAssociation() {
            String lazyConnectionAssociation = desc.getProperty(PROPERTY_PREFIX+"lazy-connection-association");
            if (lazyConnectionAssociation != null && !lazyConnectionAssociation.equals("")) {
                return lazyConnectionAssociation;
            }else{
                return "false";
            }
        }

        @Override
        public void setLazyConnectionAssociation(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getAssociateWithThread() {
            String associateWithThread = desc.getProperty(PROPERTY_PREFIX+"associate-with-thread");
            if (associateWithThread != null && !associateWithThread.equals("")) {
                return associateWithThread;
            }else{
                return "false";
            }
        }

        @Override
        public void setAssociateWithThread(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getPooling() {
            String pooling = desc.getProperty(PROPERTY_PREFIX+"pooling");
            if (pooling != null && !pooling.equals("")) {
                return pooling;
            }else{
                return "true";
            }
        }

        @Override
        public void setPooling(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMatchConnections() {
            String matchConnections = desc.getProperty(PROPERTY_PREFIX+"match-connections");
            if (matchConnections != null && !matchConnections.equals("")) {
                return matchConnections;
            }else{
                return "true";
            }
        }

        @Override
        public void setMatchConnections(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getMaxConnectionUsageCount() {
            String maxConnectionUsageCount = desc.getProperty(PROPERTY_PREFIX+"max-connection-usage-count");
            if (maxConnectionUsageCount != null && !maxConnectionUsageCount.equals("")) {
                return maxConnectionUsageCount;
            }else{
                return "0";
            }
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
        public String getName() {
            return name;
        }

        @Override
        public void setName(String value) throws PropertyVetoException {
            //do nothing
        }

        @Override
        public String getPing() {
            String ping = desc.getProperty(PROPERTY_PREFIX+"ping");
            if (ping != null && !ping.equals("")) {
                return ping;
            }else{
                return "false";
            }
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
