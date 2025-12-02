/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.dirListing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebTest {

    private static int count = 0;
    private static final int EXPECTED_COUNT = 1;

    private String contextPath = "test";

    @Test
    public void testWeb() throws Exception {
        goGet("localhost", 8080, "", contextPath+"/");
    }


    private static void goGet(String host, int port, String result, String contextPath) throws Exception {
        URL servlet = new URL("http://localhost:8080/test");
        HttpURLConnection connection = (HttpURLConnection) servlet.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = null;
            int lineNum = 0;
            while ((line = in.readLine()) != null) {
                System.out.println(lineNum + ": " + line);
                if (line.indexOf("Directory Listing") != -1) {
                    System.out.println("Getting a \"Directory Listing\"");
                    count++;
                    break;
                }
                lineNum++;
            }
            assertEquals(EXPECTED_COUNT, count);
        } finally {
            connection.disconnect();
        }
    }
}
