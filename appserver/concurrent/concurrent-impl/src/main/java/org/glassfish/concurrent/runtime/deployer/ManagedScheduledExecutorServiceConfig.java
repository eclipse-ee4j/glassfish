/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime.deployer;

import org.glassfish.concurrent.config.ManagedScheduledExecutorService;

/**
 * Contains configuration information for a ManagedScheduledExecutorService object
 */
public class ManagedScheduledExecutorServiceConfig extends BaseConfig {

    private int hungAfterSeconds;
    private boolean longRunningTasks;
    private int threadPriority;
    private int corePoolSize;
    private long keepAliveSeconds;
    private long threadLifeTimeSeconds;

    public ManagedScheduledExecutorServiceConfig(ManagedScheduledExecutorService config) {
        super(config.getJndiName(), config.getContextInfo(), config.getContextInfoEnabled());
        hungAfterSeconds = parseInt(config.getHungAfterSeconds(), 0);
        longRunningTasks = Boolean.valueOf(config.getLongRunningTasks());
        threadPriority = parseInt(config.getThreadPriority(), Thread.NORM_PRIORITY);
        corePoolSize = parseInt(config.getCorePoolSize(), 0);
        keepAliveSeconds = parseLong(config.getKeepAliveSeconds(), 60);
        threadLifeTimeSeconds = parseLong(config.getThreadLifetimeSeconds(), 0L);
    }

    public int getHungAfterSeconds() {
        return hungAfterSeconds;
    }

    public boolean isLongRunningTasks() {
        return longRunningTasks;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public long getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public long getThreadLifeTimeSeconds() {
        return threadLifeTimeSeconds;
    }

    @Override
    public TYPE getType() {
        return TYPE.MANAGED_SCHEDULED_EXECUTOR_SERVICE;
    }
}
