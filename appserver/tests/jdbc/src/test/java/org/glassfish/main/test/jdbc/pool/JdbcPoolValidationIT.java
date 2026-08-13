/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.lang.System.Logger;

import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.main.test.jdbc.pool.war.GlassFishUserRestEndpoint;
import org.glassfish.main.test.jdbc.pool.war.RestAppConfig;
import org.glassfish.main.test.jdbc.pool.war.User;
import org.glassfish.main.test.perf.rest.UserRestClient;
import org.glassfish.main.test.perf.server.DockerTestEnvironment;
import org.glassfish.tests.utils.junit.TestLoggingExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.DockerClientFactory;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Verifies JDBC connection pool behavior against a real database outage.
 *
 * <p>The test runs GlassFish and PostgreSQL in Docker (reusing {@link DockerTestEnvironment}),
 * configures connection validation for the pool backing the persistence unit, executes a batch of
 * requests, terminates every backend connection on the database server (which reliably severs every
 * pooled connection) and then executes another batch. It verifies that the pool recreates the
 * connections killed by the outage, and logs how many second-batch requests succeeded so the
 * behavior with and without validation can be compared.
 *
 * <p>A short network outage is not used to break the connections: Docker often reassigns the same
 * container IP and an established TCP connection tolerates a brief gap, so the pooled connections
 * survive and nothing is validated. A container restart is not used either, because it reassigns the
 * published host port and breaks the host-side connections. Terminating the server backends breaks
 * the pooled connections deterministically while keeping the database available.
 *
 * <p>Connection validation is enabled by default. To run the same scenario with validation
 * disabled (and observe how the pool behaves without it), set the system property:
 * <code>-Dit.connectionValidation=false</code>
 *
 * <p>The pool is created with <code>fail-all-connections=true</code>, but this test recreates only the
 * failed connection on a validation failure by default. To flush and recreate the whole pool instead, set:
 * <code>-Dit.failAllConnections=true</code>
 *
 * <p>The validation method defaults to <code>custom-validation</code> with a class that runs a real
 * <code>SELECT 1</code> round-trip, which reliably detects a killed connection. The pool's default
 * <code>auto-commit</code> method does not round-trip and misses killed connections (see issue 25930).
 * Override with e.g.:
 * <code>-Dit.validationMethod=auto-commit</code> (also accepts <code>meta-data</code>, <code>table</code>)
 *
 * <p>Example:
 * <code>
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT
 * </code>
 * <code>
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT -Dit.connectionValidation=false
 * </code>
 * <code>
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT -Dit.failAllConnections=true
 * </code>
 * <code>
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT -Dit.validationMethod=auto-commit
 * </code>
 */
@ExtendWith(TestLoggingExtension.class)
public class JdbcPoolValidationIT {
    private static final Logger LOG = System.getLogger(JdbcPoolValidationIT.class.getName());

    private static final String APPNAME = "poolValidation";

    /** Pool backing the {@code jdbc/dsPoolA} data source used by the persistence unit {@code UnitA}. */
    private static final String POOL = "domain-pool-A";

    private static final boolean DOCKER_AVAILABLE = DockerClientFactory.instance().isDockerAvailable();

    /** Connection validation is enabled by default; disable with {@code -Dit.connectionValidation=false}. */
    private static final boolean CONNECTION_VALIDATION = Boolean.parseBoolean(
        System.getProperty("it.connectionValidation", "true"));

    /** Pool recreates only the failed connection on a validation failure by default; enable flush-all with {@code -Dit.failAllConnections=true}. */
    private static final boolean FAIL_ALL_CONNECTIONS = Boolean.parseBoolean(
        System.getProperty("it.failAllConnections", "false"));

    /** Validation method; defaults to a real round-trip. Override with {@code -Dit.validationMethod=auto-commit|meta-data|table}. */
    private static final String VALIDATION_METHOD = System.getProperty("it.validationMethod", "custom-validation");

    /** Round-tripping validation class ({@code SELECT 1}) used when the method is {@code custom-validation}. */
    private static final String POSTGRES_VALIDATION_CLASS = "org.glassfish.api.jdbc.validation.PostgresConnectionValidation";

    /** Table queried when the validation method is {@code table}; must match the case-sensitive entity table. */
    private static final String VALIDATION_TABLE = "GlassFishUser";

    private static final int REQUESTS_PER_BATCH = 5;

    private static DockerTestEnvironment environment;
    private static WebTarget wsEndpoint;


    @BeforeAll
    public static void init() throws Exception {
        assumeTrue(DOCKER_AVAILABLE, "Docker is not available on this environment");
        environment = DockerTestEnvironment.getInstance();

        // The pool is created with auto-commit validation method and fail-all-connections=true,
        // but validation itself is off by default - toggle just that here. The pool is lazy
        // (steady-pool-size=0), so it is not flushed here: it has no live connections until the
        // deployed application uses it, and the first connection already honors this setting.
        assertEquals(0, environment.asadmin("set",
            "resources.jdbc-connection-pool." + POOL + ".is-connection-validation-required=" + CONNECTION_VALIDATION)
            .getExitCode());
        assertEquals(0, environment.asadmin("set",
            "resources.jdbc-connection-pool." + POOL + ".fail-all-connections=" + FAIL_ALL_CONNECTIONS)
            .getExitCode());
        // Provide the method-specific attribute before switching the method: the validator rejects
        // 'custom-validation'/'table' if the class name / table name is not set yet.
        if ("custom-validation".equals(VALIDATION_METHOD)) {
            assertEquals(0, environment.asadmin("set",
                "resources.jdbc-connection-pool." + POOL + ".validation-classname=" + POSTGRES_VALIDATION_CLASS)
                .getExitCode());
        } else if ("table".equals(VALIDATION_METHOD)) {
            assertEquals(0, environment.asadmin("set",
                "resources.jdbc-connection-pool." + POOL + ".validation-table-name=" + VALIDATION_TABLE)
                .getExitCode());
        }
        assertEquals(0, environment.asadmin("set",
            "resources.jdbc-connection-pool." + POOL + ".connection-validation-method=" + VALIDATION_METHOD)
            .getExitCode());

        environment.reinitializeDatabase();
        wsEndpoint = environment.deploy(APPNAME, getArchiveToDeploy());
    }


    @AfterAll
    public static void cleanup() throws Exception {
        if (!DOCKER_AVAILABLE) {
            return;
        }
        environment.undeploy(APPNAME);
        // Restore the shared environment for the other tests using it.
        environment.asadmin("set",
            "resources.jdbc-connection-pool." + POOL + ".is-connection-validation-required=false");
        environment.asadmin("set",
            "resources.jdbc-connection-pool." + POOL + ".fail-all-connections=true");
        environment.asadmin("set",
            "resources.jdbc-connection-pool." + POOL + ".connection-validation-method=auto-commit");
        environment.reinitializeDatabase();
    }


    @Test
    public void recreatesConnectionsAfterDatabaseOutage() throws Exception {
        final UserRestClient client = new UserRestClient(wsEndpoint);

        // First batch while the database is available - warms up the pool with live connections.
        createUsers(client, REQUESTS_PER_BATCH);
        assertEquals(REQUESTS_PER_BATCH, client.count(), "users after first batch");

        final int createdBefore = environment.asadminMonitor("server.resources." + POOL + ".numconncreated-count");

        // Terminate every pooled connection at the server so they are definitively broken.
        LOG.log(INFO, "Simulating a database outage (validation enabled: {0}, method: {1}, fail-all-connections: {2})",
            CONNECTION_VALIDATION, VALIDATION_METHOD, FAIL_ALL_CONNECTIONS);
        environment.killDatabaseConnections();

        // Second batch after the database is back. Tolerate individual failures so the behavior without
        // validation (broken connections handed out to the application) is observable instead of aborting.
        final int succeeded = tryCreateUsers(wsEndpoint, REQUESTS_PER_BATCH);
        final long total = client.count();

        final int createdAfter = environment.asadminMonitor("server.resources." + POOL + ".numconncreated-count");
        final int destroyed = environment.asadminMonitor("server.resources." + POOL + ".numconndestroyed-count");
        final int failedValidation = environment
            .asadminMonitor("server.resources." + POOL + ".numconnfailedvalidation-count");
        LOG.log(INFO, "After outage: second-batch successes={0}/{1}, total users={2}, connections created={3}"
            + " (was {4}), destroyed={5}, failed validation={6}",
            succeeded, REQUESTS_PER_BATCH, total, createdAfter, createdBefore, destroyed, failedValidation);

        // Regardless of the validation method, serving the second batch requires the pool to replace the
        // connections the outage killed.
        assertThat("the pool must create fresh connections after the outage", createdAfter, greaterThan(createdBefore));

        if (CONNECTION_VALIDATION) {
            // With working validation the pool detects and discards the broken connections before handing
            // them out, so every second-batch request succeeds and all users are persisted.
            assertEquals(REQUESTS_PER_BATCH, succeeded, "all second-batch requests must succeed with validation");
            assertEquals(2L * REQUESTS_PER_BATCH, total, "users after second batch");
        }
    }


    private static void createUsers(UserRestClient client, int count) {
        for (int i = 0; i < count; i++) {
            client.create(new User(RandomStringUtils.insecure().nextAlphabetic(32)));
        }
    }


    /**
     * Attempts {@code count} create requests, tolerating failures, and returns the number that succeeded.
     */
    private static int tryCreateUsers(WebTarget endpoint, int count) {
        final WebTarget create = endpoint.path("user").path("create");
        int succeeded = 0;
        for (int i = 0; i < count; i++) {
            final User user = new User(RandomStringUtils.insecure().nextAlphabetic(32));
            try (Response response = create.request().put(Entity.json(user))) {
                if (response.getStatusInfo().toEnum() == Status.NO_CONTENT) {
                    succeeded++;
                } else {
                    LOG.log(INFO, "Second-batch request failed with status {0}", response.getStatus());
                }
            } catch (RuntimeException e) {
                LOG.log(INFO, "Second-batch request failed: " + e.getMessage());
            }
        }
        return succeeded;
    }


    private static WebArchive getArchiveToDeploy() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(GlassFishUserRestEndpoint.class, User.class, RestAppConfig.class)
            .addAsWebInfResource("jdbc/pool/war/persistence.xml", "classes/META-INF/persistence.xml")
            .addAsWebInfResource("jdbc/pool/war/orm.xml", "classes/META-INF/orm.xml");
    }
}
