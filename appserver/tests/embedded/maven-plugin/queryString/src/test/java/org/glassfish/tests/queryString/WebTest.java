/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.queryString;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WebTest {

    private static int count = 0;
    private static int EXPECTED_COUNT = 1;

    private String contextPath = "/test";

    @BeforeAll
    public static void setup() throws IOException {
    }

    @Test
    public void testWeb() throws Exception {
        goGet("localhost", 8080, "TEST", contextPath+"/ServletTest");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath) throws Exception {
        try {
            contextPath += "?url=" + contextPath;
            System.out.println("Connecting "+contextPath);
            URL servlet = new URL("http://localhost:8080/"+contextPath);
            //URL servlet = new URL("http://localhost:8080/test/ServletTest?TEST=PASS");
            URLConnection yc = servlet.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    yc.getInputStream()));

            String line = null;
            int index;
            while ((line = in.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println("[Server response]" + line);

                int pos = line.indexOf("Location");
                if (pos != -1){
                   contextPath = line.substring(pos + "Location:".length()).trim();
                   in.close();
                   break;
                }
            }

            servlet = new URL("http://localhost:8080/"+contextPath);
            yc = servlet.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            while ((line = in.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println("[Redirect response]" + line);

                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);

                    if (status.equalsIgnoreCase("PASS")){
                        count++;
                    } else {
                        break;
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
   }

}
