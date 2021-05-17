/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for customizing complete list of session tracking cookie
 * properties via web.xml
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.0-non-duplicate-session-cookie-with-custom-cookie-name";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for CR: 7014654: Possible Duplicate session cookie when the session cookie name is configured.");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void run() throws Exception {

        String url = "http://" + host + ":" + port + contextRoot
                     + "/index.jsp";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }
        Map<String, List<String>> headers = conn.getHeaderFields();
        int result = startsWith(headers, "Set-Cookie", "MYJSESSIONID=")
            + startsWith(headers, "Set-cookie", "MYJSESSIONID=");
        if (result != 1) {
            throw new Exception("Set-cookie MYJSESSIONID " + result + " times");
        }
    }

    private static int startsWith(Map<String, List<String>> map, String name, String value) {
        int result = 0;
        if (map != null) {
            List<String> list = map.get(name);
            if (list != null) {
                for (String s : list) {
                    System.out.println(s);
                    if (s != null && s.startsWith(value)) {
                        result++;
                    }
                }
            }
        }
        return result;
    }
}
