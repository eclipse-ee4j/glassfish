/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for issue GLASSFISH-16058:
 * Deviation from servlet3 spec in ServletContext implementation (getResourcePaths()).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-3.0-multi-web-fragments-resources";
    private static final String PREFIX_1 = "getResourcePaths:/=";
    private static final String PREFIX_2 = "getResourcePaths:/catalog/=";
    private static final String PREFIX_3 = "getResourcePaths:/catalog=";
    private static final String EXPECTED_RESULT_1 = "/index.jsp,/catalog/,/catalog2/,/WEB-INF/,/customer/,/META-INF/";
    private static final String EXPECTED_RESULT_2 = "/catalog/offers/,/catalog/moreOffers/,/catalog/products.html,/catalog/index.html,/catalog/moreOffers2/,/catalog/another.html";


    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for multi web fragments resources");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        String url = "http://" + host + ":" + port + contextRoot + "/index.jsp";
        System.out.println(url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = null;
            BufferedReader input = null;
            Set<String> result1 = null;
            Set<String> result2 = null;
            Set<String> result3 = null;
            String line = null;
            try {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith(PREFIX_1)) {
                        result1 = parseResult(line, PREFIX_1);
                    } else if (line.startsWith(PREFIX_2)) {
                        result2 = parseResult(line, PREFIX_2);
                    } else if (line.startsWith(PREFIX_3)) {
                        result3 = parseResult(line, PREFIX_3);
                    }
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }

            Set<String> expectedResult1 = parseResult(EXPECTED_RESULT_1, "");
            Set<String> expectedResult2 = parseResult(EXPECTED_RESULT_2, "");
            boolean status1 = expect(PREFIX_1, result1, expectedResult1);
            boolean status2 = expect(PREFIX_2, result2, expectedResult2);
            boolean status3 = expect(PREFIX_3, result3, expectedResult2);

            stat.addStatus(TEST_NAME,
                    ((status1 && status2 && status3) ? stat.PASS : stat.FAIL));
        }
    }

    private Set<String> parseResult(String line, String prefix) {
        return new HashSet<String>(Arrays.asList(line.substring(prefix.length()).split(",")));
    }

    private boolean expect(String prefix, Set<String> result, Set<String> expectedResult) {
        boolean status = expectedResult.equals(result);

        if (!status) {
            System.out.println(prefix + ": Wrong response. Expected: " + expectedResult +", received: " + result);
        }

        return status;
    }
}
