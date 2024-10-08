/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.spi.util;

import java.util.Timer;

import org.glassfish.api.invocation.InvocationManager;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ContainerUtil {

    /**
     * Utility method to get hold of InvocationManager
     * @return InvocationManager
     */
    public InvocationManager getInvocationManager();

    /**
     * Utility method to return ComponentEnvManager
     * @return ComponentEnvManager
     */
    public ComponentEnvManager getComponentEnvManager();

    public CallFlowAgent getCallFlowAgent();

    /**
     * Utility method to return a JDK Timer. Containers must
     *  use this timer instead of creating their own
     * @return Timer
     */
    public Timer getTimer();

    /**
     * Utility method to schedule an asynchronous task. The
     *  implementation will prbaby choose a worker thread
     *  from a threadpool and execute the given runnable
     *  using the thread.
     *
     * @param runnable
     */
    public void scheduleTask(Runnable runnable);

}
