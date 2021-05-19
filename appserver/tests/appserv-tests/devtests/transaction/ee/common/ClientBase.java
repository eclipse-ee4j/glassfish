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
public abstract class ClientBase extends AdminBaseDevTest {

    public static final String CLUSTER_NAME = "c1";
    public static final String INSTANCE1_NAME = "in1";
    public static final String INSTANCE2_NAME = "in2";
    public static final String INSTANCE3_NAME = "in3";
    public static final String DEF_RESOURCE = "jdbc/__default";
    public static final String XA_RESOURCE = "jdbc/xa";
    public static final String NONTX_RESOURCE = "jdbc/nontx";
    public static final String HTTP_LISTENER_PORT_SUFF = ".system-property.HTTP_LISTENER_PORT.value";

    protected static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public void process(String[] args) {

        if ("clean".equals(args[0])) {
            clean(args[1]);
        } else if ("insert_xa_data".equals(args[0])) {
            insert_xa_data(args[1], args[2]);
        } else if ("verify_xa".equals(args[0])) {
            verify_xa(args[1], args[2], args[3]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    public void insert_xa_data(String appname, String instance) {
        execute(appname, instance, "TestServlet?2", "true");
    }

    public void verify_xa(String appname, String instance, String operation) {
        verify(appname, instance, operation, "VerifyServlet?xa");
    }

    public void verify(String appname, String instance, String operation, String servlet) {
        stat.addDescription("transaction-ee-" + appname + "-" + operation);

        boolean res = execute(appname, instance, servlet, "RESULT:3");

        stat.addStatus("transaction-ee-" + appname + "-"  + operation, ((res)? stat.PASS : stat.FAIL));
        stat.printSummary("transaction-ee-"  + appname + "-" + operation);
    }

    public void prepare(String path, String tx_log_dir) {

    }

    public void clean(String name) {
        clean(name, 2);
    }

    public void clean(String name, int count) {
        try {
            if (name != null) {
                asadmin("undeploy", "--target", CLUSTER_NAME, name);
                System.out.println("Undeployed " + name);
            }

            //asadmin("stop-local-instance", INSTANCE1_NAME);
            //asadmin("stop-local-instance", INSTANCE2_NAME);
            asadmin("stop-cluster", CLUSTER_NAME);

            asadmin("delete-local-instance", INSTANCE1_NAME);
            asadmin("delete-local-instance", INSTANCE2_NAME);
            if (count == 3)
                asadmin("delete-local-instance", INSTANCE3_NAME);

            asadmin("delete-cluster", CLUSTER_NAME);
            asadmin("set-log-levels", "ShoalLogger=CONFIG");

            System.out.println("Removed cluster");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   protected boolean execute(String appname, String instance, String servlet, String expectedResult) {
        String connection = "http://localhost:" + getPort(instance) + "/" + appname + "/" + servlet;

        System.out.println("invoking webclient servlet at " + connection);
        boolean result=false;

        try {
            URL url = new URL(connection);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println("Processing line: " + line);
                if(line.indexOf(expectedResult)!=-1){
                    result=true;
                    break;
                }
            }
          } catch (Exception e) {
              e.printStackTrace();
          }

          if (result) {
              System.out.println("SUCCESS");
          } else {
              System.out.println("FAILURE");
          }

          return result;
    }

    public String getPort(String instance) {
        String arg = "servers.server." + instance + HTTP_LISTENER_PORT_SUFF;
        AsadminReturn result = asadminWithOutput("get", arg);
        System.out.println("Executed command: " + result.out);
        if (!result.returnValue && instance.equals("in1")) {
            arg = "configs.config." + CLUSTER_NAME + "-config" + HTTP_LISTENER_PORT_SUFF;
            result = asadminWithOutput("get", arg);
            System.out.println("Executed replacement command: " + result.out);
        }

        if (!result.returnValue) {
            System.out.println("CLI FAILED: " + result.err);
        } else {
            String[] parts = result.out.split("\n");
            for (String part : parts) {
                if (part.startsWith(arg)) {
                    String[] res = part.split("=");
                    System.out.println("Returning port: " + res[1]);

                    return res[1];
                }
            }
        }

        return null;
    }
}
