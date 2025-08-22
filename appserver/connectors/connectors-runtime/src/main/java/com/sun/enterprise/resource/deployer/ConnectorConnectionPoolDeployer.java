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

package com.sun.enterprise.resource.deployer;


import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.enterprise.connectors.util.SecurityMapUtils;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.SecurityMap;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.util.ResourceUtil;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;


/**
 * @author Srikanth P, Sivakumar Thyagarajan
 */
@Service
@ResourceDeployerInfo(org.glassfish.connectors.config.ConnectorConnectionPool.class)
@Singleton
public class ConnectorConnectionPoolDeployer
    extends AbstractConnectorResourceDeployer<org.glassfish.connectors.config.ConnectorConnectionPool> {

    @Inject
    private ConnectorRuntime runtime;

    private static final Logger LOG = LogDomains.getLogger(ConnectorConnectionPoolDeployer.class, LogDomains.RSR_LOGGER);

    private static final StringManager MESSAGES = StringManager.getManager(ConnectorConnectionPoolDeployer.class);

    private static final Locale locale = Locale.getDefault();


    @Override
    public void deployResource(org.glassfish.connectors.config.ConnectorConnectionPool resource, String applicationName, String moduleName) throws Exception {
        //deployResource is not synchronized as there is only one caller
        //ResourceProxy which is synchronized

        // If the user is trying to modify the default pool,
        // redirect call to redeployResource
        if (ConnectionPoolObjectsUtils.isPoolSystemPool(resource)) {
            this.redeployResource(resource);
            return;
        }

        SimpleJndiName jndiName = SimpleJndiName.of(resource.getName());
        PoolInfo poolInfo = new PoolInfo(jndiName, applicationName, moduleName);
        final ConnectorConnectionPool ccp = getConnectorConnectionPool(resource, poolInfo);
        String rarName = resource.getResourceAdapterName();
        String connDefName = resource.getConnectionDefinitionName();
        List<Property> props = resource.getProperty();
        List<SecurityMap> securityMaps = resource.getSecurityMap();

        populateConnectorConnectionPool(ccp, connDefName, rarName, props, securityMaps);
        final String defName = resource.getConnectionDefinitionName();

        runtime.createConnectorConnectionPool(ccp, defName, resource.getResourceAdapterName(), resource.getProperty(),
            resource.getSecurityMap());
        LOG.log(Level.CONFIG, "Added connectorConnectionPool in resource adapter {0}", resource.getResourceAdapterName());
    }


    @Override
    public void deployResource(org.glassfish.connectors.config.ConnectorConnectionPool resource) throws Exception {
        PoolInfo poolInfo = ResourceUtil.getPoolInfo(resource);
        deployResource(resource, poolInfo.getApplicationName(), poolInfo.getModuleName());
    }


    @Override
    public void undeployResource(org.glassfish.connectors.config.ConnectorConnectionPool domainCcp,
        String applicationName, String moduleName) throws Exception {
        SimpleJndiName jndiName = SimpleJndiName.of(domainCcp.getName());
        PoolInfo poolInfo = new PoolInfo(jndiName, applicationName, moduleName);
        actualUndeployResource(domainCcp, poolInfo);
    }


    @Override
    public synchronized void undeployResource(org.glassfish.connectors.config.ConnectorConnectionPool resource)
        throws Exception {
        PoolInfo poolInfo = ResourceUtil.getPoolInfo(resource);
        actualUndeployResource(resource, poolInfo);
    }


    private void actualUndeployResource(org.glassfish.connectors.config.ConnectorConnectionPool domainCcp,
        PoolInfo poolInfo) throws ConnectorRuntimeException {
        runtime.deleteConnectorConnectionPool(poolInfo);
        LOG.log(Level.FINE, "Deleted ConnectorConnectionPool in backend: {0}", domainCcp);
    }


    @Override
    public synchronized void redeployResource(org.glassfish.connectors.config.ConnectorConnectionPool resource)
            throws Exception {
        //Connector connection pool reconfiguration or
        //change in security maps
        List<SecurityMap> securityMaps = resource.getSecurityMap();

        //Since 8.1 PE/SE/EE, only if pool has already been deployed in this
        //server-instance earlier, reconfig this pool
        PoolInfo poolInfo = ResourceUtil.getPoolInfo(resource);
        if (!runtime.isConnectorConnectionPoolDeployed(poolInfo)) {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine("The connector connection pool " + poolInfo
                    + " is either not referred or not yet created in "
                    + "this server instance and pool and hence "
                    + "redeployment is ignored");
            }
            return;
        }


        String rarName = resource.getResourceAdapterName();
        String connDefName = resource.getConnectionDefinitionName();
        List<Property> props = resource.getProperty();
        ConnectorConnectionPool ccp = getConnectorConnectionPool(resource, poolInfo);
        populateConnectorConnectionPool(ccp, connDefName, rarName, props, securityMaps);

        boolean poolRecreateRequired = false;
        try {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine("Calling reconfigure pool");
            }
            poolRecreateRequired = runtime.reconfigureConnectorConnectionPool(ccp,
                    new HashSet());
        } catch (ConnectorRuntimeException cre) {
            Object params[] = new Object[]{poolInfo, cre};
            LOG.log(Level.WARNING,"error.reconfiguring.pool", params);
        }

        if (poolRecreateRequired) {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine("Pool recreation required");
            }
            runtime.recreateConnectorConnectionPool(ccp);
            if(LOG.isLoggable(Level.FINE)) {
                LOG.fine("Pool recreation done");
            }
        }
    }


    @Override
    public boolean handles(Object resource){
        return resource instanceof org.glassfish.connectors.config.ConnectorConnectionPool;
    }


    @Override
    public synchronized void disableResource(org.glassfish.connectors.config.ConnectorConnectionPool resource)
            throws Exception {
    }


    @Override
    public synchronized void enableResource(org.glassfish.connectors.config.ConnectorConnectionPool resource)
            throws Exception {
    }

    private ConnectorConnectionPool getConnectorConnectionPool(
            org.glassfish.connectors.config.ConnectorConnectionPool domainCcp, PoolInfo poolInfo)
            throws Exception {
        ConnectorConnectionPool ccp ;
        ccp = new ConnectorConnectionPool(poolInfo);
        ccp.setSteadyPoolSize(domainCcp.getSteadyPoolSize());
        ccp.setMaxPoolSize(domainCcp.getMaxPoolSize());
        ccp.setMaxWaitTimeInMillis(domainCcp.getMaxWaitTimeInMillis());
        ccp.setPoolResizeQuantity(domainCcp.getPoolResizeQuantity());
        ccp.setIdleTimeoutInSeconds(domainCcp.getIdleTimeoutInSeconds());
        ccp.setFailAllConnections(Boolean.parseBoolean(domainCcp.getFailAllConnections()));
        ccp.setAuthCredentialsDefinedInPool(
                isAuthCredentialsDefinedInPool(domainCcp));
        //The line below will change for 9.0. We will get this from
        //the domain.xml
        ccp.setConnectionValidationRequired(Boolean.parseBoolean(domainCcp.getIsConnectionValidationRequired()));

        String txSupport = domainCcp.getTransactionSupport();
        int txSupportIntVal = parseTransactionSupportString(txSupport);

        if (txSupportIntVal == -1) {
            //if transaction-support attribute is null load the value
            //from the ra.xml
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Got transaction-support attr null from domain.xml");
            }
            txSupportIntVal = ConnectionPoolObjectsUtils.getTransactionSupportFromRaXml(
                    domainCcp.getResourceAdapterName());

        } else //We got some valid transaction-support attribute value
        //so go figure if it is valid.
        //The tx support is valid if it is less-than/equal-to
        //the value specified in the ra.xml
        if (!ConnectionPoolObjectsUtils.isTxSupportConfigurationSane(txSupportIntVal,
            domainCcp.getResourceAdapterName())) {

            String i18nMsg = MESSAGES.getString("ccp_deployer.incorrect_tx_support");
            ConnectorRuntimeException cre = new
                    ConnectorRuntimeException(i18nMsg);

                    LOG.log(Level.SEVERE, "rardeployment.incorrect_tx_support", ccp.getName());
                    throw cre;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("setting txSupportVal to " + txSupportIntVal +
                    " in pool " + domainCcp.getName());
        }
        ccp.setTransactionSupport(txSupportIntVal);

        //Always for ccp
        ccp.setNonComponent(false);
        ccp.setNonTransactional(false);
        ccp.setConnectionLeakTracingTimeout(domainCcp.getConnectionLeakTimeoutInSeconds());
        ccp.setConnectionReclaim(Boolean.parseBoolean(domainCcp.getConnectionLeakReclaim()));

        ccp.setMatchConnections(Boolean.parseBoolean(domainCcp.getMatchConnections()));
        ccp.setAssociateWithThread(Boolean.parseBoolean(domainCcp.getAssociateWithThread()));
        ccp.setPooling(Boolean.parseBoolean(domainCcp.getPooling()));
        ccp.setPingDuringPoolCreation(Boolean.parseBoolean(domainCcp.getPing()));

        boolean lazyConnectionEnlistment = Boolean.parseBoolean(domainCcp.getLazyConnectionEnlistment());
        boolean lazyConnectionAssociation = Boolean.parseBoolean(domainCcp.getLazyConnectionAssociation());

        if (lazyConnectionAssociation) {
            if (lazyConnectionEnlistment) {
                ccp.setLazyConnectionAssoc(true);
                ccp.setLazyConnectionEnlist(true);
            } else {
                LOG.log(Level.SEVERE,
                        "conn_pool_obj_utils.lazy_enlist-lazy_assoc-invalid-combination",
                        domainCcp.getName());
                String i18nMsg = MESSAGES.getString(
                        "cpou.lazy_enlist-lazy_assoc-invalid-combination",  domainCcp.getName());
                throw new RuntimeException(i18nMsg);
            }
        } else {
            ccp.setLazyConnectionAssoc(lazyConnectionAssociation);
            ccp.setLazyConnectionEnlist(lazyConnectionEnlistment);
        }
        boolean pooling = Boolean.parseBoolean(domainCcp.getPooling());

        //TODO: should this be added to the beginning of this method?
        if(!pooling) {
            //Throw exception if assoc with thread is set to true.
            if(Boolean.parseBoolean(domainCcp.getAssociateWithThread())) {
                LOG.log(Level.SEVERE, "conn_pool_obj_utils.pooling_disabled_assocwiththread_invalid_combination",
                        domainCcp.getName());
                String i18nMsg = MESSAGES.getString(
                        "cpou.pooling_disabled_assocwiththread_invalid_combination", domainCcp.getName());
                throw new RuntimeException(i18nMsg);
            }

            //Below are useful in pooled environment only.
            //Throw warning for connection-validation/validate-atmost-once-period/
            //match-connections/max-connection-usage-count/idle-timeout
            if(Boolean.parseBoolean(domainCcp.getIsConnectionValidationRequired())) {
                LOG.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_conn_validation_invalid_combination",
                        domainCcp.getName());
            }
            if(Integer.parseInt(domainCcp.getValidateAtmostOncePeriodInSeconds()) > 0) {
                LOG.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_validate_atmost_once_invalid_combination",
                        domainCcp.getName());
            }
            if(Boolean.parseBoolean(domainCcp.getMatchConnections())) {
                LOG.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_match_connections_invalid_combination",
                        domainCcp.getName());
            }
            if(Integer.parseInt(domainCcp.getMaxConnectionUsageCount()) > 0) {
                LOG.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_max_conn_usage_invalid_combination",
                        domainCcp.getName());
            }
            if(Integer.parseInt(domainCcp.getIdleTimeoutInSeconds()) > 0) {
                LOG.log(Level.WARNING, "conn_pool_obj_utils.pooling_disabled_idle_timeout_invalid_combination",
                        domainCcp.getName());
            }
        }
        ccp.setPooling(pooling);

        ccp.setMaxConnectionUsage(domainCcp.getMaxConnectionUsageCount());
        ccp.setValidateAtmostOncePeriod(
                domainCcp.getValidateAtmostOncePeriodInSeconds());

        ccp.setConCreationRetryAttempts(
                domainCcp.getConnectionCreationRetryAttempts());
        ccp.setConCreationRetryInterval(
                domainCcp.getConnectionCreationRetryIntervalInSeconds());

        //IMPORTANT
        //Here all properties that will be checked by the
        //convertElementPropertyToPoolProperty method need to be set to
        //their default values
        convertElementPropertyToPoolProperty(ccp, domainCcp);
        return ccp;
    }

    private void populateConnectorConnectionPool(ConnectorConnectionPool ccp,
                                                 String connectionDefinitionName, String rarName,
                                                 List<Property> props, List<SecurityMap> securityMaps)
            throws ConnectorRuntimeException {

        ConnectorDescriptor connectorDescriptor = runtime.getConnectorDescriptor(rarName);
        if (connectorDescriptor == null) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException("Failed to get connection pool object");
            LOG.log(Level.SEVERE, "rardeployment.connector_descriptor_notfound_registry", rarName);
            LOG.log(Level.SEVERE, "", cre);
            throw cre;
        }
        Set connectionDefs =
                connectorDescriptor.getOutboundResourceAdapter().getConnectionDefs();
        ConnectionDefDescriptor cdd = null;
        Iterator it = connectionDefs.iterator();
        while (it.hasNext()) {
            cdd = (ConnectionDefDescriptor) it.next();
            if (connectionDefinitionName.equals(cdd.getConnectionFactoryIntf())) {
                break;
            }

        }
        ConnectorDescriptorInfo cdi = new ConnectorDescriptorInfo();

        cdi.setRarName(rarName);
        cdi.setResourceAdapterClassName(connectorDescriptor.getResourceAdapterClass());
        cdi.setConnectionDefinitionName(cdd.getConnectionFactoryIntf());
        cdi.setManagedConnectionFactoryClass(cdd.getManagedConnectionFactoryImpl());
        cdi.setConnectionFactoryClass(cdd.getConnectionFactoryImpl());
        cdi.setConnectionFactoryInterface(cdd.getConnectionFactoryIntf());
        cdi.setConnectionClass(cdd.getConnectionImpl());
        cdi.setConnectionInterface(cdd.getConnectionIntf());
        Properties properties = new Properties();
        //skip the AddressList in case of JMS RA.
        //Refer Sun Bug :6579154 - Equivalent Oracle Bug :12206278
        if(rarName.trim().equals(ConnectorConstants.DEFAULT_JMS_ADAPTER)){
            properties.put("AddressList","localhost");
        }
        Set mergedProps = ConnectorDDTransformUtils.mergeProps(props, cdd.getConfigProperties(),properties);
        cdi.setMCFConfigProperties(mergedProps);
        cdi.setResourceAdapterConfigProperties(connectorDescriptor.getConfigProperties());
        ccp.setConnectorDescriptorInfo(cdi);
        ccp.setSecurityMaps(SecurityMapUtils.getConnectorSecurityMaps(securityMaps));

    }

    private int parseTransactionSupportString(String txSupport) {
        return ConnectionPoolObjectsUtils.parseTransactionSupportString(txSupport);
    }

    /**
     * The idea is to convert the ElementProperty values coming from the admin
     * connection pool to standard pool attributes thereby making it
     * easy in case of a reconfig
     */
    public void convertElementPropertyToPoolProperty(ConnectorConnectionPool ccp,
                                                     org.glassfish.connectors.config.ConnectorConnectionPool domainCcp) {
        List<Property> elemProps = domainCcp.getProperty();
        if (elemProps == null) {
            return;
        }
        for (Property ep : elemProps) {
            if (ep != null) {
                if ("MATCHCONNECTIONS".equals(ep.getName().toUpperCase(locale))) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(" ConnectorConnectionPoolDeployer::  Setting matchConnections");
                    }
                    ccp.setMatchConnections(toBoolean(ep.getValue(), true));
                } else if ("LAZYCONNECTIONASSOCIATION".equals(ep.getName().toUpperCase(locale))) {
                    ConnectionPoolObjectsUtils.setLazyEnlistAndLazyAssocProperties(ep.getValue(),
                            domainCcp.getProperty(), ccp);
                    if(LOG.isLoggable(Level.FINE)) {
                        LOG.fine("LAZYCONNECTIONASSOCIATION");
                    }

                } else if ("LAZYCONNECTIONENLISTMENT".equals(ep.getName().toUpperCase(locale))) {
                    ccp.setLazyConnectionEnlist(toBoolean(ep.getValue(), false));
                    if(LOG.isLoggable(Level.FINE)) {
                        LOG.fine("LAZYCONNECTIONENLISTMENT");
                    }

                } else if ("ASSOCIATEWITHTHREAD".equals(ep.getName().toUpperCase(locale))) {
                    ccp.setAssociateWithThread(toBoolean(ep.getValue(), false));
                    if(LOG.isLoggable(Level.FINE)) {
                        LOG.fine("ASSOCIATEWITHTHREAD");
                    }
                } else if ("POOLDATASTRUCTURE".equals(ep.getName().toUpperCase(locale))) {
                    ccp.setPoolDataStructureType(ep.getValue());
                    if(LOG.isLoggable(Level.FINE)) {
                        LOG.fine("POOLDATASTRUCTURE");
                    }

                } else if ("POOLWAITQUEUE".equals(ep.getName().toUpperCase(locale))) {
                    ccp.setPoolWaitQueue(ep.getValue());
                    if(LOG.isLoggable(Level.FINE)) {
                        LOG.fine("POOLWAITQUEUE");
                    }

                } else if ("DATASTRUCTUREPARAMETERS".equals(ep.getName().toUpperCase(locale))) {
                    ccp.setDataStructureParameters(ep.getValue());
                    if(LOG.isLoggable(Level.FINE)) {
                        LOG.fine("DATASTRUCTUREPARAMETERS");
                    }
                } else if ("PREFER-VALIDATE-OVER-RECREATE".equals(ep.getName().toUpperCase(locale))) {
                    String value = ep.getValue();
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(" ConnectorConnectionPoolDeployer::  " +
                                "Setting PREFER-VALIDATE-OVER-RECREATE to " +
                                value);
                    }
                    ccp.setPreferValidateOverRecreate(toBoolean(value, false));
                }
            }
        }
    }

    private boolean toBoolean(Object prop, boolean defaultVal) {
        if (prop == null) {
            return defaultVal;
        }

        return Boolean.parseBoolean((String) prop);
    }

    private boolean isAuthCredentialsDefinedInPool(
            org.glassfish.connectors.config.ConnectorConnectionPool domainCcp) {
        List<Property> elemProps = domainCcp.getProperty();
        if (elemProps == null) {
            return false;
        }

        for (Property ep : elemProps) {

            if (ep.getName().equalsIgnoreCase("UserName") ||
                    ep.getName().equalsIgnoreCase("User") ||
                    ep.getName().equalsIgnoreCase("Password")) {
                return true;
            }
        }
        return false;
    }
}
