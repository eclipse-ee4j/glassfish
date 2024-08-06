/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.test.example;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.grizzly.config.ConfigAwareElement;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Custom {@link ExecutorService} implementation.
 *
 * @author Alexey Stashok
 */
public class CustomExecutorService implements ExecutorService, ConfigAwareElement<ThreadPool> {
    private ExecutorService internalExecutorService;


    @Override
    public void configure(ServiceLocator locator, NetworkListener networkListener,
            ThreadPool configuration) {
        internalExecutorService = new ThreadPoolExecutor(
                toInt(configuration.getMinThreadPoolSize()),
                toInt(configuration.getMaxThreadPoolSize()),
                toInt(configuration.getIdleThreadTimeoutSeconds()),
                TimeUnit.SECONDS,
                toInt(configuration.getMaxQueueSize()) >= 0 ?
                new LinkedBlockingQueue<Runnable>(toInt(configuration.getMaxQueueSize())) :
                new LinkedTransferQueue<Runnable>());

    }

    @Override
    public void shutdown() {
        internalExecutorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return internalExecutorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return internalExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return internalExecutorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return internalExecutorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return internalExecutorService.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return internalExecutorService.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return internalExecutorService.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return internalExecutorService.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return internalExecutorService.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return internalExecutorService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return internalExecutorService.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        internalExecutorService.execute(command);
    }

    private static int toInt(String s) {
        return Integer.parseInt(s);
    }
}
