/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.tck.data.junit5;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.glassfish.tck.data.arquillian.JakartaNoSqlProcessor.MP_CONFIG_CONTENT_KEY;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Starts MongoDB docker container before all tests and sets a system property with MP Config.
 * This property is then used by an Arquillian extension to create microprofile-config.properties file to configure JNoSQL.
 *
 * @author Ondro Mihalyi
 */
@Order(10)
public class MongoDbBeforeAllExtension implements BeforeAllCallback, AfterAllCallback {

    private final GenericContainer<?> mongodb
            = new GenericContainer<>("mongo:latest")
                    .withExposedPorts(27017)
                    .waitingFor(Wait.defaultWaitStrategy());

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        var dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        assumeTrue(dockerAvailable, "Docker is not available on this environment");

        mongodb.start();

        String mongoDbHost = mongodb.getHost() + ":" + mongodb.getFirstMappedPort();
        System.out.println("Started MongoDB in Docker at " + mongoDbHost);

        // set GlassFish system properties for the Arquillian GlassFish connector
        System.setProperty(MP_CONFIG_CONTENT_KEY,
                Stream.of(
                        "jnosql.document.database=tck",
                        "jnosql.mongodb.host=" + mongoDbHost
                ).collect(Collectors.joining("\n"))
        );
        System.out.println("Setting " + MP_CONFIG_CONTENT_KEY + " system property to:\n" + System.getProperty(MP_CONFIG_CONTENT_KEY).indent(4));
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        mongodb.stop();
    }

}
