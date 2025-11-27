/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.enterprise.iiop.util;

import com.sun.corba.ee.spi.threadpool.NoSuchThreadPoolException;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.ThreadPoolChooser;
import com.sun.corba.ee.spi.threadpool.ThreadPoolFactory;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.logging.LogDomains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.internal.api.Globals;

public class S1ASThreadPoolManager implements ThreadPoolManager {

    private static final Logger LOG = LogDomains.getLogger(S1ASThreadPoolManager.class, LogDomains.CORBA_LOGGER, false);

    private static final int DEFAULT_MIN_THREAD_COUNT = 10;
    private static final int DEFAULT_MAX_THREAD_COUNT = 200;

    private static HashMap<String, Integer> idToIndexTable = new HashMap<>();
    private static HashMap<Integer, String> indexToIdTable = new HashMap<>();
    private static ArrayList<ThreadPool> threadpoolList = new ArrayList<>();
    private static String defaultID;

    private static ThreadPoolManager s1asThreadPoolMgr = new S1ASThreadPoolManager();
    private static IIOPUtils _iiopUtils;

    public static ThreadPoolManager getThreadPoolManager() {
        return s1asThreadPoolMgr;
    }


    static {
        try {
            /* TODO - IIOPUtils cannot be used in Web Profile. it requires IiopService which
               isn't on the classpath. Refactor this class to use something else than IIOPUtils
               or make IIOPUtils work in Web Profile
            */
            _iiopUtils = Globals.getDefaultHabitat().getService(IIOPUtils.class);
            Collection<org.glassfish.grizzly.config.dom.ThreadPool> tpCol = _iiopUtils.getAllThreadPools();
            org.glassfish.grizzly.config.dom.ThreadPool[] allThreadPools = tpCol.toArray(org.glassfish.grizzly.config.dom.ThreadPool[]::new);
            for (int i = 0; i < allThreadPools.length; i++) {
                createThreadPools(allThreadPools[i], i);
            }
            defaultID = indexToIdTable.get(Integer.valueOf(0));
        } catch (NullPointerException npe) {
            LOG.log(Level.FINE, "Server Context is NULL. Ignoring and proceeding.");
        }


    }

    S1ASThreadPoolManager() {
    }


    private static void createThreadPools(org.glassfish.grizzly.config.dom.ThreadPool
            threadpoolBean, int index) {
        String threadpoolId = null;
        String minThreadsValue, maxThreadsValue, timeoutValue;//, numberOfQueuesValue;
        int minThreads = DEFAULT_MIN_THREAD_COUNT;
        int maxThreads = DEFAULT_MAX_THREAD_COUNT;
        int idleTimeoutInSeconds = 120000;

        try {
            threadpoolId = threadpoolBean.getName();
        } catch (NullPointerException npe) {
            LOG.log(Level.WARNING, "ThreadPoolBean may be null ", npe);
        }
        try {
            minThreadsValue = threadpoolBean.getMinThreadPoolSize();
            minThreads = Integer.parseInt(minThreadsValue);
        } catch (NullPointerException npe) {
            LOG.log(Level.WARNING, "ThreadPoolBean may be null ", npe);
            LOG.log(Level.WARNING, "Using default value for steady-threadpool-size = {0}", minThreads);
        } catch (NumberFormatException nfe) {
            LOG.log(Level.WARNING, "Number Format Exception, Using default value(s)", nfe);
            LOG.log(Level.WARNING, "Using default value for min-threadpool-size = {0}", minThreads);
        }
        try {
            maxThreadsValue = threadpoolBean.getMaxThreadPoolSize();
            maxThreads = Integer.parseInt(maxThreadsValue);
        } catch (NullPointerException npe) {
            LOG.log(Level.WARNING, "ThreadPoolBean may be null ", npe);
            LOG.log(Level.WARNING, "Using default value for max-threadpool-size = " + maxThreads);
        } catch (NumberFormatException nfe) {
            LOG.log(Level.WARNING, "Number Format Exception, Using default value(s)", nfe);
            LOG.log(Level.WARNING, "Using default value for max-threadpool-size = {0}", maxThreads);
        }
        try {
            timeoutValue = threadpoolBean.getIdleThreadTimeoutSeconds();
            idleTimeoutInSeconds = Integer.parseInt(timeoutValue);
        } catch (NullPointerException npe) {
            LOG.log(Level.WARNING, "ThreadPoolBean may be null ", npe);
            LOG.log(Level.WARNING, "Using default value for idle-thread-timeout-in-seconds = {0}",
                idleTimeoutInSeconds);
        } catch (NumberFormatException nfe) {
            LOG.log(Level.WARNING, "Number Format Exception, Using default value(s)", nfe);
            LOG.log(Level.WARNING, "Using default value for idle-thread-timeout-in-seconds = {0}",
                idleTimeoutInSeconds);
        }

        // Mutiplied the idleTimeoutInSeconds by 1000 to convert to milliseconds
        ThreadPoolFactory threadPoolFactory = new ThreadPoolFactory();
        ThreadPool threadpool = threadPoolFactory.create(minThreads, maxThreads, idleTimeoutInSeconds * 1000L,
            threadpoolId, _iiopUtils.getCommonClassLoader());

        // Add the threadpool instance to the threadpoolList
        threadpoolList.add(threadpool);

        // Associate the threadpoolId to the index passed
        idToIndexTable.put(threadpoolId, Integer.valueOf(index));

        // Associate the threadpoolId to the index passed
        indexToIdTable.put(Integer.valueOf(index), threadpoolId);
    }

    /**
     * This method will return an instance of the threadpool given a threadpoolId,
     * that can be used by any component in the app. server.
     *
     * @throws NoSuchThreadPoolException thrown when invalid threadpoolId is passed
     *                                   as a parameter
     */
    @Override
    public ThreadPool getThreadPool(String id) throws NoSuchThreadPoolException {
        Integer i = idToIndexTable.get(id);
        if (i == null) {
            throw new NoSuchThreadPoolException();
        }
        try {
            ThreadPool threadpool = threadpoolList.get(i.intValue());
            return threadpool;
        } catch (IndexOutOfBoundsException iobe) {
            throw new NoSuchThreadPoolException();
        }
    }


    /**
     * This method will return an instance of the threadpool given a numeric threadpoolId.
     * This method will be used by the ORB to support the functionality of
     * dedicated threadpool for EJB beans
     *
     * @throws NoSuchThreadPoolException thrown when invalidnumericIdForThreadpool is passed
     *                                   as a parameter
     */
    @Override
    public ThreadPool getThreadPool(int numericIdForThreadpool) throws NoSuchThreadPoolException {
        try {
            ThreadPool threadpool = threadpoolList.get(numericIdForThreadpool);
            return threadpool;
        } catch (IndexOutOfBoundsException iobe) {
            throw new NoSuchThreadPoolException();
        }
    }

    /**
     * This method is used to return the numeric id of the threadpool, given a String
     * threadpoolId. This is used by the POA interceptors to add the numeric threadpool
     * Id, as a tagged component in the IOR. This is used to provide the functionality of
     * dedicated threadpool for EJB beans
     */
    @Override
    public int getThreadPoolNumericId(String id) {
        Integer i = idToIndexTable.get(id);
        return i == null ? 0 : i.intValue();
    }

    /**
     * Return a String Id for a numericId of a threadpool managed by the threadpool
     * manager
     */
    @Override
    public String getThreadPoolStringId(int numericIdForThreadpool) {
        String id = indexToIdTable.get(Integer.valueOf(numericIdForThreadpool));
        return id == null ? defaultID : id;
    }

    /**
     * Returns the first instance of ThreadPool in the ThreadPoolManager
     */
    @Override
    public ThreadPool
    getDefaultThreadPool() {
        try {
            return getThreadPool(0);
        } catch (NoSuchThreadPoolException nstpe) {
            LOG.log(Level.WARNING, "No default ThreadPool defined ", nstpe);
        }
        return null;
    }

    /**
     * Return an instance of ThreadPoolChooser based on the componentId that was
     * passed as argument
     */
    @Override
    public ThreadPoolChooser getThreadPoolChooser(String componentId) {
        //FIXME: This method is not used, but should be fixed once
        //ORB's nio select starts working and we start using ThreadPoolChooser
        //This will be mostly used by the ORB
        return null;
    }

    /**
     * Return an instance of ThreadPoolChooser based on the componentIndex that was
     * passed as argument. This is added for improved performance so that the caller
     * does not have to pay the cost of computing hashcode for the componentId
     */
    @Override
    public ThreadPoolChooser getThreadPoolChooser(int componentIndex) {
        //FIXME: This method is not used, but should be fixed once
        //ORB's nio select starts working and we start using ThreadPoolChooser
        //This will be mostly used by the ORB
        return null;
    }

    /**
     * Sets a ThreadPoolChooser for a particular componentId in the ThreadPoolManager. This
     * would enable any component to add a ThreadPoolChooser for their specific use
     */
    @Override
    public void setThreadPoolChooser(String componentId, ThreadPoolChooser aThreadPoolChooser) {
        //FIXME: This method is not used, but should be fixed once
        //ORB's nio select starts working and we start using ThreadPoolChooser
        //This will be mostly used by the ORB
    }

    /**
     * Gets the numeric index associated with the componentId specified for a
     * ThreadPoolChooser. This method would help the component call the more
     * efficient implementation i.e. getThreadPoolChooser(int componentIndex)
     */
    @Override
    public int getThreadPoolChooserNumericId(String componentId) {
        //FIXME: This method is not used, but should be fixed once
        //ORB's nio select starts working and we start using ThreadPoolChooser
        //This will be mostly used by the ORB
        return 0;
    }

    @Override
    public void close() {
        //TODO
    }
}


