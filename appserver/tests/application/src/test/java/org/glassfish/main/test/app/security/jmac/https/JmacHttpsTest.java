/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.jmac.https;

import java.io.File;
import java.io.InputStream;
import java.lang.System.Logger;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.KeyTool;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getDomain1Directory;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getDomain1TrustStore;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

@Disabled("Fails with GF 7.0.11, the attribute jakarta.servlet.request.X509Certificate is not set for some reason.")
public class JmacHttpsTest {
    private static final String MYKS_PASSWORD = "httpspassword";

    private static final Logger LOG = System.getLogger(JmacHttpsTest.class.getName());

    private static final String APP_NAME = "security-jmac-https";
    private static final String AUTH_MODULE_NAME = "httpsTestAuthModule";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final KeyTool KEYTOOL = GlassFishTestEnvironment.getKeyTool();

    @TempDir
    private static File tempDir;
    private static File myKeyStore;
    private static File warFile;
    private static File keyFile;
    private static File loginModuleFile;


    @BeforeAll
    public static void prepareDeployment() throws Exception {
        myKeyStore = new File(tempDir, "httpstest.jks");
        KEYTOOL.exec("-genkey", "-alias", "httpstest", "-keyalg", "RSA", "-dname",
            "CN=HTTPSTEST, OU=Eclipse GlassFish Tests, O=Eclipse Foundation, L=Brussels, ST=Belgium, C=Belgium",
            "-validity", "7", "-keypass", MYKS_PASSWORD, "-keystore", myKeyStore.getAbsolutePath(), "-storepass",
            MYKS_PASSWORD);

        // TODO: Is it required? Or the client certificate should be completely checked just by the auth module?
        KEYTOOL.exec("-importkeystore", "-srckeystore", myKeyStore.getAbsolutePath(), "-srcstorepass", MYKS_PASSWORD,
            "-destkeystore", GlassFishTestEnvironment.getDomain1Directory().resolve(Paths.get("config", "cacerts.jks"))
                .toFile().getAbsolutePath(), "-deststorepass", "changeit");
        ASADMIN.exec("restart-domain", "domain1");

        JavaArchive loginModule = ShrinkWrap.create(JavaArchive.class).addClass(HttpsTestAuthModule.class);
        LOG.log(INFO, loginModule.toString(true));
        loginModuleFile = new File(getDomain1Directory().toAbsolutePath().resolve("../../lib").toFile(),
            "testLoginModule.jar");
        loginModule.as(ZipExporter.class).exportTo(loginModuleFile, true);

        assertThat(ASADMIN.exec("create-message-security-provider",
            "--classname", HttpsTestAuthModule.class.getName(),
            "--layer", "HttpServlet", "--providertype", "server", "--requestauthsource", "sender",
            AUTH_MODULE_NAME), asadminOK());

        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsWebResource(HttpsTestAuthModule.class.getPackage(), "index.jsp", "index.jsp")
            .addAsWebInfResource(HttpsTestAuthModule.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(HttpsTestAuthModule.class.getPackage(), "glassfish-web.xml", "glassfish-web.xml");

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        assertThat(ASADMIN.exec("deploy", "--libraries", loginModuleFile.getAbsolutePath(), "--target", "server",
            warFile.getAbsolutePath()), asadminOK());
    }


    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-message-security-provider", "--layer", "HttpServlet", AUTH_MODULE_NAME);
        delete(warFile);
        delete(keyFile);
        delete(loginModuleFile);
    }


    @Test
    void test() throws Exception {
        HttpsURLConnection connection = openConnection(true, 8181, "/" + APP_NAME + "/index.jsp");
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(KeyTool.loadKeyStore(myKeyStore, MYKS_PASSWORD.toCharArray()), MYKS_PASSWORD.toCharArray());
        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] {new TestTrustManager()}, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        connection.setSSLSocketFactory(sslSocketFactory);
        connection.setHostnameVerifier(new NaiveHostnameVerifier());
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        assertThat(connection.getResponseCode(), equalTo(200));
        try (InputStream is = connection.getInputStream()) {
            String text = new String(is.readAllBytes(), UTF_8);
            assertThat(text, stringContainsInOrder("Hello World from 196 HttpServlet AuthModule Test!", //
                "Hello, CN=", "from " + HttpsTestAuthModule.class.getName()));
        } finally {
            connection.disconnect();
        }
    }

    private static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }


    private static class TestTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            LOG.log(TRACE, "checkClientTrusted(chain={0}, authType={1})", Arrays.toString(chain), authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            LOG.log(TRACE, "checkServerTrusted(chain={0}, authType={1})", Arrays.toString(chain), authType);
            KeyStore trustStore = getDomain1TrustStore();
            for (X509Certificate certificate : chain) {
                certificate.checkValidity();
            }
            for (X509Certificate certificate : chain) {
                try {
                    if (trustStore.getCertificateAlias(certificate) != null) {
                        LOG.log(DEBUG, "The server certificate was accepted: {0}", certificate);
                        return;
                    }
                } catch (KeyStoreException e) {
                    throw new IllegalStateException("The trust store could not be used.", e);
                }
            }
            throw new CertificateException("The server certificate was not identified.");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }


    private static class NaiveHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
