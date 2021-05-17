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
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 4925406 ("Add support for form-hint-field to determine
 * request encoding").
 *
 * In its sun-web.xml, the web module specifies a parameter-encoding element
 * with a form-hint-field attribute with value 'requestCharset'.
 *
 * Client appends to the request URI a query parameter named after the
 * form-hint-field. Query parameter specifies as its value the name of the
 * request charset, and would usually represent a hidden form field.
 *
 * Container is supposed to set the request encoding to the value of the query
 * parameter.
 *
 * JSP that is the target of the request retrieves the request encoding and
 * assigns it as the response encoding, which is checked by this client.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "form-hint-field";
    private static final String REQUEST_CHARSET = "GB18030";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 4925406");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeJsp();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/jsp/formHintField.jsp?requestCharset="
                          + REQUEST_CHARSET);
        System.out.println("Invoking URL: " + url.toString());

        URLConnection conn = url.openConnection();
        String contentType = conn.getContentType();
        System.out.println("Response Content-Type: " + contentType);
        if (contentType != null) {
            int index = contentType.indexOf("charset=" + REQUEST_CHARSET);
            if (index == -1) {
                throw new Exception("Missing or invalid response charset");
            }
        } else {
            throw new Exception("Missing Content-Type response header");
        }
    }
}
