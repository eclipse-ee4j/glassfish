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

package test;

import jakarta.ejb.*;
import jakarta.annotation.*;
import javax.naming.*;
import java.util.concurrent.*;
import java.util.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static String appName;
    private static int numOfInvocations = 50;
    private static int maxPoolSize = 32;
    private static String threadNamePrefix = "__ejb-thread-pool";

    public static void main(String args[]) throws Exception {
        appName = args[0];
        if(args.length >= 2) {
            try {
                numOfInvocations = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {  //ignore
            }
        }
        if(args.length >= 3) {
            try {
                maxPoolSize = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {  //ignore
            }
        }
        stat.addDescription(appName);
        Client client = new Client();
        client.doTest();
        stat.printSummary(appName + "ID");
    }

    public void doTest() throws Exception {
        boolean failed = false;
        InitialContext ic = new InitialContext();
        Hello helloBean = (Hello) ic.lookup("java:global/" + appName + "/HelloBean");

        List<Future<String>> results = new ArrayList<Future<String>>();
        List<String> acceptableThreadNames = new ArrayList<String>();

        for(int i = 1; i <= maxPoolSize; i++) {
            acceptableThreadNames.add(threadNamePrefix + i);
        }
        for(int i = 0; i < numOfInvocations; i++) {
            results.add(helloBean.getThreadNameId());
        }

        for(Future<String> f : results) {
            String s = f.get();
            String threadName = s.split(" ")[0];
            if(acceptableThreadNames.contains(threadName)) {
                System.out.println("Thread name is in range: " + s);
            } else {
                failed = true;
                System.out.println("Thread name is NOT in range: " + s);
            }
        }
        System.out.println("Number of results: " + results.size());
        System.out.println("All " + acceptableThreadNames.size() +
            " acceptable thread names: " + acceptableThreadNames);
        stat.addStatus(appName, (failed ? stat.FAIL: stat.PASS));
    }
}
