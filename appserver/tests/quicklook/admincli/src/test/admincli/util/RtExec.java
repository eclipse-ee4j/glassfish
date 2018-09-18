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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.admincli.util;

import java.io.*;

/**
 *
 * @author Administrator
 */
public class RtExec {
    private static boolean result=false;
    private static Process proc;
    private static Runtime rt = Runtime.getRuntime();
    private static int exitVal = 101;

    public static boolean execute(String cmd) throws IOException {
        try {
            System.out.println("RtExec.execute: " + cmd);
            proc = rt.exec(cmd);

            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // Flush out the OUTPUT and ERROR
            errorGobbler.start();
            outputGobbler.start();

            // Checking exit satus
            exitVal = proc.waitFor();
//            System.out.println("ExitValue: " + exitVal);
            if (exitVal == 0){
                result = true;
            } else {
                result = false;
                System.out.println(outputGobbler.getOutput());
                System.out.println(errorGobbler.getOutput());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }
}
