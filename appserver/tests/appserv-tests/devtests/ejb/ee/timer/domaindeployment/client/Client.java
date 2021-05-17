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
public class Client extends AdminBaseDevTest {

    public static final String INSTANCE1_NAME = "in1";
    public static final String INSTANCE2_NAME = "in2";
    public static final String XA_RESOURCE = "jdbc/mypool";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare(args[1]);
        } else if ("deploy".equals(args[0])) {
            (new Client()).deploy(args[1], args[2]);
        } else if ("add-app-ref".equals(args[0])) {
            (new Client()).addAppRef(args[1], args[2]);
        } else if ("clean".equals(args[0])) {
            (new Client()).clean(args[1]);
        } else if ("redeploy".equals(args[0])) {
            (new Client()).redeploy(args[1]);
        } else if ("undeploy".equals(args[0])) {
            (new Client()).undeploy(args[1], args[2]);
        } else if ("verify".equals(args[0])) {
            (new Client()).verify(args[1], args[2], args[0]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for automatic timers deployed to a target \"domain\"";
    }

    public void prepare(String cluster_name) {
        try {
            asadmin("create-cluster", cluster_name);
            asadmin("create-local-instance", "--cluster", cluster_name, cluster_name+INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", cluster_name, cluster_name+INSTANCE2_NAME);
            asadmin("start-cluster", cluster_name);
            asadmin("create-resource-ref", "--target", cluster_name, XA_RESOURCE);
            System.out.println("Started cluster.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deploy(String target, String path) {
        try {
            asadmin("deploy", "--target", target, path);
            System.out.println("Deployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redeploy(String path) {
        try {
            asadmin("deploy", "--force", "true", "--target", "domain", path);
            System.out.println("ReDeployed " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAppRef(String cluster_name, String name) {
        try {
            asadmin("create-application-ref", "--target", cluster_name, name);
            System.out.println("Added create-application-ref " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verify(String appname, String port, String operation) {
        stat.addDescription("ejb-ee-" + operation);

        String res = execute(appname, port, "RESULT:1");
        boolean success = "RESULT:1".equals(res);
        stat.addStatus("ejb-ee-" + operation, ((success)? stat.PASS : stat.FAIL));
        stat.printSummary("ejb-ee-" + operation);

    }

    public void undeploy(String target, String name) {
        try {
            asadmin("undeploy", "--target", target, name);
            System.out.println("Undeployed " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clean(String cluster_name) {
        try {
            asadmin("stop-cluster", cluster_name);
            asadmin("delete-local-instance", cluster_name+INSTANCE1_NAME);
            asadmin("delete-local-instance", cluster_name+INSTANCE2_NAME);
            asadmin("delete-cluster", cluster_name);
            System.out.println("Removed cluster " + cluster_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private String execute(String appname, String port, String expectedResult) {
        String connection = "http://localhost:" + port + "/" + appname + "/VerifyServlet";

        System.out.println("invoking webclient servlet at " + connection);
        String result=null;

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
                    result=line;
                    break;
                }
            }
          } catch (Exception e) {
              e.printStackTrace();
          }

          if (result != null) {
              System.out.println("FOUND " + result);
          } else {
              System.out.println("FAILURE");
          }

          return result;
    }

}
