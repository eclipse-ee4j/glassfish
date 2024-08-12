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

package org.glassfish.tests.jmxUndeployEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WebTest {

    private String contextPath = "test";

    @BeforeAll
    public static void setup() throws IOException {
    }

    @Test
    public void testWeb() throws Exception {
        URL url = new URL("http://localhost:8080/"+contextPath+"/ServletTest");
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection urlConnection = (HttpURLConnection)conn;
            urlConnection.setDoOutput(true);
            DataOutputStream out =
                    new DataOutputStream(urlConnection.getOutputStream());
            out.writeByte(1);
            int responseCode=  urlConnection.getResponseCode();
            System.out.println("responseCode: " + responseCode);
            MatcherAssert.assertThat(urlConnection.getResponseCode(), CoreMatchers.is(404));
        }
   }

}
