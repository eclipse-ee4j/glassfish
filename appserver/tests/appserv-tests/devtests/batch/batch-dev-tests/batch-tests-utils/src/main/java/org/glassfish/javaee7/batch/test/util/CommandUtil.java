/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee7.batch.test.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: makannan
 * Date: 4/5/13
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandUtil {

    private int exitCode;

    private Throwable cause;

    private List<String> result = new ArrayList<String>();

    private CommandUtil() {}

    public static CommandUtil getInstance() {
        return new CommandUtil();
    }

    public CommandUtil executeCommandAndGetAsList(String... command) {
        return executeCommandAndGetAsList(true, command);
    }

    public CommandUtil executeCommandAndGetAsList(boolean  withOutput, String... command) {
        try {
            if (withOutput) {
                System.out.println();
                for (String s : command) System.out.print(s + " ");
                System.out.println();
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    result.add(line);
                    if (withOutput)
                        System.out.println(line);
                }
            } finally {
                br.close();
            }

            exitCode = process.waitFor();

        } catch (Throwable ex) {
            cause = ex;
        }

        return this;
    }
    public CommandUtil executeCommandAndGetErrorOutput(String... command) {
        return executeCommandAndGetErrorOutput(true, command);
    }

    public CommandUtil executeCommandAndGetErrorOutput(boolean  withOutput, String... command) {
        try {
            if (withOutput) {
                System.out.println();
                for (String s : command) System.out.print(s + " ");
                System.out.println();
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    result.add(line);
                    if (withOutput)
                        System.out.println(line);
                }
            } finally {
                br.close();
            }

            exitCode = process.waitFor();

        } catch (Throwable ex) {
            cause = ex;
        }

        return this;
    }

    public boolean ranOK() {
        return cause == null && exitCode == 0;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Throwable getCause() {
        return cause;
    }

    public List<String> result() {
        return result;
    }
}
