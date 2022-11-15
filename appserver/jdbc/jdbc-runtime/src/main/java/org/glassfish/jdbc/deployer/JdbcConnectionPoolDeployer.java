/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.jdbc.DataSource;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.pool.ResourcePool;
import com.sun.enterprise.resource.pool.waitqueue.PoolWaitQueue;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.util.JdbcResourcesUtil;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

import static com.sun.appserv.connectors.internal.api.ConnectorConstants.DYNAMIC_RECONFIGURATION_FLAG;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.JAVA_SQL_DRIVER;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.JDBCXA_RA_NAME;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.LOCAL_TRANSACTION_TX_SUPPORT_STRING;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.XA_TRANSACTION_TX_SUPPORT_STRING;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourceInfo;
import static com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils.setLazyEnlistAndLazyAssocProperties;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.callable;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.glassfish.jdbc.util.JdbcResourcesUtil.getResourcesOfPool;


/**
 * Handles Jdbc connection pool events in the server instance. When user adds a
 * jdbc connection pool , the admin instance emits resource event. The jdbc
 * connection pool events are propagated to this object.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author Tamil Vengan
 */
@Service
@Singleton
@ResourceDeployerInfo(JdbcConnectionPool.class)
public class JdbcConnectionPoolDeployer implements ResourceDeployer<JdbcConnectionPool> {

    private static final Logger LOG = LogDomains.getLogger(JdbcConnectionPoolDeployer.class, LogDomains.RSR_LOGGER);
    private static final StringManager STRINGS = StringManager.getManager(JdbcConnectionPoolDeployer.class);
    private static final Locale LOCALE = Locale.getDefault();

    @Inject
    private ConnectorRuntime runtime;

    @Inject
    @Optional // we need it only in server mode
    private Domain domain;

    private final ExecutorService execService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    });

    @Override
    public boolean handles(Object resource) {
        return resource instanceof JdbcConnectionPool;
    }


    @Override
    public Class<?>[] getProxyClassesForDynamicReconfiguration() {
        return new Class[] {DataSource.class};
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
        // do nothing.
    }


    @Override
    public void deployResource(JdbcConnectionPool resource) throws Exception {
        PoolInfo poolInfo = ResourceUtil.getPoolInfo(resource);
        actualDeployResource(resource, poolInfo);
    }


    @Override
    public void deployResource(JdbcConnectionPool resource, String applicationName, String moduleName)
        throws Exception {
        // deployResource is not synchronized as there is only one caller
        // ResourceProxy which is synchronized

        // intentional no-op
        // From 8.1 PE/SE/EE, JDBC connection pools are no more resources and
        // they would be available only to server instances that have a resource-ref
        // that maps to a pool. So deploy resource would not be called during
        // JDBC connection pool creation. The actualDeployResource method
        // below is invoked by JdbcResourceDeployer when a resource-ref for a
        // resource that is pointed to this pool is added to a server instance
        SimpleJndiName jndiName = new SimpleJndiName(resource.getName());
        PoolInfo poolInfo = new PoolInfo(jndiName, applicationName, moduleName);
        actualDeployResource(resource, poolInfo);
    }


    @Override
    public void undeployResource(JdbcConnectionPool resource, String applicationName, String moduleName)
        throws Exception {
        SimpleJndiName jndiName = new SimpleJndiName(resource.getName());
        PoolInfo poolInfo = new PoolInfo(jndiName, applicationName, moduleName);
        actualUndeployResource(poolInfo);
    }


    @Override
    public synchronized void undeployResource(JdbcConnectionPool resource) throws Exception {
        PoolInfo poolInfo = ResourceUtil.getPoolInfo(resource);
        actualUndeployResource(poolInfo);
    }


    @Override
    public synchronized void redeployResource(JdbcConnectionPool resource) throws Exception {
        PoolInfo poolInfo = ResourceUtil.getPoolInfo(resource);

        // Only if pool has already been deployed in this server-instance
        // reconfig this pool

        if (!runtime.isConnectorConnectionPoolDeployed(poolInfo)) {
            LOG.fine(() ->
                "The JDBC connection pool " + poolInfo + " is not referred or not yet created in this server " +
                "instance and hence pool redeployment is ignored");
            return;
        }

        final ConnectorConnectionPool connectorConnectionPool = createConnectorConnectionPool(resource, poolInfo);
        if (connectorConnectionPool == null) {
            throw new ConnectorRuntimeException("Unable to create ConnectorConnectionPool " + "from JDBC connection pool");
        }

        // Now do internal book keeping

        Set<String> excludes = new HashSet<>();

        // Add MCF config props to the set that need to be excluded
        // in checking for the equality of the props with old pool
        excludes.add("TransactionIsolation");
        excludes.add("GuaranteeIsolationLevel");
        excludes.add("ValidationTableName");
        excludes.add("ConnectionValidationRequired");
        excludes.add("ValidationMethod");
        excludes.add("StatementWrapping");
        excludes.add("StatementTimeout");
        excludes.add("ValidationClassName");
        excludes.add("StatementCacheSize");
        excludes.add("StatementCacheType");
        excludes.add("StatementLeakTimeoutInSeconds");
        excludes.add("StatementLeakReclaim");

        try {
            LOG.finest("Calling reconfigure pool");
            boolean requirePoolRecreation = runtime.reconfigureConnectorConnectionPool(connectorConnectionPool, excludes);
            if (requirePoolRecreation) {
                if (runtime.isServer() || runtime.isEmbedded()) {
                    handlePoolRecreation(connectorConnectionPool);
                } else {
                    recreatePool(connectorConnectionPool);
                }
            }
        } catch (ConnectorRuntimeException cre) {
            LOG.log(WARNING, "error.redeploying.jdbc.pool", new Object[] { poolInfo, cre });
            throw cre;
        }
    }


    /**
     * Enable the resource in the server's runtime naming context
     *
     * @param resource a resource object
     * @throws UnsupportedOperationException Currently we are not supporting this method.
     */
    @Override
    public synchronized void enableResource(JdbcConnectionPool resource) throws Exception {
        throw new UnsupportedOperationException(STRINGS.getString("resource.restart_needed"));
    }


    /**
     * Disable the resource in the server's runtime naming context
     *
     * @param resource a resource object
     * @throws UnsupportedOperationException Currently we are not supporting this method.
     */
    @Override
    public synchronized void disableResource(JdbcConnectionPool resource) throws Exception {
        throw new UnsupportedOperationException(STRINGS.getString("resource.restart_needed"));
    }


    /**
     * Deploy the resource into the server's runtime naming context
     *
     * @param resource a resource object
     */
    private void actualDeployResource(Object resource, PoolInfo poolInfo) {
        LOG.log(Level.CONFIG, "actualDeployResource(resource={0}, poolInfo={1})", new Object[] {resource, poolInfo});
        try {
            ConnectorConnectionPool connConnPool = createConnectorConnectionPool((JdbcConnectionPool) resource, poolInfo);
            registerTransparentDynamicReconfigPool(poolInfo, (JdbcConnectionPool) resource);

            // Now do internal book keeping
            runtime.createConnectorConnectionPool(connConnPool);
        } catch (Exception e) {
            LOG.log(WARNING, "error.creating.jdbc.pool", new Object[] { poolInfo, e });
        }
    }

    private void registerTransparentDynamicReconfigPool(PoolInfo poolInfo, JdbcConnectionPool resourcePool) {
        ConnectorRegistry registry = ConnectorRegistry.getInstance();
        if (ConnectorsUtil.isDynamicReconfigurationEnabled(resourcePool)) {
            registry.addTransparentDynamicReconfigPool(poolInfo);
        } else {
            registry.removeTransparentDynamicReconfigPool(poolInfo);
        }
    }

    /**
     * Undeploy the resource from the server's runtime naming context
     *
     * @param poolInfo a resource object
     * @throws UnsupportedOperationException Currently we are not supporting this
     * method.
     */
    private synchronized void actualUndeployResource(PoolInfo poolInfo) throws Exception {
        runtime.deleteConnectorConnectionPool(poolInfo);

        // Performance issue related fix : IT 15784
        ConnectorRegistry.getInstance().removeTransparentDynamicReconfigPool(poolInfo);
        LOG.finest("Pool Undeployed");
    }

    /**
     * Pull out the MCF configuration properties and return them as an array of
     * ConnectorConfigProperty
     *
     * @param adminPool - The JdbcConnectionPool to pull out properties from
     * @param connectorConnectionPool - ConnectorConnectionPool which will be used by Resource
     * Pool
     * @param connectorDescriptor - The ConnectorDescriptor for this JDBC RA
     * @return ConnectorConfigProperty [] array of MCF Config properties specified
     * in this JDBC RA
     */
    private ConnectorConfigProperty[] getMCFConfigProperties(JdbcConnectionPool adminPool, ConnectorConnectionPool connectorConnectionPool, ConnectorDescriptor connectorDescriptor) {
        List<ConnectorConfigProperty> configProperties = new ArrayList<>();

        if (adminPool.getResType() != null) {
            if (JAVA_SQL_DRIVER.equals(adminPool.getResType())) {
                configProperties.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDriverClassname() == null ? "" : adminPool.getDriverClassname(),
                        "The driver class name",
                        String.class.getName()));
            } else {
                configProperties.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDatasourceClassname() == null ? "" : adminPool.getDatasourceClassname(),
                        "The datasource class name",
                        String.class.getName()));
            }
        } else {
            // When resType is null, one of these classnames would be specified
            if (adminPool.getDriverClassname() != null) {
                configProperties.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDriverClassname() == null ? "" : adminPool.getDriverClassname(),
                        "The driver class name",
                        String.class.getName()));
            } else if (adminPool.getDatasourceClassname() != null) {
                configProperties.add(new ConnectorConfigProperty("ClassName",
                        adminPool.getDatasourceClassname() == null ? "" : adminPool.getDatasourceClassname(),
                        "The datasource class name",
                        String.class.getName()));
            }
        }
        configProperties.add(new ConnectorConfigProperty("ConnectionValidationRequired", adminPool.getIsConnectionValidationRequired() + "",
                "Is connection validation required", String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("ValidationMethod",
                adminPool.getConnectionValidationMethod() == null ? "" : adminPool.getConnectionValidationMethod(),
                "How the connection is validated", String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("ValidationTableName",
                adminPool.getValidationTableName() == null ? "" : adminPool.getValidationTableName(), "Validation Table name",
                String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("ValidationClassName",
                adminPool.getValidationClassname() == null ? "" : adminPool.getValidationClassname(), "Validation Class name",
                String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("TransactionIsolation",
                adminPool.getTransactionIsolationLevel() == null ? "" : adminPool.getTransactionIsolationLevel(),
                "Transaction Isolatin Level", String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("GuaranteeIsolationLevel", adminPool.getIsIsolationLevelGuaranteed() + "",
                "Transaction Isolation Guarantee", String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("StatementWrapping", adminPool.getWrapJdbcObjects() + "", "Statement Wrapping",
                String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("StatementTimeout", adminPool.getStatementTimeoutInSeconds() + "", "Statement Timeout",
                String.class.getName()));

        PoolInfo poolInfo = connectorConnectionPool.getPoolInfo();

        configProperties.add(new ConnectorConfigProperty("PoolMonitoringSubTreeRoot",
                ConnectorsUtil.getPoolMonitoringSubTreeRoot(poolInfo, true) + "", "Pool Monitoring Sub Tree Root", String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("PoolName", poolInfo.getName() + "", "Pool Name", SimpleJndiName.class.getName()));

        if (poolInfo.getApplicationName() != null) {
            configProperties.add(new ConnectorConfigProperty("ApplicationName", poolInfo.getApplicationName() + "", "Application Name",
                    String.class.getName()));
        }

        if (poolInfo.getModuleName() != null) {
            configProperties.add(new ConnectorConfigProperty("ModuleName", poolInfo.getModuleName() + "", "Module name", String.class.getName()));
        }

        configProperties.add(new ConnectorConfigProperty("StatementCacheSize", adminPool.getStatementCacheSize() + "", "Statement Cache Size",
                String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("StatementCacheType", adminPool.getStatementCacheType() + "", "Statement Cache Type",
                String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("InitSql", adminPool.getInitSql() + "", "InitSql", String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("SqlTraceListeners", adminPool.getSqlTraceListeners() + "", "Sql Trace Listeners",
                String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("StatementLeakTimeoutInSeconds", adminPool.getStatementLeakTimeoutInSeconds() + "",
                "Statement Leak Timeout in seconds", String.class.getName()));

        configProperties.add(new ConnectorConfigProperty("StatementLeakReclaim", adminPool.getStatementLeakReclaim() + "", "Statement Leak Reclaim",
                String.class.getName()));

        // Dump user defined poperties into the list
        Set<ConnectionDefDescriptor> connectionDefs = connectorDescriptor.getOutboundResourceAdapter().getConnectionDefs();

        // since this a 1.0 RAR, we will have only 1 connDefDesc
        if (connectionDefs.size() != 1) {
            throw new MissingResourceException("Only one connDefDesc present", null, null);
        }

        // Now get the set of MCF config properties associated with each
        // connection-definition. Each element here is an EnviromnentProperty
        Set<ConnectorConfigProperty> mcfConfigProps = null;
        for (ConnectionDefDescriptor connectionDef : connectionDefs) {
            mcfConfigProps = connectionDef.getConfigProperties();
        }

        if (mcfConfigProps != null) {

            Map<String, String> mcfConPropKeys = new HashMap<>();
            Iterator<ConnectorConfigProperty> mcfConfigPropsIter = mcfConfigProps.iterator();
            while (mcfConfigPropsIter.hasNext()) {
                String key = mcfConfigPropsIter.next().getDisplayName();
                mcfConPropKeys.put(key.toUpperCase(LOCALE), key);
            }

            String driverProperties = "";
            for (Property adminPoolProperty : adminPool.getProperty()) {
                if (adminPoolProperty == null) {
                    continue;
                }

                String name = adminPoolProperty.getName();

                // The idea here is to convert the Environment Properties coming from
                // the admin connection pool to standard pool properties thereby
                // making it easy to compare in the event of a reconfig

                if ("MATCHCONNECTIONS".equals(name.toUpperCase(LOCALE))) {
                    // JDBC - matchConnections if not set is decided by the ConnectionManager
                    // so default is false
                    connectorConnectionPool.setMatchConnections(toBoolean(adminPoolProperty.getValue(), false));
                    LOG.log(Level.FINEST, "MATCHCONNECTIONS");

                } else if ("ASSOCIATEWITHTHREAD".equals(name.toUpperCase(LOCALE))) {
                    connectorConnectionPool.setAssociateWithThread(toBoolean(adminPoolProperty.getValue(), false));
                    LOG.log(Level.FINEST, "ASSOCIATEWITHTHREAD");

                } else if ("LAZYCONNECTIONASSOCIATION".equals(name.toUpperCase(LOCALE))) {
                    setLazyEnlistAndLazyAssocProperties(adminPoolProperty.getValue(), adminPool.getProperty(), connectorConnectionPool);
                    LOG.log(Level.FINEST, "LAZYCONNECTIONASSOCIATION");

                } else if ("LAZYCONNECTIONENLISTMENT".equals(name.toUpperCase(Locale.getDefault()))) {
                    connectorConnectionPool.setLazyConnectionEnlist(toBoolean(adminPoolProperty.getValue(), false));
                    LOG.log(Level.FINEST, "LAZYCONNECTIONENLISTMENT");

                } else if ("POOLDATASTRUCTURE".equals(name.toUpperCase(Locale.getDefault()))) {
                    connectorConnectionPool.setPoolDataStructureType(adminPoolProperty.getValue());
                    LOG.log(Level.FINEST, "POOLDATASTRUCTURE");

                } else if (DYNAMIC_RECONFIGURATION_FLAG.equals(name.toLowerCase(LOCALE))) {
                    String value = adminPoolProperty.getValue();
                    try {
                        connectorConnectionPool.setDynamicReconfigWaitTimeout(Long.parseLong(adminPoolProperty.getValue()) * 1000L);
                        LOG.log(Level.FINEST, DYNAMIC_RECONFIGURATION_FLAG);
                    } catch (NumberFormatException nfe) {
                        LOG.log(WARNING, "Invalid value for '" + DYNAMIC_RECONFIGURATION_FLAG + "' : " + value);
                    }
                } else if ("POOLWAITQUEUE".equals(name.toUpperCase(LOCALE))) {
                    connectorConnectionPool.setPoolWaitQueue(adminPoolProperty.getValue());
                    LOG.log(Level.FINEST, "POOLWAITQUEUE");

                } else if ("DATASTRUCTUREPARAMETERS".equals(name.toUpperCase(LOCALE))) {
                    connectorConnectionPool.setDataStructureParameters(adminPoolProperty.getValue());
                    LOG.log(Level.FINEST, "DATASTRUCTUREPARAMETERS");

                } else if ("USERNAME".equals(name.toUpperCase(Locale.getDefault())) || "USER".equals(name.toUpperCase(LOCALE))) {
                    configProperties.add(new ConnectorConfigProperty("User", adminPoolProperty.getValue(), "user name", String.class.getName()));

                } else if ("PASSWORD".equals(name.toUpperCase(LOCALE))) {
                    configProperties.add(new ConnectorConfigProperty("Password", adminPoolProperty.getValue(), "Password", String.class.getName()));

                } else if ("JDBC30DATASOURCE".equals(name.toUpperCase(LOCALE))) {
                    configProperties.add(new ConnectorConfigProperty("JDBC30DataSource", adminPoolProperty.getValue(), "JDBC30DataSource", String.class.getName()));

                } else if ("PREFER-VALIDATE-OVER-RECREATE".equals(name.toUpperCase(Locale.getDefault()))) {
                    String value = adminPoolProperty.getValue();
                    connectorConnectionPool.setPreferValidateOverRecreate(toBoolean(value, false));
                    LOG.log(Level.FINEST, "PREFER-VALIDATE-OVER-RECREATE: {0}", value);

                } else if ("STATEMENT-CACHE-TYPE".equals(name.toUpperCase(Locale.getDefault()))) {
                    if (adminPool.getStatementCacheType() != null) {
                        configProperties.add(
                                new ConnectorConfigProperty("StatementCacheType", adminPoolProperty.getValue(), "StatementCacheType", String.class.getName()));
                    }

                } else if ("NUMBER-OF-TOP-QUERIES-TO-REPORT".equals(name.toUpperCase(Locale.getDefault()))) {
                    configProperties.add(new ConnectorConfigProperty("NumberOfTopQueriesToReport", adminPoolProperty.getValue(), "NumberOfTopQueriesToReport",
                            String.class.getName()));

                } else if ("TIME-TO-KEEP-QUERIES-IN-MINUTES".equals(name.toUpperCase(Locale.getDefault()))) {
                    configProperties.add(new ConnectorConfigProperty("TimeToKeepQueriesInMinutes", adminPoolProperty.getValue(), "TimeToKeepQueriesInMinutes",
                            String.class.getName()));

                } else if (mcfConPropKeys.containsKey(name.toUpperCase(Locale.getDefault()))) {
                    configProperties.add(new ConnectorConfigProperty(mcfConPropKeys.get(name.toUpperCase(Locale.getDefault())),
                            adminPoolProperty.getValue() == null ? "" : adminPoolProperty.getValue(), "Some property", String.class.getName()));
                } else {
                    driverProperties = driverProperties + "set" + escape(name) + "#" + escape(adminPoolProperty.getValue()) + "##";
                }
            }

            if (!driverProperties.isEmpty()) {
                configProperties.add(
                    new ConnectorConfigProperty(
                        "DriverProperties", driverProperties,
                        "some proprietarty properties",
                        String.class.getName()));
            }
        }

        configProperties.add(new ConnectorConfigProperty("Delimiter", "#", "delim", String.class.getName()));
        configProperties.add(new ConnectorConfigProperty("EscapeCharacter", "\\", "escapeCharacter", String.class.getName()));

        // Create an array of EnvironmentProperties from above list
        ConnectorConfigProperty[] environmentProperties = new ConnectorConfigProperty[configProperties.size()];
        ListIterator<ConnectorConfigProperty> propListIter = configProperties.listIterator();

        for (int i = 0; propListIter.hasNext(); i++) {
            environmentProperties[i] = propListIter.next();
        }

        return environmentProperties;
    }

    /**
     * To escape the "delimiter" characters that are internally used by Connector &
     * JDBCRA.
     *
     * @param value String that need to be escaped
     * @return Escaped value
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("#", "\\#");
    }

    private boolean toBoolean(String property, boolean defaultValue) {
        if (property == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(property);
    }

    private ConnectorConnectionPool createConnectorConnectionPool(JdbcConnectionPool adminPool, PoolInfo poolInfo) throws ConnectorRuntimeException {
        String moduleName = JdbcResourcesUtil.createInstance().getRANameofJdbcConnectionPool(adminPool);
        int txSupport = getTxSupport(moduleName);

        ConnectorDescriptor connectorDescriptor = runtime.getConnectorDescriptor(moduleName);

        // Create the connector Connection Pool object from the configbean object
        ConnectorConnectionPool connectorConnectionPool = new ConnectorConnectionPool(poolInfo);

        connectorConnectionPool.setTransactionSupport(txSupport);
        setConnectorConnectionPoolAttributes(connectorConnectionPool, adminPool);

        // Initially create the ConnectorDescriptor
        ConnectorDescriptorInfo connectorDescriptorInfo = createConnectorDescriptorInfo(connectorDescriptor, moduleName);

        connectorDescriptorInfo.setMCFConfigProperties(getMCFConfigProperties(adminPool, connectorConnectionPool, connectorDescriptor));

        // Since we are deploying a 1.0 RAR, this is null
        connectorDescriptorInfo.setResourceAdapterConfigProperties((Set<ConnectorConfigProperty>) null);

        connectorConnectionPool.setConnectorDescriptorInfo(connectorDescriptorInfo);

        return connectorConnectionPool;
    }

    private int getTxSupport(String moduleName) {
        if (JDBCXA_RA_NAME.equals(moduleName)) {
            return ConnectionPoolObjectsUtils.parseTransactionSupportString(XA_TRANSACTION_TX_SUPPORT_STRING);
        }

        return ConnectionPoolObjectsUtils.parseTransactionSupportString(LOCAL_TRANSACTION_TX_SUPPORT_STRING);
    }

    private ConnectorDescriptorInfo createConnectorDescriptorInfo(ConnectorDescriptor connectorDescriptor, String moduleName) {
        ConnectorDescriptorInfo connectorDescriptorInfo = new ConnectorDescriptorInfo();

        connectorDescriptorInfo.setManagedConnectionFactoryClass(connectorDescriptor.getOutboundResourceAdapter().getManagedConnectionFactoryImpl());
        connectorDescriptorInfo.setRarName(moduleName);
        connectorDescriptorInfo.setResourceAdapterClassName(connectorDescriptor.getResourceAdapterClass());
        connectorDescriptorInfo.setConnectionDefinitionName(connectorDescriptor.getOutboundResourceAdapter().getConnectionFactoryIntf());
        connectorDescriptorInfo.setConnectionFactoryClass(connectorDescriptor.getOutboundResourceAdapter().getConnectionFactoryImpl());
        connectorDescriptorInfo.setConnectionFactoryInterface(connectorDescriptor.getOutboundResourceAdapter().getConnectionFactoryIntf());
        connectorDescriptorInfo.setConnectionClass(connectorDescriptor.getOutboundResourceAdapter().getConnectionImpl());
        connectorDescriptorInfo.setConnectionInterface(connectorDescriptor.getOutboundResourceAdapter().getConnectionIntf());

        return connectorDescriptorInfo;
    }

    private void setConnectorConnectionPoolAttributes(ConnectorConnectionPool connectorConnectionPool, JdbcConnectionPool adminPool) {
        SimpleJndiName poolName = connectorConnectionPool.getName();

        connectorConnectionPool.setMaxPoolSize(adminPool.getMaxPoolSize());
        connectorConnectionPool.setSteadyPoolSize(adminPool.getSteadyPoolSize());
        connectorConnectionPool.setMaxWaitTimeInMillis(adminPool.getMaxWaitTimeInMillis());
        connectorConnectionPool.setPoolResizeQuantity(adminPool.getPoolResizeQuantity());
        connectorConnectionPool.setIdleTimeoutInSeconds(adminPool.getIdleTimeoutInSeconds());
        connectorConnectionPool.setFailAllConnections(Boolean.parseBoolean(adminPool.getFailAllConnections()));
        connectorConnectionPool.setConnectionValidationRequired(Boolean.parseBoolean(adminPool.getIsConnectionValidationRequired()));
        connectorConnectionPool.setNonTransactional(Boolean.parseBoolean(adminPool.getNonTransactionalConnections()));
        connectorConnectionPool.setNonComponent(Boolean.parseBoolean(adminPool.getAllowNonComponentCallers()));
        connectorConnectionPool.setPingDuringPoolCreation(Boolean.parseBoolean(adminPool.getPing()));

        // These are default properties of all Jdbc pools
        // So set them here first and then figure out from the parsing routine
        // if they need to be reset
        connectorConnectionPool.setMatchConnections(Boolean.parseBoolean(adminPool.getMatchConnections()));
        connectorConnectionPool.setAssociateWithThread(Boolean.parseBoolean(adminPool.getAssociateWithThread()));
        connectorConnectionPool.setConnectionLeakTracingTimeout(adminPool.getConnectionLeakTimeoutInSeconds());
        connectorConnectionPool.setConnectionReclaim(Boolean.parseBoolean(adminPool.getConnectionLeakReclaim()));

        boolean lazyConnectionEnlistment = Boolean.parseBoolean(adminPool.getLazyConnectionEnlistment());
        boolean lazyConnectionAssociation = Boolean.parseBoolean(adminPool.getLazyConnectionAssociation());

        // lazy-connection-enlistment need to be ON for lazy-connection-association to
        // work.
        if (lazyConnectionAssociation) {
            if (lazyConnectionEnlistment) {
                connectorConnectionPool.setLazyConnectionAssoc(true);
                connectorConnectionPool.setLazyConnectionEnlist(true);
            } else {
                LOG.log(SEVERE, "conn_pool_obj_utils.lazy_enlist-lazy_assoc-invalid-combination", poolName);
                throw new RuntimeException(STRINGS.getString("cpou.lazy_enlist-lazy_assoc-invalid-combination", poolName));
            }
        } else {
            connectorConnectionPool.setLazyConnectionAssoc(lazyConnectionAssociation);
            connectorConnectionPool.setLazyConnectionEnlist(lazyConnectionEnlistment);
        }

        boolean pooling = Boolean.parseBoolean(adminPool.getPooling());

        if (!pooling) {
            // Throw exception if assoc with thread is set to true.
            if (Boolean.parseBoolean(adminPool.getAssociateWithThread())) {
                LOG.log(SEVERE, "conn_pool_obj_utils.pooling_disabled_assocwiththread_invalid_combination", poolName);
                throw new RuntimeException(STRINGS.getString("cpou.pooling_disabled_assocwiththread_invalid_combination", poolName));
            }

            // Below are useful in pooled environment only.
            // Throw warning for connection validation/validate-atmost-once/
            // match-connections/max-connection-usage-count/idele-timeout
            if (Boolean.parseBoolean(adminPool.getIsConnectionValidationRequired())) {
                LOG.log(WARNING, "conn_pool_obj_utils.pooling_disabled_conn_validation_invalid_combination", poolName);
            }
            if (Integer.parseInt(adminPool.getValidateAtmostOncePeriodInSeconds()) > 0) {
                LOG.log(WARNING, "conn_pool_obj_utils.pooling_disabled_validate_atmost_once_invalid_combination", poolName);
            }
            if (Boolean.parseBoolean(adminPool.getMatchConnections())) {
                LOG.log(WARNING, "conn_pool_obj_utils.pooling_disabled_match_connections_invalid_combination", poolName);
            }
            if (Integer.parseInt(adminPool.getMaxConnectionUsageCount()) > 0) {
                LOG.log(WARNING, "conn_pool_obj_utils.pooling_disabled_max_conn_usage_invalid_combination", poolName);
            }
            if (Integer.parseInt(adminPool.getIdleTimeoutInSeconds()) > 0) {
                LOG.log(WARNING, "conn_pool_obj_utils.pooling_disabled_idle_timeout_invalid_combination", poolName);
            }
        }

        connectorConnectionPool.setPooling(pooling);
        connectorConnectionPool.setMaxConnectionUsage(adminPool.getMaxConnectionUsageCount());

        connectorConnectionPool.setConCreationRetryAttempts(adminPool.getConnectionCreationRetryAttempts());
        connectorConnectionPool.setConCreationRetryInterval(adminPool.getConnectionCreationRetryIntervalInSeconds());

        connectorConnectionPool.setValidateAtmostOncePeriod(adminPool.getValidateAtmostOncePeriodInSeconds());
    }


    private void handlePoolRecreation(final ConnectorConnectionPool connConnPool) {
        LOG.log(Level.FINE, "handlePoolRecreation(connConnPool={0})", connConnPool);

        final long reconfigWaitTimeout = connConnPool.getDynamicReconfigWaitTimeout();
        PoolInfo poolInfo = new PoolInfo(connConnPool.getName(), connConnPool.getApplicationName(), connConnPool.getModuleName());
        final ResourcePool oldPool = runtime.getPoolManager().getPool(poolInfo);

        if (reconfigWaitTimeout > 0) {
            oldPool.blockRequests(reconfigWaitTimeout);

            if (oldPool.getPoolWaitQueue().getQueueLength() > 0 || oldPool.getPoolStatus().getNumConnUsed() > 0) {

                Runnable runnable = () -> {
                    long numSeconds = 5000L; // poll every 5 seconds
                    long steps = reconfigWaitTimeout / numSeconds;
                    if (steps == 0) {
                        waitForCompletion(steps, oldPool, reconfigWaitTimeout);
                    } else {
                        for (long i = 0; i < steps; i++) {
                            waitForCompletion(steps, oldPool, reconfigWaitTimeout);
                            if (oldPool.getPoolWaitQueue().getQueueLength() == 0 && oldPool.getPoolStatus().getNumConnUsed() == 0) {
                                LOG.finest("wait-queue is empty and num-con-used is 0");
                                break;
                            }
                        }
                    }

                    handlePoolRecreationForExistingProxies(connConnPool);

                    PoolWaitQueue reconfigWaitQueue = oldPool.getReconfigWaitQueue();
                    LOG.finest("checking reconfig-wait-queue for notification");
                    if (reconfigWaitQueue.getQueueContents().size() > 0) {
                        for (Object o : reconfigWaitQueue.getQueueContents()) {
                            LOG.fine("notifying reconfig-wait-queue object [ " + o + " ]");
                            synchronized (o) {
                                o.notify();
                            }
                        }
                    }

                    LOG.finest(() ->
                        "woke-up after giving time for in-use connections to return, " +
                            "WaitQueue-Length : [" + oldPool.getPoolWaitQueue().getQueueContents() + "], " +
                            "Num-Conn-Used : [" + oldPool.getPoolStatus().getNumConnUsed() + "]");
                };

                try {
                    execService.invokeAll(asList(callable(runnable)));
                } catch (Exception e) {
                    LOG.log(WARNING, "exception.redeploying.pool.transparently",
                        new Object[] {connConnPool.getName(), e});
                }

            } else {
                handlePoolRecreationForExistingProxies(connConnPool);
            }
        } else if (oldPool.getReconfigWaitTime() > 0) {
            // Reconfig is being switched off, invalidate proxies
            Collection<BindableResource> resources = getResourcesOfPool(
                runtime.getResources(oldPool.getPoolInfo()),
                oldPool.getPoolInfo().getName());

            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            for (BindableResource resource : resources) {
                registry.removeResourceFactories(getResourceInfo(resource));
            }

            // Recreate the pool now.
            recreatePool(connConnPool);
        } else {
            recreatePool(connConnPool);
        }
    }

    private void waitForCompletion(long steps, ResourcePool oldPool, long totalWaitTime) {
        LOG.finest("waiting for in-use connections to return to pool or waiting requests to complete");

        try {
            Object poolWaitQueue = oldPool.getPoolWaitQueue();
            synchronized (poolWaitQueue) {
                long waitTime = totalWaitTime / steps;
                if (waitTime > 0) {
                    poolWaitQueue.wait(waitTime);
                }
            }
        } catch (InterruptedException ie) {
            // ignore
        }

        LOG.finest("woke-up to verify in-use / waiting requests list");
    }

    private void handlePoolRecreationForExistingProxies(ConnectorConnectionPool connectorConnectionPool) {
        recreatePool(connectorConnectionPool);

        Collection<BindableResource> resourcesList;
        if (!connectorConnectionPool.isApplicationScopedResource()) {
            resourcesList = getResourcesOfPool(domain.getResources(), connectorConnectionPool.getName());
        } else {
            PoolInfo poolInfo = connectorConnectionPool.getPoolInfo();
            Resources resources = ResourcesUtil.createInstance().getResources(poolInfo);
            resourcesList = getResourcesOfPool(resources, connectorConnectionPool.getName());
        }

        for (BindableResource bindableResource : resourcesList) {
            ResourceInfo resourceInfo = getResourceInfo(bindableResource);
            ConnectorRegistry.getInstance().updateResourceInfoVersion(resourceInfo);
        }
    }

    private void recreatePool(ConnectorConnectionPool connConnPool) {
        try {
            runtime.recreateConnectorConnectionPool(connConnPool);
            LOG.log(Level.CONFIG, "Pool [{0}] recreation done", connConnPool.getName());
        } catch (ConnectorRuntimeException cre) {
            LOG.log(WARNING, "error.redeploying.jdbc.pool", new Object[] { connConnPool.getName(), cre });
        }
    }
}
