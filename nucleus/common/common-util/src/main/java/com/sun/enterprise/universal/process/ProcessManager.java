/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;


/**
 * Extension of {@link ProcessBuilder} that provides additional functionality to manage input and
 * output of the process.
 *
 * @author bnevins 2005
 * @author Ondro Mihalyi
 * @author David Matejcek
 */
public final class ProcessManager {
    private static final Logger LOG = System.getLogger(ProcessManager.class.getName());

    private String[] stdinLines;
    private boolean echo = true;
    private String stdout;
    private String stderr;
    private String textToWaitFor;
    private int timeout;
    private boolean forceExit = true;

    private final ProcessBuilder builder;

    /**
     * Creates a new ProcessManager with the specified command line.
     *
     * @param cmds must not be null or empty.
     */
    public ProcessManager(String... cmds) {
        builder = new ProcessBuilder(cmds);
    }

    /**
     * Creates a new ProcessManager with the specified command line.
     *
     * @param cmdline must not be null or empty.
     */
    public ProcessManager(List<String> cmdline) {
        builder = new ProcessBuilder(cmdline);
    }

    @Override
    public String toString() {
        return builder.command().toString();
    }

    /**
     * Sets the timeout for the process execution or for the detected text in the output.
     * If the process does not finish within the specified timeout, it will be terminated.
     *
     * @param millis the timeout in milliseconds, must not be negative
     */
    public void setTimeout(int millis) {
        setTimeout(millis, true);
    }

    /**
     * Sets the timeout for the process execution or for the detected text in the output.
     * If the process does not finish within the specified timeout, it can be terminated automatically.
     *
     * @param millis the timeout in milliseconds, must not be negative
     * @param forceExit if true, the process will be forcibly terminated if it does not finish within the timeout
     */
    public void setTimeout(int millis, boolean forceExit) {
        if (millis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative: " + millis);
        }
        this.timeout = millis;
        this.forceExit = forceExit;
    }

    /**
     * Sets an environment variable for the process.
     *
     * @param name the name of the environment variable, must not be null
     * @param value the value of the environment variable, can be null
     */
    public void setEnvironment(String name, String value) {
        builder.environment().put(name, value);
    }

    /**
     * Sets the working directory for the process.
     *
     * @param directory the working directory, must not be null
     */
    public void setWorkingDir(File directory) {
        builder.directory(directory);
    }

    /**
     * Sets the input lines for the process.
     * The lines will be written to the process's standard input.
     *
     * @param list a list of strings to be written to the process's standard input,
     *            can be null or empty
     */
    public void setStdinLines(List<String> list) {
        if (list == null || list.isEmpty()) {
            stdinLines = null;
        } else {
            stdinLines = list.toArray(String[]::new);
        }
    }

    /**
     * Should the output of the process be echoed to stdout?
     *
     * @param newEcho
     */
    public void setEcho(boolean newEcho) {
        echo = newEcho;
    }

    /**
     * If not null, should wait until this text is found in standard output instead of waiting until
     * the process terminates
     *
     * @param textToWaitFor
     */
    public void setTextToWaitFor(String textToWaitFor) {
        this.textToWaitFor = textToWaitFor;
    }

    /**
     * Returns the standard output of the process. If the textToWaitFor was set, the output
     * will contain all lines read from the process until the textToWaitFor was found.
     *
     * @return the standard output of the process
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Returns the standard error output of the process. If the textToWaitFor was set, the output
     * will contain all lines read from the process until the textToWaitFor was found.
     *
     * @return the standard error output of the process
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Executes the command and waits for it to finish while reading its output.
     *
     * @return exit code. Can be overridden internally when we are waiting for a specific text in
     *         output and we succeeded despite the process failed or even did not finish.
     *         If we have found the output, we don't kill the process.
     * @throws ProcessManagerException
     */
    public int execute() throws ProcessManagerException {
        LOG.log(DEBUG, "Executing command:\n  command={0}  \nenv={1}", builder.command(), builder.environment());
        final Process process = startProcess();
        try {
            final boolean textDetected = listenProcess(process);
            return evaluateResult(process, timeout, textDetected);
        } finally {
            if (process.isAlive() && this.forceExit) {
                destroy(process);
            }
        }
    }

    private Process startProcess() throws ProcessManagerException {
        try {
            return builder.start();
        } catch (IOException e) {
            throw new ProcessManagerException("Could not execute command: " + builder.command(), e);
        }
    }

    private boolean listenProcess(final Process process) throws ProcessManagerException {
        boolean isDead = false;
        final ReaderThread threadErr = ReaderThread.start("stderr", process.getErrorStream(), echo, textToWaitFor);
        final ReaderThread threadOut = ReaderThread.start("stdout", process.getInputStream(), echo, textToWaitFor);
        try {
            if (stdinLines != null && stdinLines.length > 0) {
                writeStdin(process, stdinLines);
            }
            isDead = waitForDeath(process, timeout);
        } finally {
            stderr = threadErr.finish(100L, isDead);
            stdout = threadOut.finish(100L, isDead);
        }
        return threadOut.isTextFound() || threadErr.isTextFound();
    }

    private int evaluateResult(Process process, int timeoutInMillis, boolean textDetected)
        throws ProcessManagerException {
        if (textToWaitFor == null) {
            if (process.isAlive()) {
                throw new ProcessManagerTimeoutException(
                    "Process is still running, timeout " + timeoutInMillis + " ms exceeded.");
            }
            final int exitCode = process.exitValue();
            LOG.log(DEBUG, "Process finished with exit code {0}", exitCode);
            return exitCode;
        }
        if (textDetected) {
            LOG.log(DEBUG, "The process produced the expected text in the output: {0}", textToWaitFor);
            return 0;
        }
        if (process.isAlive()) {
            throw new ProcessManagerTimeoutException("Process did not produce the expected output " + textToWaitFor
                + ", timeout " + timeoutInMillis + " ms exceeded.");
        }
        final int exitCode = process.exitValue();
        throw new ProcessManagerException(
            "Process finished with exit code " + exitCode + ", but did not produce expected output: " + textToWaitFor);
    }

    private static void writeStdin(Process process, String[] stdinLines) throws ProcessManagerException {
        try (OutputStreamWriter pipe = new OutputStreamWriter(process.getOutputStream(), Charset.defaultCharset())) {
            for (String stdinLine : stdinLines) {
                LOG.log(DEBUG, "InputLine --> {0} <--", stdinLine);
                try {
                    pipe.append(stdinLine);
                    pipe.append('\n');
                    pipe.flush();
                } catch (IOException e) {
                    throw new ProcessManagerException(
                        "Could not write " + stdinLine + " to stdin of process: " + process, e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close the writer to STDIN: " + process, e);
        }
    }

    private static void destroy(Process process) {
        process.destroy();
        final boolean terminated = waitForDeath(process, 10_000);
        if (!terminated) {
            LOG.log(WARNING, "Process did not exit after waiting, attempting to forcibly destroy it");
            process.destroyForcibly();
        }
    }

    private static boolean waitForDeath(Process process, int timeoutInMillis) {
        try {
            if (timeoutInMillis > 0) {
                LOG.log(TRACE, "Started waiting for process death with timeout {0} ms", timeoutInMillis);
                return process.waitFor(timeoutInMillis, TimeUnit.MILLISECONDS);
            }
            process.waitFor();
        } catch (InterruptedException e) {
            // The ReaderThread can wake us up.
            LOG.log(TRACE, "Interrupted while waiting for the process death.", e);
        } finally {
            LOG.log(TRACE, "Finished waiting for process death: {0}", process);
        }
        return !process.isAlive();
    }


    private static class ReaderThread extends Thread {
        private final BufferedReader reader;
        private final StringBuilder output;
        private final boolean echo;
        private final Thread threadWaitingForProcess;
        private final String textToWaitFor;
        private volatile boolean textFound;

        private ReaderThread(String name, InputStream stream, boolean echo, String textToWaitFor) {
            setName(name);
            this.reader = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()));
            this.output = new StringBuilder();
            this.echo = echo;
            this.textToWaitFor = textToWaitFor;
            this.threadWaitingForProcess = Thread.currentThread();
        }

        boolean isTextFound() {
            return textFound;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (isInterrupted()) {
                        LOG.log(TRACE, "ReaderThread " + getName() + " was interrupted.");
                        return;
                    }
                    if (!reader.ready()) {
                        Thread.onSpinWait();
                        continue;
                    }
                    final String line = reader.readLine();
                    final boolean textDetected = processLine(line);
                    if (textDetected) {
                        textFound = true;
                        threadWaitingForProcess.interrupt();
                        return;
                    }
                }
            } catch (IOException e) {
                LOG.log(WARNING, () -> "ReaderThread " + getName() + " cannot read the process output.", e);
            } finally {
                LOG.log(TRACE, "ReaderThread " + getName() + " stopped.");
            }
        }

        /**
         * Asks the thread to finish it's job and waits until the thread dies.
         * <p>
         * @param timeoutInMillis The maximal time for the waiting.
         *
         * @return the final output of the process.
         */
        String finish(long timeoutInMillis, boolean isProcessDead) {
            interrupt();
            try {
                join(timeoutInMillis);
            } catch (InterruptedException ex) {
                LOG.log(TRACE, "Interrupted while waiting for " + getName() + " to finish", ex);
            }
            if (isProcessDead) {
                readRemainingOutput();
            }
            return output.toString();
        }

        void readRemainingOutput() {
            try {
                if (reader.lines().filter(this::processLine).findFirst().isPresent()) {
                    textFound = true;
                }
            } catch (UncheckedIOException e) {
                LOG.log(ERROR, "Failed to read remaining output in " + getName(), e);
            } finally  {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to close reader in " + getName(), e);
                }
            }
        }

        private boolean processLine(String line) {
            output.append(line).append('\n');
            if (echo) {
                System.out.println(line);
            }
            return textToWaitFor != null && line.contains(textToWaitFor);
        }

        static ReaderThread start(String name, InputStream errorStream, boolean echo, String textToWaitFor) {
            ReaderThread thread = new ReaderThread(name, errorStream, echo, textToWaitFor);
            thread.start();
            return thread;
        }
    }
}
