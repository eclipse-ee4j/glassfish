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
 * <BR> <I>$Source: /cvs/glassfish/appserv-core/src/java/com/sun/ejb/containers/util/pool/NonBlockingPool.java,v $</I>
 * @author     $Author: cf126330 $
 * @version    $Revision: 1.4 $ $Date: 2007/03/30 19:10:26 $
 */
package com.sun.ejb.containers.util.pool;

import static com.sun.enterprise.util.Utility.setContextClassLoader;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

import com.sun.ejb.containers.EJBContextImpl;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

/**
 * <p>
 * NonBlockingPool pool provides the basic implementation of an object pool.
 *
 * <p>
 * The implementation uses a linked list to maintain a list of (available) objects. If the pool is empty it simply creates one
 * using the ObjectFactory instance.
 *
 * <p>
 * Subclasses can change this behaviour by overriding getObject(...) and returnObject(....) methods. This class provides
 * basic support for synchronization, event notification, pool shutdown and pool object recycling. It also does some
 * very basic bookkeeping like the number of objects created, number of threads waiting for object.
 *
 * <p>
 * Subclasses can make use of these book-keeping data to provide complex pooling mechanism like LRU / MRU / Random.
 * Also, note that AbstractPool does not have a notion of pool limit. It is upto to the derived classes to implement
 * these features.
 *
 */
public class NonBlockingPool extends AbstractPool {

    protected boolean addedResizeTask;
    volatile protected boolean addedIdleBeanWork;
    protected boolean inResizing;

    private String poolName;
    private TimerTask poolTimerTask;
    private boolean maintainSteadySize;

    /**
     * If glassfish-ejb-jar.xml <enterprise-beans><property>singleton-bean-pool is true, steadyPoolSize is 1, and
     * maxPoolSize is 1, then this field is set to true, and only 1 bean instance is created. The pool size at any given
     * time may be 0 or 1. Both PoolResizeTimerTask and ReSizeWork are skipped.
     */
    private boolean singletonBeanPool;

    // Set to true after close(). Prevents race condition
    // of async resize task kicking in after close().
    private boolean poolClosed;


    protected NonBlockingPool() {
    }

    public NonBlockingPool(long beanId, String poolName, ObjectFactory factory, int steadyPoolSize, int resizeQuantity, int maxPoolSize,
            int idleTimeoutInSeconds, ClassLoader loader) {
        this(beanId, poolName, factory, steadyPoolSize, resizeQuantity, maxPoolSize, idleTimeoutInSeconds, loader, false);
    }

    public NonBlockingPool(long beanId, String poolName, ObjectFactory factory, int steadyPoolSize, int resizeQuantity, int maxPoolSize,
            int idleTimeoutInSeconds, ClassLoader loader, boolean singletonBeanPool) {
        this.poolName = poolName;
        this.beanId = beanId;
        this.singletonBeanPool = singletonBeanPool && (steadyPoolSize == 1) && (maxPoolSize == 1);
        initializePool(factory, steadyPoolSize, resizeQuantity, maxPoolSize, idleTimeoutInSeconds, loader);
    }

    protected void initializePool(ObjectFactory factory, int steadyPoolSize, int resizeQuantity, int maxPoolSize, int idleTimeoutInSeconds, ClassLoader loader) {
        pooledObjects = new ArrayList<>();

        this.pooledObjectFactory = factory;
        this.steadyPoolSize = steadyPoolSize <= 0 ? 0 : steadyPoolSize;
        this.resizeQuantity = resizeQuantity <= 0 ? 0 : resizeQuantity;
        this.maxPoolSize = maxPoolSize <= 0 ? Integer.MAX_VALUE : maxPoolSize;
        this.steadyPoolSize = this.steadyPoolSize > this.maxPoolSize ? this.maxPoolSize : this.steadyPoolSize;
        this.idleTimeoutInSeconds = idleTimeoutInSeconds <= 0 || this.singletonBeanPool ? 0 : idleTimeoutInSeconds;

        this.containerClassLoader = loader;

        this.maintainSteadySize = this.singletonBeanPool ? false : this.steadyPoolSize > 0;

        if (this.idleTimeoutInSeconds > 0 && this.resizeQuantity > 0) {
            try {
                this.poolTimerTask = new PoolResizeTimerTask();
                EjbContainerUtilImpl.getInstance()
                                    .getTimer()
                                    .scheduleAtFixedRate(
                                        poolTimerTask, idleTimeoutInSeconds * 1000L, idleTimeoutInSeconds * 1000L);

                _logger.log(FINE, () -> "[Pool-" + poolName + "]: Added PoolResizeTimerTask...");
            } catch (Throwable th) {
                _logger.log(WARNING, "[Pool-" + poolName + "]: Could not add" + " PoolTimerTask. Continuing anyway...", th);
            }
        }
    }

    @Override
    public void setContainerClassLoader(ClassLoader loader) {
        this.containerClassLoader = loader;
    }

    @Override
    public Object getObject(Object param) {
        boolean toAddResizeTask = false;
        Object pooledObject = null;

        synchronized (pooledObjects) {
            int size = pooledObjects.size();
            if (size > steadyPoolSize) {
                poolSuccess++;
                return pooledObjects.remove(size - 1);
            }

            if (size > 0) {
                poolSuccess++;
                if ((maintainSteadySize) && (addedResizeTask == false)) {
                    toAddResizeTask = addedResizeTask = true;
                    pooledObject = pooledObjects.remove(size - 1);
                } else {
                    return pooledObjects.remove(size - 1);
                }
            } else if (!singletonBeanPool) {
                if ((maintainSteadySize) && (addedResizeTask == false)) {
                    toAddResizeTask = addedResizeTask = true;
                }
                poolProbeNotifier.ejbObjectAddedEvent(beanId, appName, modName, ejbName);
                createdCount++; // hope that everything will be OK.
            }
        }

        if (toAddResizeTask) {
            addResizeTaskForImmediateExecution();
        }

        if (pooledObject != null) {
            return pooledObject;
        }

        if (singletonBeanPool) {
            synchronized (pooledObjects) {
                while (pooledObjects.isEmpty() && (createdCount - destroyedCount) > 0) {
                    try {
                        pooledObjects.wait();
                    } catch (InterruptedException ex) { // ignore
                    }
                }

                if (!pooledObjects.isEmpty()) {
                    return pooledObjects.remove(0);
                }

                try {
                    pooledObject = pooledObjectFactory.create(param);
                    createdCount++;
                    return pooledObject;
                } catch (RuntimeException th) {
                    poolProbeNotifier.ejbObjectAddFailedEvent(beanId, appName, modName, ejbName);
                    throw th;
                }
            }
        } else {
            try {
                return pooledObjectFactory.create(param);
            } catch (RuntimeException th) {
                synchronized (pooledObjects) {
                    poolProbeNotifier.ejbObjectAddFailedEvent(beanId, appName, modName, ejbName);
                    createdCount--;
                }

                throw th;
            }
        }
    }

    private void addResizeTaskForImmediateExecution() {
        try {
            ReSizeWork work = new ReSizeWork();
            EjbContainerUtilImpl.getInstance().addWork(work);
            _logger.log(FINE, () -> "[Pool-" + poolName + "]: Added PoolResizeTimerTask...");
        } catch (Exception ex) {
            synchronized (pooledObjects) {
                addedResizeTask = false;
            }
            _logger.log(WARNING, ex, () -> "[Pool-" + poolName + "]: Cannot perform " + " pool resize task");
        }
    }

    /**
     * Return an object back to the pool. An object that is obtained through getObject() must always be returned back to the
     * pool using either returnObject(obj) or through destroyObject(obj).
     */
    @Override
    public void returnObject(Object object) {
        synchronized (pooledObjects) {
            if (pooledObjects.size() < maxPoolSize) {
                pooledObjects.add(object);
                if (singletonBeanPool) {
                    pooledObjects.notify();
                }
                return;
            }

            poolProbeNotifier.ejbObjectDestroyedEvent(beanId, appName, modName, ejbName);
            destroyedCount++;
        }

        try {
            pooledObjectFactory.destroy(object);
        } catch (Exception ex) {
            _logger.log(FINE, "exception in returnObj", ex);
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
            if (singletonBeanPool) {
                pooledObjects.notify();
            }
        }

        try {
            pooledObjectFactory.destroy(object);
        } catch (Exception ex) {
            _logger.log(FINE, "exception in destroyObject", ex);
        }
    }

    /**
     * Prepopulate the pool with objects.
     *
     * @param count the number of objects to be added.
     */
    public void prepopulate(int count) {
        steadyPoolSize = count <= 0 ? 0 : count;
        steadyPoolSize = steadyPoolSize > maxPoolSize ? maxPoolSize : steadyPoolSize;

        if (steadyPoolSize > 0) {
            preload(steadyPoolSize);
        }

    }

    /**
     * Preload the pool with objects.
     *
     * @param count the number of objects to be added.
     */
    @Override
    protected void preload(int count) {
        List<Object> pooledObjectnewInstances = new ArrayList<>(count);
        try {
            for (int i = 0; i < count; i++) {
                pooledObjectnewInstances.add(pooledObjectFactory.create(null));
            }
        } catch (Exception ex) {
            // Need not throw this exception up since we are pre-populating
        }

        int sz = pooledObjectnewInstances.size();
        if (sz == 0) {
            return;
        }

        synchronized (pooledObjects) {
            // check current pool size & adjust add size
            int currsize = pooledObjects.size();
            int addsz = sz;
            if (currsize + sz > maxPoolSize) {
                addsz = maxPoolSize - currsize;
            }

            for (int i = 0; i < addsz; i++) {
                pooledObjects.add(pooledObjectnewInstances.remove(0));
            }
            createdCount += sz;
        }

        // Destroys unnecessary instances
        for (Object pooledObjectInstance : pooledObjectnewInstances) {
            destroyObject(pooledObjectInstance);
        }
    }

    /**
     * Close the pool
     */
    @Override
    public void close() {
        synchronized (pooledObjects) {
            if (poolTimerTask != null) {
                try {
                    poolTimerTask.cancel();
                    _logger.log(FINE, () -> "[Pool-" + poolName + "]: Cancelled pool timer task " + " at: " + (new java.util.Date()));
                } catch (Throwable th) {
                    // Can safely ignore this!!
                }
            }

            _logger.log(FINE, () -> "[Pool-" + poolName + "]: Destroying " + pooledObjects.size() + " beans from the pool...");

            // Since we're calling into EJB code, we need to set context class loader
            ClassLoader origLoader = setContextClassLoader(containerClassLoader);

            Object[] array = pooledObjects.toArray();
            for (int i = 0; i < array.length; i++) {
                try {
                    poolProbeNotifier.ejbObjectDestroyedEvent(beanId, appName, modName, ejbName);
                    destroyedCount++;
                    try {
                        pooledObjectFactory.destroy(array[i]);
                    } catch (Throwable th) {
                        _logger.log(FINE, "exception in close", th);
                    }
                } catch (Throwable th) {
                    _logger.log(WARNING, "[Pool-" + poolName + "]: Error while destroying", th);
                }
            }
            _logger.log(FINE, "Pool-" + poolName + "]: Pool closed....");
            pooledObjects.clear();
            unregisterProbeProvider();

            setContextClassLoader(origLoader);

            poolClosed = true;

            this.pooledObjects = null;
            this.pooledObjectFactory = null;
            this.poolTimerTask = null;
            this.containerClassLoader = null;
        }

    }

    @Override
    protected void remove(int count) {
        List<Object> removeList = new ArrayList<>();
        synchronized (pooledObjects) {
            int size = pooledObjects.size();
            for (int i = 0; (i < count) && (size > 0); i++) {
                removeList.add(pooledObjects.remove(--size));
                poolProbeNotifier.ejbObjectDestroyedEvent(beanId, appName, modName, ejbName);
                destroyedCount++;
            }
        }

        int sz = removeList.size();
        for (int i = 0; i < sz; i++) {
            try {
                pooledObjectFactory.destroy(removeList.get(i));
            } catch (Throwable th) {
                _logger.log(FINE, "exception in remove", th);
            }
        }
    }

    @Override
    protected void removeIdleObjects() {
    }

    protected void doResize() {
        if (poolClosed) {
            return;
        }

        // We need to set the context class loader for this (deamon) thread!!
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();

        long startTime = 0;
        boolean enteredResizeBlock = false;
        try {
            currentThread.setContextClassLoader(containerClassLoader);

            _logger.log(FINE, () -> "[Pool-" + poolName + "]: Resize started at: " + (new Date()) + " steadyPoolSize ::"
                        + steadyPoolSize + " resizeQuantity ::" + resizeQuantity + " maxPoolSize ::" + maxPoolSize);
            startTime = System.currentTimeMillis();

            List<Object> removeList = new ArrayList<>();
            long populateCount = 0;
            synchronized (pooledObjects) {
                if ((inResizing == true) || poolClosed) {
                    return;
                }

                enteredResizeBlock = true;
                inResizing = true;

                int curSize = pooledObjects.size();

                if (curSize > steadyPoolSize) {

                    // possible to reduce pool size....
                    if ((idleTimeoutInSeconds <= 0) || (resizeQuantity <= 0)) {
                        return;
                    }
                    int victimCount = (curSize > (steadyPoolSize + resizeQuantity)) ? resizeQuantity : (curSize - steadyPoolSize);
                    long allowedIdleTime = System.currentTimeMillis() - idleTimeoutInSeconds * 1000L;
                    _logger.log(FINE, () -> "[Pool-" + poolName + "]: Resize:: reducing " + " pool size by: " + victimCount);

                    for (int i = 0; i < victimCount; i++) {
                        EJBContextImpl ejbContext = (EJBContextImpl) pooledObjects.get(0);
                        if (ejbContext.getLastTimeUsed() <= allowedIdleTime) {
                            removeList.add(pooledObjects.remove(0));

                            poolProbeNotifier.ejbObjectDestroyedEvent(beanId, appName, modName, ejbName);
                            destroyedCount++;
                        } else {
                            break;
                        }
                    }
                } else if (curSize < steadyPoolSize) {

                    // Need to populate....
                    if (maintainSteadySize == false) {
                        return;
                    }

                    if (resizeQuantity <= 0) {
                        populateCount = steadyPoolSize - curSize;
                    } else {
                        while ((curSize + populateCount) < steadyPoolSize) {
                            populateCount += resizeQuantity;
                        }
                        if ((curSize + populateCount) > maxPoolSize) {
                            populateCount -= (curSize + populateCount) - maxPoolSize;
                        }
                    }
                }
            }

            if (removeList.size() > 0) {
                int sz = removeList.size();
                for (int i = 0; i < sz; i++) {
                    try {
                        pooledObjectFactory.destroy(removeList.get(i));
                    } catch (Throwable th) {
                        _logger.log(FINE, "exception in doResize", th);
                    }
                }
            }

            if (populateCount > 0) {
                // preload adds items inside a sync block....

                if (_logger.isLoggable(FINE)) {
                    _logger.log(FINE, "[Pool-" + poolName + "]: Attempting to preload " + populateCount
                            + " beans. CurSize/MaxPoolSize: " + pooledObjects.size() + "/" + maxPoolSize);
                }

                preload((int) populateCount);

                _logger.log(FINE,
                        () -> "[Pool-" + poolName + "]: After preload " + "CurSize/MaxPoolSize: " + pooledObjects.size() + "/" + maxPoolSize);
            }

        } catch (Throwable th) {
            _logger.log(WARNING, "[Pool-" + poolName + "]: Exception during reSize", th);

        } finally {
            if (enteredResizeBlock) {
                synchronized (pooledObjects) {
                    inResizing = false;
                }
            }

            currentThread.setContextClassLoader(previousClassLoader);
        }

        long endTime = System.currentTimeMillis();
        if (_logger.isLoggable(FINE)) {
            _logger.log(FINE,
                    "[Pool-" + poolName + "]: Resize completed at: " + (new java.util.Date()) + "; after reSize: " + getAllAttrValues());
            _logger.log(FINE, "[Pool-" + poolName + "]: Resize took: " + ((endTime - startTime) / 1000.0) + " seconds.");
        }
    }

    @Override
    public String getAllAttrValues() {
        StringBuffer sbuf = new StringBuffer("[Pool-" + poolName + "] ");
        sbuf.append("CC=").append(createdCount).append("; ").append("DC=").append(destroyedCount).append("; ").append("CS=")
                .append(pooledObjects.size()).append("; ").append("SS=").append(steadyPoolSize).append("; ").append("MS=").append(maxPoolSize)
                .append(";");
        return sbuf.toString();
    }

    private class ReSizeWork implements Runnable {
        @Override
        public void run() {
            try {
                doResize();
            } catch (Exception ex) {
                _logger.log(WARNING, "[Pool-" + poolName + "]: Exception during reSize", ex);
            } finally {
                synchronized (pooledObjects) {
                    addedResizeTask = false;
                }
            }
        }
    }

    private class IdleBeanWork implements Runnable {
        @Override
        public void run() {
            try {
                doResize();
            } catch (Exception ex) {
            } finally {
                addedIdleBeanWork = false;
            }
        }
    }

    private class PoolResizeTimerTask extends java.util.TimerTask {
        PoolResizeTimerTask() {
        }

        @Override
        public void run() {
            try {
                if (addedIdleBeanWork == true) {
                    return;
                }

                addedIdleBeanWork = true;
                IdleBeanWork work = new IdleBeanWork();
                EjbContainerUtilImpl.getInstance().addWork(work);
            } catch (Exception ex) {
                addedIdleBeanWork = false;
                _logger.log(WARNING, "[Pool-" + poolName + "]: Cannot perform " + " pool idle bean cleanup", ex);
            }

        }
    }


}
