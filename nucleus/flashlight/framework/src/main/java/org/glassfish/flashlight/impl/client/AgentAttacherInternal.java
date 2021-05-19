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

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.util.logging.*;
import org.glassfish.flashlight.FlashlightLoggerInfo;
import static org.glassfish.flashlight.FlashlightLoggerInfo.*;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;

/**
 * created May 26, 2011
 * @author Byron Nevins
 */
final class AgentAttacherInternal {
    static boolean isAttached() {
        return isAttached;
    }

    static boolean attachAgent() {
        return attachAgent(-1, "");
    }

    static boolean attachAgent(int pid, String options) {
        try {
            if (isAttached)
                return true;

            if(pid < 0)
                pid = ProcessUtils.getPid();

            if (pid < 0) {
                logger.log(Level.WARNING, INVALID_PID);
                return false;
            }

            VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
            String ir = System.getProperty(INSTALL_ROOT_PROPERTY);
            File dir = new File(ir, "lib" + File.separator + "monitor");

            if (!dir.isDirectory()) {
                logger.log(Level.WARNING, MISSING_AGENT_JAR_DIR, dir);
                return false;
            }

            File agentJar = new File(dir, "flashlight-agent.jar");

            if (!agentJar.isFile()) {
                logger.log(Level.WARNING, MISSING_AGENT_JAR, dir);
                return false;
            }

            vm.loadAgent(SmartFile.sanitize(agentJar.getPath()), options);
            isAttached = true;
        }
        catch (Throwable t) {
            logger.log(Level.WARNING, ATTACH_AGENT_EXCEPTION, t.getMessage());
            isAttached = false;
        }

        return isAttached;
    }
    private static final Object syncOnMe = new Object();
    private static final Logger logger = FlashlightLoggerInfo.getLogger();
    private static boolean isAttached = false;
}
