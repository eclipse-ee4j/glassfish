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

import admin.AdminBaseDevTest;
import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.*;
import java.net.*;
import java.util.*;

public class NegClient extends AdminBaseDevTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static String expectedErr = "it does not declare a remote interface";

    public static void main(String[] args) {

        if ("deploy".equals(args[0])) {
            (new NegClient()).deploy(args[1]);
        } else if ("undeploy".equals(args[0])) {
            (new NegClient()).undeploy(args[1]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for failed deployment when wrong ejb-ref declared";
    }

    public void deploy(String path) {
        try {
            stat.addDescription("ejb-cli-negative-deploy");
            AsadminReturn ret = asadminWithOutput("deploy", path);
            if (!ret.returnValue && ret.err.contains(expectedErr)) {
                stat.addStatus("ejb-cli-negative-deploy", stat.PASS);
            } else {
                      stat.addStatus("ejb-cli-negative-deploy", stat.FAIL);
            }

            //System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stat.printSummary("ejb-cli-negative-deploy");
    }

    public void undeploy(String name) {
        try {
            asadmin("undeploy", name);
            System.out.println("Undeployed " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
