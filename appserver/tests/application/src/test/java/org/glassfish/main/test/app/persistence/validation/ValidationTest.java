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

package org.glassfish.main.test.app.persistence.validation;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.TestUtilities;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.DomainPropertiesBackup;
import org.glassfish.main.test.app.persistence.data.repository.DataRepositoryTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.switchDerbyPoolToEmbededded;
import static org.glassfish.main.itest.tools.HttpListenerType.HTTP;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(OrderAnnotation.class)
public class ValidationTest {

    private static final System.Logger LOG = System.getLogger(DataRepositoryTest.class.getName());
    private static final String APP_NAME = ValidationTest.class.getSimpleName() + "WebApp";
    private static final Asadmin ASADMIN = getAsadmin();

    private static final DomainPropertiesBackup DERBYPOOL_BACKUP = DomainPropertiesBackup.backupDerbyPool();

    @TempDir
    private static File tempDir;
    private static File warFile;

    @BeforeAll
    public static void deploy() throws Exception {
        switchDerbyPoolToEmbededded();

        WebArchive webArchive =
            ShrinkWrap.create(WebArchive.class)
                      .addPackage(TestServlet.class.getPackage())
                      .deleteClass(ValidationTest.class)
                      .addAsResource(
                          TestServlet.class.getPackage(), "persistence.xml", "META-INF/persistence.xml")
                      .addAsWebInfResource(
                          TestServlet.class.getPackage(), "web.xml", "web.xml")
                      ;

        LOG.log(INFO, webArchive.toString(true));

        warFile = new File(tempDir, APP_NAME + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);

        assertThat(ASADMIN.exec("deploy", "--target", "server", warFile.getAbsolutePath()), asadminOK());


    }



    static String result = "";
    String host = "localhost";
    String port = GlassFishTestEnvironment.getPort(HTTP) + "";

    @Test
    @Order(1)
    public void initialize() throws Exception {
        boolean result = false;

        try {
            result = test("initialize");
            assertEquals(true, result, "Unexpected Results");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);

        }
    }

    @Test
    @Order(2)
    public void validatePersist() throws Exception {
        boolean result = false;

        try {
            result = test("validatePersist");
            assertEquals(result, true, "Unexpected Results");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test
    @Order(3)
    public void validateUpdate() throws Exception {
        boolean result = false;

        try {
            result = test("validateUpdate");
            assertEquals(result, true, "Unexpected Results");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test
    @Order(4)
    public void validateRemove() throws Exception {
        boolean result = false;

        try {
            result = test("validateRemove");
            assertEquals(result, true, "Unexpected Results");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test
    @Order(5)
    public void verify() throws Exception {
        boolean result = false;

        try {
            result = test("verify");
            assertEquals(result, true, "Unexpected Results");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    private boolean test(String c) throws Exception {
        String EXPECTED_RESPONSE = c + ":pass";
        boolean result = false;
        String url = "http://" + host + ":" + port + "/" + APP_NAME + "/test?tc=" + c;
        System.out.println("******************** url="+url);

        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
                if (line.contains(EXPECTED_RESPONSE)) {
                    result = true;
                    break;
                }
            }

        }
        return result;
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

    @AfterAll
    public static void cleanup() throws Exception {
        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
        TestUtilities.delete(warFile);
        DERBYPOOL_BACKUP.restore();
    }

}
