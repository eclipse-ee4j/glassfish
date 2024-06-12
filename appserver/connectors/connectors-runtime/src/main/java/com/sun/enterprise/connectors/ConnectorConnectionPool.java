/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.connectors;

import com.sun.enterprise.connectors.authentication.ConnectorSecurityMap;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.logging.LogDomains;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * This class abstracts a connection connection pool. It contains
 * two parts
 * 1) Connector Connection Pool properties.
 * 2) ConnectorDescriptorInfo which contains some of the values of ra.xml
 * pertaining to managed connection factory class
 *
 * @author Srikanth Padakandla
 */
public class ConnectorConnectionPool implements Serializable {

    private static final long serialVersionUID = 1405898931936731912L;
    public static final String DEFAULT_MAX_CONNECTION_USAGE = "0";
    public static final String DEFAULT_CON_CREATION_RETRY_ATTEMPTS = "0";
    public static final String DEFAULT_CON_CREATION_RETRY_INTERVAL = "10";
    public static final String DEFAULT_VALIDATE_ATMOST_ONCE_PERIOD = "0";
    public static final String DEFAULT_LEAK_TIMEOUT = "0";

    private static final Logger LOG = LogDomains.getLogger(ConnectorConnectionPool.class, LogDomains.RSR_LOGGER);

    protected ConnectorDescriptorInfo connectorDescriptorInfo_;

    protected String steadyPoolSize_;
    protected String maxPoolSize_;
    protected String maxWaitTimeInMillis_;
    protected String poolResizeQuantity_;
    protected String idleTimeoutInSeconds_;
    protected boolean failAllConnections_;
    //This property will *always* initially be set to:
    // true - by ConnectorConnectionPoolDeployer
    // false - by JdbcConnectionPoolDeployer
    protected boolean matchConnections_;

    protected int transactionSupport_;
    protected boolean isConnectionValidationRequired_;


    private boolean lazyConnectionAssoc_;
    private boolean lazyConnectionEnlist_;
    private boolean associateWithThread_;
    private boolean partitionedPool;
    private boolean poolingOn = true;
    private boolean pingDuringPoolCreation;
    private String poolDataStructureType;
    private String poolWaitQueue;
    private String dataStructureParameters;
    private String resourceGatewayClass;
    private boolean nonTransactional_;
    private boolean nonComponent_;

    private long dynamicReconfigWaitTimeout;

    private ConnectorSecurityMap[] securityMaps;
    private boolean isAuthCredentialsDefinedInPool_;

    private String maxConnectionUsage;

    //To validate a Sun RA Pool Connection if it hasnot been
    //validated in the past x sec. (x=idle-timeout)
    //The property will be set from system property :
    //com.sun.enterprise.connectors.ValidateAtmostEveryIdleSecs=true
    private boolean validateAtmostEveryIdleSecs;
    //This property will be set by ConnectorConnectionPoolDeployer or
    //JdbcConnectionPoolDeployer.
    private boolean preferValidateOverRecreate_;

    private String validateAtmostOncePeriod_;

    private String conCreationRetryAttempts_;
    private String conCreationRetryInterval_;

    private String connectionLeakTracingTimeout_;
    private boolean connectionReclaim_;

    private final SimpleJndiName name;
    private String applicationName;
    private String moduleName;

    public ConnectorConnectionPool(PoolInfo poolInfo) {
        this.name = poolInfo.getName();
        this.applicationName = poolInfo.getApplicationName();
        this.moduleName = poolInfo.getModuleName();
    }


    public String getApplicationName() {
        return applicationName;
    }


    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


    public String getModuleName() {
        return moduleName;
    }


    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }


    public boolean isApplicationScopedResource() {
        return applicationName != null;
    }

    public boolean getPingDuringPoolCreation() {
        return pingDuringPoolCreation;
    }

    /**
     * Setter method of Ping pool during creation attribute.
     *
     * @param enabled enables/disables ping during creation.
     */
    public void setPingDuringPoolCreation(boolean enabled) {
        pingDuringPoolCreation = enabled;
    }

    public boolean isPoolingOn() {
        return poolingOn;
    }

    /**
     * Setter method of pooling attribute
     *
     * @param enabled enables/disables pooling
     */
    public void setPooling(boolean enabled) {
        poolingOn = enabled;
    }

    public SimpleJndiName getName() {
        return name;
    }

    public void setAuthCredentialsDefinedInPool(boolean authCred) {
        this.isAuthCredentialsDefinedInPool_ = authCred;
    }

    public boolean getAuthCredentialsDefinedInPool() {
        return this.isAuthCredentialsDefinedInPool_;
    }

    /**
     * Getter method of ConnectorDescriptorInfo which contains some the ra.xml
     * values pertainining to managed connection factory
     *
     * @return ConnectorDescriptorInfo which contains ra.xml values
     *         pertaining to managed connection factory
     */

    public ConnectorDescriptorInfo getConnectorDescriptorInfo() {
        return connectorDescriptorInfo_;
    }

    /**
     * Setter method of ConnectorDescriptorInfo which contains some the ra.xml
     * values pertainining to managed connection factory
     *
     * @param connectorDescriptorInfo which contains ra.xml values
     *                                pertaining to managed connection factory
     */

    public void setConnectorDescriptorInfo(
            ConnectorDescriptorInfo connectorDescriptorInfo) {
        connectorDescriptorInfo_ = connectorDescriptorInfo;
    }


    /**
     * Getter method of SteadyPoolSize property
     *
     * @return Steady Pool Size value
     */

    public String getSteadyPoolSize() {
        return steadyPoolSize_;
    }

    /**
     * Setter method of SteadyPoolSize property
     *
     * @param steadyPoolSize Steady pool size value
     */

    public void setSteadyPoolSize(String steadyPoolSize) {
        steadyPoolSize_ = steadyPoolSize;
    }

    /**
     * Getter method of MaxPoolSize property
     *
     * @return maximum Pool Size value
     */

    public String getMaxPoolSize() {
        return maxPoolSize_;
    }

    /**
     * Setter method of MaxPoolSize property
     *
     * @param maxPoolSize maximum pool size value
     */

    public void setMaxPoolSize(String maxPoolSize) {
        maxPoolSize_ = maxPoolSize;
    }

    /**
     * Getter method of MaxWaitTimeInMillis property
     *
     * @return maximum wait time in milli value
     */

    public String getMaxWaitTimeInMillis() {
        return maxWaitTimeInMillis_;
    }

    /**
     * Setter method of MaxWaitTimeInMillis property
     *
     * @param maxWaitTimeInMillis maximum wait time in millis value
     */

    public void setMaxWaitTimeInMillis(String maxWaitTimeInMillis) {
        maxWaitTimeInMillis_ = maxWaitTimeInMillis;
    }

    /**
     * Getter method of PoolResizeQuantity property
     *
     * @return pool resize quantity value
     */

    public String getPoolResizeQuantity() {
        return poolResizeQuantity_;
    }

    /**
     * Setter method of PoolResizeQuantity property
     *
     * @param poolResizeQuantity pool resize quantity value
     */

    public void setPoolResizeQuantity(String poolResizeQuantity) {
        poolResizeQuantity_ = poolResizeQuantity;
    }

    /**
     * Getter method of IdleTimeoutInSeconds property
     *
     * @return idle Timeout in seconds value
     */

    public String getIdleTimeoutInSeconds() {
        return idleTimeoutInSeconds_;
    }

    /**
     * Setter method of IdleTimeoutInSeconds property
     *
     * @param idleTimeoutInSeconds Idle timeout in seconds value
     */

    public void setIdleTimeoutInSeconds(String idleTimeoutInSeconds) {
        idleTimeoutInSeconds_ = idleTimeoutInSeconds;
    }

    /**
     * Getter method of FailAllConnections property
     *
     * @return whether to fail all connections or not
     */

    public boolean isFailAllConnections() {
        return failAllConnections_;
    }

    /**
     * Setter method of FailAllConnections property
     *
     * @param failAllConnections fail all connections value
     */

    public void setFailAllConnections(boolean failAllConnections) {
        failAllConnections_ = failAllConnections;
    }

    /**
     * Getter method of matchConnections property
     *
     * @return whether to match connections always with resource adapter
     *         or not
     */

    public boolean matchConnections() {
        return matchConnections_;
    }

    /**
     * Setter method of matchConnections property
     *
     * @param matchConnections fail all connections value
     */

    public void setMatchConnections(boolean matchConnections) {
        matchConnections_ = matchConnections;
    }

    /**
     * Returns the transaction support level for this pool
     * The valid values are<br>
     * <ul>
     * <li>ConnectorConstants.NO_TRANSACTION</li>
     * <li>ConnectorConstants.LOCAL_TRANSACTION</li>
     * <li>ConnectorConstants.XA_TRANSACTION</li>
     * </ul>
     *
     * @return the transaction support level for this pool
     */
    public int getTransactionSupport() {
        return transactionSupport_;
    }

    /**
     * Sets the transaction support level for this pool
     * The valid values are<br>
     *
     * @param transactionSupport int representing transaction support<br>
     *                           <ul>
     *                           <li>ConnectorConstants.NO_TRANSACTION</li>
     *                           <li>ConnectorConstants.LOCAL_TRANSACTION</li>
     *                           <li>ConnectorConstants.XA_TRANSACTION</li>
     *                           </ul>
     */
    public void setTransactionSupport(int transactionSupport) {
        transactionSupport_ = transactionSupport;
    }

    /**
     * Sets the connection-validation-required pool attribute
     *
     * @param validation boolean representing validation requirement
     */
    public void setConnectionValidationRequired(boolean validation) {
        isConnectionValidationRequired_ = validation;
    }

    /**
     * Queries the connection-validation-required pool attribute
     *
     * @return boolean representing validation requirement
     */
    public boolean isIsConnectionValidationRequired() {
        return isConnectionValidationRequired_;
    }

    /**
     * Queries the lazy-connection-association pool attribute
     *
     * @return boolean representing lazy-connection-association status
     */
    public boolean isLazyConnectionAssoc() {
        return lazyConnectionAssoc_;
    }

    /**
     * Setter method of lazyConnectionAssociation attribute
     *
     * @param enabled enables/disables lazy-connection-association
     */
    public void setLazyConnectionAssoc(boolean enabled) {
        lazyConnectionAssoc_ = enabled;
    }

    /**
     * Queries the lazy-connection-enlistment pool attribute
     *
     * @return boolean representing lazy-connection-enlistment status
     */
    public boolean isLazyConnectionEnlist() {
        return lazyConnectionEnlist_;
    }

    /**
     * Setter method of lazy-connection-enlistment attribute
     *
     * @param enabled enables/disables lazy-connection-enlistment
     */
    public void setLazyConnectionEnlist(boolean enabled) {
        lazyConnectionEnlist_ = enabled;
    }

    /**
     * Queries the associate-with-thread pool attribute
     *
     * @return boolean representing associate-with-thread status
     */
    public boolean isAssociateWithThread() {
        return associateWithThread_;
    }

    /**
     * Setter method of associate-with-thread attribute
     *
     * @param enabled enables/disables associate-with-thread
     */
    public void setAssociateWithThread(boolean enabled) {
        associateWithThread_ = enabled;
    }

    /**
     * Queries the non-transactional pool attribute
     *
     * @return boolean representing non-transactional status
     */
    public boolean isNonTransactional() {
        return nonTransactional_;
    }

    /**
     * Setter method of non-transactional attribute
     *
     * @param enabled enables/disables non-transactional status
     */
    public void setNonTransactional(boolean enabled) {
        nonTransactional_ = enabled;
    }

    /**
     * Queries the non-component pool attribute
     *
     * @return boolean representing non-component status
     */
    public boolean isNonComponent() {
        return nonComponent_;
    }

    /**
     * Setter method of non-component attribute
     *
     * @param enabled enables/disables non-component status
     */
    public void setNonComponent(boolean enabled) {
        nonComponent_ = enabled;
    }

    /**
     * Queries the connection-leak-tracing-timeout pool attribute
     *
     * @return boolean representing connection-leak-tracing-timeout status
     */
    public String getConnectionLeakTracingTimeout() {
        return connectionLeakTracingTimeout_;
    }

    /**
     * Setter method of connection-leak-tracing-timeout attribute
     *
     * @param timeout value after which connection is assumed to be leaked.
     */
    public void setConnectionLeakTracingTimeout(String timeout) {
        connectionLeakTracingTimeout_ = timeout;
    }


    /**
     * Setter method for Security Maps
     *
     * @param securityMapArray SecurityMap[]
     */

    public void setSecurityMaps(ConnectorSecurityMap[] securityMapArray) {
        this.securityMaps = securityMapArray;
    }

    /**
     * Getter method for Security Maps
     *
     * @return SecurityMap[]
     */

    public ConnectorSecurityMap[] getSecurityMaps() {
        return this.securityMaps;
    }


    /**
     * Queries the validate-atmost-every-idle-seconds pool attribute
     *
     * @return boolean representing validate-atmost-every-idle-seconds
     *         status
     */
    public boolean isValidateAtmostEveryIdleSecs() {
        return validateAtmostEveryIdleSecs;
    }

    /**
     * Setter method of validate-atmost-every-idle-seconds pool attribute
     *
     * @param enabled enables/disables validate-atmost-every-idle-seconds
     *                property
     */
    public void setValidateAtmostEveryIdleSecs(boolean enabled) {
        this.validateAtmostEveryIdleSecs = enabled;
    }

    /**
     * Setter method of max-connection-usage pool attribute
     *
     * @param count max-connection-usage count
     */
    public void setMaxConnectionUsage(String count) {
        maxConnectionUsage = count;
    }

    /**
     * Queries the max-connection-usage pool attribute
     *
     * @return boolean representing max-connection-usage count
     */
    public String getMaxConnectionUsage() {
        return maxConnectionUsage;
    }

    /**
     * Queries the connection-creation-retry-interval pool attribute
     *
     * @return boolean representing connection-creation-retry-interval
     *         duration
     */
    public String getConCreationRetryInterval() {
        return conCreationRetryInterval_;
    }

    /**
     * Setter method of connection-creation-retry-interval attribute
     *
     * @param retryInterval connection-creation-retry-interval  duration
     */
    public void setConCreationRetryInterval(String retryInterval) {
        this.conCreationRetryInterval_ = retryInterval;
    }

    /**
     * Queries the connection-creation-retry-attempt pool attribute
     *
     * @return boolean representing connection-creation-retry-attempt count
     */
    public String getConCreationRetryAttempts() {
        return conCreationRetryAttempts_;
    }

    /**
     * Setter method of connection-creation-retry-attempt attribute
     *
     * @param retryAttempts connection-creation-retry-attempt interval
     *                      duration
     */
    public void setConCreationRetryAttempts(String retryAttempts) {
        this.conCreationRetryAttempts_ = retryAttempts;
    }

    /**
     * Queries the validate-atmost-period pool attribute
     *
     * @return boolean representing validate-atmost-period duration
     */
    public String getValidateAtmostOncePeriod() {
        return validateAtmostOncePeriod_;
    }

    /**
     * Setter method of validate-atmost-period attribute
     *
     * @param validateAtmostOncePeriod validate-atmost-period duration
     */
    public void setValidateAtmostOncePeriod(String validateAtmostOncePeriod) {
        this.validateAtmostOncePeriod_ = validateAtmostOncePeriod;
    }

    /**
     * Queries the connection-reclaim attribute
     *
     * @return boolean representing connection-reclaim status
     */
    public boolean isConnectionReclaim() {
        return connectionReclaim_;
    }

    /**
     * Setter method of connection-reclaim attribute
     *
     * @param connectionReclaim onnection-reclaim status
     */
    public void setConnectionReclaim(boolean connectionReclaim) {
        this.connectionReclaim_ = connectionReclaim;
    }

    /**
     * return the String representation of the pool.
     *
     * @return String representation of pool
     */
    @Override
    public String toString() {
        String returnVal = "";
        StringBuffer sb = new StringBuffer("ConnectorConnectionPool :: ");
        try {
            sb.append(getName());
            sb.append("\nsteady size: ");
            sb.append(getSteadyPoolSize());
            sb.append("\nmax pool size: ");
            sb.append(getMaxPoolSize());
            sb.append("\nmax wait time: ");
            sb.append(getMaxWaitTimeInMillis());
            sb.append("\npool resize qty: ");
            sb.append(getPoolResizeQuantity());
            sb.append("\nIdle timeout: ");
            sb.append(getIdleTimeoutInSeconds());
            sb.append("\nfailAllConnections: ");
            sb.append(isFailAllConnections());
            sb.append("\nTransaction Support Level: ");
            sb.append(transactionSupport_);
            sb.append("\nisConnectionValidationRequired_ ");
            sb.append(isConnectionValidationRequired_);
            sb.append("\npreferValidateOverRecreate_ ");
            sb.append(preferValidateOverRecreate_);

            sb.append("\nmatchConnections_ ");
            sb.append(matchConnections_);
            sb.append("\nassociateWithThread_ ");
            sb.append(associateWithThread_);
            sb.append("\nlazyConnectionAssoc_ ");
            sb.append(lazyConnectionAssoc_);
            sb.append("\nlazyConnectionEnlist_ ");
            sb.append(lazyConnectionEnlist_);
            sb.append("\nmaxConnectionUsage_ ");
            sb.append(maxConnectionUsage);

            sb.append("\npingPoolDuringCreation_ ");
            sb.append(pingDuringPoolCreation);

            sb.append("\npoolingOn_ ");
            sb.append(poolingOn);

            sb.append("\nvalidateAtmostOncePeriod_ ");
            sb.append(validateAtmostOncePeriod_);

            sb.append("\nconnectionLeakTracingTimeout_");
            sb.append(connectionLeakTracingTimeout_);
            sb.append("\nconnectionReclaim_");
            sb.append(connectionReclaim_);

            sb.append("\nconnectionCreationRetryAttempts_");
            sb.append(conCreationRetryAttempts_);
            sb.append("\nconnectionCreationRetryIntervalInMilliSeconds_");
            sb.append(conCreationRetryInterval_);

            sb.append("\nnonTransactional_ ");
            sb.append(nonTransactional_);
            sb.append("\nnonComponent_ ");
            sb.append(nonComponent_);

            sb.append("\nConnectorDescriptorInfo -> ");
            sb.append("\nrarName: ");
            if (connectorDescriptorInfo_ != null) {
                sb.append(connectorDescriptorInfo_.getRarName());
                sb.append("\nresource adapter class: ");
                sb.append(connectorDescriptorInfo_.getResourceAdapterClassName());
                sb.append("\nconnection def name: ");
                sb.append(connectorDescriptorInfo_.getConnectionDefinitionName());
                sb.append("\nMCF Config properties-> ");
                for (Object o : connectorDescriptorInfo_.getMCFConfigProperties()) {
                    ConnectorConfigProperty  ep = (ConnectorConfigProperty) o;
                    sb.append(ep.getName());
                    sb.append(":");
                    sb.append(("password".equalsIgnoreCase(ep.getName()) ?
                            "****" : ep.getValue()));
                    sb.append("\n");
                }
            }
            if (securityMaps != null) {
                sb.append("SecurityMaps -> {");
                for (ConnectorSecurityMap securityMap : securityMaps) {
                    if (securityMap != null &&
                            securityMap.getName() != null) {
                        sb.append(securityMap.getName());
                        sb.append(" ");
                    }
                }
                sb.append("}");
            }
            returnVal = sb.toString();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception while computing toString() of connection pool [ "+name+" ]", e);
        }
        return returnVal;
    }

    public boolean isPartitionedPool() {
        return partitionedPool;
    }

    public void setPartitionedPool(boolean partitionedPool) {
        this.partitionedPool = partitionedPool;
    }

    public String getPoolDataStructureType() {
        return poolDataStructureType;
    }

    public void setPoolDataStructureType(String poolDataStructureType) {
        this.poolDataStructureType = poolDataStructureType;
    }

    public String getPoolWaitQueue() {
        return poolWaitQueue;
    }

    public void setPoolWaitQueue(String poolWaitQueue) {
        this.poolWaitQueue = poolWaitQueue;
    }

    public String getDataStructureParameters() {
        return dataStructureParameters;
    }

    public void setDataStructureParameters(String dataStructureParameters) {
        this.dataStructureParameters = dataStructureParameters;
    }

    public String getResourceGatewayClass() {
        return resourceGatewayClass;
    }

    public void setResourceGatewayClass(String resourceGatewayClass) {
        this.resourceGatewayClass = resourceGatewayClass;
    }

    public boolean isPreferValidateOverRecreate() {
        return preferValidateOverRecreate_;
    }

    public void setPreferValidateOverRecreate(boolean preferValidateOverRecreate) {
        preferValidateOverRecreate_ = preferValidateOverRecreate;
    }

    public long getDynamicReconfigWaitTimeout() {
        return dynamicReconfigWaitTimeout;
    }

    public void setDynamicReconfigWaitTimeout(long dynamicReconfigWaitTimeout) {
        this.dynamicReconfigWaitTimeout = dynamicReconfigWaitTimeout;
    }


    public PoolInfo getPoolInfo() {
        return new PoolInfo(name, applicationName, moduleName);
    }
}
