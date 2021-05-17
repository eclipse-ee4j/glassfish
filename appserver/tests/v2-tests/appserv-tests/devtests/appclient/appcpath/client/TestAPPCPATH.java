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

package com.sun.s1asdev.appclient.appcpath.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.appclient.appcpath.lib.LibUtil;
import java.net.URL;
import javax.sound.midi.SysexMessage;

/**
 *
 * @author tjquinn
 */
public class TestAPPCPATH {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    /**
     * Creates a new instance of TestAPPCPATH
     */
    public TestAPPCPATH() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.err.println(TestAPPCPATH.class.getClassLoader().getClass().getName());
        int status = 1;
        try {
            stat.addDescription("Testing APPCPATH");
            new TestAPPCPATH().run(args);
            status = 0;
        } catch (Throwable thr) {
            System.err.println("Client detected the following error:");
            thr.printStackTrace();
        } finally {
            stat.printSummary("APPCPATH");
            System.exit(status);
        }

    }

    private void run(String[] args) throws Throwable {
        /*
         *Use a class in the other jar file.  The APPCPATH env. var.
         *should point to this jar file as the client is run.
         */
        System.err.println("Attempting to instantiate LibUtil...");
        boolean passExpected = (args[0].equalsIgnoreCase("pass"));
        /*
         *args[0] will be either PASS or FAIL, depending on the expected outcome.
         */
        try {
            LibUtil lu = new LibUtil();
            stat.addStatus("APPCPATH test", passExpected ? stat.PASS : stat.FAIL);
        } catch (Throwable thr) {
            stat.addStatus("APPCPATH test", passExpected ? stat.FAIL : stat.PASS);
            throw thr;
        }
}

}
