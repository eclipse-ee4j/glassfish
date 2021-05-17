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
 * Unit test for 4921329 ("default-charset" attribute of <parameter-encoding>
 * in sun-web.xml is ignored).
 *
 * This client invokes a JSP which retrieves the request charset, which must
 * correspond to the value of the default-charset attribute of the
 * parameter-encoding element in this web module's sun-web.xml.
 *
 * The JSP sets the response charset to be the same as the request charset.
 *
 * This client then checks to see if the response charset matches the value
 * of the default-charset attribute. The test fails if there is no match.
 */
public class WebTest {

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
        stat.addDescription("Unit test for 4921329");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary("default-charset");
    }

    public void doTest() {
        try {
            invokeJsp();
        } catch (Exception ex) {
            System.out.println("default-charset test failed.");
            stat.addStatus("default-charset", stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        System.out.println(("GET " + contextRoot + "/jsp/getRequestCharset.jsp"
            + " HTTP/1.0\n"));
        os.write(("GET " + contextRoot + "/jsp/getRequestCharset.jsp"
            + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        boolean success = false;

        int i = 0;
        while ((line = bis.readLine()) != null) {
            System.out.println(i++ + ": " + line);
            if (!line.toLowerCase().startsWith("content-type:")) {
                continue;
            }
            int index = line.indexOf("charset=GB18030");
            if (index != -1) {
                success = true;
            }
        }
        if (line == null)
            System.out.println("Request failed, no response");

        if (success) {
            stat.addStatus("default-charset", stat.PASS);
        } else {
            stat.addStatus("default-charset", stat.FAIL);
        }
    }
}
