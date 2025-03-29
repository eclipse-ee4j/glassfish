/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.concurrent.runtime.deployer.cfg;

import com.sun.enterprise.deployment.types.ConcurrencyContextType;

import java.io.Serializable;
import java.util.Set;

import org.glassfish.concurrent.config.ManagedScheduledExecutorService;

import static org.glassfish.concurrent.runtime.deployer.cfg.CfgParser.parseContextInfo;
import static org.glassfish.concurrent.runtime.deployer.cfg.CfgParser.parseInt;
import static org.glassfish.concurrent.runtime.deployer.cfg.CfgParser.parseLong;

/**
 * Contains configuration information for a ManagedScheduledExecutorService object
 */
public class ManagedScheduledExecutorServiceCfg implements Serializable {

    private static final long serialVersionUID = -1935790471020746933L;

    private final ConcurrentServiceCfg serviceConfig;
    private final int hungAfterSeconds;
    private final boolean hungLoggerPrintOnce;
    private final long hungLoggerInitialDelaySeconds;
    private final long hungLoggerIntervalSeconds;
    private final boolean longRunningTasks;
    private final boolean useVirtualThreads;
    private final int threadPriority;
    private final int corePoolSize;
    private final long keepAliveSeconds;
    private final long threadLifeTimeSeconds;

    public ManagedScheduledExecutorServiceCfg(ManagedScheduledExecutorService config) {
        Set<ConcurrencyContextType> propagated = parseContextInfo(config.getContextInfo(), config.getContextInfoEnabled());
        serviceConfig = new ConcurrentServiceCfg(config.getJndiName(), propagated, config.getContext());
        hungAfterSeconds = parseInt(config.getHungAfterSeconds(), 0);
        hungLoggerPrintOnce = Boolean.parseBoolean(config.getHungLoggerPrintOnce());
        hungLoggerInitialDelaySeconds = parseLong(config.getHungLoggerInitialDelaySeconds(), 60L);
        hungLoggerIntervalSeconds = parseLong(config.getHungLoggerIntervalSeconds(), 60L);
        longRunningTasks = Boolean.parseBoolean(config.getLongRunningTasks());
        useVirtualThreads = Boolean.parseBoolean(config.getUseVirtualThreads());
        threadPriority = parseInt(config.getThreadPriority(), Thread.NORM_PRIORITY);
        corePoolSize = parseInt(config.getCorePoolSize(), 0);
        keepAliveSeconds = parseLong(config.getKeepAliveSeconds(), 60L);
        threadLifeTimeSeconds = parseLong(config.getThreadLifetimeSeconds(), 0L);
    }

    public ConcurrentServiceCfg getServiceConfig() {
        return this.serviceConfig;
    }


    public int getHungAfterSeconds() {
        return hungAfterSeconds;
    }


    public boolean isHungLoggerPrintOnce() {
        return hungLoggerPrintOnce;
    }


    public long getHungLoggerInitialDelaySeconds() {
        return hungLoggerInitialDelaySeconds;
    }


    public long getHungLoggerIntervalSeconds() {
        return hungLoggerIntervalSeconds;
    }


    public boolean isLongRunningTasks() {
        return longRunningTasks;
    }

    public boolean getUseVirtualThreads() {
        return useVirtualThreads;
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
}
