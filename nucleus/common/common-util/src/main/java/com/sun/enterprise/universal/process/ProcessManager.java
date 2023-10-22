/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Use this class for painless process spawning.
 * <p>
 * This class was originally written to be compatible with JDK 1.4, using Runtime.exec(),
 * but has been refactored to use ProcessBuilder for better control and configurability.
 *
 * @since JDK 1.4
 * @author bnevins 2005
 */
public class ProcessManager {
    private static final Logger LOG = System.getLogger(ProcessManager.class.getName());

    protected final ProcessBuilder builder;
    private String stdout;
    private String stderr;
    private int timeout;
    private boolean echo = true;
    private String[] stdinLines;

    public ProcessManager(String... cmds) {
        builder = new ProcessBuilder(cmds);
    }


    public ProcessManager(List<String> cmdline) {
        builder = new ProcessBuilder(cmdline);
    }


    public void setTimeoutMsec(int num) {
        if (num > 0) {
            timeout = num;
        }
    }

    public void setEnvironment(String name, String value) {
        Map<String, String> env = builder.environment();
        env.put(name, value);
    }

    public void setWorkingDir(File directory) {
        builder.directory(directory);
    }

    public void setStdinLines(List<String> list) {
        if (list != null && !list.isEmpty()) {
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


    public int execute() throws ProcessManagerException {
        LOG.log(Level.DEBUG, "Executing command:\n  command={0}  \nenv={1}", builder.command(), builder.environment());
        final Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Could not execute command: " + builder.command(), e);
        }
        ReaderThread threadErr = new ReaderThread(process.getErrorStream(), echo, "stderr");
        threadErr.start();
        ReaderThread threadOut = new ReaderThread(process.getInputStream(), echo, "stdout");
        threadOut.start();
        try {
            try {
                writeStdin(process);
                return await(process);
            } finally {
                stderr = threadErr.finish(1000L);
                stdout = threadOut.finish(1000L);
            }
        } catch (ProcessManagerException pme) {
            throw pme;
        } catch (Exception e) {
            throw new ProcessManagerException(e);
        } finally {
            if (process.isAlive()) {
                destroy(process);
            }
        }
    }

    private void destroy(Process process) {
        process.destroy();
        // Wait for a while to let the process stop
        try {
            boolean exited = process.waitFor(10, TimeUnit.SECONDS);
            if (!exited) {
                // If the process hasn't exited, force it to stop
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Interrupted while waiting for process to terminate", e);
            Thread.currentThread().interrupt();
        }
    }

    public String getStdout() {
        return stdout;
    }


    public String getStderr() {
        return stderr;
    }


    @Override
    public String toString() {
        return builder.command().toString();
    }


    private void writeStdin(Process process) throws ProcessManagerException {
        if (stdinLines == null || stdinLines.length == 0) {
            return;
        }
        if (process == null) {
            throw new ProcessManagerException("Parameter process was null.");
        }
        try (PrintWriter pipe = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())))) {
            for (String stdinLine : stdinLines) {
                LOG.log(Level.DEBUG, "InputLine --> {0} <--", stdinLine);
                pipe.println(stdinLine);
                pipe.flush();
            }
        } catch (Exception e) {
            throw new ProcessManagerException(e);
        }
    }


    private int await(Process process) throws InterruptedException, ProcessManagerException {
        if (timeout > 0) {
            if (process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                return process.exitValue();
            }
            throw new ProcessManagerTimeoutException("Process is still running, timeout " + timeout + " ms exceeded.");
        }
        return process.waitFor();
    }


    static class ReaderThread extends Thread {
        private final BufferedReader reader;
        private final StringBuilder sb;
        private final boolean echo;
        private final AtomicBoolean stop = new AtomicBoolean();

        ReaderThread(InputStream stream, boolean echo, String threadName) {
            setName(threadName);
            this.reader = new BufferedReader(new InputStreamReader(stream));
            this.sb = new StringBuilder();
            this.echo = echo;
        }


        @Override
        public void run() {
            try {
                while (true) {
                    String line;
                    if (reader.ready()) {
                        line = reader.readLine();
                    } else if (stop.getAcquire()) {
                        break;
                    } else {
                        Thread.yield();
                        continue;
                    }
                    sb.append(line).append('\n');
                    if (echo) {
                        System.out.println(line);
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.ERROR, "ReaderThread broke ...", e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.log(Level.ERROR, "Failed to close BufferedReader", e);
                }
            }
            LOG.log(Level.TRACE, "ReaderThread exiting...");
        }


        /**
         * Asks the thread to finish it's job and waits until the thread dies.
         * <p>
         * @param timeout The maximal time for the waiting.
         *
         * @return the final output of the process.
         */
        public String finish(long timeout) {
            stop.setRelease(true);
            try {
                join(timeout);
            } catch (InterruptedException ex) {
                // nothing to do
            }
            return sb.toString();
        }
    }
}
