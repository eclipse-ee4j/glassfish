/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.TestUtilities;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.jdke.security.KeyTool;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JmacHttpsTest {
    private static final char[] MYKS_PASSWORD = UUID.randomUUID().toString().toCharArray();

    private static final Logger LOG = System.getLogger(JmacHttpsTest.class.getName());

    private static final String APP_NAME = "security-jmac-https";
    private static final String AUTH_MODULE_NAME = "httpsTestAuthModule";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File myKeyStore;
    private static File warFile;
    private static File loginModuleFile;
    private static KeyTool myKeyStoreTool;


    @BeforeAll
    public static void prepareDeployment() throws Exception {
        myKeyStore = new File(tempDir, "httpstest.p12");
        myKeyStoreTool = new KeyTool(myKeyStore, MYKS_PASSWORD);
        myKeyStoreTool.generateKeyPair("httpstest", "CN=HTTPSTEST,OU=Eclipse GlassFish Tests", "RSA", 7);

        File cacertsFile = getDomain1Directory().resolve(Paths.get("config", TRUSTSTORE_FILENAME_DEFAULT)).toFile();
        myKeyStoreTool.copyCertificate("httpstest", cacertsFile, KEYSTORE_PASSWORD_DEFAULT.toCharArray());

        // Default is false, required to set the client certificate to the context.
        ASADMIN.exec("set", "configs.config.server-config.network-config.protocols.protocol.http-listener-2.ssl.client-auth-enabled=true");
        ASADMIN.exec("restart-domain", "domain1");

        JavaArchive loginModule = ShrinkWrap.create(JavaArchive.class).addClass(HttpsTestAuthModule.class);
        LOG.log(INFO, loginModule.toString(true));
        loginModuleFile = new File(getDomain1Directory().toAbsolutePath().resolve("../../lib").toFile(),
            AUTH_MODULE_NAME + ".jar");
        loginModule.as(ZipExporter.class).exportTo(loginModuleFile, true);
        assertTrue(loginModuleFile.canRead(),
            () -> "Login module file is readable: " + loginModuleFile.getAbsolutePath());

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
    public static void cleanup() throws Exception {
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-message-security-provider", "--layer", "HttpServlet", AUTH_MODULE_NAME);
        ASADMIN.exec("set", "configs.config.server-config.network-config.protocols.protocol.http-listener-2.ssl.client-auth-enabled=false");
        TestUtilities.delete(warFile, loginModuleFile);
    }


    @Test
    void test() throws Exception {
        HttpsURLConnection connection = openConnection(true, 8181, "/" + APP_NAME + "/index.jsp");
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(myKeyStoreTool.loadKeyStore(), MYKS_PASSWORD);
        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] {new TestTrustManager()}, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        connection.setSSLSocketFactory(sslSocketFactory);
        connection.setHostnameVerifier(new NaiveHostnameVerifier());
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        assertAll(
            () -> assertThat("HTTP response code", connection.getResponseCode(), equalTo(200)),
            () -> assertThat("HTTP response content length", connection.getContentLength(), equalTo(-1))
        );
        try (InputStream is = connection.getInputStream()) {
            String text = new String(is.readAllBytes(), UTF_8);
            assertThat(text, stringContainsInOrder("Hello World from 196 HttpServlet AuthModule Test!", //
                "Hello, CN=", "from " + HttpsTestAuthModule.class.getName()));
        } finally {
            connection.disconnect();
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
