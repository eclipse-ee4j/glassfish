/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.ResourceState;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.listener.PoolLifeCycleListener;
import com.sun.enterprise.resource.pool.datastructure.DataStructure;
import com.sun.enterprise.resource.pool.datastructure.DataStructureFactory;
import com.sun.enterprise.resource.pool.resizer.Resizer;
import com.sun.enterprise.resource.pool.waitqueue.PoolWaitQueue;
import com.sun.enterprise.resource.pool.waitqueue.PoolWaitQueueFactory;
import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.RetryableUnavailableException;
import jakarta.transaction.Transaction;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.glassfish.resourcebase.resources.api.PoolInfo;

import static com.sun.appserv.connectors.internal.spi.BadConnectionEventListener.POOL_RECONFIGURED_ERROR_CODE;
import static com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils.getReservePrefixedJNDINameForPool;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * Connection Pool for Connector & JDBC resources<br>
 *
 * @author Jagadish Ramu
 */
public class ConnectionPool implements ResourcePool, ConnectionLeakListener, ResourceHandler, PoolProperties {

    private static final Logger LOG = LogDomains.getLogger(ConnectionPool.class, LogDomains.RSR_LOGGER);

    // pool life-cycle config properties
    /**
     * Represents the "max-pool-size" configuration value.<br>
     * Specifies the maximum number of connections that can be created to satisfy client requests.<br>
     * Default: 32
     */
    protected int maxPoolSize;

    /**
     * Represents the "steady-pool-size" configuration value.<br>
     * Specifies the initial and minimum number of connections maintained in the pool.<br>
     * Default: 8
     */
    protected int steadyPoolSize;

    /**
     * Represents the "pool-resize-quantity" configuration value.<br>
     * Specifies the number of idle connections to be destroyed if the existing number of connections is above the
     * steady-pool-size (subject to the max-pool-size limit).<br>
     * This is enforced periodically at the idle-timeout-in-seconds interval. An idle connection is one that has not been
     * used for a period of idle-timeout-in-seconds. When the pool size reaches steady-pool-size, connection removal
     * stops.<br>
     * Default: 2
     */
    protected int resizeQuantity;

    /**
     * Represents the "max-wait-time-in-millis" configuration value.<br>
     * Specifies the amount of time, in milliseconds, that the caller is willing to wait for a connection. If 0, the caller
     * is blocked indefinitely until a resource is available or an error occurs.<br>
     * Default: 60.000ms
     */
    protected int maxWaitTime;

    /**
     * Represents the "idle-timeout-in-seconds" configuration value, but in millis.<br>
     * Specifies the maximum time that a connection can remain idle in the pool. After this amount of time, the pool can
     * close this connection.<br>
     * Default: 300.000ms
     */
    protected long idletime;

    // pool config properties
    /**
     * Represents the "fail-all-connections" configuration value.<br>
     * If true, closes all connections in the pool if a single validation check fails.<br>
     * Default: false
     */
    protected boolean failAllConnections;

    /**
     * Represents the "match-connections" configuration value.<br>
     * If true, enables connection matching. You can set to false if connections are homogeneous.<br>
     * Default: true
     */
    protected boolean matchConnections;
    /**
     * Represents the "is-connection-validation-required" configuration value.<br>
     * Specifies whether connections have to be validated before being given to the application. If a resource’s validation
     * fails, it is destroyed, and a new resource is created and returned.<br>
     * Default: false
     *
     * TODO: rename to 'connectionValidationRequired'
     */
    protected boolean validation;

    /**
     * Represents the "prefer-validate-over-recreate property" configuration value.<br>
     * Specifies that validating idle connections is preferable to closing them. This property has no effect on non-idle
     * connections. If set to true, idle connections are validated during pool resizing, and only those found to be invalid
     * are destroyed and recreated. If false, all idle connections are destroyed and recreated during pool resizing.<br>
     * Default: false
     */
    protected boolean preferValidateOverRecreate;

    // hold on to the resizer task so we can cancel/reschedule it.
    protected Resizer resizerTask;

    protected volatile boolean poolInitialized;
    protected Timer timer;

    // advanced pool config properties
    /**
     * Represents the "connection-creation-retry-attempts" configuration value.<br>
     * If the value connectionCreationRetryAttempts_ is > 0 then connectionCreationRetry_ is true, otherwise false.
     */
    protected boolean connectionCreationRetry_;
    /**
     * Represents the "connection-creation-retry-attempts" configuration.<br>
     * Specifies the number of attempts to create a new connection.<br>
     * Default: 0
     */
    protected int connectionCreationRetryAttempts_;

    /**
     * Represents the "connection-creation-retry-interval-in-seconds" configuration, but in millis.<br>
     * Specifies the time interval between attempts to create a connection when connection-creation-retry-attempts is
     * greater than 0.<br>
     * Default: 10.000ms
     */
    protected long conCreationRetryInterval_;

    /**
     * Represents the "validate-atmost-once-period-in-seconds" configuration, but in millis.<br>
     * Specifies the time interval within which a connection is validated at most once. Minimizes the number of validation
     * calls. A value of zero allows unlimited validation calls.<br>
     * Default: 10.000ms.
     */
    protected long validateAtmostPeriodInMilliSeconds_;

    /**
     * Represents the "max-connection-usage-count" configuration.<br>
     * Specifies the number of times a connections is reused by the pool, after which it is closed. A zero value disables
     * this feature.<br>
     * Default: 0
     */
    protected int maxConnectionUsage_;

    // To validate a Sun RA Pool Connection if it has not been validated
    // in the past x sec. (x=idle-timeout)
    // The property will be set from system property -
    // com.sun.enterprise.connectors.ValidateAtmostEveryIdleSecs=true
    /**
     * Represents the "validate-atmost-once-period-in-seconds" configuration.<br>
     * Specifies the time interval within which a connection is validated at most once. Minimizes the number of validation
     * calls. A value of zero allows unlimited validation calls.<br>
     * Default: 0
     */
    private boolean validateAtmostEveryIdleSecs;

    // TODO document
    protected String resourceSelectionStrategyClass;

    protected PoolLifeCycleListener poolLifeCycleListener;

    // Gateway used to control the concurrency within the round-trip of resource access.
    protected ResourceGateway gateway;
    protected String resourceGatewayClass;

    protected ConnectionLeakDetector leakDetector;

    protected DataStructure dataStructure;
    protected String dataStructureType;
    protected String dataStructureParameters;

    protected PoolWaitQueue waitQueue;
    protected PoolWaitQueue reconfigWaitQueue;
    private long reconfigWaitTime;
    protected String poolWaitQueueClass;

    protected final PoolInfo poolInfo; // poolName

    private final PoolTxHelper poolTxHelper;

    // NOTE: This resource allocator may not be the same as the allocator passed in to getResource()
    protected ResourceAllocator allocator;

    private boolean selfManaged_;

    private boolean blocked;

    /**
     * Reentrant lock to ensure calls to {@link #getResourceFromPool(ResourceAllocator, ResourceSpec)} and
     * {@link #freeResource(ResourceHandle)} are not executed at the same time to solve issue 24843, because one is getting
     * resources from the pool and possibly resizing the pool while the other is returning resources to the pool.
     */
    private final ReentrantLock getResourceFromPoolAndFreeResourceMethodsLock = new ReentrantLock(true);

    public ConnectionPool(PoolInfo poolInfo, Hashtable env) throws PoolingException {
        this.poolInfo = poolInfo;
        setPoolConfiguration(env);
        initializePoolDataStructure();
        initializeResourceSelectionStrategy();
        initializePoolWaitQueue();

        poolTxHelper = new PoolTxHelper(this.poolInfo);
        gateway = ResourceGateway.getInstance(resourceGatewayClass);

        LOG.log(FINE, "Connection Pool: {0}", this.poolInfo);
    }

    protected void initializePoolWaitQueue() throws PoolingException {
        waitQueue = PoolWaitQueueFactory.createPoolWaitQueue(poolWaitQueueClass);
        reconfigWaitQueue = PoolWaitQueueFactory.createPoolWaitQueue(poolWaitQueueClass);
    }

    protected void initializePoolDataStructure() throws PoolingException {
        dataStructure =
            DataStructureFactory.getDataStructure(
                dataStructureType,
                dataStructureParameters, maxPoolSize, this,
                resourceSelectionStrategyClass);
    }

    protected void initializeResourceSelectionStrategy() {
        // do nothing
    }

    private void setPoolConfiguration(Hashtable env) throws PoolingException {
        ConnectorConnectionPool poolResource = getPoolConfigurationFromJndi(env);

        idletime = Integer.parseInt(poolResource.getIdleTimeoutInSeconds()) * 1000L;
        maxPoolSize = Integer.parseInt(poolResource.getMaxPoolSize());
        steadyPoolSize = Integer.parseInt(poolResource.getSteadyPoolSize());

        if (maxPoolSize < steadyPoolSize) {
            maxPoolSize = steadyPoolSize;
        }
        resizeQuantity = Integer.parseInt(poolResource.getPoolResizeQuantity());

        maxWaitTime = Integer.parseInt(poolResource.getMaxWaitTimeInMillis());
        // Make sure it's not negative.
        if (maxWaitTime < 0) {
            maxWaitTime = 0;
        }

        failAllConnections = poolResource.isFailAllConnections();

        validation = poolResource.isIsConnectionValidationRequired();

        validateAtmostEveryIdleSecs = poolResource.isValidateAtmostEveryIdleSecs();
        dataStructureType = poolResource.getPoolDataStructureType();
        dataStructureParameters = poolResource.getDataStructureParameters();
        poolWaitQueueClass = poolResource.getPoolWaitQueue();
        resourceSelectionStrategyClass = poolResource.getResourceSelectionStrategyClass();
        resourceGatewayClass = poolResource.getResourceGatewayClass();
        reconfigWaitTime = poolResource.getDynamicReconfigWaitTimeout();

        setAdvancedPoolConfiguration(poolResource);
    }

    protected ConnectorConnectionPool getPoolConfigurationFromJndi(Hashtable env) throws PoolingException {
        try {
            return (ConnectorConnectionPool)
                ConnectorRuntime.getRuntime()
                                .getResourceNamingService()
                                .lookup(
                                    poolInfo,
                                    getReservePrefixedJNDINameForPool(poolInfo),
                                    env);
        } catch (NamingException ex) {
            throw new PoolingException(ex);
        }
    }

    // This method does not need to be synchronized since all caller methods are,
    // but it does not hurt. Just to be safe.
    protected synchronized void initPool(ResourceAllocator allocator) throws PoolingException {
        if (poolInitialized) {
            return;
        }

        this.allocator = allocator;

        createResources(this.allocator, steadyPoolSize - dataStructure.getResourcesSize());

        // if the idle time out is 0, then don't schedule the resizer task
        if (idletime > 0) {
            scheduleResizerTask();
        }

        // Need to set the numConnFree of monitoring statistics to the steadyPoolSize
        // as monitoring might be ON during the initialization of pool.
        // Need not worry about the numConnUsed here as it would be initialized to
        // 0 automatically.
        if (poolLifeCycleListener != null) {
            poolLifeCycleListener.connectionsFreed(steadyPoolSize);
        }

        poolInitialized = true;
    }

    /**
     * Schedules the resizer timer task. If a task is currently scheduled, it would be canceled and a new one is scheduled.
     */
    private void scheduleResizerTask() {
        if (resizerTask != null) {
            // cancel the current task
            resizerTask.cancel();
            resizerTask = null;
        }

        resizerTask = initializeResizer();

        if (timer == null) {
            timer = ConnectorRuntime.getRuntime().getTimer();
        }

        timer.scheduleAtFixedRate(resizerTask, idletime, idletime);
        LOG.log(FINE, "Scheduled resizer task with the idle time {0} ms", idletime);
    }

    protected Resizer initializeResizer() {
        return new Resizer(poolInfo, dataStructure, this, this, preferValidateOverRecreate);
    }

    /**
     * add a resource with status busy and not enlisted
     *
     * @param alloc ResourceAllocator
     * @throws PoolingException when unable to add a resource
     */
    public void addResource(ResourceAllocator alloc) throws PoolingException {
        int numResCreated = dataStructure.addResource(alloc, 1);
        if (numResCreated > 0) {
            for (int i = 0; i < numResCreated; i++) {
                if (poolLifeCycleListener != null) {
                    poolLifeCycleListener.incrementNumConnFree(false, steadyPoolSize);
                }
            }
        }

        LOG.log(FINE, "Pool: resource added");
    }

    /**
     * marks resource as free. This method should be used instead of directly calling
     * resoureHandle.getResourceState().setBusy(false) OR getResourceState(resourceHandle).setBusy(false) as this method
     * handles stopping of connection leak tracing If connection leak tracing is enabled, takes care of stopping connection
     * leak tracing
     *
     * @param resourceHandle Resource
     */
    protected void setResourceStateToFree(ResourceHandle resourceHandle) {
        getResourceState(resourceHandle).setBusy(false);
        leakDetector.stopConnectionLeakTracing(resourceHandle, this);
    }

    /**
     * marks resource as busy. This method should be used instead of directly calling
     * resoureHandle.getResourceState().setBusy(true) OR getResourceState(resourceHandle).setBusy(true) as this method
     * handles starting of connection leak tracing If connection leak tracing is enabled, takes care of starting connection
     * leak tracing
     *
     * @param resourceHandle Resource
     */
    protected void setResourceStateToBusy(ResourceHandle resourceHandle) {
        getResourceState(resourceHandle).setBusy(true);
        leakDetector.startConnectionLeakTracing(resourceHandle, this);
    }

    /**
     * returns resource from the pool.
     *
     * @return a free pooled resource object matching the ResourceSpec
     * @throws PoolingException - if any error occurrs - or the pool has reached its max size and the
     * max-connection-wait-time-in-millis has expired.
     */
    @Override
    public ResourceHandle getResource(ResourceSpec spec, ResourceAllocator alloc, Transaction transaction) throws PoolingException, RetryableUnavailableException {
        // Note: this method should not be synchronized or the
        // startTime would be incorrect for threads waiting to enter

        /*
         * Here are all the comments for the method put together for easy reference. 1. // - Try to get a free resource. Note:
         * internalGetResource() // will create a new resource if none is free and the max has // not been reached. // - If
         * can't get one, get on the wait queue. // - Repeat this until maxWaitTime expires. // - If maxWaitTime == 0, repeat
         * indefinitely.
         *
         * 2. //the doFailAllConnectionsProcessing method would already //have been invoked by now. //We simply go ahead and
         * create a new resource here //from the allocator that we have and adjust the resources //list accordingly so as to not
         * exceed the maxPoolSize ever //(i.e if steadyPoolSize == maxPoolSize ) ///Also since we are creating the resource out
         * of the allocator //that we came into this method with, we need not worry about //matching
         */
        ResourceHandle result = null;

        long startTime = System.currentTimeMillis();
        long elapsedWaitTime;
        long remainingWaitTime = 0;

        while (true) {
            if (gateway.allowed()) {
                // See comment #1 above
                JavaEETransaction javaEETransaction = ((JavaEETransaction) transaction);
                final Set resourcesSet = javaEETransaction == null ? null : javaEETransaction.getResources(poolInfo);

                // Allow when the pool is not blocked or at-least one resource is
                // already obtained in the current transaction.
                if (!blocked || (resourcesSet != null && !resourcesSet.isEmpty())) {
                    try {
                        result = internalGetResource(spec, alloc, transaction);
                    } finally {
                        gateway.acquiredResource();
                    }
                }
            }

            if (result != null) {
                // got one, return it
                if (poolLifeCycleListener != null) {
                    poolLifeCycleListener.connectionAcquired(result.getId());
                    elapsedWaitTime = System.currentTimeMillis() - startTime;
                    poolLifeCycleListener.connectionRequestServed(elapsedWaitTime);
                    if (LOG.isLoggable(FINE)) {
                        LOG.log(FINE,
                            "Resource Pool: elapsed time (ms) to get connection for [" + spec + "] : " + elapsedWaitTime);
                    }
                }
                // got one - seems we are not doing validation or matching
                // return it
                break;
            }

            // did not get a resource.
            if (maxWaitTime > 0) {
                elapsedWaitTime = System.currentTimeMillis() - startTime;
                if (elapsedWaitTime < maxWaitTime) {
                    // time has not expired, determine remaining wait time.
                    remainingWaitTime = maxWaitTime - elapsedWaitTime;
                } else if (!blocked) {
                    // wait time has expired
                    if (poolLifeCycleListener != null) {
                        poolLifeCycleListener.connectionTimedOut();
                    }
                    throw new PoolingException("No available resources and wait time " + maxWaitTime + " ms expired.");
                }
            }

            if (!blocked) {
                // add to wait-queue
                Object waitMonitor = new Object();
                if (poolLifeCycleListener != null) {
                    poolLifeCycleListener.connectionRequestQueued();
                }
                synchronized (waitMonitor) {
                    waitQueue.addToQueue(waitMonitor);

                    try {
                        LOG.log(FINE, "Resource Pool: getting on wait queue");
                        waitMonitor.wait(remainingWaitTime);

                    } catch (InterruptedException ex) {
                        // Could be system shutdown.
                        break;
                    }

                    // Try to remove in case that the monitor has timed out. We don't expect the queue to grow to great numbers
                    // so the overhead for removing inexistant objects is low.
                    LOG.log(FINE, "removing wait monitor from queue: {0}", waitMonitor);

                    if (waitQueue.removeFromQueue(waitMonitor)) {
                        if (poolLifeCycleListener != null) {
                            poolLifeCycleListener.connectionRequestDequeued();
                        }
                    }
                }
            } else {
                // Add to reconfig-wait-queue
                Object reconfigWaitMonitor = new Object();
                synchronized (reconfigWaitMonitor) {
                    reconfigWaitQueue.addToQueue(reconfigWaitMonitor);
                    try {
                        if (reconfigWaitTime > 0) {
                            LOG.log(FINEST, "[DRC] getting into reconfig wait queue for time [{0}]", reconfigWaitTime);
                            reconfigWaitMonitor.wait(reconfigWaitTime);
                        }
                    } catch (InterruptedException ex) {
                        // Could be system shutdown.
                        break;
                    }

                    // Try to remove in case that the monitor has timed
                    // out. We don't expect the queue to grow to great numbers
                    // so the overhead for removing inexistent objects is low.
                    LOG.log(FINEST, "[DRC] removing wait monitor from reconfig-wait-queue: {0}", reconfigWaitMonitor);

                    reconfigWaitQueue.removeFromQueue(reconfigWaitMonitor);

                    LOG.log(FINEST, "[DRC] throwing Retryable-Unavailable-Exception");
                    RetryableUnavailableException rue = new RetryableUnavailableException(
                        "Pool Reconfigured, Connection Factory can retry the lookup");
                    rue.setErrorCode(POOL_RECONFIGURED_ERROR_CODE);

                    throw rue;
                }
            }
        }

        alloc.fillInResourceObjects(result);
        return result;
    }

    /**
     * Overridden in AssocWithThreadResourcePool to fetch the resource cached in the ThreadLocal In ConnectionPool this
     * simply returns null.
     *
     * @param spec ResourceSpec
     * @param alloc ResourceAllocator to create a resource
     * @param tran Transaction
     * @return ResourceHandle resource from ThreadLocal
     */
    protected ResourceHandle prefetch(ResourceSpec spec, ResourceAllocator alloc, Transaction tran) {
        return null;
    }

    protected ResourceHandle internalGetResource(ResourceSpec resourceSpec, ResourceAllocator resourceAllocator, Transaction transaction) throws PoolingException {
        if (!poolInitialized) {
            initPool(resourceAllocator);
        }

        ResourceHandle resourceHandle = getResourceFromTransaction(transaction, resourceAllocator, resourceSpec);
        if (resourceHandle != null) {
            return resourceHandle;
        }

        resourceHandle = prefetch(resourceSpec, resourceAllocator, transaction);
        if (resourceHandle != null) {
            return resourceHandle;
        }

        // We didnt get a connection that is already enlisted in the current transaction (if any).
        resourceHandle = getUnenlistedResource(resourceSpec, resourceAllocator, transaction);
        if (resourceHandle != null) {
            if (maxConnectionUsage_ > 0) {
                resourceHandle.incrementUsageCount();
            }
            if (poolLifeCycleListener != null) {
                poolLifeCycleListener.connectionUsed(resourceHandle.getId());
                // Decrement numConnFree
                poolLifeCycleListener.decrementNumConnFree();
            }
        }

        return resourceHandle;
    }

    /**
     * Try to get a resource from current transaction if it is shareable<br>
     *
     * @param transaction Current Transaction
     * @param resourceAllocator ResourceAllocator
     * @param resourceSpec ResourceSpec
     * @return result ResourceHandle
     */
    private ResourceHandle getResourceFromTransaction(Transaction transaction, ResourceAllocator resourceAllocator, ResourceSpec resourceSpec) {
        ResourceHandle resourceFromTransaction = null;
        try {
            // comment-1: sharing is possible only if caller is marked
            // shareable, so abort right here if that's not the case
            if (transaction != null && resourceAllocator.shareableWithinComponent()) {
                // TODO should be handled by PoolTxHelper

                JavaEETransaction javaEETransaction = (JavaEETransaction) transaction;

                // case 1. look for free and enlisted in same tx
                Set set = javaEETransaction.getResources(poolInfo);
                if (set != null) {
                    Iterator iter = set.iterator();
                    while (iter.hasNext()) {
                        ResourceHandle resourceHandle = (ResourceHandle) iter.next();
                        if (resourceHandle.hasConnectionErrorOccurred()) {
                            iter.remove();
                            continue;
                        }

                        ResourceState state = resourceHandle.getResourceState();

                        /*
                         * One can share a resource only for the following conditions:
                         *
                         * 1. The caller resource is shareable (look at the outermost if marked comment-1
                         *
                         * 2. The resource enlisted inside the transaction is shareable
                         *
                         * 3. We are dealing with XA resources OR we are dealing with a non-XA resource that's not in use
                         *    Note that sharing a non-xa resource that's in use involves associating physical connections.
                         *
                         * 4. The credentials of the resources match
                         */
                        if (resourceHandle.getResourceAllocator().shareableWithinComponent()) {
                            if (resourceSpec.isXA() || poolTxHelper.isNonXAResourceAndFree(javaEETransaction, resourceHandle)) {
                                if (matchConnections) {
                                    if (!resourceAllocator.matchConnection(resourceHandle)) {
                                        if (poolLifeCycleListener != null) {
                                            poolLifeCycleListener.connectionNotMatched();
                                        }
                                        continue;
                                    }

                                    if (resourceHandle.hasConnectionErrorOccurred()) {
                                        if (failAllConnections) {
                                            // if failAllConnections has happened, we flushed the
                                            // pool, so we don't have to do iter.remove else we
                                            // will get a ConncurrentModificationException
                                            resourceFromTransaction = null;
                                            break;
                                        }
                                        iter.remove();
                                        continue;
                                    }
                                    if (poolLifeCycleListener != null) {
                                        poolLifeCycleListener.connectionMatched();
                                    }
                                }

                                if (state.isFree()) {
                                    setResourceStateToBusy(resourceHandle);
                                }

                                resourceFromTransaction = resourceHandle;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            if (LOG.isLoggable(FINE)) {
                LOG.log(FINE, "Pool: getResource : transaction is not JavaEETransaction but a "
                    + transaction.getClass().getName(), e);
            }
        }

        return resourceFromTransaction;
    }

    /**
     * To provide an unenlisted, valid, matched resource from pool.
     *
     * @param resourceSpec ResourceSpec
     * @param resourceAllocator ResourceAllocator
     * @param transaction Transaction
     * @return ResourceHandle resource from pool
     * @throws PoolingException Exception while getting resource from pool
     */
    protected ResourceHandle getUnenlistedResource(ResourceSpec resourceSpec, ResourceAllocator resourceAllocator, Transaction transaction) throws PoolingException {
        return getResourceFromPool(resourceAllocator, resourceSpec);
    }

    /**
     * Check whether the connection is valid
     *
     * @param resourceHandle Resource to be validated
     * @param resourceAllocator Allocator to validate the resource
     * @return boolean representing validation result
     */
    protected boolean isConnectionValid(ResourceHandle resourceHandle, ResourceAllocator resourceAllocator) {
        boolean connectionValid = true;

        if (validation || validateAtmostEveryIdleSecs) {
            long validationPeriod;
            // validation period is idle timeout if validateAtmostEveryIdleSecs is set to true
            // else it is validateAtmostPeriodInMilliSeconds_
            if (validation) {
                validationPeriod = validateAtmostPeriodInMilliSeconds_;
            } else {
                validationPeriod = idletime;
            }

            boolean validationRequired = true;
            long currentTime = resourceHandle.getLastValidated();
            if (validationPeriod > 0) {
                currentTime = System.currentTimeMillis();
                long timeSinceValidation = currentTime - resourceHandle.getLastValidated();
                if (timeSinceValidation < validationPeriod) {
                    validationRequired = false;
                }
            }

            if (validationRequired) {
                if (!resourceAllocator.isConnectionValid(resourceHandle)) {
                    connectionValid = false;
                    incrementNumConnFailedValidation();
                } else {
                    resourceHandle.setLastValidated(currentTime);
                }
            }
        }

        return connectionValid;
    }

    /**
     * check whether the connection retrieved from the pool matches with the request.
     *
     * @param resource Resource to be matched
     * @param alloc ResourceAllocator used to match the connection
     * @return boolean representing the match status of the connection
     */
    protected boolean matchConnection(ResourceHandle resource, ResourceAllocator alloc) {
        boolean matched = true;
        if (matchConnections) {
            matched = alloc.matchConnection(resource);
            if (poolLifeCycleListener != null) {
                if (matched) {
                    poolLifeCycleListener.connectionMatched();
                } else {
                    poolLifeCycleListener.connectionNotMatched();
                }
            }
        }

        return matched;
    }

    /**
     * return resource in free list. If none is found, try to scale up the pool/purge pool and <br>
     * return a new resource. returns null if the pool new resources cannot be created. <br>
     *
     * @param resourceAllocator ResourceAllocator
     * @return ResourceHandle resource from pool
     * @throws PoolingException if unable to create a new resource
     */
    protected ResourceHandle getResourceFromPool(ResourceAllocator resourceAllocator, ResourceSpec resourceSpec) throws PoolingException {

        // The order of serving a resource request
        // 1. free and enlisted in the same transaction
        // 2. free and unenlisted
        // Do NOT give out a connection that is
        // free and enlisted in a different transaction
        ResourceHandle resourceFromPool = null;

        ResourceHandle resourceHandle;
        List<ResourceHandle> freeResources = new ArrayList<>();

        try {
            getResourceFromPoolAndFreeResourceMethodsLock.lock();
            try {
                while ((resourceHandle = dataStructure.getResource()) != null) {
                    if (resourceHandle.hasConnectionErrorOccurred()) {
                        dataStructure.removeResource(resourceHandle);
                        continue;
                    }

                    if (matchConnection(resourceHandle, resourceAllocator)) {

                        boolean isValid = isConnectionValid(resourceHandle, resourceAllocator);
                        if (resourceHandle.hasConnectionErrorOccurred() || !isValid) {
                            if (failAllConnections) {
                                resourceFromPool = createSingleResourceAndAdjustPool(resourceAllocator, resourceSpec);
                                // No need to match since the resource is created with the allocator of caller.
                                break;
                            }
                            dataStructure.removeResource(resourceHandle);
                            // Resource is invalid, continue iteration.
                            continue;
                        }
                        if (resourceHandle.isShareable() == resourceAllocator.shareableWithinComponent()) {
                            // Got a matched, valid resource
                            resourceFromPool = resourceHandle;
                            break;
                        }
                        freeResources.add(resourceHandle);
                    } else {
                        freeResources.add(resourceHandle);
                    }
                }
            } finally {
                // Return all unmatched, free resources
                for (ResourceHandle freeResource : freeResources) {
                    dataStructure.returnResource(freeResource);
                }
                freeResources.clear();
            }

            if (resourceFromPool != null) {
                // Set correct state
                setResourceStateToBusy(resourceFromPool);
            } else {
                resourceFromPool = resizePoolAndGetNewResource(resourceAllocator);
            }
        } finally {
            getResourceFromPoolAndFreeResourceMethodsLock.unlock();
        }

        return resourceFromPool;
    }

    /**
     * Scale-up the pool to serve the new request. <br>
     * If pool is at max-pool-size and free resources are found, purge unmatched<br>
     * resources, create new connections and serve the request.<br>
     *
     * @param resourceAllocator ResourceAllocator used to create new resources
     * @return ResourceHandle newly created resource
     * @throws PoolingException when not able to create resources
     */
    private ResourceHandle resizePoolAndGetNewResource(ResourceAllocator resourceAllocator) throws PoolingException {

        // Must be called from the thread holding the lock to this pool.
        ResourceHandle newResource = null;

        int numOfConnsToCreate = 0;
        int dataStructureSize = dataStructure.getResourcesSize();

        if (dataStructureSize < steadyPoolSize) {
            // May be all invalid resources are destroyed as
            // a result no free resource found and no. of resources is less than steady-pool-size
            numOfConnsToCreate = steadyPoolSize - dataStructureSize;
        } else if (dataStructureSize + resizeQuantity <= maxPoolSize) {
            // Create and add resources of quantity "resizeQuantity"
            numOfConnsToCreate = resizeQuantity;
        } else if (dataStructureSize < maxPoolSize) {
            // This else if "test condition" is not needed. Just to be safe.
            // still few more connections (less than "resizeQuantity" and to reach the count of maxPoolSize)
            // can be added
            numOfConnsToCreate = maxPoolSize - dataStructureSize;
        }

        if (numOfConnsToCreate > 0) {
            createResources(resourceAllocator, numOfConnsToCreate);
            newResource = getMatchedResourceFromPool(resourceAllocator);
        } else if (dataStructure.getFreeListSize() > 0) {
            // pool cannot create more connections as it is at max-pool-size.
            // If there are free resources at max-pool-size, then none of the free resources
            // has matched this allocator's request (credential). Hence purge free resources
            // of size <=resizeQuantity
            if (purgeResources(resizeQuantity) > 0) {
                newResource = resizePoolAndGetNewResource(resourceAllocator);
            }
        }

        return newResource;
    }

    // TODO can't this be replaced by getResourceFromPool ?
    private ResourceHandle getMatchedResourceFromPool(ResourceAllocator alloc) {

        ResourceHandle matchedResourceFromPool = null;
        List<ResourceHandle> activeResources = new ArrayList<>();

        try {
            ResourceHandle handle;
            while ((handle = dataStructure.getResource()) != null) {
                if (matchConnection(handle, alloc)) {
                    matchedResourceFromPool = handle;
                    setResourceStateToBusy(matchedResourceFromPool);
                    break;
                }
                activeResources.add(handle);
            }
        } finally {
            // Return unmatched resources
            for (ResourceHandle activeResource : activeResources) {
                dataStructure.returnResource(activeResource);
            }
            activeResources.clear();
        }

        return matchedResourceFromPool;
    }

    /**
     * Try to purge resources by size <= quantity <br>
     *
     * @param quantity maximum no. of resources to remove. <br>
     * @return resourceCount No. of resources actually removed. <br>
     */
    private int purgeResources(int quantity) {
        // Must be called from the thread holding the lock to this pool.
        int totalResourcesRemoved = 0;
        int freeResourcesCount = dataStructure.getFreeListSize();
        int resourcesCount = (freeResourcesCount >= quantity) ? quantity : freeResourcesCount;
        LOG.log(FINE, "Purging resources of size: {0}", resourcesCount);

        for (int i = resourcesCount - 1; i >= 0; i--) {
            ResourceHandle resource = dataStructure.getResource();
            if (resource != null) {
                dataStructure.removeResource(resource);
                totalResourcesRemoved += 1;
            }
        }

        return totalResourcesRemoved;
    }

    /**
     * This method will be called from the getUnenlistedResource method if we detect a failAllConnection flag. Here we
     * simply create a new resource and replace a free resource in the pool by this resource and then give it out. This
     * replacement is required since the steadypoolsize might equal maxpoolsize and in that case if we were not to remove a
     * resource from the pool, our resource would be above maxPoolSize
     *
     * @param resourceAllocator ResourceAllocator to create resource
     * @param resourceSpec ResourceSpec
     * @return newly created resource
     * @throws PoolingException when unable to create a resource
     */
    protected ResourceHandle createSingleResourceAndAdjustPool(ResourceAllocator resourceAllocator, ResourceSpec resourceSpec) throws PoolingException {
        ResourceHandle handle = dataStructure.getResource();
        if (handle != null) {
            dataStructure.removeResource(handle);
        }

        return getNewResource(resourceAllocator);
    }

    /**
     * Method to be used to create resource, instead of calling ResourceAllocator.createConfigBean(). This method handles
     * the connection creation retrial in case of failure
     *
     * @param resourceAllocator ResourceAllocator
     * @return ResourceHandle newly created resource
     * @throws PoolingException when unable create a resource
     */
    protected ResourceHandle createSingleResource(ResourceAllocator resourceAllocator) throws PoolingException {
        int count = 0;
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                count++;
                ResourceHandle resourceHandle = resourceAllocator.createResource();
                long now = System.currentTimeMillis();
                LOG.log(FINE,
                    () -> "Time taken to create a single resource: " + resourceHandle.getResourceSpec().getResourceId()
                        + " and adding to the pool: " + (now - startTime) + " ms.");
                if (validation || validateAtmostEveryIdleSecs) {
                    resourceHandle.setLastValidated(now);
                }
                return resourceHandle;
            } catch (Exception ex) {
                if (!connectionCreationRetry_ || count > connectionCreationRetryAttempts_) {
                    throw new PoolingException("Connection creation failed for " + count + " times.", ex);
                }
                LOG.log(WARNING, "Connection creation failed for " + count + " times. It will be retried in "
                    + conCreationRetryInterval_ + " ms.", ex);
                try {
                    Thread.sleep(conCreationRetryInterval_);
                } catch (InterruptedException ie) {
                    // ignore this exception
                }
            }
        }
    }

    /**
     * Create specified number of resources.
     *
     * @param alloc ResourceAllocator
     * @param size number of resources to create.
     * @throws PoolingException When unable to create a resource
     */
    private void createResources(ResourceAllocator alloc, int size) throws PoolingException {
        for (int i = 0; i < size; i++) {
            createResourceAndAddToPool(alloc);
        }
    }

    @Override
    public void setPoolLifeCycleListener(PoolLifeCycleListener listener) {
        this.poolLifeCycleListener = listener;
    }

    @Override
    public void removePoolLifeCycleListener() {
        poolLifeCycleListener = null;
    }

    @Override
    public void deleteResource(ResourceHandle resourceHandle) {
        try {
            resourceHandle.getResourceAllocator().destroyResource(resourceHandle);
        } catch (Exception ex) {
            LOG.log(WARNING, "Unexpected exception while destroying resource from pool " + poolInfo.getName(), ex);
        } finally {
            // if connection leak tracing is running on connection being
            // destroyed due to error, then stop it
            if (resourceHandle.getResourceState().isBusy()) {
                leakDetector.stopConnectionLeakTracing(resourceHandle, this);
            }
            if (poolLifeCycleListener != null) {
                poolLifeCycleListener.connectionDestroyed(resourceHandle.getId());

                if (resourceHandle.getResourceState().isBusy()) {
                    // Destroying a Connection due to error
                    poolLifeCycleListener.decrementConnectionUsed(resourceHandle.getId());
                    if (!resourceHandle.isMarkedForReclaim()) {
                        // If a connection is not reclaimed (in case of a reconfig)
                        // increment numConnFree
                        poolLifeCycleListener.incrementNumConnFree(true, steadyPoolSize);
                    }
                } else {
                    // Destroying a free Connection
                    poolLifeCycleListener.decrementNumConnFree();
                }
            }
        }
    }

    /**
     * this method is called to indicate that the resource is not used by a bean/application anymore
     */
    @Override
    public void resourceClosed(ResourceHandle handle) throws IllegalStateException {
        LOG.log(FINE, "Resource was closed, processing handle: {0}", handle);

        ResourceState state = getResourceState(handle);
        if (state == null) {
            throw new IllegalStateException("State is null");
        }

        if (!state.isBusy()) {
            // Do not throw an exception, the current transaction should not fail if the state is already 'free'.
            LOG.log(WARNING, "resourceClosed - Expecting 'state.isBusy(): false', but was true for handle: {0}", handle);
        }

        // mark as not busy
        setResourceStateToFree(handle);
        state.touchTimestamp();

        if (state.isUnenlisted() || (poolTxHelper.isNonXAResource(handle) && poolTxHelper.isLocalTransactionInProgress()
                && poolTxHelper.isLocalResourceEligibleForReuse(handle))) {
            freeUnenlistedResource(handle);
        }

        if (poolLifeCycleListener != null && !handle.getDestroyByLeakTimeOut()) {
            poolLifeCycleListener.connectionReleased(handle.getId());
        }

        // Note handle might already be altered by another thread before it is logged!
        LOG.log(FINE, "Resource was freed after its closure: {0}", handle);
    }

    /**
     * If the resource is used for <i>maxConnectionUsage</i> times, destroy and create one
     *
     * @param handle Resource to be checked
     */
    protected void performMaxConnectionUsageOperation(ResourceHandle handle) {
        dataStructure.removeResource(handle);
        LOG.log(INFO, "Destroying connection {0} since it has reached the maximum usage of: {1}",
            new Object[] {handle.getId(), handle.getUsageCount()});

        if (poolLifeCycleListener != null) {
            poolLifeCycleListener.decrementConnectionUsed(handle.getId());
        }

        // compensate with a new resource only when the pool-size is less than steady-pool-size
        if (dataStructure.getResourcesSize() < steadyPoolSize) {
            try {
                createResourceAndAddToPool(handle.getResourceAllocator());
            } catch (Exception e) {
                LOG.log(WARNING, "Unable to create a new resource.", e);
            }
        }
    }

    protected void freeUnenlistedResource(ResourceHandle h) {
        freeResource(h);
    }

    protected void freeResource(ResourceHandle resourceHandle) {
        try {
            getResourceFromPoolAndFreeResourceMethodsLock.lock();
            if (cleanupResource(resourceHandle)) {
                // Only when resource handle usage count is more than maxConnUsage
                if (maxConnectionUsage_ > 0 && resourceHandle.getUsageCount() >= maxConnectionUsage_) {
                    performMaxConnectionUsageOperation(resourceHandle);
                } else {
                    // Put it back to the free collection.
                    dataStructure.returnResource(resourceHandle);
                    // update the monitoring data
                    if (poolLifeCycleListener != null && !resourceHandle.getDestroyByLeakTimeOut()) {
                        poolLifeCycleListener.decrementConnectionUsed(resourceHandle.getId());
                        poolLifeCycleListener.incrementNumConnFree(false, steadyPoolSize);
                    }
                }
                // For both the cases of free.add and maxConUsageOperation, a free resource is added.
                // Hence notify waiting threads.
                notifyWaitingThreads();
            }
        } finally {
            getResourceFromPoolAndFreeResourceMethodsLock.unlock();
        }

    }

    protected boolean cleanupResource(ResourceHandle handle) {
        boolean cleanupSuccessful = true;
        // cleanup resource
        try {
            ResourceAllocator alloc = handle.getResourceAllocator();
            alloc.cleanup(handle);
        } catch (PoolingException ex) {
            LOG.log(WARNING, "Cleanup of a resource from pool [" + poolInfo.getName() + "] failed.", ex);
            cleanupSuccessful = false;
            resourceErrorOccurred(handle);
        }
        return cleanupSuccessful;
    }

    @Override
    public void resourceErrorOccurred(ResourceHandle resourceHandle) throws IllegalStateException {
        LOG.log(FINE, "Resource error occured: {0}", resourceHandle);
        if (failAllConnections) {
            doFailAllConnectionsProcessing();
            return;
        }

        ResourceState state = getResourceState(resourceHandle);
        // Normally a connection error is expected
        // to occur only when the connection is in use by the application.
        // When there is a connection validation involved, the connection
        // can be checked for validity "before" it is passed to the
        // application i.e. when the resource is still free. Since,
        // the connection error can occur when the resource
        // is free, the state could still be 'isBusy: false', therefore
        // no exception is thrown based on the isBusy state, and only
        // if the state object is missing.
        if (state == null) {
            throw new IllegalStateException();
        }

        // mark as not busy
        setResourceStateToFree(resourceHandle);
        state.touchTimestamp();

        // changed order of commands

        // Commenting resources.remove() out since we will call an iter.remove()
        // in the getUnenlistedResource method in the if check after
        // matchManagedConnections or in the internalGetResource method
        // If we were to call remove directly here there is always the danger
        // of a ConcurrentModificationExceptionbeing thrown when we return
        //
        // In case of this method being called asynchronously, since
        // the resource has been marked as "errorOccured", it will get
        // removed in the next iteration of getUnenlistedResource
        // or internalGetResource
        dataStructure.removeResource(resourceHandle);

        // Removing a resource, means a free resource is available for the pool.
        // Hence notify waiting threads.
        notifyWaitingThreads();
    }

    private void doFailAllConnectionsProcessing() {
        LOG.log(FINE, "doFailAllConnectionsProcessing()");
        cancelResizerTask();
        if (poolLifeCycleListener != null) {
            poolLifeCycleListener.connectionValidationFailed(dataStructure.getResourcesSize());
        }

        emptyPool();
        try {
            createResources(allocator, steadyPoolSize);
            LOG.log(FINE, "Successfully created new resources.");
        } catch (PoolingException pe) {
            // Ignore and hope the resizer does its stuff
            LOG.log(FINE, "Could not create " + steadyPoolSize + " resources.", pe);
        }
        scheduleResizerTask();
    }

    /**
     * this method is called when a resource is enlisted in
     *
     * @param tran Transaction
     * @param resource ResourceHandle
     */
    @Override
    public void resourceEnlisted(Transaction tran, ResourceHandle resource) throws IllegalStateException {
        poolTxHelper.resourceEnlisted(tran, resource);
    }

    /**
     * this method is called when transaction tran is completed
     *
     * @param tran Transaction
     * @param status status of transaction
     */
    @Override
    public void transactionCompleted(Transaction tran, int status) throws IllegalStateException {
        List<ResourceHandle> delistedResources = poolTxHelper.transactionCompleted(tran, status, poolInfo);
        for (ResourceHandle resource : delistedResources) {
            // Application might not have closed the connection.
            if (isResourceUnused(resource)) {
                freeResource(resource);
            }
        }
    }

    protected boolean isResourceUnused(ResourceHandle h) {
        return h.getResourceState().isFree();
    }

    @Override
    public ResourceHandle createResource(ResourceAllocator alloc) throws PoolingException {
        // NOTE : Pool should not call this method directly, it should be called only by pool-datastructure
        ResourceHandle result = createSingleResource(alloc);

        ResourceState state = new ResourceState();
        state.setBusy(false);
        state.setEnlisted(false);
        result.setResourceState(state);

        if (poolLifeCycleListener != null) {
            poolLifeCycleListener.connectionCreated();
        }
        return result;
    }

    @Override
    public void createResourceAndAddToPool() throws PoolingException {
        createResourceAndAddToPool(allocator);
    }

    @Override
    public Set<ManagedConnection> getInvalidConnections(Set connections) throws ResourceException {
        return allocator.getInvalidConnections(connections);
    }

    @Override
    public void invalidConnectionDetected(ResourceHandle h) {
        incrementNumConnFailedValidation();
    }

    @Override
    public void resizePool(boolean forced) {
        resizerTask.resizePool(forced);
    }

    protected void notifyWaitingThreads() {
        // notify the first thread in the waitqueue
        Object waitMonitor = null;
        synchronized (waitQueue) {
            if (waitQueue.getQueueLength() > 0) {
                waitMonitor = waitQueue.remove();
                if (poolLifeCycleListener != null) {
                    poolLifeCycleListener.connectionRequestDequeued();
                }
            }
        }
        if (waitMonitor == null) {
            LOG.log(FINE, "Wait monitor is null");
        } else {
            synchronized (waitMonitor) {
                LOG.log(FINE, "Notifying wait monitor: {0}", waitMonitor);
                waitMonitor.notifyAll();
            }
        }
    }

    private void incrementNumConnFailedValidation() {
        if (poolLifeCycleListener != null) {
            poolLifeCycleListener.connectionValidationFailed(1);
        }
    }

    private ResourceHandle getNewResource(ResourceAllocator alloc) throws PoolingException {
        addResource(alloc);
        return dataStructure.getResource();
    }

    private ResourceState getResourceState(ResourceHandle h) {
        return h.getResourceState();
    }

    @Override
    public void emptyPool() {
        LOG.log(FINE, "Emptying pool {0}", poolInfo.getName());
        dataStructure.removeAll();
    }

    @Override
    public void emptyFreeConnectionsInPool() {
        LOG.log(FINE, "Emptying free connections in the pool {0}", poolInfo.getName());
        ResourceHandle h;
        while ((h = dataStructure.getResource()) != null) {
            dataStructure.removeResource(h);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Pool [");
        sb.append(poolInfo);
        sb.append("] PoolSize=");
        sb.append(dataStructure.getResourcesSize());
        sb.append("  FreeResources=");
        sb.append(dataStructure.getFreeListSize());
        sb.append("  QueueSize=");
        sb.append(waitQueue.getQueueLength());
        sb.append(" matching=");
        sb.append((matchConnections ? "on" : "off"));
        sb.append(" validation=");
        sb.append((validation ? "on" : "off"));
        return sb.toString();
    }

    @Override
    public void blockRequests(long waitTimeout) {
        blocked = true;
        this.reconfigWaitTime = waitTimeout;
    }

    @Override
    public PoolWaitQueue getPoolWaitQueue() {
        return waitQueue;
    }

    @Override
    public PoolWaitQueue getReconfigWaitQueue() {
        return reconfigWaitQueue;
    }

    @Override
    public long getReconfigWaitTime() {
        return reconfigWaitTime;
    }

    /**
     * Reinitialize connections established in the connection pool and bring the pool to steady pool size.
     *
     * @throws com.sun.appserv.connectors.internal.api.PoolingException
     */
    @Override
    public synchronized boolean flushConnectionPool() throws PoolingException {
        LOG.log(FINE, "Flushing Connection Pool {0}", poolInfo);

        if (!poolInitialized) {
            throw new PoolingException(
                "Flush Connection Pool did not happen as pool " + poolInfo + " is not initialized");
        }

        cancelResizerTask();
        dataStructure.removeAll();
        scheduleResizerTask();
        increaseSteadyPoolSize(steadyPoolSize);
        LOG.log(FINE, "Flush Connection Pool done");
        return true;
    }

    /**
     * Reconfigure the Pool's properties. The reconfigConnectorConnectionPool method in the ConnectorRuntime will use this
     * method (through PoolManager) if it needs to just change pool properties and not recreate the pool
     *
     * @param poolResource - the ConnectorConnectionPool JavaBean that holds the new pool properties
     * @throws PoolingException if the pool resizing fails
     */
    @Override
    public synchronized void reconfigurePool(ConnectorConnectionPool poolResource) throws PoolingException {
        int _idleTime = Integer.parseInt(poolResource.getIdleTimeoutInSeconds()) * 1000;
        if (poolInitialized) {
            if (_idleTime != idletime && _idleTime != 0) {
                idletime = _idleTime;
                scheduleResizerTask();
            }
            if (_idleTime == 0) {
                // resizerTask.cancel();
                cancelResizerTask();
            }
        }
        idletime = _idleTime;

        resizeQuantity = Integer.parseInt(poolResource.getPoolResizeQuantity());

        maxWaitTime = Integer.parseInt(poolResource.getMaxWaitTimeInMillis());
        // Make sure it's not negative.
        if (maxWaitTime < 0) {
            maxWaitTime = 0;
        }

        validation = poolResource.isIsConnectionValidationRequired();
        failAllConnections = poolResource.isFailAllConnections();
        setAdvancedPoolConfiguration(poolResource);

        // Self managed quantities. These are ignored if self management
        // is on
        if (!isSelfManaged()) {
            int _maxPoolSize = Integer.parseInt(poolResource.getMaxPoolSize());
            int oldMaxPoolSize = maxPoolSize;

            if (_maxPoolSize < steadyPoolSize) {
                // should not happen, admin must throw exception when this condition happens.
                // as a precaution set max pool size to steady pool size
                maxPoolSize = steadyPoolSize;
            } else {
                maxPoolSize = _maxPoolSize;
            }

            if (oldMaxPoolSize != maxPoolSize) {
                dataStructure.setMaxSize(maxPoolSize);
            }
            int _steadyPoolSize = Integer.parseInt(poolResource.getSteadyPoolSize());
            int oldSteadyPoolSize = steadyPoolSize;

            if (_steadyPoolSize > maxPoolSize) {
                // should not happen, admin must throw exception when this condition happens.
                // as a precaution set steady pool size to max pool size
                steadyPoolSize = maxPoolSize;
            } else {
                steadyPoolSize = _steadyPoolSize;
            }

            if (poolInitialized) {
                // In this case we need to kill extra connections in the pool
                // For the case where the value is increased, we need not
                // do anything
                // num resources to kill is decided by the resources in the pool.
                // if we have less than current maxPoolSize resources, we need to
                // kill less.
                int toKill = dataStructure.getResourcesSize() - maxPoolSize;

                if (toKill > 0) {
                    killExtraResources(toKill);
                }
            }
            reconfigureSteadyPoolSize(oldSteadyPoolSize, _steadyPoolSize);
        }
    }

    protected void reconfigureSteadyPoolSize(int oldSteadyPoolSize, int newSteadyPoolSize) throws PoolingException {
        if (oldSteadyPoolSize != steadyPoolSize) {
            if (poolInitialized) {
                if (oldSteadyPoolSize < steadyPoolSize) {
                    increaseSteadyPoolSize(newSteadyPoolSize);
                    if (poolLifeCycleListener != null) {
                        poolLifeCycleListener.connectionsFreed(steadyPoolSize);
                    }
                }
            }
        }
    }

    /**
     * sets advanced pool properties<br>
     * used during pool configuration (initialization) and re-configuration<br>
     *
     * @param poolResource Connector Connection Pool
     */
    private void setAdvancedPoolConfiguration(ConnectorConnectionPool poolResource) {
        matchConnections = poolResource.matchConnections();
        preferValidateOverRecreate = poolResource.isPreferValidateOverRecreate();
        maxConnectionUsage_ = Integer.parseInt(poolResource.getMaxConnectionUsage());
        connectionCreationRetryAttempts_ = Integer.parseInt(poolResource.getConCreationRetryAttempts());
        // Converting seconds to milliseconds as TimerTask will take input in milliseconds
        conCreationRetryInterval_ = Integer.parseInt(poolResource.getConCreationRetryInterval()) * 1000L;
        connectionCreationRetry_ = connectionCreationRetryAttempts_ > 0;

        validateAtmostPeriodInMilliSeconds_ = Integer.parseInt(poolResource.getValidateAtmostOncePeriod()) * 1000L;
        boolean connectionLeakReclaim_ = poolResource.isConnectionReclaim();
        long connectionLeakTimeoutInMilliSeconds_ = Integer.parseInt(poolResource.getConnectionLeakTracingTimeout()) * 1000L;

        boolean connectionLeakTracing_ = connectionLeakTimeoutInMilliSeconds_ > 0;
        if (leakDetector == null) {
            leakDetector = new ConnectionLeakDetector(poolInfo, connectionLeakTracing_, connectionLeakTimeoutInMilliSeconds_,
                    connectionLeakReclaim_);
        } else {
            leakDetector.reset(connectionLeakTracing_, connectionLeakTimeoutInMilliSeconds_, connectionLeakReclaim_);
        }
    }

    /**
     * Kill the extra resources.<br>
     * The maxPoolSize being reduced causes this method to be called
     */
    private void killExtraResources(int numToKill) {
        cancelResizerTask();

        ResourceHandle h;
        for (int i = 0; i < numToKill && ((h = dataStructure.getResource()) != null); i++) {
            dataStructure.removeResource(h);
        }
        scheduleResizerTask();
    }

    /*
     * Increase the number of steady resources in the pool if we detect that the steadyPoolSize has been increased
     */
    private void increaseSteadyPoolSize(int newSteadyPoolSize) throws PoolingException {
        cancelResizerTask();
        for (int i = dataStructure.getResourcesSize(); i < newSteadyPoolSize; i++) {
            createResourceAndAddToPool(allocator);
        }
        scheduleResizerTask();
    }

    /**
     * @param alloc ResourceAllocator
     * @throws PoolingException when unable to create a resource
     */
    private void createResourceAndAddToPool(ResourceAllocator alloc) throws PoolingException {
        addResource(alloc);
    }

    /**
     * Switch on matching of connections in the pool.
     */
    @Override
    public void switchOnMatching() {
        matchConnections = true;
    }

    /**
     * query the name of this pool. Required by monitoring
     *
     * @return the name of this pool
     */
    @Override
    public final PoolInfo getPoolInfo() {
        return poolInfo;
    }

    @Override
    public synchronized void cancelResizerTask() {
        LOG.log(FINE, "Cancelling resizer task.");
        if (resizerTask != null) {
            resizerTask.cancel();
        }
        resizerTask = null;
        if (timer != null) {
            timer.purge();
        }
    }

    // Self management methods
    @Override
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    @Override
    public int getResizeQuantity() {
        return resizeQuantity;
    }

    @Override
    public long getIdleTimeout() {
        return idletime;
    }

    @Override
    public int getWaitQueueLength() {
        return waitQueue.getQueueLength();
    }

    @Override
    public int getSteadyPoolSize() {
        return steadyPoolSize;
    }

    @Override
    public void setMaxPoolSize(int size) {
        if (size < dataStructure.getResourcesSize()) {
            synchronized (this) {
                int toKill = dataStructure.getResourcesSize() - size;
                if (toKill > 0) {
                    try {
                        killExtraResources(toKill);
                    } catch (Exception re) {
                        // ignore for now
                        LOG.log(FINE, "setMaxPoolSize:: killExtraResources throws exception!", re);
                    }
                }
            }
        }
        maxPoolSize = size;
    }

    @Override
    public void setSteadyPoolSize(int size) {
        steadyPoolSize = size;
    }

    @Override
    public void setSelfManaged(boolean selfManaged) {
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "Setting selfManaged to: " + selfManaged + " in pool: " + poolInfo.getName());
        }
        selfManaged_ = selfManaged;
    }

    protected boolean isSelfManaged() {
        return selfManaged_;
    }

    @Override
    public void potentialConnectionLeakFound() {
        if (poolLifeCycleListener != null) {
            poolLifeCycleListener.foundPotentialConnectionLeak();
        }
    }

    @Override
    public void printConnectionLeakTrace(StringBuffer stackTrace) {
        if (poolLifeCycleListener != null) {
            stackTrace.append('\n');
            stackTrace.append("Monitoring Statistics: ");
            stackTrace.append('\n');
            poolLifeCycleListener.toString(stackTrace);
        }
    }

    @Override
    public void reclaimConnection(ResourceHandle handle) {
        // all reclaimed connections must be killed instead of returning them to the pool.
        // Entity beans when used in bean managed transaction will face an issue since connections
        // are destroyed during reclaim.
        // Stateful session beans will work fine.
        LOG.log(INFO,
            "Reclaiming the leaked connection of pool [{0}] and destroying it so as to avoid both"
                + " the application that leaked the connection and any other request that can potentially acquire"
                + " the same connection from the pool end up using the connection at the same time",
            poolInfo.getName());
        dataStructure.removeResource(handle);
        handle.setDestroyByLeakTimeOut(true);
        notifyWaitingThreads();
    }

    /**
     * Get Connection Pool status by computing the free/used values of the connections in the pool. Computations are based
     * on whether the pool is initialized or not when this method is invoked.
     *
     * @return PoolStatus object
     */
    @Override
    public PoolStatus getPoolStatus() {
        PoolStatus poolStatus = new PoolStatus(this.poolInfo);
        int numFree = this.poolInitialized ? dataStructure.getFreeListSize() : 0;
        int numUsed = this.poolInitialized ? dataStructure.getResourcesSize() - dataStructure.getFreeListSize() : 0;
        poolStatus.setNumConnFree(numFree);
        poolStatus.setNumConnUsed(numUsed);
        return poolStatus;
    }
}
