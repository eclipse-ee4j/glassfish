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
            (new Client()).prepare(args[1], args[2], Boolean.valueOf(args[3]));
        } else if ("recover".equals(args[0])) {
            (new Client()).recover();
        } else {
            (new Client()).process(args);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for DBLogs/Base";
    }

    public void prepare(String path, String tx_log_dir, boolean enable_delegate) {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("create-system-properties", "--target", CLUSTER_NAME, "-Dcom.sun.appserv.transaction.nofdsync");
            asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.db-logging-resource=" + NONTX_RESOURCE);
            //asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.automatic-recovery=true");
            if (enable_delegate) {
                asadmin("set", "configs.config." + CLUSTER_NAME + "-config.transaction-service.property.delegated-recovery=true");
            }

            //asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            AsadminReturn result = asadminWithOutput("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            System.out.println("Executed command: " + result.out);
            if (!result.returnValue) {
                System.out.println("CLI FAILED: " + result.err);
            }
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);

            asadmin("create-resource-ref", "--target", CLUSTER_NAME, DEF_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, NONTX_RESOURCE);

            if (Boolean.getBoolean("enableShoalLogger")) {
                 asadmin("set-log-levels", "ShoalLogger=FINER");
                 asadmin("set-log-levels", "--target", CLUSTER_NAME, "ShoalLogger=FINER");
            }
            asadmin("set-log-levels", "--target", CLUSTER_NAME, "jakarta.enterprise.system.core.transaction=FINE");
            asadmin("start-cluster", CLUSTER_NAME);
            System.out.println("Started cluster. Setting up resources.");

            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recover() {
        System.out.println("Executing recover CLI");
        try {
            AsadminReturn result = null;
            result = asadminWithOutput("recover-transactions", "--target", INSTANCE2_NAME, INSTANCE1_NAME);
            System.out.println("Executed command: " + result.out);
            if (!result.returnValue) {
                System.out.println("CLI FAILED: " + result.err);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished recover CLI");
    }

}
