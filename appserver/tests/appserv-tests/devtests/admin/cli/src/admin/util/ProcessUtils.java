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

package admin.util;

import java.io.*;
import java.lang.management.ManagementFactory;

import java.util.*;

/**
 * Lifted verbatim from GlassFish common-util
 * @author Byron Nevins
 */
public final class ProcessUtils {
    private ProcessUtils() {
        // all static class -- no instances allowed!!
    }

    public static File getExe(String name) {
        for (String path : paths) {
            File f = new File(path + "/" + name);

            if (f.canExecute()) {
                return f.getAbsoluteFile();
            }
        }
        return null;
    }

    /**
     * Try and find the Process ID of "our" process.
     * @return the process id or -1 if not known
     */
    public static final int getPid() {
        return pid;
    }

    /**
     * Kill the process with the given Process ID.
     * @param pid
     * @return a String if the process was not killed for any reason including if it does not exist.
     *  Return null if it was killed.
     */
    public static String kill(int pid) {
        try {
            String pidString = Integer.toString(pid);
            ProcessManager pm = null;
            String cmdline;

            if (isWindows()) {
                pm = new ProcessManager("taskkill", "/F", "/T", "/pid", pidString);
                cmdline = "taskkill /F /T /pid " + pidString;
            }
            else {
                pm = new ProcessManager("kill", "-9", "" + pidString);
                cmdline = "kill -9 " + pidString;
            }

            pm.setEcho(false);
            pm.execute();
            int exitValue = pm.getExitValue();

            if (exitValue == 0)
                return null;
            else
                return "Error killing pid #" + pid;
        }
        catch (ProcessManagerException ex) {
            return ex.getMessage();
        }
    }

    /**
     * If we can determine it -- find out if the process that owns the given
     * process id is running.
     * @param aPid
     * @return true if it's running, false if not and null if we don't know.
     * I.e the return value is a true tri-state Boolean.
     */
    public static final Boolean isProcessRunning(int aPid) {
        try {
            if (isWindows())
                return isProcessRunningWindows(aPid);
            else
                return isProcessRunningUnix(aPid);
        }
        catch (Exception e) {
            return null;
        }
    }
    //////////////////////////////////////////////////////////////////////////
    //////////     all private below     /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    private static final int pid;
    private static final String[] paths;

    private static boolean isProcessRunningWindows(int aPid) throws ProcessManagerException {
        String pidString = Integer.toString(aPid);
        ProcessManager pm = new ProcessManager("tasklist", "/FI", "\"pid eq " + pidString + "\"");
        pm.setEcho(false);
        pm.execute();
        String out = pm.getStdout() + pm.getStderr();

        /* output is either
        (1)
        INFO: No tasks running with the specified criteria.
        (2)
        Image Name                   PID Session Name     Session#    Mem Usage
        ========================= ====== ================ ======== ============
        java.exe                    3760 Console                 0     64,192 K
         */

        if (debug) {
            System.out.println("------------   Output from tasklist   ----------");
            System.out.println(out);
            System.out.println("------------------------------------------------");
        }

        if (ok(out)) {
            if (out.indexOf("" + aPid) >= 0)
                return true;
            else
                return false;
        }

        throw new ProcessManagerException("unknown");
    }

    private static Boolean isProcessRunningUnix(int aPid) throws ProcessManagerException {
        ProcessManager pm = new ProcessManager("kill", "-0", "" + aPid);
        pm.setEcho(false);
        pm.execute();
        int retval = pm.getExitValue();
        return retval == 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    static {
        // variables named with 'temp' are here so that we can legally set the
        // 2 final variables above.

        int tempPid = -1;

        try {
            String pids = ManagementFactory.getRuntimeMXBean().getName();
            int index = -1;

            if (ok(pids) && (index = pids.indexOf('@')) >= 0) {
                tempPid = Integer.parseInt(pids.substring(0, index));
            }
        }
        catch (Exception e) {
            tempPid = -1;
        }
        // final assignment
        pid = tempPid;

        String tempPaths = null;

        if (isWindows()) {
            tempPaths = System.getenv("Path");

            if (!ok(tempPaths))
                tempPaths = System.getenv("PATH"); // give it a try
        }
        else {
            tempPaths = System.getenv("PATH");
        }

        if (ok(tempPaths))
            paths = tempPaths.split(File.pathSeparator);
        else
            paths = new String[0];
    }
    private static boolean debug;
    public static boolean isWindows() {
        String osname = System.getProperty("os.name");

        if(osname == null || osname.length() <= 0)
            return false;

        // case insensitive compare...
        osname        = osname.toLowerCase();

        if(osname.indexOf("windows") >= 0)
            return true;

        return false;

    }

    static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
}
