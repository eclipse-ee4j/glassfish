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

package com.sun.enterprise.universal.process;

import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.File;
import java.util.*;
import org.glassfish.api.admin.ServerEnvironment;

/**
 * Very simple -- run an Asadmin command.
 *
 * @author bnevins
 */
public final class LocalAdminCommand {

    public LocalAdminCommand(String command, String... args) {
        asadmin = new File(SystemPropertyConstants.getAsAdminScriptLocation());
        cmds.add(asadmin.getAbsolutePath());
        cmds.add(command);

        if (args != null && args.length > 0)
            cmds.addAll(Arrays.asList(args));
    }

    public final void waitForReaderThreads(boolean b) {
        waitForReaderThreads = b;
    }

    public int execute() throws ProcessManagerException {
        if (!asadmin.canExecute())
            throw new ProcessManagerException("asadmin is not executable!");

        pm = new ProcessManager(cmds);
        pm.waitForReaderThreads(waitForReaderThreads);
        pm.execute();  // blocks until command is complete
        return pm.getExitValue();
    }

    public ProcessManager getProcessManager() {
        return pm;
    }

    private final File asadmin;
    private final List<String> cmds = new ArrayList<String>();
    private ProcessManager pm;
    private boolean waitForReaderThreads = true;
}
