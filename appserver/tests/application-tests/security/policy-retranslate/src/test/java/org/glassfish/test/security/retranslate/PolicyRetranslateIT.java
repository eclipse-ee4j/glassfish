/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.glassfish.test.security.retranslate;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.getProperty;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ArquillianTest
public class PolicyRetranslateIT {

    Logger LOGGER = System.getLogger(PolicyRetranslateIT.class.getName());

    @ArquillianResource
    private URI base;

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return create(ZipImporter.class, getProperty("finalName") + ".war")
                .importFrom(new File("target/" + getProperty("finalName") + ".war"))
                .as(WebArchive.class);
    }

    @Test
    public void testAuthenticated() throws IOException, InterruptedException, URISyntaxException {
        var body =
            newHttpClient()
                  .send(
                      HttpRequest.newBuilder(base.resolve("hello"))
                                 .header("X-API-Key", "javajoe")
                                 .build(),
                      ofString())
                  .body();

        LOGGER.log(INFO, body);

        assertTrue(body.contains("hi javajoe"));
        assertTrue(body.contains("viewer"));
        assertTrue(body.contains("api-user"));

        // The key test - for this to work the policy context has to be cleared correctly
        // before the retranslate happens.
        assertTrue(body.contains("authorization-name=javajoe"));
    }
}
