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

package org.glassfish.main.test.app.security.jmac.http.soap.embedded;

import java.io.File;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.nio.file.Path;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.hello.common.ObjectFactory;
import org.glassfish.main.test.app.hello.ejb.HelloEjbPort;
import org.glassfish.main.test.app.hello.ejb.HelloEjbService;
import org.glassfish.main.test.app.hello.servlet.HelloServletPort;
import org.glassfish.main.test.app.hello.servlet.HelloServletService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.createFileUser;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getDomain1Directory;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.openConnection;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

public class HttpSoapEmbeddedAuthTest {
    private static final Logger LOG = System.getLogger(HttpSoapEmbeddedAuthTest.class.getName());

    private static final String APP_NAME = "security-jmac-soap-embedded";
    private static final String APP_NAME_CLIENT = APP_NAME + "-client";
    private static final String AUTH_MODULE_NAME_SERVER = "HttpSoapEmbeddedTestAuthModuleServer";
    private static final String AUTH_MODULE_NAME_CLIENT = "HttpSoapEmbeddedTestAuthModuleClient";
    private static final String FILE_REALM_NAME = "file123";
    private static final String USER_NAME = "shingwai";
    private static final String USER_NAME2 = "shingwai_2";
    private static final String USER_PASSWORD = "shingwai";
    private static final String USER_PASSWORD2 = "adminadmin";

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @TempDir
    private static File tempDir;
    private static File earFile;
    private static File warFile;
    private static File keyFile;

    private static File loginModuleFile;


    @BeforeAll
    public static void prepareDeployment() {
        keyFile = getDomain1Directory().resolve(Path.of("config", "file123.txt")).toFile();
        assertThat(ASADMIN.exec("create-auth-realm", "--classname",
            "com.sun.enterprise.security.auth.realm.file.FileRealm", "--property",
            "file=" + keyFile.getAbsolutePath() + ":jaas-context=fileRealm", "--target", "server", FILE_REALM_NAME),
            asadminOK());
        createFileUser(FILE_REALM_NAME, USER_NAME, USER_PASSWORD, "mygroup");
        createFileUser(FILE_REALM_NAME, USER_NAME2, USER_PASSWORD2, "mygroup");

        final JavaArchive loginModule = ShrinkWrap.create(JavaArchive.class)
            .addClass(SOAPEmbeddedTestServerAuthModule.class)
            .addClass(SOAPEmbeddedTestClientAuthModule.class)
            .addClass(SoapMessageAuthModuleUtilities.class);
        LOG.log(INFO, loginModule.toString(true));

        loginModuleFile = new File(getDomain1Directory().toAbsolutePath().resolve("../../lib").toFile(),
            "HttpSoapEmbeddedAuthTestAuthModules.jar");
        loginModule.as(ZipExporter.class).exportTo(loginModuleFile, true);

        assertThat(ASADMIN.exec("create-message-security-provider",
            "--classname", SOAPEmbeddedTestServerAuthModule.class.getName(),
            "--layer", "SOAP", "--providertype", "server", "--requestauthsource", "sender",
            AUTH_MODULE_NAME_SERVER), asadminOK());

        assertThat(ASADMIN.exec("create-message-security-provider",
            "--classname", SOAPEmbeddedTestClientAuthModule.class.getName(),
            "--layer", "SOAP", "--providertype", "client", "--requestauthsource", "sender",
            AUTH_MODULE_NAME_CLIENT), asadminOK());

        final JavaArchive ejbArchive = ShrinkWrap.create(JavaArchive.class, "ejbArchive.jar")
            .addClass(HelloEjb.class)
            .addPackage(HelloEjbPort.class.getPackage())
            .addPackage(ObjectFactory.class.getPackage())
            .addAsManifestResource("wsdl/hello-ejb.wsdl", "wsdl/hello-ejb.wsdl")
            .addAsManifestResource("wsdl/hello.xsd", "wsdl/hello.xsd")
            .addAsManifestResource(HttpSoapEmbeddedAuthTest.class.getPackage(), "sun-ejb-jar.xml", "sun-ejb-jar.xml");
        LOG.log(INFO, ejbArchive.toString(true));

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
            .addClass(HelloServlet.class)
            .addPackage(HelloServletPort.class.getPackage())
            .addPackage(ObjectFactory.class.getPackage())
            .addAsWebInfResource("wsdl/hello-servlet.wsdl", "wsdl/hello-servlet.wsdl")
            .addAsWebInfResource("wsdl/hello.xsd", "wsdl/hello.xsd")
            .addAsWebInfResource(HttpSoapEmbeddedAuthTest.class.getPackage(), "web.xml", "web.xml")
            .addAsWebInfResource(HttpSoapEmbeddedAuthTest.class.getPackage(), "sun-web.xml", "sun-web.xml");
        LOG.log(INFO, webArchive.toString(true));

        final EnterpriseArchive earArchive = ShrinkWrap.create(EnterpriseArchive.class)
            .addAsModule(webArchive).addAsModule(ejbArchive)
            .setApplicationXML(HttpSoapEmbeddedAuthTest.class.getPackage(), "application.xml");
        LOG.log(INFO, earArchive.toString(true));

        earFile = new File(tempDir, APP_NAME + ".ear");
        earArchive.as(ZipExporter.class).exportTo(earFile, true);
        assertThat(ASADMIN.exec("deploy", "--libraries", loginModuleFile.getAbsolutePath(), "--upload", "true",
            "--target", "server", earFile.getAbsolutePath()), asadminOK());

        final WebArchive clientArchive = ShrinkWrap.create(WebArchive.class)
            .addClass(Servlet.class)
            .addPackage(HelloEjbService.class.getPackage())
            .addPackage(HelloServletService.class.getPackage())
            .addPackage(ObjectFactory.class.getPackage())
            .addAsManifestResource("wsdl/hello-ejb.wsdl", "wsdl/hello-ejb.wsdl")
            .addAsManifestResource("wsdl/hello-servlet.wsdl", "wsdl/hello-servlet.wsdl")
            .addAsManifestResource("wsdl/hello.xsd", "wsdl/hello.xsd")
            .addAsWebInfResource(HttpSoapEmbeddedAuthTest.class.getPackage(), "web-client.xml", "web.xml")
            .addAsWebInfResource(HttpSoapEmbeddedAuthTest.class.getPackage(), "sun-web-client.xml", "sun-web.xml");
        LOG.log(INFO, clientArchive.toString(true));

        warFile = new File(tempDir, APP_NAME_CLIENT + ".war");
        clientArchive.as(ZipExporter.class).exportTo(warFile, true);
        assertThat(ASADMIN.exec("deploy", "--contextroot", APP_NAME_CLIENT, //
            "--libraries", loginModuleFile.getAbsolutePath(), "--upload", "true", "--target", "server",
            warFile.getAbsolutePath()), asadminOK());
    }


    @AfterAll
    public static void cleanup() {
        ASADMIN.exec("undeploy", APP_NAME_CLIENT);
        ASADMIN.exec("undeploy", APP_NAME);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_NAME2);
        ASADMIN.exec("delete-file-user", "--authrealmname", FILE_REALM_NAME, "--target", "server", USER_NAME);
        ASADMIN.exec("delete-message-security-provider", "--layer", "SOAP", AUTH_MODULE_NAME_CLIENT);
        ASADMIN.exec("delete-message-security-provider", "--layer", "SOAP", AUTH_MODULE_NAME_SERVER);
        ASADMIN.exec("delete-auth-realm", FILE_REALM_NAME);
        delete(warFile);
        delete(earFile);
        delete(keyFile);
        delete(loginModuleFile);
    }


    @Test
    void test() throws Exception {
        final HttpURLConnection connection = openConnection(8080, "/" + APP_NAME_CLIENT + "/webclient/Servlet");
        connection.setRequestMethod("GET");
        try (InputStream is = connection.getInputStream()) {
            assertThat(connection.getResponseCode(), equalTo(200));
            final String text = new String(is.readAllBytes(), UTF_8);
            assertThat(text, stringContainsInOrder(
                "Servlet Output",
                "ValResp SecResp HelloEjb ValReq SecReq Sun",
                "ValResp SecResp HelloServlet ValReq SecReq Sun"));
        } finally {
            connection.disconnect();
        }
    }

    private static void delete(final File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
