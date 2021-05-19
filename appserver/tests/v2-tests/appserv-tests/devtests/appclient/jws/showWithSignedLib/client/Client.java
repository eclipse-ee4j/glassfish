/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import lib.LibClass;

import java.net.URLClassLoader;
import java.net.URL;



/**
 *
 * @author tjquinn
 */
public class Client {

    private Vector<String> otherArgs = new Vector<String>();

    private Map<String,String> optionValues = new HashMap<String,String>();

    /** Creates a new instance of ShowArgsClient */
    public Client() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int status = 1;
        try {
            new Client().run(args);
            status = 0;
        } catch (Throwable thr) {
            status = 1;
            throw new RuntimeException("Client detected the following error", thr);
        }

    }

    private void run(String[] args) throws FileNotFoundException, IOException {
        System.err.println("In Client");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            System.err.println("CL " + cl.toString());
            if (cl instanceof URLClassLoader) {
                System.err.println("  URLs for this loader:");
                for (URL url : ((URLClassLoader)cl).getURLs()) {
                    System.err.println("  " + url.toString());
                }
            }
            cl = cl.getParent();
        }
        final LibClass lc = new LibClass();
        System.out.println("Command line arguments:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println();
        prepareArguments(args);

        System.out.println("Command line arguments:");
        for (String arg : args) {
            System.out.println(arg);
        }

        System.out.flush();
    }


    private void prepareArguments(String[] args) throws IllegalArgumentException, FileNotFoundException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                optionValues.put(args[i].substring(1), args[++i]);
            } else {
                otherArgs.add(args[i]);
            }
        }

        System.out.println("Other arguments: " + otherArgs);

    }
}
