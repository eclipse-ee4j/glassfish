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

package org.glassfish.tests.queryString;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryStringTest {

    private static final int EXPECTED_COUNT = 1;

    @Test
    public void testWeb() throws Exception {
        String servletName = "ServletTest";
        String context = "test/" + servletName;
        String basicUrl = "http://localhost:8080/" + context;
        URL url = URI.create(basicUrl + "?url=" + servletName).toURL();
        System.out.println("Connecting " + url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        String contextPathInHtml = null;
        String redirectLocation = connection.getHeaderField("Location");
        assertEquals(basicUrl + "?TEST=PASS", redirectLocation);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println("[Server response]" + line);
                String pattern = "<a href=\"";
                int pos = line.indexOf(pattern);
                if (pos != -1) {
                    int pos2 = line.lastIndexOf("\">here</a>");
                    contextPathInHtml = line.substring(pos + pattern.length(), pos2);
                    break;
                }
            }
        } finally {
            connection.disconnect();
        }

        assertEquals(redirectLocation, contextPathInHtml);

        URL redirectedUrl = URI.create(redirectLocation).toURL();
        HttpURLConnection connection2 = (HttpURLConnection) redirectedUrl.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection2.getInputStream()))) {
            String line;
            int count = 0;
            while ((line = in.readLine()) != null) {
                int index = line.indexOf("TEST");
                System.out.println("[Redirect response]" + line);

                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index + 1);

                    if (status.equalsIgnoreCase("PASS")) {
                        count++;
                    } else {
                        break;
                    }
                }
            }
            assertEquals(EXPECTED_COUNT, count);
        }
    }
}
