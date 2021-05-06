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

package com.sun.s1asdev.ejb.ejb30.ee.remote_sfsb.client;

import java.io.*;
import java.util.*;
import java.net.*;
import jakarta.ejb.EJB;
import javax.naming.InitialContext;

import com.sun.s1asdev.ejb.ejb30.ee.remote_sfsb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-ee-remote_sfsb");
        Client client = new Client(args);
        //lookupRemoteUsingJndi();
        client.doTest();
        stat.printSummary("ejb-ejb30-ee-remote_sfsbID");
    }

    public Client (String[] args) {
    }

    @EJB(name="ejb/SfulDriver", beanInterface=SfulDriver.class)
    private static SfulDriver driver;

    public void doTest() {

/*
        try {
            System.out.println("invoking stateless");
            String result = driver.sayHello();
            stat.addStatus("remote hello",
                "Hello".equals(result) ? stat.PASS : stat.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("remote hello" , stat.FAIL);
        }

        try {
            String result = driver.sayRemoteHello();
            stat.addStatus("remote remote_hello", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("remote remote_hello" , stat.FAIL);
        }
*/


        try {
          String url = "http://" + "localhost" + ":" + "4848" +
              "/ejb-ejb30-hello-session3/servlet";
          System.out.println("invoking webclient servlet at " + url);
          int code = invokeServlet(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("test complete");
    }


    private static void sleepFor(int seconds) {
        while (seconds-- > 0) {
            try { Thread.sleep(1000); } catch (Exception ex) {}
            System.out.println("Sleeping for 1 second. Still " + seconds + " seconds left...");
        }
    }

    private int invokeServlet(String url) throws Exception {

        URL u = new URL(url);

        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while((line = input.readLine()) != null)
            System.out.println(line);
        if(code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }

}
