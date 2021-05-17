/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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
            (new Client()).prepare();
        } else if ("deploy".equals(args[0])) {
            (new Client()).deploy(args[1]);
        } else if ("undeploy".equals(args[0])) {
            (new Client()).undeploy(args[1]);
        } else {
            (new Client()).process(args);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for transaction recovery with MDBs";
    }

    public void prepare() {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);
            System.out.println("Creating JMS resources");
            asadmin("create-jms-resource", "--target", CLUSTER_NAME, "--restype", "jakarta.jms.QueueConnectionFactory", "jms/ejb_mdb_QCF");
            asadmin("create-jmsdest", "--target", CLUSTER_NAME, "--desttype", "ejb_mdb_Queue");
            asadmin("create-jms-resource", "--target", CLUSTER_NAME, "--restype", "jakarta.jms.Queue", "--property", "imqDestinationName=ejb_mdb_Queue", "jms/ejb_mdb_Queue");
            System.out.println("Finished creating JMS resources");

            if (Boolean.getBoolean("enableShoalLogger")) {
                asadmin("set-log-levels", "ShoalLogger=FINER");
                asadmin("set-log-levels", "--target", CLUSTER_NAME, "ShoalLogger=FINER");
            }
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, "jms/ejb_mdb_QCF");
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, "jms/ejb_mdb_Queue");
            asadmin("start-cluster", CLUSTER_NAME);
            System.out.println("Started cluster.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deploy(String path) {
        try {
            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verify(String appname, String instance, String operation, String servlet) {
        stat.addDescription("transaction-ee-" + operation);

        boolean res = execute(appname, instance, servlet, "RESULT:6");

        stat.addStatus("transaction-ee-mdb" + operation, ((res)? stat.PASS : stat.FAIL));
        stat.printSummary("transaction-ee-mdb" + operation);
    }

    @Override
    public void clean(String name) {
        try {
            asadmin("delete-jms-resource", "--target", CLUSTER_NAME, "jms/ejb_mdb_QCF");
            asadmin("delete-jms-resource", "--target", CLUSTER_NAME, "jms/ejb_mdb_Queue");
            asadmin("delete-jmsdest", "--target", CLUSTER_NAME, "ejb_mdb_Queue");
            System.out.println("Deleted JMS resources.");

            super.clean(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void undeploy(String name) {
        try {
            asadmin("undeploy", "--target", CLUSTER_NAME, name);
            System.out.println("Undeployed " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
