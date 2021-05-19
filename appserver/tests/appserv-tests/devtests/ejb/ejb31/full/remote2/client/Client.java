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

import jakarta.ejb.*;
import jakarta.annotation.*;

import javax.naming.InitialContext;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {

        appName = args[0];
        stat.addDescription(appName);
        Client client = new Client(args);
        client.doTest(args[1]);
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {
    }

    public void doTest(String otherApp) {

        try {

            Hello hGlobal =  (Hello) new InitialContext().lookup("java:global/HBGlobal");
            System.out.println("hGlobal = " + hGlobal);
            System.out.println("hGlobal hello = " + hGlobal.hello());

            SingletonRemote singleton = (SingletonRemote) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");

            // pass name of remotestateless app
            singleton.doTest(otherApp);

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            stat.addStatus("local main", stat.FAIL);
            e.printStackTrace();
        }

    }

}
