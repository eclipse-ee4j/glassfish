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
 * Unit test for Issue 9549: incorrect range stats
 *
 */
public class WebTest {

    private static final String TEST_NAME = "monitor-http-service";
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
        stat.addDescription("Unit test for issue 9549");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
            stat.printSummary();
    }

    public void doTest() {
        try {
            int count503 = getCount("count503");
            System.out.println("count503 = " + count503);
            int count5xx = getCount("count5xx");
            System.out.println("count5xx = " + count5xx);
            int errorcount = getCount("errorcount");
            System.out.println("errorcount = " + errorcount);

            invokeURL("http://" + host + ":" + port + contextRoot + "/statuscode?code=503");

            int count503_2 = getCount("count503");
            System.out.println("count503_2 = " + count503_2);
            int count5xx_2 = getCount("count5xx");
            System.out.println("count5xx_2 = " + count5xx_2);
            int errorcount_2 = getCount("errorcount");
            System.out.println("errorcount_2 = " + errorcount_2);

            boolean ok = (count503 >= 0 && count503_2 == (count503 + 1)) &&
                    (count5xx >= 0 && count5xx_2 == (count5xx + 1)) &&
                    (errorcount >= 0 && errorcount_2 == (errorcount + 1));

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

    private int getCount(String monitorPath) throws Exception {
        String url = "http://" + adminHost + ":" + adminPort +
                "/monitoring/domain/server/http-service/" + instanceName +
                "/request/" + monitorPath;
        String resultStr = invokeURL(url);
        System.out.println("getCount: "+resultStr);
        RestResponse response = RestUtil.get(url);
        Map<String, Object> map = response.getResponse();

        return ((Long)((Map)((Map)((Map)((Map)map.get("data")).get("extraProperties")).get(
                "entity")).get(monitorPath)).get("count")).intValue();
    }


}
