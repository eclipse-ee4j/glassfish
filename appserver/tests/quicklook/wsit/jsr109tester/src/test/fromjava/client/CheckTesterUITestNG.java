/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.client;

import org.testng.annotations.*;
import org.testng.Assert;

import java.lang.reflect.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Test very basic Webservice deployed using jsr109 deployment and tests that "?Tester" console page loads correctly
 *
 * @author miroslav
 */
public class CheckTesterUITestNG {

    public static final String CLASS_SERVICE = "fromjava.client.AddNumbersService";
    public static final String METHOD_GET_PORT = "getAddNumbersPort";
    public static final String METHOD_ADD_NUMBERS = "addNumbers";
    public static final String URL_TESTER_PAGE = "http://localhost:8080/JaxwsFromJava/AddNumbersService?Tester";
    public static final String HEADER_TESTER_PAGE = "AddNumbersService Web Service Tester";

    private Object port;
    private Method method;

    @BeforeTest
    public void loadClass() throws Exception {
        try {
            Class cls = Class.forName(CLASS_SERVICE);
            Constructor ct = cls.getConstructor();
            Object svc = ct.newInstance();
            Method getPort = cls.getMethod(METHOD_GET_PORT);
            port = getPort.invoke(svc, (Object[]) null);
        } catch (Exception ex) {
            System.out.println("Got ex, class is not loaded.");
            throw new Exception(ex);
        }
        System.out.println("done for init");
    }

    @Test(groups = { "functional" })
    public void testAddNumbers() throws Exception {
        int result = 0;
        try {
            for (Method m : port.getClass().getDeclaredMethods()) {
                System.out.println("method = " + m.getName());
            }
            method = port.getClass().getMethod(METHOD_ADD_NUMBERS, int.class, int.class);
            result = (Integer) method.invoke(port, 1, 2);
        } catch (Exception ex) {
            System.out.println("got unexpected exception.");
            throw new Exception(ex);
        }
        
        Assert.assertTrue(result == 3);
    }

    @Test(groups = { "functional" })
    public void testTesterUI() throws Exception {
        String testerPageHTML = wget(URL_TESTER_PAGE);
        System.out.println("Tester Page HTML = " + testerPageHTML);
        
        Assert.assertTrue(testerPageHTML.contains(HEADER_TESTER_PAGE));
    }

    public static String wget(String url) throws Exception {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
            StringBuilder response = new StringBuilder();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

}
