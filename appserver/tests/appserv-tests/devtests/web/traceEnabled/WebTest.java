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

import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 * - 6185574 ("[8.1 PE] Disabling TRACE returns wrong response code
 *   and does not include Allow response header")
 *
 * - 6182013 ("[8.1 EE] HTTP spec violation: response does not
 *   include any "Allow" header if TRACE disabled")
 */
public class WebTest{

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
                                                            "appserv-tests");

    public static void main(String args[]) {

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try {
            stat.addDescription("Trace not allowed test");

            URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/ServletTest");
            System.out.println("Invoking url: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("TRACE");
            try {
                conn.getInputStream().close();
            } catch (IOException ex) {
                // Do nothing: If TRACE is disabled, we get IOException
                // here if response body is empty
            }

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_BAD_METHOD){
                stat.addStatus("traceEnabled", stat.FAIL);
            } else {
                String allowHeader = conn.getHeaderField("Allow");
                System.out.println("Allow response header: " + allowHeader);
                if (allowHeader != null && !allowHeader.toUpperCase().contains("GET")) {
                    stat.addStatus("traceEnabled", stat.PASS);
                } else {
                    stat.addStatus("traceEnabled", stat.FAIL);
                }
            }

            stat.printSummary("web/traceEnabled");

        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("traceEnabled", stat.FAIL);
        }
    }
}
