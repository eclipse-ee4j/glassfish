/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.itest.tools;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessManagerTimeoutException;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tool for executing jarsigner/jarsigner.exe commands.
 * The tool is stateless.
 */
public class JarSigner {

    private static final Logger LOG = System.getLogger(JarSigner.class.getName());

    private final File jarsigner;

    public JarSigner(File jarsigner) {
        this.jarsigner = jarsigner;
    }

    /**
     * Executes the command with arguments.
     *
     * @param args the arguments
     */
    public void exec(String... args) {
        final List<String> parameters = Arrays.asList(args);
        LOG.log(Level.INFO, "exec(args={0})", parameters);
        final List<String> command = new ArrayList<>();
        command.add(jarsigner.getAbsolutePath());
        command.addAll(parameters);

        final ProcessManager processManager = new ProcessManager(command);
        processManager.setTimeout(60_000);
        processManager.setEcho(true);

        int exitCode;
        String errorMessage = "";
        try {
            exitCode = processManager.execute();
        } catch (ProcessManagerTimeoutException e) {
            errorMessage = e.getMessage();
            exitCode = 1;
        } catch (ProcessManagerException e) {
            LOG.log(Level.ERROR, "The execution failed.", e);
            errorMessage = e.getMessage();
            exitCode = 1;
        }

        final String stdErr = processManager.getStderr() + "\n" + errorMessage;
        if (!processManager.getStdout().isEmpty()) {
            System.out.println(processManager.getStdout());
        }
        if (!processManager.getStderr().isEmpty()) {
            System.err.println(processManager.getStderr());
        }
        if (exitCode != 0) {
            throw new RuntimeException(stdErr);
        }
    }
}
