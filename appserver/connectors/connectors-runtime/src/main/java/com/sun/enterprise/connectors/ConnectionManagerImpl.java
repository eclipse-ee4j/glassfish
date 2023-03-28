/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
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

import java.io.Serializable;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import static com.sun.appserv.connectors.internal.api.ConnectorConstants.DEFAULT_JMS_ADAPTER;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.LOCAL_TRANSACTION_INT;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.NO_TRANSACTION_INT;
import static com.sun.appserv.connectors.internal.api.ConnectorConstants.XA_TRANSACTION_INT;
import static com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils.createSubject;
import static com.sun.enterprise.deployment.ResourceReferenceDescriptor.APPLICATION_AUTHORIZATION;

/**
 * @author Tony Ng
 */
public class ConnectionManagerImpl implements ConnectionManager, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogDomains.getLogger(ConnectionManagerImpl.class, LogDomains.RSR_LOGGER);
    private static final StringManager I18N = StringManager.getManager(ConnectionManagerImpl.class);

    protected SimpleJndiName jndiName;
    protected SimpleJndiName logicalName;
    protected PoolInfo poolInfo;
    protected ResourceInfo resourceInfo;

    protected String rarName;

    private transient BindableResource resourceConfiguration;

    protected ResourcePrincipalDescriptor defaultResourcePrincipalDescriptor;

    public ConnectionManagerImpl(PoolInfo poolInfo, ResourceInfo resourceInfo) {
        this.poolInfo = poolInfo;
        this.resourceInfo = resourceInfo;
    }

    public void setJndiName(SimpleJndiName jndiName) {
        this.jndiName = jndiName;
    }

    @Override
    public SimpleJndiName getJndiName() {
        return jndiName;
    }

    public void setLogicalName(SimpleJndiName logicalName) {
        this.logicalName = logicalName;
    }

    public SimpleJndiName getLogicalName() {
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
    public Object allocateNonTxConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo connectionRequestInfo)
        throws ResourceException {
        LOG.finest("Allocating NonTxConnection");

        //If a resource has been created with __nontx, we don't want to
        //add it again.
        //Otherwise we need to add __nontx at the end to ensure that the
        //mechanism to check for the correct resource manager still works
        //We do the addition if and only if we are getting this call
        //from a normal datasource and not a __nontx datasource.
        final SimpleJndiName localJndiName;
        if (jndiName.hasSuffix(ConnectorConstants.NON_TX_JNDI_SUFFIX)) {
            localJndiName = jndiName;
            LOG.finest("lookup happened from a __nontx datasource directly");
        } else {
            localJndiName = new SimpleJndiName(jndiName + ConnectorConstants.NON_TX_JNDI_SUFFIX);
            LOG.finest("Adding __nontx to jndiname");
        }

        return allocateConnection(mcf, connectionRequestInfo, localJndiName);
    }


    @Override
    public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return allocateConnection(mcf, cxRequestInfo, jndiName);
    }


    public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo,
        SimpleJndiName jndiNameToUse) throws ResourceException {
        return this.allocateConnection(mcf, cxRequestInfo, jndiNameToUse, null);
    }


    public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo,
        SimpleJndiName jndiNameToUse, Object connection) throws ResourceException {
        LOG.log(Level.FINEST, "allocateConnection(mcf={0}, cxRequestInfo={1}, jndiNameToUse={2}, connection={3})",
            new Object[] {mcf, cxRequestInfo, jndiNameToUse, connection});
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

        //TODO V3 refactor all the 3 cases viz, no res-ref, app-auth, cont-auth.
        if (resourceReferenceDescriptor == null) {
            LOG.log(Level.FINE, "poolmgr.no_resource_reference", jndiNameToUse);

            return internalGetConnection(
                    mcf,
                    defaultResourcePrincipalDescriptor,
                    cxRequestInfo,
                    resourceShareable,
                    jndiNameToUse,
                    connection, true);
        }

        String authorization = resourceReferenceDescriptor.getAuthorization();

        if (authorization.equals(APPLICATION_AUTHORIZATION)) {
            if (cxRequestInfo == null) {
                throw new ResourceException(I18N.getString("con_mgr.null_userpass"));
            }
            ConnectorRuntime.getRuntime().switchOnMatching(rarName, poolInfo);

            return internalGetConnection(
                    mcf,
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
                LOG.log(Level.FINE, "default-resource-principal not specified for {0}"
                    + ". Defaulting to user/password specified in the pool", jndiNameToUse);
                resourcePrincipalDescriptor = defaultResourcePrincipalDescriptor;
            } else if (!resourcePrincipalDescriptor.equals(defaultResourcePrincipalDescriptor)) {
                ConnectorRuntime.getRuntime().switchOnMatching(rarName, poolInfo);
            }
        }

        return internalGetConnection(
                mcf,
                resourcePrincipalDescriptor,
                cxRequestInfo,
                resourceShareable,
                jndiNameToUse,
                connection, false);
    }

    protected Object internalGetConnection(ManagedConnectionFactory mcf,
                                           final ResourcePrincipalDescriptor prin, ConnectionRequestInfo cxRequestInfo,
                                           boolean shareable, SimpleJndiName jndiNameToUse, Object connection, boolean isUnknownAuth)
            throws ResourceException {
        try {
            PoolManager poolManager = ConnectorRuntime.getRuntime().getPoolManager();
            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            PoolMetaData poolMetaData = registry.getPoolMetaData(poolInfo);

            ResourceSpec resourceSpec = new ResourceSpec(jndiNameToUse, ResourceSpec.JNDI_NAME, poolMetaData);
            resourceSpec.setPoolInfo(this.poolInfo);
            ManagedConnectionFactory freshManagedConnectionFactory = poolMetaData.getMCF();

            if (!freshManagedConnectionFactory.equals(mcf)) {
                LOG.info("conmgr.mcf_not_equal");
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
            LOG.log(Level.FINE, "Providing connection: poolName={0}, txLevel={1}", new Object[] {poolInfo, txLevel});
            if (connection != null) {
                resourceSpec.setConnectionToAssociate(connection);
            }

            return getResource(txLevel, poolManager, mcf, resourceSpec, subject, cxRequestInfo, info, rarConnectorDescriptor, shareable);

        } catch (PoolingException e) {
            // We can't simply look for ResourceException and throw back since
            // Connector Container also throws ResourceException which might
            // hide the SecurityException thrown by RA.
            // So, we try to track SecurityException
            unwrapSecurityException(e);
            throw new ResourceAllocationException(
                MessageFormat.format("Failed to obtain/create connection from connection pool [{0}]. Reason: {1}",
                    poolInfo.getName(), e.getMessage()),
                e);
        }
    }

    private void unwrapSecurityException(Throwable ex) throws ResourceException {
        if (ex != null) {
            if (ex instanceof SecurityException) {
                LOG.log(Level.WARNING, "poolmgr.get_connection_failure", new Object[] {poolInfo, ex});
                throw (SecurityException) ex;
            }
            unwrapSecurityException(ex.getCause());
        }
    }


    private Object getResource(int txLevel, PoolManager poolManager, ManagedConnectionFactory mcf,
        ResourceSpec resourceSpec, Subject subject, ConnectionRequestInfo connectionRequestInfo,
        ClientSecurityInfo info, ConnectorDescriptor descriptor, boolean shareable)
        throws PoolingException, IllegalStateException, RetryableUnavailableException {
        ResourceAllocator resourceAllocator;

        switch (txLevel) {
            case NO_TRANSACTION_INT:
                resourceAllocator = new NoTxConnectorAllocator(poolManager, mcf, resourceSpec, subject,
                    connectionRequestInfo, info, descriptor);
                break;

            case LOCAL_TRANSACTION_INT:
                resourceAllocator = new LocalTxConnectorAllocator(poolManager, mcf, resourceSpec, subject,
                    connectionRequestInfo, info, descriptor, shareable);
                break;

            case XA_TRANSACTION_INT:
                if (rarName.equals(DEFAULT_JMS_ADAPTER)) {
                    shareable = false;
                }
                resourceSpec.markAsXA();
                resourceAllocator = new ConnectorAllocator(poolManager, mcf, resourceSpec, subject,
                    connectionRequestInfo, info, descriptor, shareable);
                break;

            default:
                String i18nMsg = I18N.getString("con_mgr.illegal_tx_level", txLevel + " ");
                throw new IllegalStateException(i18nMsg);
        }

        return poolManager.getResource(resourceSpec, resourceAllocator, info);
    }

    public void setRarName(String _rarName) {
        rarName = _rarName;
    }

    public String getRarName() {
        return rarName;
    }

    /**
     * This method is called from the ConnectorObjectFactory lookup
     * With this we move all the housekeeping work in allocateConnection
     * up-front
     */
    public void initialize() throws ConnectorRuntimeException {
        ConnectorRuntime runtime = ConnectorRuntime.getRuntime();

        if (runtime.isNonACCRuntime()) {
            jndiName = ConnectorsUtil.getPMJndiName(jndiName);
        }

        defaultResourcePrincipalDescriptor =
            ConnectorRegistry.getInstance()
                             .getPoolMetaData(poolInfo)
                             .getResourcePrincipal();
    }

    private void validateResourceAndPool() throws ResourceException {
        ResourceInfo resInfo = this.resourceInfo;
        ResourcesUtil resourcesUtil = ResourcesUtil.createInstance();

        ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
        ConnectorRegistry registry = ConnectorRegistry.getInstance();

        // Adding a performance optimization check so that "config-bean" is not accessed at all for
        // cases where the resource is enabled (deployed).
        //
        // Only for cases where resource/ is not available, we look further and determine whether
        // resource/resource-ref are disabled.

        if (!registry.isResourceDeployed(resInfo)) {
            LOG.log(Level.FINEST,"resourceInfo not found in connector-registry: {0}", resInfo);
            boolean isDefaultResource = false;
            boolean isSunRAResource = false;
            ConnectorDescriptor descriptor = registry.getDescriptor(rarName);
            if (descriptor != null) {
                isDefaultResource = descriptor.getDefaultResourcesNames().contains(resInfo.getName());
                if (descriptor.getSunDescriptor() != null) {
                    ResourceAdapter rar = descriptor.getSunDescriptor().getResourceAdapter();
                    if (rar != null) {
                        SimpleJndiName sunRAJndiName = rar.getValue(ResourceAdapter.JNDI_NAME);
                        isSunRAResource = resInfo.getName().equals(sunRAJndiName);
                    }
                }
            }

            if ((runtime.isServer() || runtime.isEmbedded())
                && (!resInfo.getName().contains(ResourceConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX)
                    && !isDefaultResource && !isSunRAResource)) {
                // performance optimization so that resource configuration is not retrieved from
                // resources config bean each time.
                if (resourceConfiguration == null) {
                    resourceConfiguration = resourcesUtil.getResource(resInfo, BindableResource.class);
                    if (resourceConfiguration == null) {
                        String suffix = ConnectorsUtil.getValidSuffix(resInfo.getName());

                        // It is possible that the resource is a __PM or __NONTX suffixed resource used by JPA/EJB Container
                        // check for the enabled status and existence using non-prefixed resource-name

                        if (suffix != null) {
                            SimpleJndiName nonPrefixedName = resInfo.getName().removeSuffix(suffix);
                            resInfo = new ResourceInfo(nonPrefixedName, resInfo.getApplicationName(),
                                resInfo.getModuleName());
                            resourceConfiguration = resourcesUtil.getResource(resInfo, BindableResource.class);
                        }
                    }
                } else {
                    // We cache the resourceConfiguration for performance optimization.
                    // make sure that appropriate (actual) resourceInfo is used for validation.
                    String suffix = ConnectorsUtil.getValidSuffix(resInfo.getName());

                    // It is possible that the resource is a __PM or __NONTX suffixed resource used by JPA/EJB Container
                    // check for the enabled status and existence using non-prefixed resource-name
                    if (suffix != null) {
                        SimpleJndiName nonPrefixedName = resInfo.getName().removeSuffix(suffix);
                        resInfo = new ResourceInfo(nonPrefixedName, resInfo.getApplicationName(),
                                resInfo.getModuleName());
                    }
                }

                if (resourceConfiguration == null) {
                    throw new ResourceException("No such resource: " + resInfo);
                }

                if (!resourcesUtil.isEnabled(resourceConfiguration, resInfo)) {
                    throw new ResourceException(resInfo + " is not enabled");
                }
            }
        }

        if (registry.getPoolMetaData(poolInfo) == null) {
            String msg = I18N.getString("con_mgr.no_pool_meta_data", poolInfo);
            throw new ResourceException(poolInfo + ": " + msg);
        }
    }
}
