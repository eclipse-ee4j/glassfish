/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

    protected ConnectorDescriptorInfo connectorDescriptorInfo;

    protected String steadyPoolSize;
    protected String maxPoolSize;
    protected String maxWaitTimeInMillis;
    protected String poolResizeQuantity;
    protected String idleTimeoutInSeconds;
    protected boolean failAllConnections;
    //This property will *always* initially be set to:
    // true - by ConnectorConnectionPoolDeployer
    // false - by JdbcConnectionPoolDeployer
    protected boolean matchConnections;

    protected int transactionSupport;
    protected boolean isConnectionValidationRequired;


    private boolean lazyConnectionAssoc;
    private boolean lazyConnectionEnlist;
    private boolean associateWithThread;
    private boolean partitionedPool;
    private boolean poolingOn = true;
    private boolean pingDuringPoolCreation;
    private String poolDataStructureType;
    private String poolWaitQueue;
    private String dataStructureParameters;
    private String resourceGatewayClass;
    private boolean nonTransactional;
    private boolean nonComponent;

    private long dynamicReconfigWaitTimeout;

    private ConnectorSecurityMap[] securityMaps;
    private boolean isAuthCredentialsDefinedInPool;

    private String maxConnectionUsage;

    //To validate a Sun RA Pool Connection if it hasnot been
    //validated in the past x sec. (x=idle-timeout)
    //The property will be set from system property :
    //com.sun.enterprise.connectors.ValidateAtmostEveryIdleSecs=true
    private boolean validateAtmostEveryIdleSecs;
    //This property will be set by ConnectorConnectionPoolDeployer or
    //JdbcConnectionPoolDeployer.
    private boolean preferValidateOverRecreate;

    private String validateAtmostOncePeriod;

    private String conCreationRetryAttempts;
    private String conCreationRetryInterval;

    private String connectionLeakTracingTimeout;
    private boolean connectionReclaim;

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
        this.pingDuringPoolCreation = enabled;
    }

    /**
     * Getter of "--pooling, pooling" attribute
     * @return true if pooling is on, otherwise false
     */
    public boolean isPoolingOn() {
        return poolingOn;
    }

    /**
     * Setter method of "--pooling, pooling" attribute
     *
     * @param enabled enables/disables pooling
     */
    public void setPooling(boolean enabled) {
        this.poolingOn = enabled;
    }

    public SimpleJndiName getName() {
        return name;
    }

    public void setAuthCredentialsDefinedInPool(boolean authCred) {
        this.isAuthCredentialsDefinedInPool = authCred;
    }

    public boolean getAuthCredentialsDefinedInPool() {
        return this.isAuthCredentialsDefinedInPool;
    }

    /**
     * Getter method of ConnectorDescriptorInfo which contains some the ra.xml
     * values pertaining to managed connection factory
     *
     * @return ConnectorDescriptorInfo which contains ra.xml values
     *         pertaining to managed connection factory
     */
    public ConnectorDescriptorInfo getConnectorDescriptorInfo() {
        return connectorDescriptorInfo;
    }

    /**
     * Setter method of ConnectorDescriptorInfo which contains some the ra.xml
     * values pertaining to managed connection factory
     *
     * @param connectorDescriptorInfo which contains ra.xml values
     *                                pertaining to managed connection factory
     */
    public void setConnectorDescriptorInfo(
            ConnectorDescriptorInfo connectorDescriptorInfo) {
        this.connectorDescriptorInfo = connectorDescriptorInfo;
    }

    /**
     * Getter method of "--steadypoolsize, steady-pool-size" property
     *
     * @return Steady Pool Size value
     */
    public String getSteadyPoolSize() {
        return steadyPoolSize;
    }

    /**
     * Setter method of "--steadypoolsize, steady-pool-size" property
     *
     * @param steadyPoolSize Steady pool size value
     */

    public void setSteadyPoolSize(String steadyPoolSize) {
        this.steadyPoolSize = steadyPoolSize;
    }

    /**
     * Getter method of "--maxpoolsize, max-pool-size" property
     *
     * @return maximum Pool Size value
     */
    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Setter method of "--maxpoolsize, max-pool-size" property
     *
     * @param maxPoolSize maximum pool size value
     */
    public void setMaxPoolSize(String maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * Getter method of "--maxwait, max-wait-time-in-millis" property
     *
     * @return maximum wait time in milliseconds
     */
    public String getMaxWaitTimeInMillis() {
        return maxWaitTimeInMillis;
    }

    /**
     * Setter method of "--maxwait, max-wait-time-in-millis" property
     *
     * @param maxWaitTimeInMillis maximum wait time in millis value
     */
    public void setMaxWaitTimeInMillis(String maxWaitTimeInMillis) {
        this.maxWaitTimeInMillis = maxWaitTimeInMillis;
    }

    /**
     * Getter method of "--poolresize, pool-resize-quantity" property
     *
     * @return pool resize quantity value
     */
    public String getPoolResizeQuantity() {
        return poolResizeQuantity;
    }

    /**
     * Setter method of "--poolresize, pool-resize-quantity" property
     *
     * @param poolResizeQuantity pool resize quantity value
     */

    public void setPoolResizeQuantity(String poolResizeQuantity) {
        this.poolResizeQuantity = poolResizeQuantity;
    }

    /**
     * Getter method of "--idletimeout, idle-timeout-in-seconds" property
     *
     * @return idle Timeout in seconds value
     */
    public String getIdleTimeoutInSeconds() {
        return idleTimeoutInSeconds;
    }

    /**
     * Setter method of "--idletimeout, idle-timeout-in-seconds" property
     *
     * @param idleTimeoutInSeconds Idle timeout in seconds value
     */
    public void setIdleTimeoutInSeconds(String idleTimeoutInSeconds) {
        this.idleTimeoutInSeconds = idleTimeoutInSeconds;
    }

    /**
     * Getter method of "--failconnection, fail-all-connections" property
     *
     * @return whether to fail all connections or not
     */
    public boolean isFailAllConnections() {
        return failAllConnections;
    }

    /**
     * Setter method of "--failconnection, fail-all-connections" property
     *
     * @param failAllConnections fail all connections value
     */
    public void setFailAllConnections(boolean failAllConnections) {
        this.failAllConnections = failAllConnections;
    }

    /**
     * Getter method of "--matchconnections, match-connections" property
     *
     * @return whether to match connections always with resource adapter
     *         or not
     */
    public boolean matchConnections() {
        return matchConnections;
    }

    /**
     * Setter method of "--matchconnections, match-connections" property
     *
     * @param matchConnections fail all connections value
     */
    public void setMatchConnections(boolean matchConnections) {
        this.matchConnections = matchConnections;
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
        return transactionSupport;
    }

    /**
     * Sets the transaction support level for this pool.<br>
     * Property: "--transactionsupport, transaction-support"<br>
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
        this.transactionSupport = transactionSupport;
    }

    /**
     * Sets the "--isconnectvalidatereq, is-connection-validation-required" pool attribute
     *
     * @param validation boolean representing validation requirement
     */
    public void setConnectionValidationRequired(boolean validation) {
        isConnectionValidationRequired = validation;
    }

    /**
     * Queries the "--isconnectvalidatereq, is-connection-validation-required" pool attribute
     *
     * @return boolean representing validation requirement
     */
    public boolean isIsConnectionValidationRequired() {
        return isConnectionValidationRequired;
    }

    /**
     * Queries the "--lazyconnectionassociation, lazy-connection-association" pool attribute
     *
     * @return boolean representing lazy-connection-association status
     */
    public boolean isLazyConnectionAssoc() {
        return lazyConnectionAssoc;
    }

    /**
     * Setter method of "--lazyconnectionassociation, lazy-connection-association" attribute
     *
     * @param enabled enables/disables lazy-connection-association
     */
    public void setLazyConnectionAssoc(boolean enabled) {
        this.lazyConnectionAssoc = enabled;
    }

    /**
     * Queries the "--lazyconnectionenlistment, lazy-connection-enlistment" pool attribute
     *
     * @return boolean representing lazy-connection-enlistment status
     */
    public boolean isLazyConnectionEnlist() {
        return lazyConnectionEnlist;
    }

    /**
     * Setter method of "--lazyconnectionenlistment, lazy-connection-enlistment" attribute
     *
     * @param enabled enables/disables lazy-connection-enlistment
     */
    public void setLazyConnectionEnlist(boolean enabled) {
        this.lazyConnectionEnlist = enabled;
    }

    /**
     * Queries the "--associatewiththread, associate-with-thread" pool attribute
     *
     * @return boolean representing associate-with-thread status
     */
    public boolean isAssociateWithThread() {
        return associateWithThread;
    }

    /**
     * Setter method of "--associatewiththread, associate-with-thread" attribute
     *
     * @param enabled enables/disables associate-with-thread
     */
    public void setAssociateWithThread(boolean enabled) {
        this.associateWithThread = enabled;
    }

    /**
     * Queries the "--nontransactionalconnections, non-transactional-connections" pool attribute
     *
     * @return boolean representing non-transactional status
     */
    public boolean isNonTransactional() {
        return nonTransactional;
    }

    /**
     * Setter method of "--nontransactionalconnections, non-transactional-connections" attribute
     *
     * @param enabled enables/disables non-transactional status
     */
    public void setNonTransactional(boolean enabled) {
        this.nonTransactional = enabled;
    }

    /**
     * Queries the "--allownoncomponentcallers, allow-non-component-callers" pool attribute
     *
     * @return boolean representing non-component status
     */
    public boolean isNonComponent() {
        return nonComponent;
    }

    /**
     * Setter method of "--allownoncomponentcallers, allow-non-component-callers" attribute
     *
     * @param enabled enables/disables non-component status
     */
    public void setNonComponent(boolean enabled) {
        this.nonComponent = enabled;
    }

    /**
     * Queries the "--leaktimeout, connection-leak-timeout-in-seconds" pool attribute
     *
     * @return boolean representing connection-leak-tracing-timeout status
     */
    public String getConnectionLeakTracingTimeout() {
        return connectionLeakTracingTimeout;
    }

    /**
     * Setter method of "--leaktimeout, connection-leak-timeout-in-seconds" attribute
     *
     * @param timeout value after which connection is assumed to be leaked.
     */
    public void setConnectionLeakTracingTimeout(String timeout) {
        this.connectionLeakTracingTimeout = timeout;
    }

    /**
     * Setter method for "security-map" pool attributes
     *
     * @param securityMapArray SecurityMap[]
     */
    public void setSecurityMaps(ConnectorSecurityMap[] securityMapArray) {
        this.securityMaps = securityMapArray;
    }

    /**
     * Getter method for "security-map" pool attributes
     *
     * @return SecurityMap[]
     */
    public ConnectorSecurityMap[] getSecurityMaps() {
        return this.securityMaps;
    }

    /**
     * Queries the "--validateatmostonceperiod, validate-atmost-once-period-in-seconds" pool attribute
     *
     * @return boolean representing validate-atmost-every-idle-seconds
     *         status
     */
    public boolean isValidateAtmostEveryIdleSecs() {
        return validateAtmostEveryIdleSecs;
    }

    /**
     * Setter method of "--validateatmostonceperiod, validate-atmost-once-period-in-seconds" pool attribute
     *
     * @param enabled enables/disables validate-atmost-every-idle-seconds
     *                property
     */
    public void setValidateAtmostEveryIdleSecs(boolean enabled) {
        this.validateAtmostEveryIdleSecs = enabled;
    }

    /**
     * Setter method of "--maxconnectionusagecount, max-connection-usage-count" pool attribute
     *
     * @param count value for max-connection-usage-count
     */
    public void setMaxConnectionUsage(String count) {
        this.maxConnectionUsage = count;
    }

    /**
     * Queries the "--maxconnectionusagecount, max-connection-usage-count" pool attribute
     *
     * @return boolean representing max-connection-usage count
     */
    public String getMaxConnectionUsage() {
        return maxConnectionUsage;
    }

    /**
     * Queries the "--creationretryinterval, connection-creation-retry-interval-in-seconds" pool attribute
     *
     * @return boolean representing connection-creation-retry-interval
     *         duration
     */
    public String getConCreationRetryInterval() {
        return conCreationRetryInterval;
    }

    /**
     * Setter method of "--creationretryinterval, connection-creation-retry-interval-in-seconds" attribute
     *
     * @param retryInterval connection-creation-retry-interval  duration
     */
    public void setConCreationRetryInterval(String retryInterval) {
        this.conCreationRetryInterval = retryInterval;
    }

    /**
     * Queries the "--creationretryattempts, connection-creation-retry-attempts" pool attribute
     *
     * @return boolean representing connection-creation-retry-attempt count
     */
    public String getConCreationRetryAttempts() {
        return conCreationRetryAttempts;
    }

    /**
     * Setter method of "--creationretryattempts, connection-creation-retry-attempts" attribute
     *
     * @param retryAttempts connection-creation-retry-attempt interval
     *                      duration
     */
    public void setConCreationRetryAttempts(String retryAttempts) {
        this.conCreationRetryAttempts = retryAttempts;
    }

    /**
     * Queries the "--validateatmostonceperiod, validate-atmost-once-period-in-seconds" pool attribute
     *
     * @return boolean representing validate-atmost-period duration
     */
    public String getValidateAtmostOncePeriod() {
        return validateAtmostOncePeriod;
    }

    /**
     * Setter method of "--validateatmostonceperiod, validate-atmost-once-period-in-seconds" attribute
     *
     * @param validateAtmostOncePeriod validate-atmost-period duration
     */
    public void setValidateAtmostOncePeriod(String validateAtmostOncePeriod) {
        this.validateAtmostOncePeriod = validateAtmostOncePeriod;
    }

    /**
     * Queries the "--leakreclaim, connection-leak-reclaim" attribute
     *
     * @return boolean representing connection-reclaim status
     */
    public boolean isConnectionReclaim() {
        return connectionReclaim;
    }

    /**
     * Setter method of "--leakreclaim, connection-leak-reclaim" attribute
     *
     * @param connectionReclaim onnection-reclaim status
     */
    public void setConnectionReclaim(boolean connectionReclaim) {
        this.connectionReclaim = connectionReclaim;
    }

    /**
     * Return the String representation of the pool.
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
            sb.append(transactionSupport);
            sb.append("\nisConnectionValidationRequired ");
            sb.append(isConnectionValidationRequired);
            sb.append("\npreferValidateOverRecreate ");
            sb.append(preferValidateOverRecreate);

            sb.append("\nmatchConnections ");
            sb.append(matchConnections);
            sb.append("\nassociateWithThread ");
            sb.append(associateWithThread);
            sb.append("\nlazyConnectionAssoc ");
            sb.append(lazyConnectionAssoc);
            sb.append("\nlazyConnectionEnlist ");
            sb.append(lazyConnectionEnlist);
            sb.append("\nmaxConnectionUsage ");
            sb.append(maxConnectionUsage);

            sb.append("\npingPoolDuringCreation ");
            sb.append(pingDuringPoolCreation);

            sb.append("\npoolingOn ");
            sb.append(poolingOn);

            sb.append("\nvalidateAtmostOncePeriod ");
            sb.append(validateAtmostOncePeriod);

            sb.append("\nconnectionLeakTracingTimeout");
            sb.append(connectionLeakTracingTimeout);
            sb.append("\nconnectionReclaim");
            sb.append(connectionReclaim);

            sb.append("\nconnectionCreationRetryAttempts");
            sb.append(conCreationRetryAttempts);
            sb.append("\nconnectionCreationRetryIntervalInMilliSeconds");
            sb.append(conCreationRetryInterval);

            sb.append("\nnonTransactional ");
            sb.append(nonTransactional);
            sb.append("\nnonComponent ");
            sb.append(nonComponent);

            sb.append("\nConnectorDescriptorInfo -> ");
            sb.append("\nrarName: ");
            if (connectorDescriptorInfo != null) {
                sb.append(connectorDescriptorInfo.getRarName());
                sb.append("\nresource adapter class: ");
                sb.append(connectorDescriptorInfo.getResourceAdapterClassName());
                sb.append("\nconnection def name: ");
                sb.append(connectorDescriptorInfo.getConnectionDefinitionName());
                sb.append("\nMCF Config properties-> ");
                for (Object o : connectorDescriptorInfo.getMCFConfigProperties()) {
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
        return preferValidateOverRecreate;
    }

    public void setPreferValidateOverRecreate(boolean preferValidateOverRecreate) {
        this.preferValidateOverRecreate = preferValidateOverRecreate;
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
