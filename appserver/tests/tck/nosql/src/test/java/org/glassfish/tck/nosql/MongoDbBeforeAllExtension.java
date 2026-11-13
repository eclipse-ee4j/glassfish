/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License, version 2.0, which is available at
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
package org.glassfish.tck.nosql;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * JUnit 5 extension to start MongoDB container before TCK tests run.
 */
@Order(10)
public class MongoDbBeforeAllExtension implements BeforeAllCallback, AfterAllCallback {

    private static MongoDBContainer mongodb;

    public static final String MP_CONFIG_CONTENT_KEY = "glassfish.tck.nosql.mongodb.config";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        var dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        assumeTrue(dockerAvailable, "Docker is not available on this environment");

        if (mongodb == null) {
            mongodb = new MongoDBContainer("mongo:latest");
            mongodb.start();

            String mongoDbHost = mongodb.getHost() + ":" + mongodb.getFirstMappedPort();
            System.out.println("Started MongoDB in Docker at " + mongoDbHost);

            String config = "jnosql.document.database=tck\n"
                    + "jnosql.mongodb.host=" + mongoDbHost + "\n";
            System.setProperty(MP_CONFIG_CONTENT_KEY, config);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // We keep it running across test classes for performance, Maven will kill it at the end
    }

}
