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
 * Unit test for CR 4882996:
 * request.getAttribute("javax.servlet.error.request_uri") is not working ..
 *
 * The following response body lines must be returned in order for this unit
 * test to succeed:
 *
 *  /web-javax-servlet-error-request-uri-dynamic-resource/junk.jsp
 *  404
 *  /web-javax-servlet-error-request-uri-dynamic-resource/404handler.jsp
 *  http://<host>:<port>/web-javax-servlet-error-request-uri-dynamic-resource/404handler.jsp
 */
public class WebTest {

    private static final String TEST_NAME = "javax-servlet-error-request-uri-dynamic-resource";

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
        stat.addDescription("Unit test for 4882996");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    public void doTest() throws Exception {
 
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/junk.jsp HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        int i=0;
        while ((line = bis.readLine()) != null) {
            System.out.println(i++ + ": " + line);
            if (line.equals(contextRoot + "/junk.jsp")) {
                break;
            }
        }

        if(line != null){
            String status = bis.readLine();
            System.out.println("status: " + status);
            if(status != null && "404".equals(status)){

                String requestURI = bis.readLine();
                System.out.println("requestURI: " + requestURI);

                if(requestURI != null
                    && requestURI.equals(contextRoot + "/404handler.jsp")){

                    String requestURL = bis.readLine();
                    System.out.println("requestURL: " + requestURL);

                    if(requestURL.equals("http://" + host + ":" + port + contextRoot + "/404handler.jsp")
                        || requestURL.equals("http://" +  InetAddress.getLocalHost().getHostName() +  ":" + port + contextRoot + "/404handler.jsp")){
                        stat.addStatus(TEST_NAME, stat.PASS);
                        return;
                    }
                }
            }
        }
        stat.addStatus(TEST_NAME, stat.FAIL);
    }
}
