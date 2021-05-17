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

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.universal.process.ProcessStreamDrainer;

/**
 * CLIProcessExecutor A simple process executor class that is used by CLI.
 *
 * @author jane.young@sun.com
 */
public class CLIProcessExecutor {

    public CLIProcessExecutor() {
        process = null;
    }

    /**
     * This method invokes the runtime exec
     *
     * @param cmd the command to execute
     * @param wait if true, wait for process to end.
     * @exception Exception
     */
    public void execute(String name, String[] cmd, boolean wait) throws Exception {
        process = Runtime.getRuntime().exec(cmd);
        ProcessStreamDrainer.redirect(name, process);

        try {
            if (wait) {
                process.waitFor();
            }
        } catch (InterruptedException ie) {
        }
    }

    /**
     * return the exit value of this process. if process is null, then there is no process running therefore the return
     * value is 0.
     */
    public int exitValue() {
        if (process == null)
            return -1;
        return process.exitValue();
    }

    private Process process;
}
