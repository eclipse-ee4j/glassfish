/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors;

import static com.sun.appserv.connectors.internal.api.ConnectorConstants.DEFAULT_JMS_ADAPTER;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.LOCAL_TRANSACTION_INT;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.NON_TX_JNDI_SUFFIX;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.NO_TRANSACTION_INT;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.XA_TRANSACTION_INT;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getPMJndiName;
import static com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils.createSubject;
import static com.sun.enterprise.deployment.ResourceReferenceDescriptor.APPLICATION_AUTHORIZATION;
import static com.sun.logging.LogDomains.RSR_LOGGER;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static org.glassfish.resourcebase.resources.api.ResourceConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.appserv.connectors.internal.spi.ConnectionManager;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.connectors.authentication.AuthenticationService;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ConnectorAllocator;
import com.sun.enterprise.resource.allocator.LocalTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.NoTxConnectorAllocator;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.IllegalStateException;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.resource.spi.RetryableUnavailableException;
import jakarta.resource.spi.SecurityException;

/**
 * @author Tony Ng
 */
public class ConnectionManagerImpl implements ConnectionManager, Serializable {

    private static final long serialVersionUID = 1L;
    protected String jndiName;
    protected String logicalName;
    protected PoolInfo poolInfo;
    protected ResourceInfo resourceInfo;

    private volatile static Logger logger = LogDomains.getLogger(ConnectionManagerImpl.class, RSR_LOGGER);
    private volatile static StringManager localStrings = StringManager.getManager(ConnectionManagerImpl.class);

    protected String rarName;

    private transient BindableResource resourceConfiguration;

    protected ResourcePrincipalDescriptor defaultResourcePrincipalDescriptor;

    public ConnectionManagerImpl(PoolInfo poolInfo, ResourceInfo resourceInfo) {
        this.poolInfo = poolInfo;
        this.resourceInfo = resourceInfo;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    @Override
    public String getJndiName() {
        return jndiName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String getLogicalName() {
        return logicalName;
    }

    /**
     * Allocate a non transactional connection. This connection, even if acquired in the context of an existing transaction,
     * will never be associated with a transaction The typical use case may be to check the original contents of an EIS when
     * a transacted connection is changing the contents, and the tx is yet to be committed.
     *
     * <p/>
     * We create a ResourceSpec for a non tx connection with a name ending in __nontx. This is to maintain uniformity with
     * the scheme of having __pm connections. If one were to create a resource with a jndiName ending with __nontx the same
     * functionality might be achieved.
     */
    @Override
    public Object allocateNonTxConnection(ManagedConnectionFactory managedConnectionFactory, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        String localJndiName = jndiName;

        logFine("Allocating NonTxConnection");

        // If a resource has been created with __nontx, we don't want to
        // add it again.
        // Otherwise we need to add __nontx at the end to ensure that the
        // mechanism to check for the correct resource manager still works
        // We do the addition if and only if we are getting this call
        // from a normal datasource and not a __nontx datasource.
        if (!jndiName.endsWith(NON_TX_JNDI_SUFFIX)) {
            localJndiName = jndiName + NON_TX_JNDI_SUFFIX;

            logFine("Adding __nontx to jndiname");
        } else {
            logFine("lookup happened from a __nontx datasource directly");
        }

        return allocateConnection(managedConnectionFactory, connectionRequestInfo, localJndiName);
    }

    @Override
    public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return allocateConnection(mcf, cxRequestInfo, jndiName);
    }

    public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo, String jndiNameToUse) throws ResourceException {
        return allocateConnection(mcf, cxRequestInfo, jndiNameToUse, null);
    }

    public Object allocateConnection(ManagedConnectionFactory managedConnectionFactory, ConnectionRequestInfo cxRequestInfo, String jndiNameToUse, Object connection) throws ResourceException {
        validateResourceAndPool();
        PoolManager poolmgr = ConnectorRuntime.getRuntime().getPoolManager();
        boolean resourceShareable = true;

        ResourceReferenceDescriptor resourceReferenceDescriptor = poolmgr.getResourceReference(jndiNameToUse, logicalName);

        if (resourceReferenceDescriptor != null) {
            String shareableStr = resourceReferenceDescriptor.getSharingScope();

            if (shareableStr.equals(ResourceReferenceDescriptor.RESOURCE_UNSHAREABLE)) {
                resourceShareable = false;
            }
        }

        // TODO V3 refactor all the 3 cases viz, no res-ref, app-auth, cont-auth.
        if (resourceReferenceDescriptor == null) {
            if (getLogger().isLoggable(FINE)) {
                getLogger().log(FINE, "poolmgr.no_resource_reference", jndiNameToUse);
            }

            return internalGetConnection(
                    managedConnectionFactory,
                    defaultResourcePrincipalDescriptor,
                    cxRequestInfo,
                    resourceShareable,
                    jndiNameToUse,
                    connection, true);
        }

        String authorization = resourceReferenceDescriptor.getAuthorization();

        if (authorization.equals(APPLICATION_AUTHORIZATION)) {
            if (cxRequestInfo == null) {
                throw new ResourceException(getLocalStrings().getString("con_mgr.null_userpass"));
            }
            ConnectorRuntime.getRuntime().switchOnMatching(rarName, poolInfo);

            return internalGetConnection(
                    managedConnectionFactory,
                    null,
                    cxRequestInfo,
                    resourceShareable,
                    jndiNameToUse,
                    connection, false);
        }

        ResourcePrincipalDescriptor resourcePrincipalDescriptor = null;
        Set<Principal> principalSet = null;
        Principal callerPrincipal = null;
        SecurityContext securityContext = null;
        ConnectorRuntime connectorRuntime = ConnectorRuntime.getRuntime();

        // TODO V3 is SecurityContext.getCurrent() the right way ? Does it need to be injected ?
        if (connectorRuntime.isServer() && (securityContext = SecurityContext.getCurrent()) != null
                && (callerPrincipal = securityContext.getCallerPrincipal()) != null
                && (principalSet = securityContext.getPrincipalSet()) != null) {

            AuthenticationService authenticationService = connectorRuntime.getAuthenticationService(rarName, poolInfo);
            if (authenticationService != null) {
                resourcePrincipalDescriptor = authenticationService.mapPrincipal(callerPrincipal, principalSet);
            }
        }

        if (resourcePrincipalDescriptor == null) {
            resourcePrincipalDescriptor = resourceReferenceDescriptor.getResourcePrincipal();
            if (resourcePrincipalDescriptor == null) {
                getLogger().log(FINE, () ->
                                "default-resource-principal not specified for " + jndiNameToUse +
                                ". Defaulting to user/password specified in the pool");

                resourcePrincipalDescriptor = defaultResourcePrincipalDescriptor;
            } else if (!resourcePrincipalDescriptor.equals(defaultResourcePrincipalDescriptor)) {
                ConnectorRuntime.getRuntime().switchOnMatching(rarName, poolInfo);
            }
        }

        return internalGetConnection(
                managedConnectionFactory,
                resourcePrincipalDescriptor,
                cxRequestInfo,
                resourceShareable,
                jndiNameToUse,
                connection, false);
    }

    protected Object internalGetConnection(ManagedConnectionFactory mcf, final ResourcePrincipalDescriptor prin, ConnectionRequestInfo cxRequestInfo, boolean shareable, String jndiNameToUse, Object connection, boolean isUnknownAuth) throws ResourceException {
        try {
            PoolManager poolManager = ConnectorRuntime.getRuntime().getPoolManager();
            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            PoolMetaData poolMetaData = registry.getPoolMetaData(poolInfo);

            ResourceSpec resourceSpec = new ResourceSpec(jndiNameToUse, ResourceSpec.JNDI_NAME, poolMetaData);
            resourceSpec.setPoolInfo(this.poolInfo);
            ManagedConnectionFactory freshManagedConnectionFactory = poolMetaData.getMCF();

            if (getLogger().isLoggable(Level.INFO)) {
                if (!freshManagedConnectionFactory.equals(mcf)) {
                    getLogger().info("conmgr.mcf_not_equal");
                }
            }
            ConnectorDescriptor rarConnectorDescriptor = registry.getDescriptor(rarName);

            Subject subject = null;
            ClientSecurityInfo info = null;
            if (isUnknownAuth && rarName.equals(DEFAULT_JMS_ADAPTER) && !(poolMetaData.isAuthCredentialsDefinedInPool())) {

                // Unknown authorization.
                //
                // This is the case for standalone java clients, where the authorization is neither
                // container nor component managed.
                //
                // In this case we associate an non-null Subject with no credentials, so that the RA
                // can either use its own custom logic for figuring out the credentials.
                //
                // Relevant connector spec section is 9.1.8.2.
                //
                // create non-null Subject associated with no credentials

                subject = createSubject(mcf, null);
            } else if (prin == null) {
                info = new ClientSecurityInfo(cxRequestInfo);
            } else {
                info = new ClientSecurityInfo(prin);
                if (prin.equals(defaultResourcePrincipalDescriptor)) {
                    subject = poolMetaData.getSubject();
                } else {
                    subject = ConnectionPoolObjectsUtils.createSubject(mcf, prin);
                }
            }

            int txLevel = poolMetaData.getTransactionSupport();
            if (getLogger().isLoggable(FINE)) {
                logFine("ConnectionMgr: poolName " + poolInfo + "  txLevel : " + txLevel);
            }

            if (connection != null) {
                resourceSpec.setConnectionToAssociate(connection);
            }

            return getResource(txLevel, poolManager, mcf, resourceSpec, subject, cxRequestInfo, info, rarConnectorDescriptor, shareable);

        } catch (PoolingException ex) {
            Object[] params = new Object[] { poolInfo, ex };
            getLogger().log(Level.WARNING, "poolmgr.get_connection_failure", params);

            // GLASSFISH-19609
            //
            // We can't simply look for ResourceException and throw back since
            // Connector Container also throws ResourceException which might
            // hide the SecurityException thrown by RA.
            //
            // So, we try to track SecurityException

            unwrapSecurityException(ex);

            throw new ResourceAllocationException(getLocalStrings().getString("con_mgr.error_creating_connection", ex.getMessage()), ex);
        }
    }

    private void unwrapSecurityException(Throwable ex) throws ResourceException {
        if (ex != null) {
            if (ex instanceof SecurityException) {
                throw (SecurityException) ex;
            }

            unwrapSecurityException(ex.getCause());
        }
    }

    private Object getResource(int txLevel, PoolManager poolManager, ManagedConnectionFactory managedConnectionFactory, ResourceSpec resourceSpec, Subject subject, ConnectionRequestInfo connectionRequestInfo, ClientSecurityInfo info, ConnectorDescriptor connectorDescriptor, boolean shareable) throws PoolingException, ResourceAllocationException, IllegalStateException, RetryableUnavailableException {
        ResourceAllocator resourceAllocator;

        switch (txLevel) {
            case NO_TRANSACTION_INT:
                resourceAllocator = new NoTxConnectorAllocator(poolManager, managedConnectionFactory, resourceSpec, subject, connectionRequestInfo, info, connectorDescriptor);
                break;

            case LOCAL_TRANSACTION_INT:
                resourceAllocator = new LocalTxConnectorAllocator(poolManager, managedConnectionFactory, resourceSpec, subject, connectionRequestInfo, info, connectorDescriptor, shareable);
                break;

            case XA_TRANSACTION_INT:
                if (rarName.equals(DEFAULT_JMS_ADAPTER)) {
                    shareable = false;
                }
                resourceSpec.markAsXA();
                resourceAllocator = new ConnectorAllocator(poolManager, managedConnectionFactory, resourceSpec, subject, connectionRequestInfo, info, connectorDescriptor, shareable);
                break;

            default:
                throw new IllegalStateException(getLocalStrings().getString("con_mgr.illegal_tx_level", txLevel + " "));
        }

        return poolManager.getResource(resourceSpec, resourceAllocator, info);
    }

    public void setRarName(String _rarName) {
        rarName = _rarName;
    }

    public String getRarName() {
        return rarName;
    }

    /*
     * This method is called from the ConnectorObjectFactory lookup With this we move all the housekeeping work in
     * allocateConnection up-front
     */
    public void initialize() throws ConnectorRuntimeException {
        ConnectorRuntime runtime = ConnectorRuntime.getRuntime();

        if (runtime.isNonACCRuntime()) {
            jndiName = getPMJndiName(jndiName);
        }

        defaultResourcePrincipalDescriptor =
            ConnectorRegistry.getInstance()
                             .getPoolMetaData(poolInfo)
                             .getResourcePrincipal();
    }

    private void validateResourceAndPool() throws ResourceException {
        ResourceInfo resourceInfo = this.resourceInfo;
        ResourcesUtil resourcesUtil = ResourcesUtil.createInstance();

        ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
        ConnectorRegistry registry = ConnectorRegistry.getInstance();

        // Adding a performance optimization check so that "config-bean" is not accessed at all for
        // cases where the resource is enabled (deployed).
        //
        // Only for cases where resource/ is not available, we look further and determine whether
        // resource/resource-ref are disabled.

        if (!registry.isResourceDeployed(resourceInfo)) {
            if (logger.isLoggable(FINEST)) {
                logger.log(FINEST, "resourceInfo not found in connector-registry : " + resourceInfo);
            }

            boolean isDefaultResource = false;
            boolean isSunRAResource = false;
            ConnectorDescriptor descriptor = registry.getDescriptor(rarName);
            if (descriptor != null) {
                isDefaultResource = descriptor.getDefaultResourcesNames().contains(resourceInfo.getName());
                if (descriptor.getSunDescriptor() != null) {
                    ResourceAdapter resourceAdapter = descriptor.getSunDescriptor().getResourceAdapter();
                    if (resourceAdapter != null) {
                        String sunRAJndiName = (String) resourceAdapter.getValue(ResourceAdapter.JNDI_NAME);
                        isSunRAResource = resourceInfo.getName().equals(sunRAJndiName);
                    }
                }
            }

            if (
                (runtime.isServer() || runtime.isEmbedded()) &&
                (!resourceInfo.getName().contains(DATASOURCE_DEFINITION_JNDINAME_PREFIX) &&
                !isDefaultResource && !isSunRAResource)) {

                // performance optimization so that resource configuration is not retrieved from
                // resources config bean each time.
                if (resourceConfiguration == null) {
                    resourceConfiguration = (BindableResource) resourcesUtil.getResource(resourceInfo, BindableResource.class);
                    if (resourceConfiguration == null) {
                        String suffix = ConnectorsUtil.getValidSuffix(resourceInfo.getName());

                        // It is possible that the resource is a __PM or __NONTX suffixed resource used by JPA/EJB Container
                        // check for the enabled status and existence using non-prefixed resource-name

                        if (suffix != null) {
                            String nonPrefixedName = resourceInfo.getName().substring(0, resourceInfo.getName().lastIndexOf(suffix));
                            resourceInfo =
                                new ResourceInfo(nonPrefixedName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());

                            resourceConfiguration = (BindableResource) resourcesUtil.getResource(resourceInfo, BindableResource.class);
                        }
                    }
                } else {
                    // We cache the resourceConfiguration for performance optimization.
                    // make sure that appropriate (actual) resourceInfo is used for validation.
                    String suffix = ConnectorsUtil.getValidSuffix(resourceInfo.getName());

                    // It is possible that the resource is a __PM or __NONTX suffixed resource used by JPA/EJB Container
                    // check for the enabled status and existence using non-prefixed resource-name
                    if (suffix != null) {
                        String nonPrefixedName = resourceInfo.getName().substring(0, resourceInfo.getName().lastIndexOf(suffix));
                        resourceInfo = new ResourceInfo(nonPrefixedName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
                    }
                }

                if (resourceConfiguration == null) {
                    throw new ResourceException("No such resource : " + resourceInfo);
                }

                if (!resourcesUtil.isEnabled(resourceConfiguration, resourceInfo)) {
                    throw new ResourceException(resourceInfo + " is not enabled");
                }
            }
        }

        if (registry.getPoolMetaData(poolInfo) == null) {
            throw new ResourceException(poolInfo + ": " + getLocalStrings().getString("con_mgr.no_pool_meta_data", poolInfo));
        }
    }

    public void logFine(String message) {
        if (getLogger().isLoggable(FINE)) {
            getLogger().fine(message);
        }
    }

    private static StringManager getLocalStrings() {
        if (localStrings == null) {
            synchronized (ConnectionManagerImpl.class) {
                if (localStrings == null) {
                    localStrings = StringManager.getManager(ConnectionManagerImpl.class);
                }
            }
        }
        return localStrings;
    }

    protected static Logger getLogger() {
        if (logger == null) {
            synchronized (ConnectionManagerImpl.class) {
                if (logger == null) {
                    logger = LogDomains.getLogger(ConnectionManagerImpl.class, RSR_LOGGER);
                }
            }
        }

        return logger;
    }
}
