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

package org.glassfish.main.test.jdbc.pool;

import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.glassfish.main.test.jdbc.pool.war.DataSourceDefinitionBean;
import org.glassfish.main.test.jdbc.pool.war.JdbcDsName;
import org.glassfish.main.test.jdbc.pool.war.RestAppConfig;
import org.glassfish.main.test.perf.server.DockerTestEnvironment;
import org.glassfish.tests.utils.junit.TestLoggingExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.DockerClientFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(TestLoggingExtension.class)
public class JdbcPoolInjectionsIT {
    private static final String APPNAME = "dspools";
    private static boolean dockerAvailable;
    private static DockerTestEnvironment environment;
    private static WebTarget wsEndpoint;


    @BeforeAll
    public static void init() throws Exception {
        dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        assumeTrue(dockerAvailable, "Docker is not available on this environment");
        environment = DockerTestEnvironment.getInstance();
        wsEndpoint = environment.deploy(APPNAME, DataSourceDefinitionBean.class, JdbcDsName.class, RestAppConfig.class);
    }

    @AfterAll
    public static void cleanup() throws Exception {
        if (!dockerAvailable) {
            return;
        }
        environment.undeploy(APPNAME);
        environment.reinitializeDatabase();
    }

    @Test
    public void testJdbcResourceInjections() throws Exception {
        final Builder builder = wsEndpoint.path("rest").path("versions").request();
        try (Response response = builder.get()) {
            assertEquals(Status.OK, response.getStatusInfo().toEnum(), "response.status");
            assertTrue(response.hasEntity(), "response.hasEntity");

            final String stringEntity = response.readEntity(String.class);
            assertEquals(
                "[J2EE 1.2, J2EE 1.3, J2EE 1.4, Java EE 6, Java EE 7, Java EE 8, Jakarta EE 8, Jakarta EE 9, Jakarta EE 10]",
                stringEntity, "response.text");
        }
    }
}
