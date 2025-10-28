/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package com.sun.enterprise.v3.services.impl.monitor.stats;

import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

/**
 * An object to hold thread pool statistics. Shared by ThreadPoolMonitor and stats providers (probe listeners),
 * as some stats should be preserved even if the listeners is down.
 * For example, thread counters, if there's no reliable way to find out the actual thread numbers from the thread pool (like busy threads).
 *
 * @author Ondro Mihalyi
 */
public class ThreadPoolStats {

    public ThreadPoolConfig threadPoolConfig;

    public long currentThreadCount;

    public long currentBusyThreadCount;

    public ThreadPoolStats(ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
    }

}
