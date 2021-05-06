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

            // Ensure that MEJB is registered under all three of its JNDI names
            System.out.println("Looking up MEJB Homes");
            ManagementHome mh1Obj = (ManagementHome) new InitialContext().lookup("ejb/mgmt/MEJB");
            ManagementHome mh2Obj = (ManagementHome) new InitialContext().lookup("java:global/mejb/MEJBBean");
            ManagementHome mh3Obj = (ManagementHome) new InitialContext().lookup("java:global/mejb/MEJBBean!javax.management.j2ee.ManagementHome");

            System.out.println("mejb home obj 1 = " + mh1Obj);
            System.out.println("mejb home obj 2 = " + mh2Obj);
            System.out.println("mejb home obj 3 = " + mh3Obj);

            Hello hello = (Hello) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");


            System.out.println("Singleton says : " + hello.hello());
            hello.async();

            try {
                hello.test_Err_or("foo", "bar");
                throw new RuntimeException("Expected EJBException");
            } catch(EJBException e) {
                System.out.println("Got expected EJBException after java.lang.Error thrown from ejb");
            }

             stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            stat.addStatus("local main", stat.FAIL);
            e.printStackTrace();
        }
    }


}
