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

public class WebTest{

    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    private static URLConnection conn = null;
    private static URL url;
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;

    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try{
            stat.addDescription("Standalone Servlet/Filter war test");

            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + contextRoot + "/ServletTest");
            String originalLoc = url.toString();
            for (int k=0; k < 3; k++){
                System.out.println("\n Invoking url: " + url.toString());
                conn = url.openConnection();
                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection urlConnection = (HttpURLConnection)conn;
                    urlConnection.setDoOutput(true);

                    DataOutputStream out =
                       new DataOutputStream(urlConnection.getOutputStream());
                                        out.writeByte(1);

                   int responseCode=  urlConnection.getResponseCode();
                   String encodedURL = urlConnection.getHeaderField ("Location");
                   System.out.println("responseCode: " + responseCode);
                   System.err.println("encodedURL : " + encodedURL);

                   if (urlConnection.getResponseCode() != 201){
                        stat.addStatus("contentLength-responseCode", stat.FAIL);
                   } else {
                        stat.addStatus("contentLength-responseCode", stat.PASS);
                   }

                   if (encodedURL != null && !encodedURL.startsWith(originalLoc)){
                        stat.addStatus("contentLength-header", stat.FAIL);
                   } else {
                        stat.addStatus("contentLength-header", stat.PASS);
                   }
                }
            }
            stat.printSummary("web/contentLength");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
