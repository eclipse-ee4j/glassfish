/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=2507
 * ("NPE When stressing GlassFish and the Pipeline Queue is full")
 */
public class WebTest {

    private static int count = 0;
    private static int EXPECTED_COUNT = 1;

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");
    private static URLConnection conn = null;
    private static URL url;
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Queue full exception");
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try{
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + contextRoot + "/ServletTest");
            String originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
               HttpURLConnection urlConnection = (HttpURLConnection)conn;
               urlConnection.setDoOutput(true);
               int responseCode=  urlConnection.getResponseCode();
               System.out.println("Response code: " + responseCode + " Expected code: 503");
               if (responseCode != 503){
                    stat.addStatus("pipelineQueueFull", stat.FAIL);
               } else {
                    stat.addStatus("pipelineQueueFull", stat.PASS);
               }
            }
        } catch (SocketException e) {
            stat.addStatus("pipelineQueueFull", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("pipelineQueueFull", stat.FAIL);
        }

        stat.printSummary("web/pipelineQueueFull");
    }
}
