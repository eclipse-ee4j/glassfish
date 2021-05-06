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

package client;

import jakarta.ejb.*;

import java.util.Collection;
import java.util.List;

import ejb.Test;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.text.*;


public class AppClient {

    @EJB(name="ejb/Test")
    private static Test sb;
    private static String testSuiteID;
    private static SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");


    public static void main(String[] args) {
        System.out.println("args ->"+ args);
        System.out.println("args0 ->"+ args[0]);
        testSuiteID = args[0];
        AppClient test = new AppClient();
        status.addDescription("Testing  JPA packaging scenarious.."+testSuiteID);
        test.runTest();
        status.printSummary(testSuiteID);
    }


    public void runTest() {


        // Persist all entities
        String testInsert = sb.testInsert();
        System.out.println("Inserting Customer and Orders... " + testInsert);
        if("OK".equals(testInsert)) {
                status.addStatus(testSuiteID + "-InsertCustomer", status.PASS);
        } else {
                status.addStatus(testSuiteID + "-InsertCustomer", status.FAIL);
        }

        String verInsert = sb.verifyInsert();
        // Test query and navigation
        System.out.println("Verifying that all are inserted... " + verInsert);
        if("OK".equals(verInsert)) {
                status.addStatus(testSuiteID + "-VerifyCustomerInsert", status.PASS);
        } else {
                status.addStatus(testSuiteID + "-VerifyCustomerInsert", status.FAIL);
        }

        // Get a detached instance
        String c = "Joe Smith";
        String testDelete = sb.testDelete(c);

        // Remove all entities
        System.out.println("Removing all... " + testDelete);
        if("OK".equals(testDelete)) {
                status.addStatus(testSuiteID + "-DeleteCustomer", status.PASS);
        } else {
                status.addStatus(testSuiteID + "-DeleteCustomer", status.FAIL);
        }

        String verDelete = sb.verifyDelete();
        // Query the results
        System.out.println("Verifying that all are removed... " + verDelete);
        if("OK".equals(verDelete)) {
                status.addStatus(testSuiteID + "-VerifyDeleteCustomer", status.PASS);
        } else {
                status.addStatus(testSuiteID + "-VerifyDeleteCustomer", status.FAIL);
        }

    }
}
