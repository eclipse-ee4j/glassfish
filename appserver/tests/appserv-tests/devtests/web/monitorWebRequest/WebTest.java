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
import java.util.Map;
import java.net.HttpURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.sun.ejte.ccl.reporter.*;

import org.apache.catalina.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.glassfish.admingui.common.util.RestResponse;
import org.glassfish.admingui.common.util.RestUtil;

/*
 * Unit test for Issue 9309: [monitoring] request-count is incorrect
 * Unit test for Issue 8984: errorcount-count statistics is missing
 *
 */
public class WebTest {

    private static final String TEST_NAME = "monitor-web-request";
    private static final String EXPECTED = "OK";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String adminHost;
    private String adminPort;
    private String adminUser;
    private String adminPassword;
    private String instanceName;
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        adminHost = args[0];
        adminPort = args[1];
        adminUser = args[2];
        adminPassword = args[3];
        instanceName = args[4];
        host = args[5];
        port = args[6];
        contextRoot = args[7];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for issue 9309");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
            stat.printSummary();
    }

    public void doTest() {
        try {
            int webReqCount1 = getCount("web/request/requestcount", "requestcount");
            System.out.println("web request count: " + webReqCount1);
            int appReqCount1 = getCount("applications" + contextRoot + "-web/" + instanceName + "/requestcount", "requestcount");
            System.out.println("app request count: " + appReqCount1);

            String testResult = invokeURL("http://" + host + ":" + port + contextRoot + "/test");
            System.out.println(testResult);

            int webReqCount2 = getCount("web/request/requestcount", "requestcount");
            System.out.println("web request count: " + webReqCount2);
            int appReqCount2 = getCount("applications" + contextRoot + "-web/" + instanceName + "/requestcount", "requestcount");
            System.out.println("app request count: " + appReqCount2);

            boolean ok1 = (EXPECTED.equals(testResult) &&
                    (webReqCount1 >= 0 && webReqCount2 == (webReqCount1 + 1)) &&
                    (appReqCount1 >= 0 && appReqCount2 == (appReqCount1 + 1)));


            int webErrorCount1 = getCount("web/request/errorcount", "errorcount");
            System.out.println("web error count: " + webErrorCount1);
            int appErrorCount1 = getCount("applications" + contextRoot + "-web/" + instanceName + "/errorcount", "errorcount");
            System.out.println("app error count: " + appErrorCount1);

            invokeURL("http://" + host + ":" + port + contextRoot + "/badrequest");

            int webErrorCount2 = getCount("web/request/errorcount", "errorcount");
            System.out.println("web error count: " + webErrorCount2);
            int appErrorCount2 = getCount("applications" + contextRoot + "-web/" + instanceName + "/errorcount", "errorcount");
            System.out.println("app error count: " + appErrorCount2);

            boolean ok2 = (webErrorCount1 >= 0 && webErrorCount2 == (webErrorCount1 + 1)) &&
                    (appErrorCount1 >= 0 && appErrorCount2 == (appErrorCount1 + 1));

            boolean ok = ok1 && ok2;
            stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    private String invokeURL(String urlString) throws Exception {

        StringBuilder sb = new StringBuilder();

        URL url = new URL(urlString);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("accept", "application/xml");
        if (adminPassword != null) {
            conn.setRequestProperty("Authorization", "Basic " +
                new String(Base64.encode((adminUser + ":" + adminPassword).getBytes())));
        }
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = null;
            BufferedReader reader = null;
            try {
                is = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch(IOException ex) {
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch(IOException ex) {
                    }
                }
            }
        } else {
            System.out.println("Get response code: " + responseCode);
        }

        return sb.toString();
    }

    private int getCount(String monitorPath, String countName) throws Exception {
        String url = "http://" + adminHost + ":" + adminPort +
                "/monitoring/domain/server/" + monitorPath;
        String resultStr = invokeURL(url);
        System.out.println("getCount: "+resultStr);
        RestResponse response = RestUtil.get(url);
        Map<String, Object> map = response.getResponse();

        return ((Long)((Map)((Map)((Map)((Map)map.get("data")).get("extraProperties")).get(
                "entity")).get(countName)).get("count")).intValue();
    }

}
