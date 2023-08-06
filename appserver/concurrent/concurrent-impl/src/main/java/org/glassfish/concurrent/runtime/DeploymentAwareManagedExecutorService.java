/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Payara Foundation and/or its affiliates.
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

package org.glassfish.concurrent.runtime;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedExecutorServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedThreadFactoryImpl;
import org.glassfish.enterprise.concurrent.internal.ManagedFutureTask;

/**
 * A ManagedExecutorService that delays tasks submitted during deployment until the app is deployed.
 */
public class DeploymentAwareManagedExecutorService extends ManagedExecutorServiceImpl {

    Supplier<DeploymentContext> deploymentContextSupplier = null;

    public DeploymentAwareManagedExecutorService(String name, ManagedThreadFactoryImpl managedThreadFactory, long hungTaskThreshold, boolean longRunningTasks, int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit keepAliveTimeUnit, long threadLifeTime, ContextServiceImpl contextService, RejectPolicy rejectPolicy, BlockingQueue<Runnable> queue) {
        super(name, managedThreadFactory, hungTaskThreshold, longRunningTasks, corePoolSize, maxPoolSize, keepAliveTime, keepAliveTimeUnit, threadLifeTime, contextService, rejectPolicy, queue);
    }

    public DeploymentAwareManagedExecutorService(String name, ManagedThreadFactoryImpl managedThreadFactory, long hungTaskThreshold, boolean longRunningTasks, int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit keepAliveTimeUnit, long threadLifeTime, int queueCapacity, ContextServiceImpl contextService, RejectPolicy rejectPolicy) {
        super(name, managedThreadFactory, hungTaskThreshold, longRunningTasks, corePoolSize, maxPoolSize, keepAliveTime, keepAliveTimeUnit, threadLifeTime, queueCapacity, contextService, rejectPolicy);
    }

    public void setDeploymentContextSupplier(Supplier<DeploymentContext> deploymentContextSupplier) {
        this.deploymentContextSupplier = deploymentContextSupplier;
    }

    @Override
    protected void executeManagedFutureTask(ManagedFutureTask<?> task) {
        DeploymentContext deploymentContext = null;
        if (deploymentContextSupplier != null) {
            deploymentContext = deploymentContextSupplier.get();
        }
        if (deploymentContext != null) {
            AppLifecycleListener.TasksDelayedAfterDeployment delayedTasks = deploymentContext.getTransientAppMetaData(
                    AppLifecycleListener.TasksDelayedAfterDeployment.class.getName(),
                    AppLifecycleListener.TasksDelayedAfterDeployment.class);
            if (delayedTasks != null) {
                delayedTasks.add(this, task);
                // task is delayed to execute after deployment finishes, don't execute now
                return;
            }
        }
        super.executeManagedFutureTask(task); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    public void executeManagedFutureTaskImmediately(ManagedFutureTask<?> task) {
        super.executeManagedFutureTask(task);
    }


}
