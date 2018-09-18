/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.admin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Manifest;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

/** The base class for asadmin tests. Designed for extension.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class BaseAsadminTest {
    
    String adminUrl;
    String adminUser;
    String adminPassword;
    
    @BeforeClass
    @Parameters({"admin.url", "admin.user", "admin.password"})
    void setUpEnvironment(String url, String adminUser, String adminPassword) {
        this.adminUrl      = url;
        this.adminUser     = adminUser;
        this.adminPassword = adminPassword;
    }
    
    protected Manifest invokeURLAndGetManifest(String urls) {
        try {
            URL url = new URL(urls);
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setRequestMethod("GET");
            uc.setRequestProperty("User-Agent", "hk2-agent");
            uc.connect();
            Manifest man = new Manifest(uc.getInputStream());
            return ( man );
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    protected void logEnv() {
        Properties props = System.getProperties();
        Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Reporter.log((key + " = " + props.get(key)));
        }
    }
}
