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

package org.glassfish.tests.filterURIMapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebTest {

    private static final int EXPECTED_COUNT = 1;

    private String contextPath = "test";

    @Test
    public void testWeb() throws Exception {
        goGet("localhost", 8080, "DESTROYED", contextPath+"/ServletTest;test=aaa");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath) throws Exception {
        boolean pass = false;
        URL servlet = new URL("http://localhost:8080/" + contextPath);
        HttpURLConnection connection = (HttpURLConnection) servlet.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Filter invoked")) {
                    pass = true;
                    break;
                }
            }
            assertTrue(pass);
        } finally {
            connection.disconnect();
        }
    }
}
