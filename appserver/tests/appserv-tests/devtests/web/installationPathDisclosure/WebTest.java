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
/**
 * Bugtraq 5047700 Installation Path Disclosure
 */
public class WebTest{

    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    private static URLConnection conn = null;
    private static URL url;

    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try{
            stat.addDescription("Basic Host/Context mapping");
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + "///BREAK");
            String originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection)conn;
                urlConnection.setDoOutput(true);

                writeOneByte(urlConnection);

                int responseCode=  urlConnection.getResponseCode();
                System.out.println("installationPathDisclosure: " + responseCode + " Expected code: 40X");
                if (urlConnection.getResponseCode() >= 400 && urlConnection.getResponseCode() < 500){
                    stat.addStatus("Test installationPathDisclosure", stat.PASS);
                } else {
                    stat.addStatus("Test installationPathDisclosure", stat.FAIL);
                }
            }
            url = new URL("http://" + host  + ":" + port + "/BREAK////");
            originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection)conn;
                urlConnection.setDoOutput(true);

                writeOneByte(urlConnection);

                int responseCode=  urlConnection.getResponseCode();
                System.out.println("installationPathDisclosure: " + responseCode + " Expected code: 40X");
                if (urlConnection.getResponseCode() >= 400 && urlConnection.getResponseCode() < 500){
                    stat.addStatus("Test installationPathDisclosure-wrongUrl", stat.PASS);
                } else {
                    stat.addStatus("Test installationPathDisclosure-wrongUrl", stat.FAIL);
                }
            }
            stat.printSummary("web/installationPathDisclosure");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void writeOneByte(HttpURLConnection urlConnection) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(urlConnection.getOutputStream());
            out.writeByte(1);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }
    }
}
