/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URL;
import java.security.KeyStore;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.web.*;
import org.glassfish.embeddable.web.config.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests WebContainer#addWebListener(HttpListener) & HttpListener.setConfig
 *
 * @author Amy Roh
 */
public class EmbeddedAddWebListenerTest {

    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;
    static String contextRoot = "test";
    static int port = 9090;
    static int newPort = 9292;

    @BeforeClass
    public static void setupServer() throws GlassFishException {
        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedAddWebListener Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
    }

    @Test
    public void test() throws Exception {

        HttpListener testListener = new HttpListener("test-listener", port);
        embedded.addWebListener(testListener);

        WebListenerConfig config = new WebListenerConfig("test-listener", newPort);
        config.setProtocol("http");
        testListener.setConfig(config);

        List<WebListener> listenerList = new ArrayList(embedded.getWebListeners());
        Assert.assertTrue(listenerList.size()==1);
        for (WebListener listener : embedded.getWebListeners())
            System.out.println("Web listener "+listener.getId()+" "+listener.getPort());

        Deployer deployer = glassfish.getDeployer();

        URL source = WebHello.class.getClassLoader().getResource(
                "org/glassfish/tests/embedded/web/WebHello.class");
        String p = source.getPath().substring(0, source.getPath().length() -
                "org/glassfish/tests/embedded/web/WebHello.class".length());
        File path = new File(p).getParentFile().getParentFile();

        String name = null;

        if (path.getName().lastIndexOf('.') != -1) {
            name = path.getName().substring(0, path.getName().lastIndexOf('.'));
        } else {
            name = path.getName();
        }

        System.out.println("Deploying " + path + ", name = " + name);

        String appName = deployer.deploy(path.toURI(), "--contextroot", contextRoot, "--name=" + name);

        System.out.println("Deployed " + appName);

        Assert.assertTrue(appName != null);

        URL servlet = new URL("http://localhost:"+newPort+"/"+contextRoot+"/hello");
        URLConnection yc = servlet.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();
        System.out.println(inputLine);
        Assert.assertEquals("Hello World!", sb.toString());

        if (appName!=null)
            deployer.undeploy(appName);

    }

    @AfterClass
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }

}
