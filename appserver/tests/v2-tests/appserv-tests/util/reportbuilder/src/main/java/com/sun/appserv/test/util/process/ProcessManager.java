/*
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

/*
 * ProcessManager.java
 * Use this class for painless process spawning.
 * This class was specifically written to be compatable with 1.4
 * @since JDK 1.4
 * @author bnevins
 * Created on October 28, 2005, 10:08 PM
 */
package com.sun.appserv.test.util.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessManager {

    public ProcessManager(String... cmds) {
        cmdline = cmds;
    }

    ////////////////////////////////////////////////////////////////////////////
    public ProcessManager(List<String> Cmdline) {
        cmdline = new String[Cmdline.size()];
        cmdline = Cmdline.toArray(cmdline);
    }

    ////////////////////////////////////////////////////////////////////////////
    public final void setTimeoutMsec(int num) {
        if (num > 0) {
            timeout = num;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    public final void setStdinLines(List<String> list) {
        if (list != null && !list.isEmpty()) {
            stdinLines = new String[list.size()];
            stdinLines = list.toArray(cmdline);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /** Should the output of the process be echoed to stdout?
     *
     * @param newEcho
     */

    public final void setEcho(boolean newEcho) {
        echo = newEcho;
    }



    ////////////////////////////////////////////////////////////////////////////
    public final int execute() throws ProcessManagerException {
        try {
            sb_out = new StringBuffer();
            sb_err = new StringBuffer();

            Runtime rt = Runtime.getRuntime();
            process = rt.exec(cmdline);
            writeStdin();
            readStream("stderr", process.getErrorStream(), sb_err);
            readStream("stdout", process.getInputStream(), sb_out);
            await();

            try {
                exit = process.exitValue();
            } catch (IllegalThreadStateException tse) {
                // this means that the process is still running...
                process.destroy();
                throw new ProcessManagerTimeoutException(tse);
            }
            // wait for stdin and stderr to finish up
            for (Thread t : threads) {
                t.join();
            }
        } catch (ProcessManagerException pme) {
            throw pme;
        } catch (Exception e) {
            if (process != null) {
                process.destroy();
            }

            throw new ProcessManagerException(e);
        }

        return exit;
    }

    ////////////////////////////////////////////////////////////////////////////
    public final String getStdout() {
        return sb_out.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    public final String getStderr() {
        return sb_err.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    public final int getExitValue() {
        return exit;
    }



    ////////////////////////////////////////////////////////////////////////////
    public String toString() {
        return Arrays.toString(cmdline);
    }

    ////////////////////////////////////////////////////////////////////////////
    private void writeStdin() throws ProcessManagerException {
        if (stdinLines == null || stdinLines.length <= 0) {
            return;
        }

        PrintWriter pipe = null;

        try {
            pipe = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));
            for (String stdinLine : stdinLines) {
                debug("InputLine ->" + stdinLine + "<-");
                pipe.println(stdinLine);
            }
            pipe.flush();
        } catch (Exception e) {
            throw new ProcessManagerException(e);
        } finally {
            try {
                pipe.close();
            } catch (Throwable t) {
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void readStream(String name, InputStream stream, StringBuffer sb) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        Thread thread = new Thread(new ReaderThread(reader, sb, echo), name);
        threads.add(thread);
        thread.start();
    }

    ////////////////////////////////////////////////////////////////////////////
    private void await() throws InterruptedException {
        if (timeout <= 0) {
            waitForever();
        } else {
            waitAwhile();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void waitForever() throws InterruptedException {
        process.waitFor();
    }

    ////////////////////////////////////////////////////////////////////////////
    private void waitAwhile() throws InterruptedException {
        Thread processWaiter = new Thread(new TimeoutThread(process));
        processWaiter.start();
        processWaiter.join(timeout);
    }

    ////////////////////////////////////////////////////////////////////////////
    private static void debug(String s) {
        if (debugOn) {
            System.out.println(s);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        try {
            if (args.length <= 0) {
                System.out.println("Usage: ProcessManager cmd arg1 arg2 ... argn");
                System.exit(1);
            }

            List<String> cmds = new ArrayList<String>();
            cmds.addAll(Arrays.asList(args));

            ProcessManager pm = new ProcessManager(cmds);
            pm.execute();

            System.out.println("*********** STDOUT ***********\n" + pm.getStdout());
            System.out.println("*********** STDERR ***********\n" + pm.getStderr());
            System.out.println("*********** EXIT VALUE: " + pm.getExitValue());
        } catch (ProcessManagerException pme) {
            pme.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    private String[] cmdline;
    private StringBuffer sb_out;
    private StringBuffer sb_err;
    private int exit = -1;
    private int timeout;
    private Process process;
    private boolean echo = true;
    private static final boolean debugOn = false;
    private String[] stdinLines;
    private List<Thread> threads = new ArrayList<Thread>(2);

    ////////////////////////////////////////////////////////////////////////////
    static class ReaderThread implements Runnable {
        ReaderThread(BufferedReader Reader, StringBuffer SB, boolean echo) {
            reader = Reader;
            sb = SB;
            this.echo = echo;
        }

        public void run() {
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    sb.append(line).append('\n');

                    if(echo)
                        System.out.println(line);
                }
            } catch (Exception e) {
            }
            ProcessManager.debug("ReaderThread exiting...");
        }
        private BufferedReader reader;
        private StringBuffer sb;
        private boolean echo;
    }

    static class TimeoutThread implements Runnable {

        TimeoutThread(Process p) {
            process = p;
        }

        public void run() {
            try {
                process.waitFor();
            } catch (Exception e) {
            }
            ProcessManager.debug("TimeoutThread exiting...");
        }
        private Process process;
    }
}
