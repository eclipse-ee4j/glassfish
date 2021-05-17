/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.Map;
import java.util.Iterator;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author dochez
 */

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        stat.addDescription("webservices-filter-url-handling");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-filter-url-handling");
    }

    public void doTest(String[] args) {
        try {
            String webURL = args[0];
            URL url = new URL(args[0] + "//");
            System.out.println("Invoking " + url.toExternalForm());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            System.out.println("Response code " + connection.getResponseCode());
            if (HttpURLConnection.HTTP_NOT_FOUND == responseCode) {
                // passed
                System.out.println("Passed, page is not found");
                stat.addStatus("webservice filter url handling", stat.PASS);
                return;
            }
            if (HttpURLConnection.HTTP_INTERNAL_ERROR == responseCode) {
                // server not started
                System.out.println("Error, server is down ?");
                stat.addStatus("webservice filter url handling", stat.FAIL);
                return;
            }
            if (HttpURLConnection.HTTP_OK == responseCode) {
               // failed
                System.out.println("Got a page back, check it is a valid one...");
                int length = connection.getContentLength();
                System.out.println("Content-length " + length);
                if (length==-1) {
                    stat.addStatus("webservice filter url handling", stat.FAIL);
                } else {
                    stat.addStatus("webservice filter url handling", stat.PASS);
                }
                stat.addStatus("webservice filter url handling", stat.FAIL);
            } else {
                System.out.println("ERROR - Unknow return code " + responseCode);
                Map map = connection.getHeaderFields();
                for (Iterator itr=map.keySet().iterator();itr.hasNext();) {
                    String header = (String) itr.next();
                    System.out.println("Header " + header + "-->" + map.get(header));
                }
                stat.addStatus("webservice filter url handling", stat.FAIL);
            }
        } catch(Exception e) {
            System.out.println("Errror - exception " + e.getMessage());
            stat.addStatus("webservice filter url handling", stat.FAIL);
        }
    }
}
