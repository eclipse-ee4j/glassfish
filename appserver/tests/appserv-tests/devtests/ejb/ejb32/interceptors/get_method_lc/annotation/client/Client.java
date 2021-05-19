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

package com.acme;

import java.io.*;
import java.util.*;
import javax.naming.*;
import jakarta.ejb.EJB;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

         private static SimpleReporterAdapter stat =
                  new SimpleReporterAdapter("appserv-tests");

        private static String appName;

    public static void main (String[] args) {

        appName = args[0];

        stat.addDescription(appName);
        Client client = new Client(args);
           client.doTest();
        stat.printSummary(appName + "ID");
    }

    public Client (String[] args) {
    }

    private void doTest() {
        try {
            Snglt b = (Snglt) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");
            System.out.println("test : " + b.hello());
            stat.addStatus(appName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(appName, stat.FAIL);
            e.printStackTrace();
        }

    }
}

