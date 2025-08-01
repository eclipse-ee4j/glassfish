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

package org.glassfish.main.itest.tools.asadmin;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessManagerTimeoutException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_ROOT;

/**
 * Tool for executing startserv command.
 * The tool is stateless.
 *
 * @author Ondro Mihalyi
 */
public class StartServ {
    private static final Logger LOG = Logger.getLogger(StartServ.class.getName());

    private static final int DEFAULT_TIMEOUT_MSEC = 30 * 1000;

    private final File startServ;
    private final Map<String, String> environment = new HashMap<>();
    private String textToWaitFor;

    /**
     * Creates a stateless instance of the tool.
     *
     * @param startServ - executable file
     */
    public StartServ(final File startServ) {
        this.startServ = startServ;
    }


    /**
     * Adds environment property set for the asadmin execution.
     *
     * @param name
     * @param value
     * @return this
     */
    public StartServ withEnv(final String name, final String value) {
        this.environment.put(name, value);
        return this;
    }

    /**
     * Wait for this text in stdout or stderr and exit if found, or until timeout. Don't wait for the process to stop
     *
     * @param textToWaitFor
     * @return this
     */
    public StartServ withTextToWaitFor(final String textToWaitFor) {
        this.textToWaitFor = textToWaitFor;
        return this;
    }

    /**
     * Do not wait for any text in stdout or stderr, wait for the process to stop or until timeout
     *
     * @return this
     */
    public StartServ withNoTextToWaitFor() {
        this.textToWaitFor = null;
        return this;
    }

    /**
     * @return asadmin command file name
     */
    public String getCommandName() {
        return startServ.getName();
    }

    /**
     * Executes the command with arguments, waits until its standard output contains given text.
     * Doesn't terminate the command, the command should either stop later or should be killed later.
     *
     * @param args command and arguments.
     * @return {@link AsadminResult} never null. OK if text found in output, error if not found and command terminates or timeout reached.
     */
    public AsadminResult exec(final String... args) {
        return StartServ.this.exec(DEFAULT_TIMEOUT_MSEC, args);
    }

    /**
     * Executes the command with arguments.
     *
     * @param timeout timeout in millis
     * @param args command and arguments.
     * @return {@link AsadminResult} never null.
     */
    public AsadminResult exec(final int timeout, final String... args) {
        final List<String> parameters = Arrays.asList(args);
        LOG.log(Level.INFO, "exec(script={0}, timeout={1}, args={2})",
            new Object[] {startServ.getName(), timeout, parameters});
        final List<String> command = new ArrayList<>();
        command.add(startServ.getAbsolutePath());
        command.addAll(parameters);

        final ProcessManager processManager = new ProcessManager(command);
        processManager.setTimeout(timeout, false);
        processManager.setEcho(false);
        processManager.setTextToWaitFor(textToWaitFor);
        for (Entry<String, String> env : this.environment.entrySet()) {
            processManager.setEnvironment(env.getKey(), env.getValue());
        }
        if (System.getenv("AS_TRACE") == null && LOG.isLoggable(Level.FINEST)) {
            processManager.setEnvironment("AS_TRACE", "true");
        }
        // override any env property to what is used by tests
        processManager.setEnvironment(JAVA_HOME.getEnvName(), System.getProperty(JAVA_HOME.getSystemPropertyName()));
        processManager.setEnvironment(JAVA_ROOT.getEnvName(), System.getProperty(JAVA_HOME.getSystemPropertyName()));

        int exitCode;
        String asadminErrorMessage = "";
        try {
            exitCode = processManager.execute();
        } catch (final ProcessManagerTimeoutException e) {
            asadminErrorMessage = e.getMessage();
            exitCode = 1;
        } catch (final ProcessManagerException e) {
            LOG.log(Level.SEVERE, "The execution failed.", e);
            asadminErrorMessage = e.getMessage();
            exitCode = 1;
        }

        final String stdErr = processManager.getStderr() + '\n' + asadminErrorMessage;
        final AsadminResult result = new AsadminResult(getCommandName(), exitCode, processManager.getStdout(), stdErr);
        if (!result.getStdOut().isEmpty()) {
            System.out.println(result.getStdOut());
        }
        if (!result.getStdErr().isEmpty()) {
            System.err.println(result.getStdErr());
        }
        return result;
    }


    @Override
    public String toString() {
        return this.startServ.toString();
    }

}
