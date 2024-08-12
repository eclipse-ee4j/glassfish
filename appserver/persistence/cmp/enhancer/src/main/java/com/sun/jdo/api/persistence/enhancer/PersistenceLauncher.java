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
 * PersistenceLauncher.java
 *
 * Created on July 3, 2000, 8:22 AM
 */

package com.sun.jdo.api.persistence.enhancer;

import com.sun.jdo.api.persistence.enhancer.util.Support;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Application launcher for persistence-capable classes.
 *
 * @author Martin Zaun
 * @version 1.1
 */
public class PersistenceLauncher {

    // chose whether to separate or join out and err channels
    // static private final PrintWriter err = new PrintWriter(System.err, true);
    static private final PrintWriter err = new PrintWriter(System.out, true);
    static private final PrintWriter out = new PrintWriter(System.out, true);
    static private final String prefix = "PersistenceLauncher.main() : ";// NOI18N

    /**
     * Creates new PersistenceLauncher.
     */
    private PersistenceLauncher() {
    }

    /**
     * Prints usage message.
     */
    static void usage() {
        out.flush();
        err.println("PersistenceLauncher:");
        err.println("    usage: <options> ... <target class name> <args> ...");
        err.println("    options:");
        err.println("           -h | --help");
        err.println("           -q | --quiet");
        err.println("           -w | --warn");
        err.println("           -d | --debug");
        err.println("           -t | --timing");
        err.println("    class names have to be fully qualified");
        err.println("done.");
        err.println();
        err.flush();
    }

    /**
     * Creates a class loader and launches a target class.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        // get launcher options
        String classpath = System.getProperty("java.class.path");
        boolean debug = false;
        boolean timing = false;
        String targetClassname = null;
        String[] targetClassArgs = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h")// NOI18N
                    || arg.equals("--help")) {// NOI18N
                usage();

                // exit gently
                return;
            }
            if (arg.equals("-t")// NOI18N
                    || arg.equals("--timing")) {// NOI18N
                timing = true;
                continue;
            }
            if (arg.equals("-d")// NOI18N
                    || arg.equals("--debug")) {// NOI18N
                debug = true;
                continue;
            }
            if (arg.equals("-w")// NOI18N
                    || arg.equals("--warn")) {// NOI18N
                debug = false;
                continue;
            }
            if (arg.equals("-q")// NOI18N
                    || arg.equals("--quiet")) {// NOI18N
                debug = false;
                continue;
            }

            // get target class name
            targetClassname = arg;

            // copy remaining arguments and leave loop
            i++;
            final int length = args.length - i;
            targetClassArgs = new String[length];
            System.arraycopy(args, i, targetClassArgs, 0, length);
            break;
        }

        // debugging oputput
        if (debug) {
            out.println(prefix + "...");// NOI18N
            out.println("settings and arguments:");// NOI18N
            out.println("    classpath = " + classpath);// NOI18N
            out.println("    debug = " + debug);// NOI18N
            out.println("    targetClassname = " + targetClassname);// NOI18N
            out.print("    targetClassArgs = { ");// NOI18N
            for (int i = 0; i < targetClassArgs.length; i++) {
                out.print(targetClassArgs[i] + " ");// NOI18N
            }
            out.println("}");// NOI18N
        }

        // check options
        if (targetClassname == null) {
            usage();
            throw new IllegalArgumentException("targetClassname == null");// NOI18N
        }

        // get class loader
        final ClassLoader loader;
        if (debug) {
            out.println(prefix + "using system class loader");// NOI18N
        }
        // out.println("using system class loader");
        loader = PersistenceLauncher.class.getClassLoader();

        // get target class' main method
        Class<?> clazz;
        Method main;
        try {
            String mname = "main";
            Class<?>[] mparams = new Class[] { String[].class };
            boolean init = true;
            if (debug) {
                out.println(prefix + "getting method " + targetClassname + "." + mname + "(String[])");
            }
            clazz = Class.forName(targetClassname, init, loader);
            main = clazz.getDeclaredMethod(mname, mparams);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        }

        // Invoke target class' main method
        try {
            Object[] margs = new Object[] { targetClassArgs };
            if (debug) {
                out.println("invoking method " + clazz.getName() + "." + main.getName() + "(String[])");
            }
            main.invoke(null, margs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        } finally {
            if (timing) {
                Support.timer.print();
            }
        }

        if (debug) {
            out.println(prefix + "done.");// NOI18N
            out.flush();
            err.flush();
        }
    }
}
