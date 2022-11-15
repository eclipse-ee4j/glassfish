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

package org.glassfish.jdbc.deployer;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.beans.PropertyVetoException;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.glassfish.api.jdbc.objects.TxIsolationLevel;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.javaee.services.CommonResourceProxy;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;
import org.glassfish.resourcebase.resources.util.ResourceManagerFactory;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static com.sun.appserv.connectors.internal.api.ConnectorConstants.JAVAX_SQL_CONNECTION_POOL_DATASOURCE;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.JAVAX_SQL_DATASOURCE;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.JAVAX_SQL_XA_DATASOURCE;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.JAVA_SQL_DRIVER;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.deriveResourceName;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;
import static org.glassfish.deployment.common.JavaEEResourceType.DSDPOOL;

/**
 * @author Jagadish Ramu
 */
@Service
@ResourceDeployerInfo(DataSourceDefinitionDescriptor.class)
public class DataSourceDefinitionDeployer implements ResourceDeployer<DataSourceDefinitionDescriptor> {

    private static final Logger LOG = LogDomains.getLogger(DataSourceDefinitionDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private Provider<ResourceManagerFactory> resourceManagerFactoryProvider;
    @Inject
    private Provider<CommonResourceProxy> dataSourceDefinitionProxyProvider;
    @Inject
    private Provider<ResourceNamingService> resourceNamingServiceProvider;

    @Override
    public void deployResource(DataSourceDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        // TODO ASR
    }

    @Override
    public void deployResource(DataSourceDefinitionDescriptor resource) throws Exception {
        SimpleJndiName jndiName = SimpleJndiName.of(resource.getName());
        SimpleJndiName poolName = deriveResourceName(resource.getResourceId(), jndiName, DSDPOOL);
        SimpleJndiName resourceName = deriveResourceName(resource.getResourceId(), jndiName, resource.getResourceType());
        JdbcConnectionPool jdbcConnectionPool = new MyJdbcConnectionPool(resource, poolName);
        getDeployer(jdbcConnectionPool).deployResource(jdbcConnectionPool);
        JdbcResource jdbcResource = new MyJdbcResource(poolName, resourceName);
        getDeployer(jdbcResource).deployResource(jdbcResource);
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources)
            throws ResourceConflictException {
        // do nothing.
    }

    private ResourceDeployer getDeployer(Object resource) {
        return resourceManagerFactoryProvider.get().getResourceDeployer(resource);
    }

    private DataSourceProperty convertProperty(String name, String value) {
        return new DataSourceProperty(name, value);
    }

    public void registerDataSourceDefinitions(com.sun.enterprise.deployment.Application application) {
        String appName = application.getAppName();
        Set<BundleDescriptor> bundles = application.getBundleDescriptors();
        for (BundleDescriptor bundle : bundles) {
            registerDataSourceDefinitions(appName, bundle);
            Collection<RootDeploymentDescriptor> deploymentDescriptors = bundle.getExtensionsDescriptors();
            if (deploymentDescriptors != null) {
                for (RootDeploymentDescriptor deploymentDescriptor : deploymentDescriptors) {
                    registerDataSourceDefinitions(appName, deploymentDescriptor);
                }
            }
        }
    }

    private void registerDataSourceDefinitions(String appName, Descriptor descriptor) {
        if (descriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment env = (JndiNameEnvironment) descriptor;
            for (Descriptor resourceDescriptor : env.getResourceDescriptors(JavaEEResourceType.DSD)) {
                registerDSDReferredByApplication(appName, (DataSourceDefinitionDescriptor) resourceDescriptor);
            }
        }

        // EJB descriptor
        if (descriptor instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbDesc = (EjbBundleDescriptor) descriptor;
            Set<? extends EjbDescriptor> ejbDescriptors = ejbDesc.getEjbs();
            for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                for (Descriptor resourceDescriptor : ejbDescriptor.getResourceDescriptors(JavaEEResourceType.DSD)) {
                    registerDSDReferredByApplication(appName, (DataSourceDefinitionDescriptor) resourceDescriptor);
                }
            }

            // ejb interceptors
            Set<EjbInterceptor> ejbInterceptors = ejbDesc.getInterceptors();
            for (EjbInterceptor ejbInterceptor : ejbInterceptors) {
                for (Descriptor resourceDescriptor : ejbInterceptor.getResourceDescriptors(JavaEEResourceType.DSD)) {
                    registerDSDReferredByApplication(appName, (DataSourceDefinitionDescriptor) resourceDescriptor);
                }
            }
        }

        if (descriptor instanceof BundleDescriptor) {
            // managed bean descriptors
            Set<ManagedBeanDescriptor> managedBeanDescriptors = ((BundleDescriptor) descriptor).getManagedBeans();
            for (ManagedBeanDescriptor managedBeanDescriptor : managedBeanDescriptors) {
                for (Descriptor resourceDescriptor : managedBeanDescriptor.getResourceDescriptors(JavaEEResourceType.DSD)) {
                    registerDSDReferredByApplication(appName, (DataSourceDefinitionDescriptor) resourceDescriptor);
                }
            }
        }
    }

    private void unregisterDSDReferredByApplication(DataSourceDefinitionDescriptor dataSourceDefinitionDescriptor) {
        try {
            if (dataSourceDefinitionDescriptor.isDeployed()) {
                undeployResource(dataSourceDefinitionDescriptor);
            }
        } catch (Exception e) {
            LOG.log(WARNING, "exception while unregistering DSD [ " + dataSourceDefinitionDescriptor.getName() + " ]", e);
        }
    }

    public void unRegisterDataSourceDefinitions(com.sun.enterprise.deployment.Application application) {
        Set<BundleDescriptor> bundles = application.getBundleDescriptors();
        for (BundleDescriptor bundle : bundles) {
            unRegisterDataSourceDefinitions(bundle);
            Collection<RootDeploymentDescriptor> deploymentDescriptors = bundle.getExtensionsDescriptors();
            if (deploymentDescriptors != null) {
                for (RootDeploymentDescriptor deploymentDescriptor : deploymentDescriptors) {
                    unRegisterDataSourceDefinitions(deploymentDescriptor);
                }
            }
        }
    }

    private void unRegisterDataSourceDefinitions(Descriptor descriptor) {
        if (descriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment env = (JndiNameEnvironment) descriptor;
            for (Descriptor resourceDescriptor : env.getResourceDescriptors(JavaEEResourceType.DSD)) {
                unregisterDSDReferredByApplication((DataSourceDefinitionDescriptor) resourceDescriptor);
            }
        }

        // ejb descriptor
        if (descriptor instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbDesc = (EjbBundleDescriptor) descriptor;
            Set<? extends EjbDescriptor> ejbDescriptors = ejbDesc.getEjbs();
            for (EjbDescriptor ejbDescriptor : ejbDescriptors) {
                for (Descriptor resourceDescriptor : ejbDescriptor.getResourceDescriptors(JavaEEResourceType.DSD)) {
                    unregisterDSDReferredByApplication((DataSourceDefinitionDescriptor) resourceDescriptor);
                }
            }
            // ejb interceptors
            Set<EjbInterceptor> ejbInterceptors = ejbDesc.getInterceptors();
            for (EjbInterceptor ejbInterceptor : ejbInterceptors) {
                for (Descriptor resourceDescriptor : ejbInterceptor.getResourceDescriptors(JavaEEResourceType.DSD)) {
                    unregisterDSDReferredByApplication((DataSourceDefinitionDescriptor) resourceDescriptor);
                }
            }
        }

        // managed bean descriptors
        if (descriptor instanceof BundleDescriptor) {
            Set<ManagedBeanDescriptor> managedBeanDescriptors = ((BundleDescriptor) descriptor).getManagedBeans();
            for (ManagedBeanDescriptor managedBeanDescriptor : managedBeanDescriptors) {
                for (Descriptor resourceDescriptor : managedBeanDescriptor.getResourceDescriptors(JavaEEResourceType.DSD)) {
                    unregisterDSDReferredByApplication((DataSourceDefinitionDescriptor) resourceDescriptor);
                }
            }
        }
    }

    private void registerDSDReferredByApplication(String appName, DataSourceDefinitionDescriptor dataSourceDefinitionDescriptor) {

        // It is possible that Jakarta Persistence might call this method multiple times in a single
        // deployment, when there are multiple persistence units eg:
        // one persistence units in each of war, ejb-jar.
        // Make sure that DSD is bound to JNDI only when it is not already deployed.

        if (!dataSourceDefinitionDescriptor.isDeployed()) {
            CommonResourceProxy proxy = dataSourceDefinitionProxyProvider.get();
            ResourceNamingService resourceNamingService = resourceNamingServiceProvider.get();
            proxy.setDescriptor(dataSourceDefinitionDescriptor);

            SimpleJndiName dsdName = SimpleJndiName.of(dataSourceDefinitionDescriptor.getName());
            if (dsdName.isJavaApp()) {
                dataSourceDefinitionDescriptor.setResourceId(appName);
            }

            if (dsdName.isJavaGlobal() || dsdName.isJavaApp()) {
                ResourceInfo resourceInfo = new ResourceInfo(dsdName, appName, null);
                try {
                    resourceNamingService.publishObject(resourceInfo, proxy, true);
                    dataSourceDefinitionDescriptor.setDeployed(true);
                } catch (NamingException e) {
                    LOG.log(WARNING, "dsd.registration.failed", new Object[] { appName, dsdName, e });
                }
            }
        }
    }

    @Override
    public void undeployResource(DataSourceDefinitionDescriptor resource, String applicationName, String moduleName) throws Exception {
        // TODO ASR
    }

    @Override
    public void undeployResource(DataSourceDefinitionDescriptor resource) throws Exception {
        SimpleJndiName simpleJndiName = new SimpleJndiName(resource.getName());
        SimpleJndiName poolName = deriveResourceName(resource.getResourceId(), simpleJndiName, DSDPOOL);
        SimpleJndiName resourceName = deriveResourceName(resource.getResourceId(), simpleJndiName, resource.getResourceType());

        LOG.log(FINE, () ->
            "DataSourceDefinitionDeployer.undeployResource() : pool-name [" + poolName + "], " +
            " resource-name [" + resourceName + "]");

        // Undeploy resource
        JdbcResource jdbcResource = new MyJdbcResource(poolName, resourceName);
        getDeployer(jdbcResource).undeployResource(jdbcResource);

        // Undeploy pool
        JdbcConnectionPool jdbcCp = new MyJdbcConnectionPool(resource, poolName);
        getDeployer(jdbcCp).undeployResource(jdbcCp);

        resource.setDeployed(false);
    }

    @Override
    public void enableResource(DataSourceDefinitionDescriptor resource) throws Exception {
        throw new UnsupportedOperationException("enable() not supported for datasource-definition type");
    }

    @Override
    public void disableResource(DataSourceDefinitionDescriptor resource) throws Exception {
        throw new UnsupportedOperationException("disable() not supported for datasource-definition type");
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof DataSourceDefinitionDescriptor;
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

    class DataSourceProperty extends FakeConfigBean implements Property {

        private String name;
        private String value;
        private String description;

        DataSourceProperty(String name, String value) {
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
            // do nothing
        }
    }

    class MyJdbcResource extends FakeConfigBean implements JdbcResource {

        private SimpleJndiName poolName;
        private SimpleJndiName jndiName;

        MyJdbcResource(SimpleJndiName poolName, SimpleJndiName jndiName) {
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
            // do nothing
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

    class MyJdbcConnectionPool extends FakeConfigBean implements JdbcConnectionPool {

        private final DataSourceDefinitionDescriptor dataSourceDefinitionDescriptor;
        private final SimpleJndiName name;

        public MyJdbcConnectionPool(DataSourceDefinitionDescriptor desc, SimpleJndiName name) {
            this.dataSourceDefinitionDescriptor = desc;
            this.name = name;
        }

        @Override
        public String getDatasourceClassname() {
            if (!getResType().equals(JAVA_SQL_DRIVER)) {
                return dataSourceDefinitionDescriptor.getClassName();
            }

            return null;
        }

        @Override
        public void setDatasourceClassname(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getResType() {
            String type = JAVAX_SQL_DATASOURCE;
            try {
                Class<?> dataSoureClass =
                    Thread.currentThread().getContextClassLoader().loadClass(dataSourceDefinitionDescriptor.getClassName());

                if (XADataSource.class.isAssignableFrom(dataSoureClass)) {
                    type = JAVAX_SQL_XA_DATASOURCE;
                } else if (ConnectionPoolDataSource.class.isAssignableFrom(dataSoureClass)) {
                    type = JAVAX_SQL_CONNECTION_POOL_DATASOURCE;
                } else if (DataSource.class.isAssignableFrom(dataSoureClass)) {
                    type = JAVAX_SQL_DATASOURCE;
                } else if (Driver.class.isAssignableFrom(dataSoureClass)) {
                    type = JAVA_SQL_DRIVER;
                }
            } catch (ClassNotFoundException e) {
                    LOG.log(FINEST, () ->
                        "Unable to load class [ " + dataSourceDefinitionDescriptor.getClassName() + " ] to " +
                        "determine its res-type, defaulting to [" + JAVAX_SQL_DATASOURCE + "]");

                    // ignore and default to "javax.sql.DataSource"
            }

            return type;
        }

        @Override
        public void setResType(String value) throws PropertyVetoException {
            // do nothing
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
            return name.toString();
        }

        @Override
        public String getSteadyPoolSize() {
            int minPoolSize = dataSourceDefinitionDescriptor.getMinPoolSize();
            if (minPoolSize == -1) {
                minPoolSize = 8;
            }

            return String.valueOf(minPoolSize);
        }

        @Override
        public void setSteadyPoolSize(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getMaxPoolSize() {
            int maxPoolSize = dataSourceDefinitionDescriptor.getMaxPoolSize();
            if (maxPoolSize == -1) {
                maxPoolSize = 32;
            }

            return String.valueOf(maxPoolSize);
        }

        @Override
        public void setMaxPoolSize(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getMaxWaitTimeInMillis() {
            return String.valueOf(60000);
        }

        @Override
        public void setMaxWaitTimeInMillis(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getPoolResizeQuantity() {
            return String.valueOf(2);
        }

        @Override
        public void setPoolResizeQuantity(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getIdleTimeoutInSeconds() {
            long maxIdleTime = dataSourceDefinitionDescriptor.getMaxIdleTime();
            if (maxIdleTime == -1) {
                maxIdleTime = 300;
            }

            return String.valueOf(maxIdleTime);
        }

        @Override
        public void setIdleTimeoutInSeconds(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getTransactionIsolationLevel() {
            if (dataSourceDefinitionDescriptor.getIsolationLevel() == -1) {
                return null;
            }
            return TxIsolationLevel.byId(dataSourceDefinitionDescriptor.getIsolationLevel()).getName();

        }

        @Override
        public void setTransactionIsolationLevel(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getIsIsolationLevelGuaranteed() {
            return String.valueOf("true");
        }

        @Override
        public void setIsIsolationLevelGuaranteed(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getIsConnectionValidationRequired() {
            return String.valueOf("false");
        }

        @Override
        public void setIsConnectionValidationRequired(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getConnectionValidationMethod() {
            return null;
        }

        @Override
        public void setConnectionValidationMethod(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getValidationTableName() {
            return null;
        }

        @Override
        public void setValidationTableName(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getValidationClassname() {
            return null;
        }

        @Override
        public void setValidationClassname(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getFailAllConnections() {
            return String.valueOf("false");
        }

        @Override
        public void setFailAllConnections(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getNonTransactionalConnections() {
            return String.valueOf(!dataSourceDefinitionDescriptor.isTransactional());
        }

        @Override
        public void setNonTransactionalConnections(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getAllowNonComponentCallers() {
            return String.valueOf("false");
        }

        @Override
        public void setAllowNonComponentCallers(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getValidateAtmostOncePeriodInSeconds() {
            return String.valueOf(0);
        }

        @Override
        public void setValidateAtmostOncePeriodInSeconds(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getConnectionLeakTimeoutInSeconds() {
            return String.valueOf(0);
        }

        @Override
        public void setConnectionLeakTimeoutInSeconds(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getConnectionLeakReclaim() {
            return String.valueOf(false);
        }

        @Override
        public void setConnectionLeakReclaim(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getConnectionCreationRetryAttempts() {
            return String.valueOf(0);
        }

        @Override
        public void setConnectionCreationRetryAttempts(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getConnectionCreationRetryIntervalInSeconds() {
            return String.valueOf(10);
        }

        @Override
        public void setConnectionCreationRetryIntervalInSeconds(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getStatementTimeoutInSeconds() {
            return String.valueOf(-1);
        }

        @Override
        public void setStatementTimeoutInSeconds(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getLazyConnectionEnlistment() {
            return String.valueOf(false);
        }

        @Override
        public void setLazyConnectionEnlistment(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getLazyConnectionAssociation() {
            return String.valueOf(false);
        }

        @Override
        public void setLazyConnectionAssociation(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getAssociateWithThread() {
            return String.valueOf(false);
        }

        @Override
        public void setAssociateWithThread(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getPooling() {
            return String.valueOf(true);
        }

        @Override
        public void setPooling(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getStatementCacheSize() {
            return String.valueOf(0);
        }

        @Override
        public void setStatementCacheSize(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getMatchConnections() {
            return String.valueOf(true);
        }

        @Override
        public void setMatchConnections(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getMaxConnectionUsageCount() {
            return String.valueOf(0);
        }

        @Override
        public void setMaxConnectionUsageCount(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getWrapJdbcObjects() {
            return String.valueOf(true);
        }

        @Override
        public void setWrapJdbcObjects(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getDescription() {
            return dataSourceDefinitionDescriptor.getDescription();
        }

        @Override
        public void setDescription(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public List<Property> getProperty() {
            Properties descriptorProperties = dataSourceDefinitionDescriptor.getProperties();
            List<Property> dataSourceProperties = new ArrayList<>();

            for (Map.Entry<Object, Object> entry : descriptorProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                dataSourceProperties.add(convertProperty(key, value));
            }

            if (dataSourceDefinitionDescriptor.getUser() != null) {
                DataSourceProperty property = convertProperty("user", dataSourceDefinitionDescriptor.getUser());
                dataSourceProperties.add(property);
            }

            if (dataSourceDefinitionDescriptor.getPassword() != null) {
                DataSourceProperty property = convertProperty("password", dataSourceDefinitionDescriptor.getPassword());
                dataSourceProperties.add(property);
            }

            if (dataSourceDefinitionDescriptor.getDatabaseName() != null) {
                DataSourceProperty property = convertProperty("databaseName", dataSourceDefinitionDescriptor.getDatabaseName());
                dataSourceProperties.add(property);
            }

            if (dataSourceDefinitionDescriptor.getServerName() != null) {
                DataSourceProperty property = convertProperty("serverName", dataSourceDefinitionDescriptor.getServerName());
                dataSourceProperties.add(property);
            }

            if (dataSourceDefinitionDescriptor.getPortNumber() != -1) {
                DataSourceProperty property = convertProperty("portNumber", String.valueOf(dataSourceDefinitionDescriptor.getPortNumber()));
                dataSourceProperties.add(property);
            }

            // Process URL only when standard properties are not set
            if (dataSourceDefinitionDescriptor.getUrl() != null && !isStandardPropertiesSet(dataSourceDefinitionDescriptor)) {
                DataSourceProperty property = convertProperty("url", dataSourceDefinitionDescriptor.getUrl());
                dataSourceProperties.add(property);
            }

            if (dataSourceDefinitionDescriptor.getLoginTimeout() != 0) {
                DataSourceProperty property = convertProperty("loginTimeout", String.valueOf(dataSourceDefinitionDescriptor.getLoginTimeout()));
                dataSourceProperties.add(property);
            }

            if (dataSourceDefinitionDescriptor.getMaxStatements() != -1) {
                DataSourceProperty property = convertProperty("maxStatements", String.valueOf(dataSourceDefinitionDescriptor.getMaxStatements()));
                dataSourceProperties.add(property);
            }

            return dataSourceProperties;
        }

        private boolean isStandardPropertiesSet(DataSourceDefinitionDescriptor dataSourceDefinitionDescriptor) {
            return
                dataSourceDefinitionDescriptor.getServerName() != null &&
                dataSourceDefinitionDescriptor.getDatabaseName() != null &&
                dataSourceDefinitionDescriptor.getPortNumber() != -1;
        }

        @Override
        public Property getProperty(String name) {
            String value = (String) dataSourceDefinitionDescriptor.getProperties().get(name);
            return new DataSourceProperty(name, value);
        }

        @Override
        public String getPropertyValue(String name) {
            return (String) dataSourceDefinitionDescriptor.getProperties().get(name);
        }

        @Override
        public String getPropertyValue(String name, String defaultValue) {
            String value = (String) dataSourceDefinitionDescriptor.getProperties().get(name);
            if (value != null) {
                return value;
            }

            return defaultValue;
        }

        public void injectedInto(Object o) {
            // do nothing
        }

        @Override
        public String getName() {
            return name.toString();
        }

        @Override
        public void setName(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getSqlTraceListeners() {
            return null;
        }

        @Override
        public void setSqlTraceListeners(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getPing() {
            return String.valueOf(false);
        }

        @Override
        public void setPing(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getInitSql() {
            return null;
        }

        @Override
        public void setInitSql(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getDriverClassname() {
            if (getResType().equals(JAVA_SQL_DRIVER)) {
                return dataSourceDefinitionDescriptor.getClassName();
            }

            return null;
        }

        @Override
        public void setDriverClassname(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getStatementLeakTimeoutInSeconds() {
            return String.valueOf(0);
        }

        @Override
        public void setStatementLeakTimeoutInSeconds(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getStatementLeakReclaim() {
            return String.valueOf(false);
        }

        @Override
        public void setStatementLeakReclaim(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getStatementCacheType() {
            return null;
        }

        @Override
        public void setStatementCacheType(String value) throws PropertyVetoException {
            // do nothing
        }

        @Override
        public String getDeploymentOrder() {
            return null;
        }

        @Override
        public void setDeploymentOrder(String value) {
            // do nothing
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
