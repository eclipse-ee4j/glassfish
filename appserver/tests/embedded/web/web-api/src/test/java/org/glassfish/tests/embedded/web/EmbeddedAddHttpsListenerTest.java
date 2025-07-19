/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.GlassFishVariable;
import org.glassfish.embeddable.web.HttpsListener;
import org.glassfish.embeddable.web.WebContainer;
import org.glassfish.embeddable.web.config.SslConfig;
import org.glassfish.embeddable.web.config.WebContainerConfig;
import org.glassfish.main.jdke.security.KeyTool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * Tests WebContainer#addWebListener(HttpsListener)
 *
 * @author Amy Roh
 */
public class EmbeddedAddHttpsListenerTest {

    @TempDir
    static File tempDir;
    static GlassFish glassfish;
    static WebContainer embedded;
    static File root;
    static String contextRoot = "test";

    @BeforeAll
    public static void setupServer() throws GlassFishException {

        File keystore = new File("keystore.jks");
        setProperty(GlassFishVariable.KEYSTORE_FILE.getSystemPropertyName(), keystore.getAbsolutePath(), true);
        KeyTool keyTool = new KeyTool(keystore, "changeit".toCharArray());
        keyTool.generateKeyPair("s1as", "CN=localhost", "RSA", 1);

        glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        glassfish.start();
        embedded = glassfish.getService(WebContainer.class);
        System.out.println("================ EmbeddedAddHttpsListener Test");
        System.out.println("Starting Web "+embedded);
        embedded.setLogLevel(Level.INFO);
        WebContainerConfig config = new WebContainerConfig();
        root = new File(TestConfiguration.PROJECT_DIR, "target/classes");
        config.setDocRootDir(root);
        config.setListings(true);
        config.setPort(8080);
        System.out.println("Added Web with base directory "+root.getAbsolutePath());
        embedded.setConfiguration(config);
    }

    private void createHttpsListener(int port,
                                     String name,
                                     String keystore,
                                     String password,
                                     String certname) throws Exception {

        HttpsListener listener = new HttpsListener();
        listener.setPort(port);
        listener.setId(name);

        String keyStorePath = root.getAbsolutePath() + keystore;
        String trustStorePath = root.getAbsolutePath() + "/cacerts.jks";
        SslConfig sslConfig = new SslConfig(keyStorePath, trustStorePath);
        sslConfig.setKeyPassword(password.toCharArray());
        String trustPassword = "changeit";
        sslConfig.setTrustPassword(trustPassword.toCharArray());
        if (certname != null) {
            sslConfig.setCertNickname(certname);
        }
        listener.setSslConfig(sslConfig);

        embedded.addWebListener(listener);
    }

    private void verify(int port) throws Exception {

        URL servlet = new URL("https://localhost:"+port+"/classes/hello");
        HttpsURLConnection uc = (HttpsURLConnection) servlet.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            sb.append(inputLine);
        }
        in.close();
        System.out.println(sb);
        Assertions.assertEquals("Hello World!", sb.toString());
    }

    @Test
    public void test() throws Exception {

        createHttpsListener(9191, "default-ssl-listener", "/keystore.jks", "changeit", "s1as");
        //createHttpsListener(9292, "ssl-listener0", "/keystore0", "password0", "keystore0");
        //createHttpsListener(9393, "ssl-listener1", "/keystore1", "password1", null);

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
        String appName = deployer.deploy(path.toURI(), "--name=" + name);
        System.out.println("Deployed " + appName);
        Assertions.assertTrue(appName != null);

        disableCertValidation();
        verify(9191);
        //verify(9292);
        //verify(9393);

        if (appName!=null) {
            deployer.undeploy(appName);
        }

    }

    public static void disableCertValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                return;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                return;
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            return;
        }
    }

    @AfterAll
    public static void shutdownServer() throws GlassFishException {
        System.out.println("Stopping server " + glassfish);
        if (glassfish != null) {
            glassfish.stop();
            glassfish.dispose();
            glassfish = null;
        }
    }

}
