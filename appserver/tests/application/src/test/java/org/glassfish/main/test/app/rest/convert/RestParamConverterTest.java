/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.test.app.rest.convert;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;
import org.glassfish.main.test.app.rest.convert.webapp.ParamConverterResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestParamConverterTest {

    private static final Logger LOG = System.getLogger(RestParamConverterTest.class.getName());

    private static final String APP_NAME = RestParamConverterTest.class.getSimpleName();
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @BeforeAll
    static void deploy() {
        File war = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", "--contextroot", "/", "--name", APP_NAME,
                war.getAbsolutePath());
            assertThat(result, AsadminResultMatcher.asadminOK());
        } finally {
            war.delete();
        }
    }


    @AfterAll
    static void undeploy() {
        AsadminResult result = ASADMIN.exec("undeploy", APP_NAME);
        assertThat(result, AsadminResultMatcher.asadminOK());
    }


    @Test
    public void testValidPattern() throws Exception {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target("http://localhost:8080/");
            Response response = target.path("pattern").path("[a-z]+").request(TEXT_PLAIN_TYPE).buildGet().invoke();
            assertEquals(200, response.getStatus());
            assertEquals("1234", response.readEntity(String.class));
        }
    }


    @Test
    public void testEmptyPattern() throws Exception {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target("http://localhost:8080/");
            assertEquals(404,
                target.path("pattern").path("").request().buildGet().invoke().getStatus());
        }
    }


    @Test
    public void testInvalidPattern() throws Exception {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target("http://localhost:8080/");
            assertEquals(404,
                target.path("pattern").path("[a-z").request().buildGet().invoke().getStatus());
        }
    }


    private static File createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "RestParamConverter.war")
            .addPackage(ParamConverterResource.class.getPackage());
        LOG.log(Level.INFO, war.toString(true));
        try {
            File tempFile = File.createTempFile(APP_NAME, ".war");
            war.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Deployment failed - cannot load the input archive!", e);
        }
    }
}
