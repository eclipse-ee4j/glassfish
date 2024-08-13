/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.tck.rest.cdi;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static java.lang.System.getProperty;
import static java.util.logging.Level.INFO;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class RestCdiIT {

    private final static Logger LOGGER = Logger.getLogger(RestCdiIT.class.getName());

    @ArquillianResource
    private URL baseUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        var webArchive = create(ZipImporter.class, getProperty("finalName") + ".war")
                    .importFrom(new File("target/" + getProperty("finalName") + ".war"))
                    .as(WebArchive.class);

        LOGGER.log(INFO, "webArchive content: {0}", webArchive.toString(true));
        return webArchive;
    }

    @Test
    @RunAsClient
    public void testInjectViaCDI() throws Exception {
        Response response =
            ClientBuilder.newClient()
                         .target(URI.create(baseUrl.toExternalForm() + "api/example/foo"))
                         .request()
                         .accept(APPLICATION_JSON_TYPE)
                         .get();

        // Essentially seeing a 200 or this test starting at all is the main test. If injection of REST artefacts
        // using CDI is not supported, our app will not deploy.
        assertEquals(200, response.getStatus());

        // Test that the injected CDI artefacts also actually work
        String stringEntity = response.readEntity(String.class);

        LOGGER.log(INFO, "Get api/example/foo response: {0}", stringEntity);

        assertTrue(stringEntity.contains("id:foo"));
        assertTrue(stringEntity.contains("path:example/foo"));
    }
}
