/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.webservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebTest {

    @Test
    public void testWeb() throws Exception {
        Assertions.assertTrue(goGet());
    }

    private static boolean goGet() throws Exception {
        URL servlet = URI.create("http://localhost:8080/test/SimpleWebServiceService?wsdl").toURL();
        HttpURLConnection uc = (HttpURLConnection) servlet.openConnection();
        try {
            System.out.println("\nURLConnection = " + uc + " : ");
            if (uc.getResponseCode() != 200) {
                throw new Exception("Servlet did not return 200 OK response code");
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()))) {
                String line = null;
                while ((line = in.readLine()) != null) {
                    System.err.println("RESPONSE LINE: " + line);
                    if (line.contains("SimpleWebServicePort")) {
                        return true;
                    }
                }
            }
            return false;
        } finally {
            uc.disconnect();
        }
    }
}
