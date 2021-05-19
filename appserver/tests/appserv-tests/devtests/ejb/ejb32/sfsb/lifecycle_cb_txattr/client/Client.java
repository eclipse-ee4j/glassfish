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

import javax.management.j2ee.ManagementHome;
import javax.management.j2ee.Management;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {

        appName = args[0];
        stat.addDescription(appName);
        Client client = new Client(args);
        client.doTest();
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {

        try {

            Hello bean = (Hello) new InitialContext().lookup("java:global/" + appName + "/SFSB!com.acme.Hello");
            System.out.println("SFSB test : " + bean.test());

            Hello1 bean1 = (Hello1) new InitialContext().lookup("java:global/" + appName + "/SFSB1!com.acme.Hello1");
            System.out.println("SFSB1 test : " + bean1.test());

            Hello2 bean2 = (Hello2) new InitialContext().lookup("java:global/" + appName + "/SFSB2!com.acme.Hello2");
            System.out.println("SFSB2 test : " + bean2.test());

            stat.addStatus("local main", stat.PASS);

        } catch (Exception e) {
            stat.addStatus("local main", stat.FAIL);
            e.printStackTrace();
        }
    }

}
