/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.I18n;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.resourcebase.resources.api.ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER;
import static org.glassfish.resources.admin.cli.ResourceConstants.ASSOCIATE_WITH_THREAD;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_ATTEMPTS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_RECLAIM;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_LEAK_TIMEOUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTOR_CONNECTION_POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_DEF_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.CONN_TRANSACTION_SUPPORT;
import static org.glassfish.resources.admin.cli.ResourceConstants.FAIL_ALL_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.IDLE_TIME_OUT_IN_SECONDS;
import static org.glassfish.resources.admin.cli.ResourceConstants.IS_CONNECTION_VALIDATION_REQUIRED;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ASSOCIATION;
import static org.glassfish.resources.admin.cli.ResourceConstants.LAZY_CONNECTION_ENLISTMENT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MATCH_CONNECTIONS;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_CONNECTION_USAGE_COUNT;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.MAX_WAIT_TIME_IN_MILLIS;
import static org.glassfish.resources.admin.cli.ResourceConstants.PING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOLING;
import static org.glassfish.resources.admin.cli.ResourceConstants.POOL_SIZE_QUANTITY;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_ADAPTER_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.STEADY_POOL_SIZE;
import static org.glassfish.resources.admin.cli.ResourceConstants.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS;


/**
 *
 * @author Jennifer Chou, Jagadish Ramu
 */
@Service (name=ServerTags.CONNECTOR_CONNECTION_POOL)
@PerLookup
@I18n("create.connector.connection.pool")
public class ConnectorConnectionPoolManager implements ResourceManager {

    @Inject
    private Applications applications;

    @Inject
    private ConnectorRuntime connectorRuntime;

    @Inject
    private ServerEnvironment environment;

    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(ConnectorConnectionPoolManager.class);

    private String raname;
    private String connectiondefinition;
    private String steadypoolsize = "8";
    private String maxpoolsize = "32";
    private String maxwait = "60000";
    private String poolresize = "2";
    private String idletimeout = "300";
    private String isconnectvalidatereq = Boolean.FALSE.toString();
    private String failconnection = Boolean.FALSE.toString();
    private String validateAtmostOncePeriod = "0";
    private String connectionLeakTimeout = "0";
    private String connectionLeakReclaim = Boolean.FALSE.toString();
    private String connectionCreationRetryAttempts = "0";
    private String connectionCreationRetryInterval = "10";
    private String lazyConnectionEnlistment = Boolean.FALSE.toString();
    private String lazyConnectionAssociation = Boolean.FALSE.toString();
    private String associateWithThread = Boolean.FALSE.toString();
    private String matchConnections = Boolean.FALSE.toString();
    private String maxConnectionUsageCount = "0";
    private String ping = Boolean.FALSE.toString();
    private String pooling = Boolean.TRUE.toString();
    private String transactionSupport;

    private String description;
    private String poolname;

    @Override
    public String getResourceType() {
        return ServerTags.CONNECTOR_CONNECTION_POOL;
    }

    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception {
        setParams(attributes);

        ResourceStatus validationStatus = isValid(resources, true);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }
        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {
                @Override
                public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                    return createResource(param, properties);
                }
            }, resources);

        } catch (TransactionFailure tfe) {
            Logger.getLogger(ConnectorConnectionPoolManager.class.getName()).log(Level.SEVERE,
                    "create-connector-connection-pool failed", tfe);
            String msg = I18N.getLocalString(
                    "create.connector.connection.pool.fail", "Connector connection pool {0} create failed: {1}",
                    poolname) + " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }

        String msg = I18N.getLocalString(
                "create.connector.connection.pool.success", "Connector connection pool {0} created successfully",
                poolname);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg);

    }

    private ResourceStatus isValid(Resources resources, boolean requiresNewTransaction){
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "Validation Successful");
        if (poolname == null) {
            String msg = I18N.getLocalString("create.connector.connection.pool.noJndiName",
                "No pool name defined for connector connection pool.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg);
        }
        // ensure we don't already have one of this name
        final SimpleJndiName jndiName = new SimpleJndiName(poolname);
        if (resources.getResourceByName(ConnectorConnectionPool.class, jndiName) != null) {
            String errMsg = I18N.getLocalString("create.connector.connection.pool.duplicate",
                "A resource named {0} already exists.", poolname);
            return new ResourceStatus(ResourceStatus.FAILURE, errMsg);
        }

        // no need to validate in remote instance as the validation would have happened in DAS.
        if (environment.isDas() && requiresNewTransaction) {

            if (applications == null) {
                String msg = I18N.getLocalString("noApplications", "No applications found.");
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }

            try {
                status = validateConnectorConnPoolAttributes(raname, connectiondefinition);
                if (status.getStatus() == ResourceStatus.FAILURE) {
                    return status;
                }
            } catch (ConnectorRuntimeException cre) {
                Logger.getLogger(ConnectorConnectionPoolManager.class.getName()).log(Level.SEVERE,
                    "Could not find connection definitions from ConnectorRuntime for resource adapter " + raname, cre);
                String msg = I18N.getLocalString("create.connector.connection.pool.noConnDefs",
                    "Could not find connection definitions for resource adapter {0}", raname) + " "
                    + cre.getLocalizedMessage();
                return new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        }
        return status;
    }

    private ConnectorConnectionPool createResource(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ConnectorConnectionPool newResource = createConfigBean(param, properties);
        param.getResources().add(newResource);
        return newResource;
    }


    private ConnectorConnectionPool createConfigBean(Resources param, Properties properties) throws PropertyVetoException,
            TransactionFailure {
        ConnectorConnectionPool newResource = param.createChild(ConnectorConnectionPool.class);

        newResource.setResourceAdapterName(raname);
        newResource.setConnectionDefinitionName(connectiondefinition);
        if(validateAtmostOncePeriod != null){
            newResource.setValidateAtmostOncePeriodInSeconds(validateAtmostOncePeriod);
        }
        newResource.setSteadyPoolSize(steadypoolsize);
        newResource.setPoolResizeQuantity(poolresize);
        newResource.setMaxWaitTimeInMillis(maxwait);
        newResource.setMaxPoolSize(maxpoolsize);
        if(maxConnectionUsageCount != null){
            newResource.setMaxConnectionUsageCount(maxConnectionUsageCount);
        }
        newResource.setMatchConnections(matchConnections);
        if(lazyConnectionEnlistment != null){
            newResource.setLazyConnectionEnlistment(lazyConnectionEnlistment);
        }
        if(lazyConnectionAssociation != null){
            newResource.setLazyConnectionAssociation(lazyConnectionAssociation);
        }
        if(isconnectvalidatereq != null){
            newResource.setIsConnectionValidationRequired(isconnectvalidatereq);
        }
        newResource.setIdleTimeoutInSeconds(idletimeout);
        newResource.setFailAllConnections(failconnection);
        if(connectionLeakTimeout != null){
            newResource.setConnectionLeakTimeoutInSeconds(connectionLeakTimeout);
        }
        if(connectionLeakReclaim != null){
            newResource.setConnectionLeakReclaim(connectionLeakReclaim);
        }
        if(connectionCreationRetryInterval != null){
            newResource.setConnectionCreationRetryIntervalInSeconds(connectionCreationRetryInterval);
        }
        if(connectionCreationRetryAttempts != null){
            newResource.setConnectionCreationRetryAttempts(connectionCreationRetryAttempts);
        }
        if(associateWithThread != null){
            newResource.setAssociateWithThread(associateWithThread);
        }
        if(pooling != null){
            newResource.setPooling(pooling);
        }
        if(ping != null){
            newResource.setPing(ping);
        }
        if (transactionSupport != null) {
            newResource.setTransactionSupport(transactionSupport);
        }
        if (description != null) {
            newResource.setDescription(description);
        }
        newResource.setName(poolname);
        if (properties != null) {
            for (String propertyName : properties.stringPropertyNames()) {
                Property prop = newResource.createChild(Property.class);
                prop.setName(propertyName);
                prop.setValue(properties.getProperty(propertyName));
                newResource.getProperty().add(prop);
            }
        }
        return newResource;
    }

    private void setParams(ResourceAttributes attrList) {
        raname = attrList.getString(RES_ADAPTER_NAME);
        connectiondefinition = attrList.getString(CONN_DEF_NAME);
        steadypoolsize = attrList.getString(STEADY_POOL_SIZE);
        maxpoolsize = attrList.getString(MAX_POOL_SIZE);
        maxwait = attrList.getString(MAX_WAIT_TIME_IN_MILLIS);
        poolresize = attrList.getString(POOL_SIZE_QUANTITY);
        idletimeout = attrList.getString(IDLE_TIME_OUT_IN_SECONDS);
        isconnectvalidatereq = attrList.getString(IS_CONNECTION_VALIDATION_REQUIRED);
        failconnection = attrList.getString(FAIL_ALL_CONNECTIONS);
        validateAtmostOncePeriod = attrList.getString(VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS);
        connectionLeakTimeout = attrList.getString(CONNECTION_LEAK_TIMEOUT_IN_SECONDS);
        connectionLeakReclaim = attrList.getString(CONNECTION_LEAK_RECLAIM);
        connectionCreationRetryAttempts = attrList.getString(CONNECTION_CREATION_RETRY_ATTEMPTS);
        connectionCreationRetryInterval = attrList.getString(CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS);
        lazyConnectionEnlistment = attrList.getString(LAZY_CONNECTION_ENLISTMENT);
        lazyConnectionAssociation = attrList.getString(LAZY_CONNECTION_ASSOCIATION);
        associateWithThread = attrList.getString(ASSOCIATE_WITH_THREAD);
        matchConnections = attrList.getString(MATCH_CONNECTIONS);
        maxConnectionUsageCount = attrList.getString(MAX_CONNECTION_USAGE_COUNT);
        description = attrList.getString(DESCRIPTION);
        poolname = attrList.getString(CONNECTOR_CONNECTION_POOL_NAME);
        pooling = attrList.getString(POOLING);
        ping = attrList.getString(PING);
        transactionSupport = attrList.getString(CONN_TRANSACTION_SUPPORT);
    }

    private ResourceStatus validateConnectorConnPoolAttributes(String raName, String connDef)
            throws ConnectorRuntimeException {
        ResourceStatus status = isValidRAName(raName);
        if(status.getStatus() == ResourceStatus.SUCCESS) {
            if(!isValidConnectionDefinition(connDef,raName)) {

                String msg = I18N.getLocalString("admin.mbeans.rmb.invalid_ra_connectdef_not_found",
                            "Invalid connection definition. Connector Module with connection definition {0} not found.", connDef);
                status = new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
        }
        return status;
    }

    private ResourceStatus isValidRAName(String raName) {
        //TODO turn on validation.  For now, turn validation off until connector modules ready
        //boolean retVal = false;
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "");

        if ((raName == null) || (raName.equals(""))) {
            String msg = I18N.getLocalString("admin.mbeans.rmb.null_res_adapter",
                    "Resource Adapter Name is null.");
            status = new ResourceStatus(ResourceStatus.FAILURE, msg);
        } else {
            // To check for embedded connector module
            // System RA, so don't validate
            if (!ConnectorsUtil.getNonJdbcSystemRars().contains(raName)) {
                // Check if the raName contains double underscore or hash.
                // If that is the case then this is the case of an embedded rar,
                // hence look for the application which embeds this rar,
                // otherwise look for the webconnector module with this raName.

                int indx = raName.indexOf(EMBEDDEDRAR_NAME_DELIMITER);
                if (indx != -1) {
                    String appName = raName.substring(0, indx);
                    Application app = applications.getModule(Application.class, appName);
                    if (app == null) {
                        String msg = I18N.getLocalString("admin.mbeans.rmb.invalid_ra_app_not_found",
                                "Invalid raname. Application with name {0} not found.", appName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                } else {
                    Application app = applications.getModule(Application.class, raName);
                    if (app == null) {
                        String msg = I18N.getLocalString("admin.mbeans.rmb.invalid_ra_cm_not_found",
                                "Invalid raname. Connector Module with name {0} not found.", raName);
                        status = new ResourceStatus(ResourceStatus.FAILURE, msg);
                    }
                }
            }
        }

        return status;
    }

    private boolean isValidConnectionDefinition(String connectionDef,String raName)
            throws ConnectorRuntimeException {
        String[] names = connectorRuntime.getConnectionDefinitionNames(raName);
        for (String name : names) {
            if(name.equals(connectionDef)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        setParams(attributes);
        final ResourceStatus status;
        if (validate) {
            status = isValid(resources, false);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources, properties);
        }
        throw new ResourceException(status.getMessage());
    }
}
