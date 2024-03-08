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

/*
 * ProcessStreamDrainer.java
 *
 * Created on October 26, 2006, 9:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.appserv.test.util.process;

/**
 * If you don't drain a process' stdout and stderr it will cause a deadlock after a few hundred bytes of output.
 * At that point the Process is blocked because its stdout and/or stderr buffer is full and it is waiting for the Java caller
 * to drain it.  Meanwhile the Java program is blocked waiting on the external process.
 * This class makes this common, but messy and tricky, procedure easier.
 * It creates 2 threads that drain output on stdout and stderr of the external process.
 * <p> Sample Code:
 *
 * <pre>
 * ProcessBuilder pb = new ProcessBuilder("ls",  "-R", "c:/as");
 * try
 * {
 *      Process p = pb.start();
 *      ProcessStreamDrainer psd = ProcessStreamDrainer.drain("MyProcess", p);
 *      // or
 *      ProcessStreamDrainer psd = ProcessStreamDrainer.redirect("MyProcess", p);
 *      psd.waitFor(); // this is optional.
 * }
 * catch (Exception ex)
 * {
 *      ex.printStackTrace();
 * }
 * </pre>
 *
 * @author bnevins
 */
public class ProcessStreamDrainer
{
    /**
     * Create an instance and drain the process' stderr and stdout
     * @param process The Process to drain
     * @param processName The name will be used to name the drainer threads
     */
    public static ProcessStreamDrainer drain(String processName, Process process)
    {
        ProcessStreamDrainer psd = new ProcessStreamDrainer(processName, process, false, false);
        psd.drain();
        return psd;
    }

    /**
     * Create an instance and drain the process' stderr and stdout and save it to
     * strings.
     * @param process The Process to drain
     * @param processName The name will be used to name the drainer threads
     */
    public static ProcessStreamDrainer save(String processName, Process process)
    {
        ProcessStreamDrainer psd = new ProcessStreamDrainer(processName, process, false, true);
        psd.drain();
        return psd;
    }

    /**
     * Create an instance, drain and redirect the process' stderr and stdout to
     * System.err and System.out respectively.
     * @param process The Process to drain
     * @param processName The name will be used to name the drainer threads
     */
    public static ProcessStreamDrainer redirect(String processName, Process process)
    {
        ProcessStreamDrainer psd = new ProcessStreamDrainer(processName, process, true, false);
        psd.drain();
        return psd;
    }

    /**
     * Wait for the drain threads to die.  This is guaranteed to occur after the
     * external process dies.  Note that this may, of course, block indefinitely.
     */
    public final void waitFor() throws InterruptedException
    {
        errThread.join();
        outThread.join();
    }

    /* Gets the stdout that was collected into a String
     * @return an empty string if nothing is available
     */
    public final String getOutString() {
        return outWorker.getString();
    }

    /* Gets the stdout that was collected into a String
     * @return an empty string if nothing is available
     */
    public final String getErrString() {
        return errWorker.getString();
    }

    /* Concatenates the stdout and stderr output and returns it as a String
     * @return an empty string if nothing is available
     */
    public final String getOutErrString() {
        return outWorker.getString() + errWorker.getString();
    }

    ///////////////////////////////////////////////////////////////////////////

    private ProcessStreamDrainer(String processName, Process process, boolean redirect, boolean save)
    {
        if(process == null)
            throw new NullPointerException("Internal Error: null Process object");

        this.process = process;

        if(processName == null || processName.length() <= 0)
            this.processName = "UnknownProcessName";
        else
            this.processName = processName;

        redirectStandardStreams = redirect;

        ProcessStreamDrainerWorker worker;

        if(redirectStandardStreams)
            outWorker = new ProcessStreamDrainerWorker(process.getInputStream(), System.out, save);
        else
            outWorker = new ProcessStreamDrainerWorker(process.getInputStream(), null, save);

        outThread = new Thread(outWorker, processName + "-" + OUT_DRAINER);
        outThread.setDaemon(true);

        if(redirectStandardStreams)
            errWorker = new ProcessStreamDrainerWorker(process.getErrorStream(), System.err, save);
        else
            errWorker = new ProcessStreamDrainerWorker(process.getErrorStream(), null, save);

        errThread = new Thread(errWorker, processName + "-" + ERROR_DRAINER);
        errThread.setDaemon(true);
    }

    /**
     * Start the draining.
     * We start them here instead of the constructor so that "this" doesn't
     * leak out of the constructor.
     */
    private void drain()
    {
        outThread.start();
        errThread.start();
    }

    ///////////////////////////////////////////////////////////////////////////

    private final           Process                     process;
    private final           ProcessStreamDrainerWorker  outWorker;
    private final           ProcessStreamDrainerWorker  errWorker;
    private final           Thread                      errThread;
    private final           Thread                      outThread;
    private final           String                      processName;
    private final           boolean                     redirectStandardStreams;
    private final   static  String                      ERROR_DRAINER   = "StderrDrainer";
    private final   static  String                      OUT_DRAINER     = "StdoutDrainer";

    ///////////////////////////////////////////////////////////////////////////

}

