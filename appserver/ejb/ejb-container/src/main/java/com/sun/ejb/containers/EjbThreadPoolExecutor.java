/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.transaction.Status;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class EjbThreadPoolExecutor extends ThreadPoolExecutor {
    public EjbThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
            long keepAliveTime, BlockingQueue<Runnable> workQueue, String threadPoolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue,
                new ThreadFactoryImpl(threadPoolName));
    }

    /**
     * Ensure that we give out our EjbFutureTask as opposed to JDK's FutureTask
     * @param callable
     * @return a RunnableFuture
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        if (callable instanceof EjbAsyncTask) {
            return ((EjbAsyncTask) callable).getFutureTask();
        }
        return super.newTaskFor(callable);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EjbThreadPoolExecutor with ");
        sb.append(RuntimeTagNames.THREAD_CORE_POOL_SIZE).append(" ").append(getCorePoolSize()).append(" ");
        sb.append(RuntimeTagNames.THREAD_MAX_POOL_SIZE).append(" ").append(getMaximumPoolSize()).append(" ");
        sb.append(RuntimeTagNames.THREAD_KEEP_ALIVE_SECONDS).append(" ").append(getKeepAliveTime(TimeUnit.SECONDS)).append(" ");
        sb.append(RuntimeTagNames.THREAD_QUEUE_CAPACITY).append(" ").append(getQueue().remainingCapacity()).append(" ");
        sb.append(RuntimeTagNames.ALLOW_CORE_THREAD_TIMEOUT).append(" ").append(allowsCoreThreadTimeOut()).append(" ");
        return sb.toString();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        try {
            JavaEETransactionManager tm = EjbContainerUtilImpl.getInstance().getTransactionManager();
            if (tm.getTransaction() != null) {
                int st = tm.getStatus();
                Logger logger = EjbContainerUtilImpl.getLogger();
                logger.warning("NON-NULL TX IN AFTER_EXECUTE. TX STATUS: " + st);
                if (st == Status.STATUS_ROLLEDBACK || st == Status.STATUS_COMMITTED ||
                        st == Status.STATUS_UNKNOWN) {
                    tm.clearThreadTx();
                } else {
                    tm.rollback();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ThreadFactoryImpl implements ThreadFactory {
        private AtomicInteger threadId = new AtomicInteger(0);
        private String threadPoolName;

        public ThreadFactoryImpl(String threadPoolName) {
            this.threadPoolName = threadPoolName;
        }

        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, threadPoolName + threadId.incrementAndGet());
            th.setDaemon(true);
            th.setContextClassLoader(null); //Prevent any app classloader being set as CCL
            return th;
        }
    }
}
