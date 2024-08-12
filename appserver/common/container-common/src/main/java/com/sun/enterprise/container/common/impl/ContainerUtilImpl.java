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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.util.CallFlowAgent;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.ContainerUtil;

import jakarta.inject.Inject;

import java.util.Timer;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.PreDestroy;
import org.jvnet.hk2.annotations.Service;

@Service
public class ContainerUtilImpl
    implements ContainerUtil, PreDestroy {

    @Inject
    private InvocationManager invMgr;

    @Inject
    private ComponentEnvManager compEnvMgr;

    @Inject
    private CallFlowAgent callFlowAgent;

    private static Timer _timer = new Timer("container-util",true);

    private static ContainerUtil _util;

    public ContainerUtilImpl() {
        _initializeContainerUtilImpl(this);
    }

    private static void _initializeContainerUtilImpl(ContainerUtilImpl me) {
        _util = me;
    }

    public static ContainerUtil getContainerUtil() {
        return _util;
    }

    public InvocationManager getInvocationManager() {
        return invMgr;
    }

    public ComponentEnvManager getComponentEnvManager() {
        return compEnvMgr;
    }

    public CallFlowAgent getCallFlowAgent() {
        return callFlowAgent;
    }
    public Timer getTimer() {
        return _timer;
    }

    public void scheduleTask(Runnable runnable) {
        //TODO: Get hold of a worker threadpool and run this runnable
        //TODO: Should we take the ContextClassLoader as parameter
    }

    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
        _timer.cancel();
    }
}
