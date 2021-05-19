/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * CLI Dev test
 * @author mvatkina
 */
public class Client extends ClientBase {

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare(args[1]);
        } else if ("create_timer".equals(args[0])) {
            (new Client()).create_timer(args[1], args[2]);
        } else {
            (new Client()).process(args);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for transaction CLIs";
    }

    public void prepare(String path) {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.delegated-recovery=true");
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);
            if (Boolean.getBoolean("enableShoalLogger")) {
                asadmin("set-log-levels", "ShoalLogger=FINER");
                asadmin("set-log-levels", "--target", CLUSTER_NAME, "ShoalLogger=FINER");
            }
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, DEF_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("start-cluster", CLUSTER_NAME);
            System.out.println("Started cluster. Setting up resources.");

            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create_timer(String appname, String port) {
        execute(appname, port, "TestServlet", "false");
    }

    public void verify(String appname, String instance, String operation, String servlet) {
        stat.addDescription("transaction-ee-" + operation);

        boolean res = execute(appname, instance, servlet, "RESULT:3+1");

        stat.addStatus("transaction-ee-timer-with-autorecovery" + operation, ((res)? stat.PASS : stat.FAIL));
        stat.printSummary("transaction-ee-timer-with-autorecovery" + operation);
    }

}
