/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/containers/util/pool/AbstractPool.java,v $</I>
 * @author     $Author: cf126330 $
 * @version    $Revision: 1.5 $ $Date: 2007/03/30 19:10:26 $
 */

package com.sun.ejb.containers.util.pool;

import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.monitoring.probes.EjbPoolProbeProvider;
import com.sun.ejb.monitoring.stats.EjbMonitoringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.sun.enterprise.util.Utility.setContextClassLoader;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * <p>
 * Abstract pool provides the basic implementation of an object pool. The implementation uses a linked list to maintain
 * a list of (available) objects. If the pool is empty it simply creates one using the ObjectFactory instance.
 * Subclasses can change this behaviour by overriding getObject(...) and returnObject(....) methods. This class provides
 * basic support for synchronization, event notification, pool shutdown and pool object recycling. It also does some
 * very basic bookkeeping like the number of objects created, number of threads waiting for object.
 * <p>
 * Subclasses can make use of these book-keeping data to provide complex pooling mechanism like LRU / MRU / Random.
 * Also, note that AbstractPool does not have a notion of pool limit. It is upto to the derived classes to implement
 * these features.
 */
public abstract class AbstractPool implements Pool {

    protected static final Logger _logger = EjbContainerUtilImpl.getLogger();

    protected List<Object> pooledObjects;
    protected ObjectFactory pooledObjectFactory;
    protected int waitCount;
    protected int createdCount;

    protected int steadyPoolSize;
    protected int resizeQuantity = 1;
    protected int maxPoolSize = Integer.MAX_VALUE;
    protected long maxWaitTimeInMillis;
    protected int idleTimeoutInSeconds;

    private AbstractPoolTimerTask poolTimerTask;

    // class loader used as context class loader for asynchronous operations
    protected ClassLoader containerClassLoader;

    protected int destroyedCount;
    protected int poolSuccess;
    protected String poolName;
    protected int poolReturned;

    protected String configData;
    protected EjbPoolProbeProvider poolProbeNotifier;

    protected String appName;
    protected String modName;
    protected String ejbName;

    protected long beanId;

    protected AbstractPool() {
    }

    protected AbstractPool(ObjectFactory factory, long beanId, int steadyPoolSize, int resizeQuantity, int maxPoolsize,
            long maxWaitTimeInMillis, int idleTimeoutInSeconds, ClassLoader loader) {
        initializePool(factory, beanId, steadyPoolSize, resizeQuantity, maxPoolsize, maxWaitTimeInMillis, idleTimeoutInSeconds, loader);
    }

    protected void initializePool(ObjectFactory factory, long beanId, int steadyPoolSize, int resizeQuantity, int maxPoolsize,
            long maxWaitTimeInMillis, int idleTimeoutInSeconds, ClassLoader loader) {

        pooledObjects = new ArrayList<>();

        this.pooledObjectFactory = factory;
        this.steadyPoolSize = steadyPoolSize;
        this.resizeQuantity = resizeQuantity;
        this.maxPoolSize = maxPoolsize;
        this.maxWaitTimeInMillis = maxWaitTimeInMillis;
        this.idleTimeoutInSeconds = idleTimeoutInSeconds;

        this.beanId = beanId;

        if (steadyPoolSize > 0) {
            for (int i = 0; i < steadyPoolSize; i++) {
                pooledObjects.add(factory.create(null));
                poolProbeNotifier.ejbObjectAddedEvent(beanId, appName, modName, ejbName);
                createdCount++;
            }
        }

        this.containerClassLoader = loader;

        if (this.idleTimeoutInSeconds > 0) {
            try {
                this.poolTimerTask = new AbstractPoolTimerTask();
                EjbContainerUtilImpl.getInstance()
                                    .getTimer()
                                    .scheduleAtFixedRate(poolTimerTask, idleTimeoutInSeconds * 1000L, idleTimeoutInSeconds * 1000L);
            } catch (Throwable th) {
                _logger.log(WARNING, "[AbstractPool]: Could not add AbstractPoolTimerTask" + " ... Continuing anyway...");
            }
        }
    }

    public void setContainerClassLoader(ClassLoader loader) {
        this.containerClassLoader = loader;
    }

    public void setInfo(String appName, String modName, String ejbName) {
        this.appName = appName;
        this.modName = modName;
        this.ejbName = ejbName;
        try {
            poolProbeNotifier =
                EjbContainerUtilImpl.getInstance()
                                    .getProbeProviderFactory()
                                    .getProbeProvider(
                                        EjbPoolProbeProvider.class,
                                        EjbMonitoringUtils.getInvokerId(appName, modName, ejbName));

            _logger.log(FINE, () -> "Got poolProbeNotifier: " + poolProbeNotifier.getClass().getName());
        } catch (Exception ex) {
            poolProbeNotifier = new EjbPoolProbeProvider();
            _logger.log(FINE, "Error getting the EjbPoolProbeProvider");
        }
    }

    @Override
    public Object getObject(Object param) throws PoolException {
        long t1 = 0, totalWaitTime = 0;
        int size;

        synchronized (pooledObjects) {
            while (true) {
                if ((size = pooledObjects.size()) > 0) {
                    poolSuccess++;
                    return pooledObjects.remove(size - 1);
                } else if ((createdCount - destroyedCount) < maxPoolSize) {
                    poolProbeNotifier.ejbObjectAddedEvent(beanId, appName, modName, ejbName);
                    createdCount++; // hope that everything will be OK.
                    break;
                }

                if (maxWaitTimeInMillis >= 0) {
                    waitCount++;
                    t1 = System.currentTimeMillis();
                    try {
                        _logger.log(FINE, "[AbstractPool]: Waiting on" + " the pool to get a bean instance...");
                        pooledObjects.wait(maxWaitTimeInMillis);
                    } catch (InterruptedException inEx) {
                        throw new PoolException("Thread interrupted.", inEx);
                    }
                    waitCount--;
                    totalWaitTime += System.currentTimeMillis() - t1;
                    if ((size = pooledObjects.size()) > 0) {
                        poolSuccess++;
                        return pooledObjects.remove(size - 1);
                    }

                    if (maxWaitTimeInMillis == 0) {
                        // nothing special to do in this case
                    } else if (totalWaitTime >= maxWaitTimeInMillis) {
                        throw new PoolException("Pool Instance not obtained" + " within given time interval.");
                    }
                } else {
                    throw new PoolException("Pool Instance not obtained" + " within given time interval.");
                }
            }
        }

        try {
            return pooledObjectFactory.create(param);
        } catch (Exception poolEx) {
            synchronized (pooledObjects) {
                poolProbeNotifier.ejbObjectAddFailedEvent(beanId, appName, modName, ejbName);
                createdCount--;
            }

            throw new RuntimeException("Caught Exception when trying " + "to create pool Object ", poolEx);
        }
    }

    /**
     * Return an object back to the pool. An object that is obtained through getObject() must always be returned back to the
     * pool using either returnObject(obj) or through destroyObject(obj).
     */
    @Override
    public void returnObject(Object object) {
        synchronized (pooledObjects) {
            pooledObjects.add(object);
            poolReturned++;

            if (waitCount > 0) {
                pooledObjects.notify();
            }
        }
    }

    /**
     * Destroys an Object. Note that applications should not ignore the reference to the object that they got from
     * getObject(). An object that is obtained through getObject() must always be returned back to the pool using either
     * returnObject(obj) or through destroyObject(obj). This method tells that the object should be destroyed and cannot be
     * reused.
     */
    @Override
    public void destroyObject(Object object) {
        synchronized (pooledObjects) {
            poolProbeNotifier.ejbObjectDestroyedEvent(beanId, appName, modName, ejbName);
            destroyedCount++;
            if (waitCount > 0) {
                pooledObjects.notify();
            }
        }

        try {
            pooledObjectFactory.destroy(object);
        } catch (Exception ex) {
            _logger.log(FINE, "Exception in destroyObject()", ex);
        }
    }

    /**
     * Preload the pool with objects.
     *
     * @param count the number of objects to be added.
     */
    protected void preload(int count) {
        synchronized (pooledObjects) {
            for (int i = 0; i < count; i++) {
                try {
                    pooledObjects.add(pooledObjectFactory.create(null));
                    poolProbeNotifier.ejbObjectAddedEvent(beanId, appName, modName, ejbName);
                    createdCount++;
                } catch (PoolException poolEx) {
                    _logger.log(FINE, "Exception in preload()", poolEx);
                }
            }
        }
    }

    /**
     * Close the pool
     */
    public void close() {
        synchronized (pooledObjects) {
            if (poolTimerTask != null) {
                try {
                    poolTimerTask.cancel();
                    _logger.log(WARNING, "[AbstractPool]: Cancelled pool timer task " + " at: " + (new java.util.Date()));
                } catch (Throwable th) {
                    // Can safely ignore this!!
                }
            }

            _logger.log(FINE, "[AbstractPool]: Destroying " + pooledObjects.size() + " beans from the pool...");

            // Since we're calling into ejb code, we need to set context class loader
            ClassLoader originalClassLoader = setContextClassLoader(containerClassLoader);

            for (Object pooledObject : pooledObjects) {
                try {
                    poolProbeNotifier.ejbObjectDestroyedEvent(beanId, appName, modName, ejbName);
                    destroyedCount++;
                    try {
                        pooledObjectFactory.destroy(pooledObject);
                    } catch (Throwable th) {
                        _logger.log(FINE, "Exception in destroy()", th);
                    }
                } catch (Throwable th) {
                    _logger.log(WARNING, "[AbstractPool]: Error while destroying: " + th);
                }
            }
            _logger.log(FINE, "[AbstractPool]: Pool closed....");
            unregisterProbeProvider();

            setContextClassLoader(originalClassLoader);
        }

        // helps garbage collection
        this.pooledObjects = null;
        this.pooledObjectFactory = null;
        this.poolTimerTask = null;
        this.containerClassLoader = null;
    }

    protected void remove(int count) {
        List<Object> removeList = new ArrayList<>();

        synchronized (pooledObjects) {
            int size = pooledObjects.size();
            for (int i = 0; (i < count) && (size > 0); i++) {
                removeList.add(pooledObjects.remove(--size));
                poolProbeNotifier.ejbObjectDestroyedEvent(beanId, appName, modName, ejbName);
                destroyedCount++;
            }

            pooledObjects.notifyAll();
        }

        for (int i = removeList.size() - 1; i >= 0; i--) {
            pooledObjectFactory.destroy(removeList.remove(i));
            try {
                pooledObjectFactory.destroy(removeList.remove(i));
            } catch (Throwable th) {
                _logger.log(FINE, "Exception in destroy()", th);
            }
        }
    }

    protected abstract void removeIdleObjects();

    private class AbstractPoolTimerTask extends java.util.TimerTask {

        AbstractPoolTimerTask() {
        }

        @Override
        public void run() {
            // We need to set the context class loader for this (deamon)thread!!
            final Thread currentThread = Thread.currentThread();
            final ClassLoader previousClassLoader = currentThread.getContextClassLoader();

            try {
                currentThread.setContextClassLoader(containerClassLoader);

                try {
                    if (pooledObjects.size() > steadyPoolSize) {
                        _logger.log(FINE, "[AbstractPool]: Removing idle " + " objects from pool. Current Size: "
                                + pooledObjects.size() + "/" + steadyPoolSize + ". Time: " + (new java.util.Date()));
                        removeIdleObjects();
                    }
                } catch (Throwable th) {
                    // removeIdleObjects would have logged the error
                }

                currentThread.setContextClassLoader(previousClassLoader);
            } catch (Throwable th) {
                _logger.log(FINE, "Exception in run()", th);
            }
        }
    }

    /**************** For Monitoring ***********************/
    /*******************************************************/

    public int getCreatedCount() {
        return createdCount;
    }

    public int getDestroyedCount() {
        return destroyedCount;
    }

    public int getPoolSuccess() {
        return poolSuccess;
    }

    public int getSize() {
        return pooledObjects.size();
    }

    public int getWaitCount() {
        return waitCount;
    }

    public int getSteadyPoolSize() {
        return steadyPoolSize;
    }

    public int getResizeQuantity() {
        return resizeQuantity;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public long getMaxWaitTimeInMillis() {
        return maxWaitTimeInMillis;
    }

    public int getIdleTimeoutInSeconds() {
        return idleTimeoutInSeconds;
    }

    public void setConfigData(String configData) {
        this.configData = configData;
    }

    // Methods on EJBPoolStatsProvider
    public void appendStats(StringBuffer sbuf) {
        sbuf.append("[Pool: ")
            .append("SZ=").append(pooledObjects.size()).append("; ")
            .append("CC=").append(createdCount).append("; ")
            .append("DC=").append(destroyedCount).append("; ")
            .append("WC=").append(waitCount).append("; ")
            .append("MSG=0");

        if (configData != null) {
            sbuf.append(configData);
        }

        sbuf.append("]");
    }

    public int getJmsMaxMessagesLoad() {
        return 0;
    }

    public int getNumBeansInPool() {
        return pooledObjects.size();
    }

    public int getNumThreadsWaiting() {
        return waitCount;
    }

    public int getTotalBeansCreated() {
        return createdCount;
    }

    public int getTotalBeansDestroyed() {
        return destroyedCount;
    }

    public String getAllMonitoredAttrbuteValues() {
        StringBuffer sbuf = new StringBuffer();
        synchronized (pooledObjects) {
            sbuf.append("createdCount=").append(createdCount).append(";")
                .append("destroyedCount=").append(destroyedCount).append(";")
                .append("waitCount=").append(waitCount).append(";")
                .append("size=").append(pooledObjects.size()).append(";");
        }
        sbuf.append("maxPoolSize=").append(maxPoolSize).append(";");

        return sbuf.toString();
    }

    public String getAllAttrValues() {
        StringBuffer sbuf = new StringBuffer();
        if (null != poolName) {
            sbuf.append(":").append(poolName);
        } else {
            sbuf.append(":POOL");
        }

        sbuf.append("[FP=").append(poolSuccess).append(",")
            .append("TC=").append(createdCount).append(",")
            .append("TD=").append(destroyedCount).append(",")
            .append("PR=").append(poolReturned).append(",")
            .append("TW=").append(waitCount).append(",")
            .append("CS=").append(pooledObjects.size()).append(",")
            .append("MS=").append(maxPoolSize);

        return sbuf.toString();
    }

    protected void unregisterProbeProvider() {
        try {
            EjbContainerUtilImpl.getInstance()
                                .getProbeProviderFactory()
                                .unregisterProbeProvider(poolProbeNotifier);
        } catch (Exception ex) {
            _logger.log(FINE, "Error getting the EjbPoolProbeProvider");
        }
    }
}
