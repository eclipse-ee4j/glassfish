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

package test.weld.osgi;

import org.testng.annotations.*;
import org.testng.Assert;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Test Weld Osgi Bundle Integrity
 *
 * @author Santiago.PericasGeertsen@oracle.com
 * @author Roger.Kitain@oracle.com
 */
public class OsgiWeldTestNG {

    private String strContextRoot="osgiweld";

    String m_host="";
    String m_port="";

    @BeforeMethod
    public void beforeTest(){
        m_host=System.getProperty("http.host");
        m_port=System.getProperty("http.port");
    }

    @DataProvider(name = "exports")
    public Object[][] getExportData() throws Exception {
        Properties props = new Properties();
        props.load(this.getClass().getResourceAsStream("weld-osgi.properties"));

        Object[] exportPackages = new Object[1];
        exportPackages[0] = props.getProperty("exports");
        return (new Object[][] {exportPackages});
    }

//    @Test(groups = {"pulse"}, dataProvider = "exports")
    public void testOsgiModuleIntegrity(String exports) throws Exception {
        try {
            boolean result = checkManifestAttributes();
            Assert.assertEquals(result, true, "Unexpected HTML");
            result = checkExports(exports);
            Assert.assertEquals(result, true, "Unexpected Package Exports");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private boolean checkManifestAttributes() throws Exception {
        String testUrl = "http://" + m_host  + ":" + m_port + "/"
                    + strContextRoot +"/OsgiWeld?command=manifest";
        boolean result = checkForString(testUrl, "OK");
        return result;
    }

    private boolean checkExports(String exports) throws Exception {
        boolean result = false;
        String testurl = "http://" + m_host  + ":" + m_port + "/"
                + strContextRoot +"/OsgiWeld?command=exports";
        URL url = new URL(testurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = input.readLine();
        if (line.equals("ERROR")) {
            return false;
        }
        result = exports.equals(line);
        if (!result) {
            System.out.println("The packages exported by the weld-osgi-bundle do not match the expected packages");
        }
        return result;
    }

    private boolean checkForString(String testurl, String str) throws Exception {
        //System.out.println("Checking for " + str + "in " + testurl);
        URL url = new URL(testurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean result = false;
        String testLine = null;
        while ((line = input.readLine()) != null) {
            //System.out.println("line:" + line);
            if (line.indexOf(str) != -1) {
                result = true;
                testLine = line;
            }
        }
        return result;
    }
}
