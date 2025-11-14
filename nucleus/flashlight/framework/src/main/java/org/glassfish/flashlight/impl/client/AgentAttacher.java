/*
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
package org.glassfish.flashlight.impl.client;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.flashlight.FlashlightLoggerInfo;


/**
 * created May 26, 2011
 *
 * @author Byron Nevins
 */
public final class AgentAttacher {

    private static final String AGENT_CLASSNAME = "org.glassfish.flashlight.agent.ProbeAgentMain";

    private static final Logger logger = FlashlightLoggerInfo.getLogger();

    public static Optional<Instrumentation> getInstrumentation() {
        try {
            Class agentMainClass = null;
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();

            try {
                agentMainClass = classLoader.loadClass(AGENT_CLASSNAME);
            } catch (Throwable t) {
                // need throwable, not Exception - it may throw an Error!
                // try one more time after attempting to attach.
                AgentAttacher.attachAgent();
                // might throw
                agentMainClass = classLoader.loadClass(AGENT_CLASSNAME);
            }

            Method mthd = agentMainClass.getMethod("getInstrumentation", null);
            return Optional.ofNullable((Instrumentation) mthd.invoke(null, null));
        } catch (Throwable throwable) {
            logger.log(Level.WARNING, "Error while getting Instrumentation object from ProbeAgentMain", throwable);
            return Optional.empty();
        }
    }

    public synchronized static boolean canAttach() {
        return canAttach;
    }

    public synchronized static boolean isAttached() {
        try {
            if (!canAttach) {
                return false;
            }

            return AgentAttacherInternal.isAttached();
        } catch (Throwable t) {
            return false;
        }
    }

    public synchronized static boolean attachAgent() {

        try {
            if (!canAttach) {
                return false;
            }

            return attachAgent(-1, "");
        } catch (Throwable t) {
            return false;
        }
    }

    public synchronized static boolean attachAgent(int pid, String options) {
        try {
            if (!canAttach) {
                return false;
            }

            return AgentAttacherInternal.attachAgent(pid, options);
        } catch (Throwable t) {
            return false;
        }
    }

    private final static boolean canAttach;

    static {
        boolean b = false;
        try {
            // this will cause a class not found error if tools.jar is missing
            // this is a distinct possibility in embedded mode.
            AgentAttacherInternal.isAttached();
            b = true;
        } catch (Throwable t) {
            b = false;
        }
        canAttach = b;
    }
}
