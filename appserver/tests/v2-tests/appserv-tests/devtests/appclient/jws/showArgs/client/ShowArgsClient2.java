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

package com.sun.s1asdev.deployment.appclient.jws.showArgs.client2;


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



/**
 *
 * @author tjquinn
 */
public class ShowArgsClient2 {

    private String outFileSpec = null;
    private PrintStream outStream = null;

    private String expectedArgsFileSpec = null;

    private Vector<String> otherArgs = new Vector<String>();

    private Map<String,String> optionValues = new HashMap<String,String>();

    /** Creates a new instance of ShowArgsClient */
    public ShowArgsClient2() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int status = 1;
        System.out.println("Just entered client2.main");
        try {
            new ShowArgsClient2().run(args);
            status = 0;
        } catch (Throwable thr) {
            status = 1;
            throw new RuntimeException("Client detected the following error", thr);
        }

    }

    private void run(String[] args) throws FileNotFoundException, IOException {
        System.err.println("Command line arguments from client #2:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println();
        prepareArguments(args);

        /*
         *Default is for all output to go to System.out, which will be a
         *trace file in the Java Web Start directory if Java Web Start tracing is on.
         */
        if (outStream == null) {
            outStream = System.out;
        }

        outStream.println("Command line arguments:");
        for (int i = 0; i < 25; i++) {
            outStream.println("This is a test line to use up some space: " + i);
        }
        for (String arg : args) {
            outStream.println(arg);
        }

//        /*
//         *Make sure the command line argument values for otherArgs agree with
//         *what is stored in the temporary file.
//         */
//        checkActualArgsVsExpected();
//

        outStream.flush();
    }

    private void checkActualArgsVsExpected() throws FileNotFoundException, IOException {
        File expectedArgsFile = new File(expectedArgsFileSpec);
        outStream.println("expected args file is " + expectedArgsFile.getAbsolutePath());
        BufferedReader rdr = new BufferedReader(new FileReader(expectedArgsFile));
        String delimiter = rdr.readLine();
        String expectedArgValues = rdr.readLine();
        rdr.close();

        StringBuilder otherArgsAsLine = new StringBuilder();
        for (String s : otherArgs) {
            if (otherArgsAsLine.length() > 0) {
                otherArgsAsLine.append(delimiter);
            }
            otherArgsAsLine.append(s);
        }

        if ( ! otherArgsAsLine.toString().equals(expectedArgValues)) {
            throw new IllegalArgumentException("Actual arguments were " + otherArgsAsLine.toString() + "; expected " + expectedArgValues);
        }
    }

    private void prepareArguments(String[] args) throws IllegalArgumentException, FileNotFoundException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                optionValues.put(args[i].substring(1), args[++i]);
            } else {
                otherArgs.add(args[i]);
            }
        }

        outFileSpec = optionValues.get("out");
        File outFile = null;
        if (outFileSpec != null) {
            outFile = new File(outFileSpec);
            outStream = new PrintStream(outFile);
        }
//        statusFileSpec = optionValues.get("statusFile");
//        expectedArgsFileSpec = optionValues.get("expectedArgsFile");

        System.err.println("out = " + outFileSpec);
        if (outFile != null) {
            System.err.println("     which is the file " + outFile.getAbsolutePath());
        }
//        System.err.println("statusFile = " + statusFileSpec);
//        System.err.println("expectedArgsFile = " + expectedArgsFileSpec);

        System.err.println("Other arguments: " + otherArgs);

//        if (outFileSpec == null || statusFileSpec == null || expectedArgsFileSpec == null) {
//            throw new IllegalArgumentException("At least one of -out, -statusFile, and -expectedArgsFile is missing and all are required");
//        }
    }
}
