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

package test.web.extensions;

import org.testng.annotations.*;
import org.testng.Assert;
import java.io.*;
import java.net.*;

/**
 * Test Weld extension beans
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class ExtensionsTestNG {

    private String strContextRoot="extensions";

    String m_host="";
    String m_port="";

    @BeforeMethod
    public void beforeTest(){
        m_host=System.getProperty("http.host");
        m_port=System.getProperty("http.port");
    }

    @Test(groups = {"pulse"})
    public void extensionBeanReferenceTest() throws Exception {
        try {
            String testurl;
            testurl = "http://" + m_host  + ":" + m_port + "/"
                    + strContextRoot +"/ExtensionBean";
            boolean result = checkForString(testurl, "ExtensionBean");
            Assert.assertEquals(result, true, "Unexpected HTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Test(groups = {"pulse"})
    public void extensionBeanObserversTest() throws Exception {
        try {
            String testurl;
            testurl = "http://" + m_host  + ":" + m_port + "/"
                    + strContextRoot +"/ExtensionBean";
            boolean result = checkForString(testurl, "false");
            Assert.assertEquals(result, false, "Unexpected HTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Test(groups = {"pulse"})
    public void webBeanTest() throws Exception {
        try {
            String testurl;
            testurl = "http://" + m_host  + ":" + m_port + "/"
                    + strContextRoot +"/WebBean";
            boolean result = checkForString(testurl, "BEAN15");
            Assert.assertEquals(result, true, "Unexpected HTML");
        } catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Test(groups = {"pulse"})
    public void jarExtensionBeanReferenceTest() throws Exception {
        try {
            String testurl;
            testurl = "http://" + m_host  + ":" + m_port + "/"
                    + strContextRoot +"/JarExtensionBean";
            boolean result = checkForString(testurl, "ExtensionBean");
            Assert.assertEquals(result, true, "Unexpected HTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Test(groups = {"pulse"})
    public void jarExtensionBeanObserversTest() throws Exception {
        try {
            String testurl;
            testurl = "http://" + m_host  + ":" + m_port + "/"
                    + strContextRoot +"/JarExtensionBean";
            boolean result = checkForString(testurl, "false");
            Assert.assertEquals(result, false, "Unexpected HTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    @Test(groups = {"pulse"})
    public void injectionTargetAdaptionTest() throws Exception {
        try {
            String testurl;
            testurl = "http://" + m_host  + ":" + m_port + "/"
                    + strContextRoot +"/WebBean";
            boolean result = checkForString(testurl, "false");
            Assert.assertEquals(result, false, "Unexpected HTML");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    private boolean checkForString(String testurl, String str) throws Exception {
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
            if (line.indexOf(str) != -1) {
                result = true;
                testLine = line;
            }
        }
        return result;
    }
}
