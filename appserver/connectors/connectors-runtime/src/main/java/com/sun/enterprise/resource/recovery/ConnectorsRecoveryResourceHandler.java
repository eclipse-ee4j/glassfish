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

package com.sun.enterprise.resource.recovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.security.common.UserNameAndPassword;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsClassLoaderUtil;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.deployer.ConnectorResourceDeployer;
import com.sun.enterprise.transaction.spi.RecoveryResourceHandler;
import com.sun.enterprise.v3.server.ApplicationLoaderService;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.security.PasswordCredential;

/**
 * Recovery handler for connector resources
 *
 * @author Jagadish Ramu
 */
@Service
public class ConnectorsRecoveryResourceHandler implements RecoveryResourceHandler {

    @Inject
    private Domain domain;

    @Inject
    private Applications applications;

    @Inject
    private ConnectorsClassLoaderUtil cclUtil;

    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;

    @Inject
    private Provider<ConnectorResourceDeployer> connectorResourceDeployerProvider;

    @Inject
    @Named("ApplicationLoaderService")
    private Provider<ApplicationLoaderService> startupProvider;

    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    private ResourcesUtil resourcesUtil;

    private static final Logger LOG = LogDomains.getLogger(ConnectorsRecoveryResourceHandler.class, LogDomains.RSR_LOGGER);
    private static final Locale locale = Locale.getDefault();

    private Collection<ConnectorResource> getAllConnectorResources() {
        Collection<ConnectorResource> allResources = new ArrayList<>();
        Collection<ConnectorResource> connectorResources = domain.getResources().getResources(ConnectorResource.class);
        allResources.addAll(connectorResources);
        for (Application app : applications.getApplications()) {
            if (ResourcesUtil.createInstance().isEnabled(app)) {
                Resources appScopedResources = app.getResources();
                if (appScopedResources != null && appScopedResources.getResources() != null) {
                    allResources.addAll(appScopedResources.getResources(ConnectorResource.class));
                }
                List<Module> modules = app.getModule();
                if (modules != null) {
                    for (Module module : modules) {
                        Resources msr = module.getResources();
                        if (msr != null && msr.getResources() != null) {
                            allResources.addAll(msr.getResources(ConnectorResource.class));
                        }
                    }
                }
            }
        }
        return allResources;
    }

    /**
     * does a lookup of resources so that they are loaded for sure.
     */
    private void loadAllConnectorResources() {
        try {
            Collection<ConnectorResource> connResources = getAllConnectorResources();
            InitialContext ic = new InitialContext();
            for (ConnectorResource connResource : connResources) {
                if (getResourcesUtil().isEnabled(connResource)) {
                    try {
                        ic.lookup(connResource.getJndiName());
                    } catch (NamingException ne) {
                        // If you are here then it is most probably an embedded RAR resource
                        // So we need to explicitly load that rar and create the resources
                        try {
                            ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(connResource);
                            ConnectorConnectionPool connConnectionPool = getResourcesUtil()
                                    .getConnectorConnectionPoolOfResource(resourceInfo);
                            if (connConnectionPool != null) {
                                // TODO V3 ideally this should not happen if connector modules (and
                                // embedded rars) are loaded before recovery
                                createActiveResourceAdapter(connConnectionPool.getResourceAdapterName());
                                getConnectorResourceDeployer().deployResource(connResource);
                            }
                        } catch (Exception ex) {
                            LOG.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", connResource.getJndiName());
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, ne.toString(), ne);
                            }
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", connResource.getJndiName());
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, ex.toString(), ex);
                        }
                    }
                }
            }
        } catch (NamingException ne) {
            LOG.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", ne.getMessage());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, ne.toString(), ne);
            }
        }
    }

    private ResourcesUtil getResourcesUtil() {
        if (resourcesUtil == null) {
            resourcesUtil = ResourcesUtil.createInstance();
        }
        return resourcesUtil;
    }

    private ConnectorResourceDeployer getConnectorResourceDeployer() {
        return connectorResourceDeployerProvider.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadXAResourcesAndItsConnections(List xaresList, List connList) {

        // Done so as to initialize connectors-runtime before loading connector-resources. need a better way ?
        ConnectorRuntime crt = connectorRuntimeProvider.get();

        // Done so as to load all connector-modules. need to load only connector-modules instead of all apps
        startupProvider.get();

        Collection<ConnectorResource> connectorResources = getAllConnectorResources();

        if (connectorResources.isEmpty()) {
            return;
        }

        List<ConnectorConnectionPool> connPools = new ArrayList<>();
        for (Resource resource : connectorResources) {
            ConnectorResource connResource = (ConnectorResource) resource;
            if (getResourcesUtil().isEnabled(connResource)) {
                ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(connResource);
                ConnectorConnectionPool pool = ResourcesUtil.createInstance().getConnectorConnectionPoolOfResource(resourceInfo);
                if (pool != null && ConnectorConstants.XA_TRANSACTION_TX_SUPPORT_STRING.equals(getTransactionSupport(pool))) {
                    connPools.add(pool);
                    LOG.log(Level.FINE, "ConnectorsRecoveryResourceHandler loadXAResourcesAndItsConnections :: adding : {0}",
                            connResource.getPoolName());
                }
            }
        }
        loadAllConnectorResources();

        LOG.log(Level.FINE, "Recovering pools : {0}", connPools.size());

        for (ConnectorConnectionPool connPool : connPools) {

            PoolInfo poolInfo = ConnectorsUtil.getPoolInfo(connPool);

            try {
                String[] dbUserPassword = getdbUserPasswordOfConnectorConnectionPool(connPool);
                Subject subject = new Subject();

                // If username or password of the connector connection pool
                // is null a warning is logged and recovery continues with
                // empty String username or password as the case may be,
                // because some databases allow null[as in empty string]
                // username [pointbase interprets this as "root"]/password.
                final String dbPassword;
                if (dbUserPassword[1] == null) {
                    dbPassword = "";
                    LOG.log(Level.FINEST, "datasource.xadatasource_nullpassword_error", poolInfo);
                } else {
                    dbPassword = dbUserPassword[1];
                }
                final String dbUser;
                if (dbUserPassword[0] == null) {
                    dbUser = "";
                    LOG.log(Level.FINEST, "datasource.xadatasource_nulluser_error", poolInfo);
                } else {
                    dbUser = dbUserPassword[0];
                }
                final UserNameAndPassword principal = new UserNameAndPassword(dbUser, dbPassword);
                String rarName = connPool.getResourceAdapterName();
                if (ConnectorAdminServiceUtils.isJMSRA(rarName)) {
                    LOG.log(Level.FINE, "Performing recovery for JMS RA, poolName {0}", poolInfo);
                    ManagedConnectionFactory[] mcfs = crt.obtainManagedConnectionFactories(poolInfo);
                    LOG.log(Level.INFO, "JMS resource recovery has created CFs = {0}", mcfs.length);
                    for (ManagedConnectionFactory mcf : mcfs) {
                        PasswordCredential pc = new PasswordCredential(principal.getName(), principal.getPassword());
                        pc.setManagedConnectionFactory(mcf);
                        subject.getPrincipals().add(principal);
                        subject.getPrivateCredentials().add(pc);
                        ManagedConnection mc = mcf.createManagedConnection(subject, null);
                        connList.add(mc);
                        try {
                            XAResource xares = mc.getXAResource();
                            if (xares != null) {
                                xaresList.add(xares);
                            }
                        } catch (ResourceException ex) {
                            // ignored. Not at XA_TRANSACTION level
                        }
                    }

                } else {
                    ManagedConnectionFactory mcf = crt.obtainManagedConnectionFactory(poolInfo);
                    PasswordCredential pc = new PasswordCredential(dbUser, dbPassword.toCharArray());
                    pc.setManagedConnectionFactory(mcf);
                    subject.getPrincipals().add(principal);
                    subject.getPrivateCredentials().add(pc);
                    ManagedConnection mc = mcf.createManagedConnection(subject, null);
                    connList.add(mc);
                    try {
                        XAResource xares = mc.getXAResource();
                        if (xares != null) {
                            xaresList.add(xares);
                        }
                    } catch (ResourceException ex) {
                        // ignored. Not at XA_TRANSACTION level
                    }
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "datasource.xadatasource_error", poolInfo);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "datasource.xadatasource_error_excp", ex);
                }
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Total XAResources identified for recovery is " + xaresList.size());
            LOG.log(Level.FINE, "Total connections identified for recovery is " + connList.size());
        }
    }

    /**
     * provides the transaction support for the pool. If none specified in the pool, tx support at RA level will be
     * returned.
     *
     * @param pool connector connection pool
     * @return tx support level
     */
    private String getTransactionSupport(ConnectorConnectionPool pool) {

        String txSupport = pool.getTransactionSupport();

        if (txSupport != null) {
            return txSupport;
        }

        try {
            txSupport = ConnectorRuntime.getRuntime().getConnectorDescriptor(pool.getResourceAdapterName()).getOutboundResourceAdapter()
                    .getTransSupport();
        } catch (ConnectorRuntimeException cre) {
            Object[] params = new Object[] { pool.getResourceAdapterName(), cre };
            LOG.log(Level.WARNING, "error.retrieving.tx-support.from.rar", params);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("setting no-tx-support as tx-support-level for pool : " + pool.getName());
            }
            txSupport = ConnectorConstants.NO_TRANSACTION_TX_SUPPORT_STRING;
        }

        return txSupport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnections(List connList) {
        for (Object obj : connList) {
            try {
                ((ManagedConnection) obj).destroy();
            } catch (Exception ex) {
                // Since closing error has been advised to be ignored
                // so we are not logging the message as an exception
                // but treating the same as a debug message
                // Santanu De, Sun Microsystems, 2002.
                LOG.log(Level.WARNING, "Connector Resource could not be closed", ex);
            }
        }
    }

    private String[] getdbUserPasswordOfConnectorConnectionPool(ConnectorConnectionPool connectorConnectionPool) {
        String[] userPassword = new String[2];
        userPassword[0] = null;
        userPassword[1] = null;
        List<Property> properties = connectorConnectionPool.getProperty();
        if (properties != null) {
            boolean foundUserPassword = false;
            for (Property elementProperty : properties) {
                String prop = elementProperty.getName().toUpperCase(locale);
                if ("USERNAME".equals(prop) || "USER".equals(prop)) {
                    userPassword[0] = elementProperty.getValue();
                    foundUserPassword = true;
                } else if ("PASSWORD".equals(prop)) {
                    userPassword[1] = elementProperty.getValue();
                    foundUserPassword = true;
                }
            }
            if (foundUserPassword) {
                return userPassword;
            }
        }

        PoolInfo poolInfo = ConnectorsUtil.getPoolInfo(connectorConnectionPool);
        String rarName = connectorConnectionPool.getResourceAdapterName();
        String connectionDefName = connectorConnectionPool.getConnectionDefinitionName();
        ConnectorRegistry connectorRegistry = ConnectorRegistry.getInstance();
        ConnectorDescriptor connectorDescriptor = connectorRegistry.getDescriptor(rarName);
        ConnectionDefDescriptor cdd = connectorDescriptor.getConnectionDefinitionByCFType(connectionDefName);
        Set<ConnectorConfigProperty> configProps = cdd.getConfigProperties();
        for (ConnectorConfigProperty envProp : configProps) {
            String prop = envProp.getName().toUpperCase(locale);
            if ("USER".equals(prop) || "USERNAME".equals(prop)) {
                userPassword[0] = envProp.getValue();
            } else if ("PASSWORD".equals(prop)) {
                userPassword[1] = envProp.getValue();
            }
        }
        if (userPassword[0] != null && !userPassword[0].isBlank()) {
            return userPassword;
        }

        // else read the default username and password from the ra.xml
        ManagedConnectionFactory mcf = connectorRegistry.getManagedConnectionFactory(poolInfo);
        userPassword[0] = ConnectionPoolObjectsUtils.getValueFromMCF("User", poolInfo, mcf);
        userPassword[1] = ConnectionPoolObjectsUtils.getValueFromMCF("Password", poolInfo, mcf);
        return userPassword;
    }

    private void createActiveResourceAdapter(String rarModuleName) throws ConnectorRuntimeException {
        ConnectorRuntime cr = ConnectorRuntime.getRuntime();
        ConnectorRegistry creg = ConnectorRegistry.getInstance();

        if (creg.isRegistered(rarModuleName)) {
            return;
        }

        if (ConnectorAdminServiceUtils.isEmbeddedConnectorModule(rarModuleName)) {
            cr.createActiveResourceAdapterForEmbeddedRar(rarModuleName);
        } else {
            String moduleDir;
            if (ConnectorsUtil.belongsToSystemRA(rarModuleName)) {
                moduleDir = ConnectorsUtil.getSystemModuleLocation(rarModuleName);
            } else {
                moduleDir = configBeansUtilities.getLocation(rarModuleName);
            }
            ClassLoader loader = cr.createConnectorClassLoader(moduleDir, null, rarModuleName);
            cr.createActiveResourceAdapter(moduleDir, rarModuleName, loader);
        }
    }
}
