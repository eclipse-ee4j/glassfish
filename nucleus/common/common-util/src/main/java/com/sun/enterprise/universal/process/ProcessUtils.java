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

package com.sun.enterprise.universal.process;

import java.io.*;
import java.lang.management.ManagementFactory;

import com.sun.enterprise.universal.io.*;
import com.sun.enterprise.util.*;
import java.util.*;

import static com.sun.enterprise.util.StringUtils.ok;

/**
 * Includes a somewhat kludgy way to get the pid for "me". Another casualty of
 * the JDK catering to the LEAST common denominator. Some obscure OS might not
 * have a pid! The name returned from the JMX method is like so: 12345
 *
 * @mycomputername where 12345 is the PID
 * @author bnevins
 */
public final class ProcessUtils {
    static final File jpsExe;
    static final String jpsName;
    static final File jstackExe;
    static final String jstackName;

    private ProcessUtils() {
        // all static class -- no instances allowed!!
    }

    // for informal testing.  Too difficult to make a unit test...
    public static void main(String[] args) {
        debug = true;
        for (String s : args) {
            String ret = killJvm(s);

            if (ret == null)
                ret = "SUCCESS!!";

            System.out.println(s + " ===> " + ret);
        }
    }

    /**
     * Look for <strong>name</strong> in the Path. If it is found and if it is
     * executable then return a File object pointing to it. Otherwise return nu
     *
     * @param name the name of the file with no path
     * @return the File object or null
     */
    public static File getExe(String name) {
        for (String path : paths) {
            File f = new File(path + "/" + name);

            if (f.canExecute()) {
                return SmartFile.sanitize(f);
            }
        }
        return null;
    }

    /**
     * Try and find the Process ID of "our" process.
     *
     * @return the process id or -1 if not known
     */
    public static int getPid() {
        return pid;
    }

    /**
     * Kill the process with the given Process ID.
     *
     * @param pid
     * @return a String if the process was not killed for any reason including
     * if it does not exist. Return null if it was killed.
     */
    public static String kill(int pid) {
        try {
            String pidString = Integer.toString(pid);
            ProcessManager pm = null;
            String cmdline;

            if (OS.isWindowsForSure()) {
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
                return Strings.get("ProcessUtils.killerror", cmdline,
                        pm.getStderr() + pm.getStdout(), "" + exitValue);
        }
        catch (ProcessManagerException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Kill the JVM with the given main classname. The classname can be
     * fully-qualified or just the classname (i.e. without the package name
     * prepended).
     *
     * @param pid
     * @return a String if the process was not killed for any reason including
     * if it does not exist. Return null if it was killed.
     */
    public static String killJvm(String classname) {
        List<Integer> pids = Jps.getPid(classname);
        StringBuilder sb = new StringBuilder();
        int numDead = 0;

        for (int p : pids) {
            String s = kill(p);
            if (s != null)
                sb.append(s).append('\n');
            else
                ++numDead;
        }
        String err = sb.toString();

        if (err.length() > 0 || numDead <= 0)
            return Strings.get("ProcessUtils.killjvmerror", err, numDead);
        return null;
    }

    /**
     * If we can determine it -- find out if the process that owns the given
     * process id is running.
     *
     * @param aPid
     * @return true if it's running, false if not and null if we don't know. I.e
     * the return value is a true tri-state Boolean.
     */
    public static Boolean isProcessRunning(int aPid) {
        try {
            if (OS.isWindowsForSure())
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
   private static boolean debug;

   private static boolean isProcessRunningWindows(int aPid) throws ProcessManagerException {
        String pidString = Integer.toString(aPid);
        ProcessManager pm = new ProcessManager("tasklist", "/NH", "/FI", "\"pid eq " + pidString + "\"");
        pm.setEcho(false);
        pm.execute();
        String out = pm.getStdout() + pm.getStderr();

        /* output is either
         (1)
         INFO: No tasks running with the specified criteria.
         (2)
         java.exe                    3760 Console                 0     64,192 K
         */

        if (debug) {
            System.out.println("------------   Output from tasklist   ----------");
            System.out.println(out);
            System.out.println("------------------------------------------------");
        }

        if (ok(out)) {
            // check for java.exe because tasklist or some other command might
            // be reusing the pid. This isn't a guarantee because some other
            // java process might be reusing the pid.
            if (out.indexOf("java.exe") >= 0 && out.indexOf(pidString) >= 0)
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

        if (OS.isWindows()) {
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

        if (OS.isWindows()) {
            jpsName = "jps.exe";
            jstackName = "jstack.exe";
        }
        else {
            jpsName = "jps";
            jstackName = "jstack";
        }

        // byron sez:
        // looks VERY messy here.  Please feel free to clean up.  I just don't
        // want to invest the time to do it right now.

        final String javaroot = System.getProperty("java.home");
        final String relpath = "/bin";
        final File fhere1 = new File(javaroot + relpath + "/" + jpsName);
        final File fhere2 = new File(javaroot + relpath + "/" + jstackName);
        File fthere1 = new File(javaroot + "/.." + relpath + "/" + jpsName);
        File fthere2 = new File(javaroot + "/.." + relpath + "/" + jstackName);

        if (fhere1.isFile()) {
            jpsExe = SmartFile.sanitize(fhere1);
        }
        else if (fthere1.isFile()) {
            jpsExe = SmartFile.sanitize(fthere1);
        }
        else {
            jpsExe = null;
        }
        if (fhere2.isFile()) {
            jstackExe = SmartFile.sanitize(fhere2);
        }
        else if (fthere2.isFile()) {
            jstackExe = SmartFile.sanitize(fthere2);
        }
        else {
            jstackExe = null;
        }
    }
}
