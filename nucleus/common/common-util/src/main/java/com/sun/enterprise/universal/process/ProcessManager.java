/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * ProcessManager.java
 * Use this class for painless process spawning.
 * This class was originally written to be compatible with JDK 1.4, using Runtime.exec(),
 * but has been refactored to use ProcessBuilder for better control and configurability.
 *
 * @since JDK 1.4
 * @author bnevins 2005
 */
@Deprecated
public final class ProcessManager {
    private static final Logger LOG = System.getLogger(ProcessManager.class.getName());

    private final ProcessBuilder builder;
    private final StringBuffer sb_out;
    private final StringBuffer sb_err;
    private int timeout;
    private boolean echo = true;
    private String[] stdinLines;
    private final List<Thread> threads = new ArrayList<>(2);
    private boolean waitForReaderThreads = true;

    public ProcessManager(String... cmds) {
        builder = new ProcessBuilder(cmds);
        sb_out = new StringBuffer();
        sb_err = new StringBuffer();
    }


    public ProcessManager(List<String> cmdline) {
        builder = new ProcessBuilder(cmdline);
        sb_out = new StringBuffer();
        sb_err = new StringBuffer();
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
            stdinLines = new String[list.size()];
            stdinLines = list.toArray(stdinLines);
        }
    }

    public void waitForReaderThreads(boolean b) {
        waitForReaderThreads = b;
    }


    /** Should the output of the process be echoed to stdout?
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
        try {
            readStream("stderr", process.getErrorStream(), sb_err);
            readStream("stdout", process.getInputStream(), sb_out);
            writeStdin(process);
            await(process);
            try {
                return process.exitValue();
            } catch (IllegalThreadStateException tse) {
                // this means that the process is still running...
                throw new ProcessManagerTimeoutException("Process is still running.", tse);
            }
        } catch (ProcessManagerException pme) {
            throw pme;
        } catch (Exception e) {
            throw new ProcessManagerException(e);
        } finally {
            destroy(process);
            // Always wait for reader threads -- unless the boolean flag is false.
            // note that this won't block when there was a timeout because the process
            // has been forcibly destroyed above.
            doWaitForReaderThreads();
        }
    }

    private void destroy(Process process) {
        if (process == null) {
            return;
        }
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
        return sb_out.toString();
    }


    public String getStderr() {
        return sb_err.toString();
    }


    @Override
    public String toString() {
        return builder.command().toString();
    }


    private void writeStdin(Process process) throws ProcessManagerException {
        if (stdinLines == null || stdinLines.length <= 0) {
            return;
        }
        if (process == null) {
            throw new ProcessManagerException("Parameter process was null.");
        }
        try (PrintWriter pipe = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())))) {
            for (String stdinLine : stdinLines) {
                LOG.log(Level.DEBUG, "InputLine -->" + stdinLine + "<--");
                pipe.println(stdinLine);
                pipe.flush();
            }
        } catch (Exception e) {
            throw new ProcessManagerException(e);
        }
    }


    private void readStream(String name, InputStream stream, StringBuffer sb) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        Thread thread = new Thread(new ReaderThread(reader, sb, echo), name);
        threads.add(thread);
        thread.start();
    }


    private void await(Process process) throws InterruptedException, ProcessManagerException {
        if (timeout <= 0) {
            waitForever(process);
        } else {
            waitAwhile(process);
        }
    }


    private void waitForever(Process process) throws InterruptedException, ProcessManagerException {
        if (process == null) {
            throw new ProcessManagerException("Parameter process was null.");
        }
        process.waitFor();
    }


    private void waitAwhile(Process process) throws InterruptedException, ProcessManagerException {
        if (process == null) {
            throw new ProcessManagerException("Parameter process was null.");
        }
        process.waitFor(timeout, TimeUnit.MILLISECONDS);
    }


    private void doWaitForReaderThreads() {
        if (waitForReaderThreads) {
            // wait for stdin and stderr to finish up
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    // nothing to do
                }
            }
        }
    }


    static class ReaderThread implements Runnable {
        private final BufferedReader reader;
        private final StringBuffer sb;
        private final boolean echo;

        ReaderThread(BufferedReader Reader, StringBuffer SB, boolean echo) {
            reader = Reader;
            sb = SB;
            this.echo = echo;
        }


        @Override
        public void run() {
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    sb.append(line).append('\n');

                    if (echo) {
                        System.out.println(line);
                    }
                }
            } catch (Exception e) {
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.log(Level.TRACE, "Failed to close BufferedReader", e);
                }
            }
            LOG.log(Level.TRACE, "ReaderThread exiting...");
        }
    }
}
