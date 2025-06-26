/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
import java.util.Properties;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebTest{


    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");

    private static int count = 0;
    private static int EXPECTED_COUNT = 3;

    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String trustStorePath = args[3];

        stat.addDescription("Testing Filter chain under SSL");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);
            HttpsURLConnection connection = doSSLHandshake(
                            "https://" + host  + ":" + port + "/", ssf);
            checkStatus(connection);

            connection = doSSLHandshake(
                "https://" + host  + ":" + port + "/" + contextRoot
                + "/ServletTest", ssf);

            parseResponse(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("ssl", stat.FAIL);
        }

        stat.printSummary("web/ssl ---> expect 4 PASS");
    }

    private static SSLSocketFactory getSSLSocketFactory(String trustStorePath)
                    throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private static HttpsURLConnection doSSLHandshake(String urlAddress,
                                                     SSLSocketFactory ssf)
                    throws Exception{
        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String rserver, SSLSession sses) {
                    return true;
                }
        });
        connection.setDoOutput(true);
        return connection;
    }

    private static void checkStatus(HttpsURLConnection connection)
                    throws Exception{

        int responseCode=  connection.getResponseCode();
        System.out.println("Response code: " + responseCode + " Expected code: 200");
        if (connection.getResponseCode() != 200){
            stat.addStatus("ssl-responseCode", stat.FAIL);
        } else {
            stat.addStatus("ssl-responseCode", stat.PASS);
        }
    }

    private static void parseResponse(HttpsURLConnection connection)
                    throws Exception{

        BufferedReader in = null;

        String line = "";
        int index;
        try {
            String header = connection.getHeaderField("Set-Cookie");
            System.out.println(header);
            if (!header.contains("Secure")) {
                stat.addStatus("ssl-FILTER", stat.FAIL);
                return;
            }

            in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

            while ((line = in.readLine()) != null) {
                index = line.indexOf("FILTER");
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);

                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("ssl-" + line.substring(0,index),
                                       stat.PASS);
                    } else {
                        stat.addStatus("ssl-FILTER", stat.FAIL);
                    }
                    count++;
                }
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }

        in.close();
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(new File(path), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }
}
