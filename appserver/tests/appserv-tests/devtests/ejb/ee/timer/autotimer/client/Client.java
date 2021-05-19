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

    public static final String CLUSTER_NAME = "c1";
    public static final String INSTANCE1_NAME = "in1";
    public static final String INSTANCE2_NAME = "in2";
    public static final String XA_RESOURCE = "jdbc/mypool";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare();
        } else if ("deploy".equals(args[0])) {
            (new Client()).deploy(args[1]);
        } else if ("clean".equals(args[0])) {
            (new Client()).clean();
        } else if ("undeploy".equals(args[0])) {
            (new Client()).undeploy(args[1]);
        } else if ("verify1".equals(args[0])) {
            (new Client()).verify(args[1], args[2], args[0]);
        } else if ("verify2".equals(args[0])) {
            (new Client()).verify(args[1], args[2], args[3], args[0]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for automatic timers";
    }

    public void prepare() {
        try {
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, INSTANCE2_NAME);
            asadmin("start-cluster", CLUSTER_NAME);
            //asadmin("set-log-level", "javax.enterprise.resource.jta=FINE");
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
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

    public void verify(String appname, String port, String operation) {
        stat.addDescription("ejb-ee-" + operation);

        String res = execute(appname, port, "RESULT:true");
        boolean success = "RESULT:true".equals(res);
        stat.addStatus("ejb-ee-" + operation, ((success)? stat.PASS : stat.FAIL));
        stat.printSummary("ejb-ee-" + operation);
    }

    public void verify(String appname, String port1, String port2, String operation) {
        stat.addDescription("ejb-ee-" + operation);

        String res1 = execute(appname, port1, "RESULT:");
        String res2 = execute(appname, port2, "RESULT:");

        boolean success1 = "RESULT:true".equals(res1);
        boolean success2 = "RESULT:true".equals(res2);
        boolean failure1 = "RESULT:false".equals(res1);
        boolean failure2 = "RESULT:false".equals(res2);

        boolean result = (success1 && failure2) || (success2 && failure1);
        stat.addStatus("ejb-ee-" + operation, ((result)? stat.PASS : stat.FAIL));
        stat.printSummary("ejb-ee-" + operation);

        File s = new File("success");
        File e = new File("error");
        try {
            if (s.exists())
                s.delete();

            if (e.exists())
                e.delete();

            s.createNewFile();
            e.createNewFile();
            PrintWriter p = new PrintWriter(s);
            p.print(((success1 && failure2)? port1 : ((success2 && failure1)? port2 : "NULL")));
            p.close();
            p = new PrintWriter(e);
            p.print(((success1 && failure2)? port2 : ((success2 && failure1)? port1 : "NULL")));
            p.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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

    public void clean() {
        try {
            asadmin("stop-cluster", CLUSTER_NAME);
            asadmin("delete-local-instance", INSTANCE1_NAME);
            asadmin("delete-local-instance", INSTANCE2_NAME);
            asadmin("delete-cluster", CLUSTER_NAME);
            System.out.println("Removed cluster");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private String execute(String appname, String port, String expectedResult) {
        String connection = "http://localhost:" + port + "/" + appname + "/VerifyServlet?" + port;

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
