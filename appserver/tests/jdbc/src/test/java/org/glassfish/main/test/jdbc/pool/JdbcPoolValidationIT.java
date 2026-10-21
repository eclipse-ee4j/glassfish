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
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.DockerClientFactory;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Verifies JDBC connection pool behavior against a real database outage.
 *
 * <p>
 * The test runs GlassFish and PostgreSQL in Docker (reusing
 * {@link DockerTestEnvironment}), configures connection validation for the pool
 * backing the persistence unit, executes a batch of requests, breaks every
 * pooled connection with a genuine socket-read failure and then executes a
 * sequence of requests, 1 request per second. It verifies that the pool
 * recreates the connections broken by the outage, and logs how many requests in
 * the sequence succeeded so the behavior with and without validation can be
 * compared.
 *
 * <p>
 * The connections are broken through a real in-flight socket read timeout (see
 * issue 25930): each pooled connection runs a slow query
 * ({@code SELECT pg_sleep(...)}) while the pool's PostgreSQL
 * {@code socketTimeout} is shorter than the sleep. The blocked read then fails
 * with an 08-class connection error
 * ({@code SocketTimeoutException -> PSQLException}) instead of server-side
 * connection termination. The failed connections stay in the pool (the error is
 * not propagated as a connection error), so the pool must detect and replace
 * them on the next checkout.
 *
 * <p>
 * This test is parameterized over all combinations of connection validation,
 * fail-all behavior, validation method, and validate-at-most-once period. By
 * default, the property values below expand to the full cartesian product of
 * scenarios, so the test exercises every supported combination unless you
 * restrict the set explicitly.
 *
 * <p>
 * You can narrow the run to a single combination or a subset by overriding any
 * of these properties, for example: {@code -Dit.connectionValidation=false}
 * {@code -Dit.failAllConnections=true}
 * {@code -Dit.validationMethod=auto-commit}
 * {@code -Dit.validateAtMostOncePeriod=0,10}
 *
 * <p>
 * When multiple values are provided, the test expands them into all
 * combinations. Restricting a property to a single value or a smaller CSV
 * subset keeps the run focused on that subset while still exercising the actual
 * parameterized logic.
 *
 * <p>
 * The default set includes {@code custom-validation} with a class that runs a
 * real {@code SELECT 1} round-trip, which reliably detects a killed connection.
 * The pool's {@code auto-commit} and {@code meta-data} methods usually do not
 * force a SQL round-trip, so detection can depend on driver behavior and
 * failure mode, If the test is flaky for these types, remove them from the
 * default validation types.
 *
 * <p>
 * The validate-at-most-once period is also parameterized. To exercise one
 * specific period window, set the value in seconds, e.g.:
 * {@code -Dit.validateAtMostOncePeriod=60} A period up to 15s is waited out
 * before the second batch, so validation runs again and the test still expects
 * success. A larger period keeps the second batch inside the window, so the
 * pool serves stale (broken) connections and the test instead expects at least
 * one failure.
 *
 * <p>
 * Example:
 * <pre>{@code
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT -Dit.connectionValidation=false
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT -Dit.failAllConnections=true
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT -Dit.validationMethod=auto-commit
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolValidationIT -Dit.validateAtMostOncePeriod=60
 * }</pre>
 */
@ExtendWith(TestLoggingExtension.class)
public class JdbcPoolValidationIT {

    private static final Logger LOG = System.getLogger(JdbcPoolValidationIT.class.getName());

    private static final boolean DOCKER_AVAILABLE = DockerClientFactory.instance().isDockerAvailable();

    private static final String APPNAME = "poolValidation";

    /**
     * Pool backing the {@code jdbc/dsPoolA} data source used by the persistence
     * unit {@code UnitA}.
     */
    private static final String POOL = "domain-pool-A";

    /**
     * Round-tripping validation class ({@code SELECT 1}) used when the method
     * is {@code custom-validation}.
     */
    private static final String POSTGRES_VALIDATION_CLASS = "org.glassfish.api.jdbc.validation.PostgresConnectionValidation";

    /**
     * Table queried when the validation method is {@code table}; must match the
     * case-sensitive entity table. PostgreSQL requires quotes around table name
     * to keep the case of characters, otherwise it won't find the table
     */
    private static final String VALIDATION_TABLE = "\"GlassFishUser\"";

    /**
     * Maximum pool size used by this test; the outage batch sends this many
     * parallel requests so every pooled connection is invalidated at once.
     */
    private static final int MAX_CONNECTIONS_IN_POOL = 5;

    /**
     * Socket read timeout (seconds) configured on the pool so a blocked read
     * fails promptly with an 08-class connection error
     * ({@code SocketTimeoutException} \u2192 {@code PSQLException}) instead of
     * hanging on the PostgreSQL driver's blocking sockets. Must comfortably
     * exceed the normal (fast) query and validation round-trips, but be shorter
     * than {@link #SLEEP_QUERY_SECONDS}.
     */
    private static final int SOCKET_TIMEOUT_SECONDS = 2;

    /**
     * Duration of the {@code pg_sleep} query used to keep a pooled connection
     * blocked in a socket read - should take longer than SOCKET_TIMEOUT_SECONDS
     * to trigger the timeout
     */
    private static final int SLEEP_QUERY_SECONDS = SOCKET_TIMEOUT_SECONDS + 5;

    private static DockerTestEnvironment environment;
    private static WebTarget wsEndpoint;

    @BeforeAll
    public static void init() throws Exception {
        assumeTrue(DOCKER_AVAILABLE, "Docker is not available on this environment");
        environment = DockerTestEnvironment.getInstance();
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
        environment.asadmin("set",
                "resources.jdbc-connection-pool." + POOL + ".validate-atmost-once-period-in-seconds=0");
        environment.asadmin("set",
                "resources.jdbc-connection-pool." + POOL + ".property.socketTimeout=0");
        environment.asadmin("set",
                "resources.jdbc-connection-pool." + POOL + ".max-pool-size=32");
        environment.reinitializeDatabase();
    }

    @ParameterizedTest(name = "[{index}] validation={0}, failAll={1}, method={2}, atMostOnce={3}s")
    @MethodSource("poolValidationScenarios")
    public void recreatesConnectionsAfterDatabaseOutage(boolean connectionValidation, boolean failAllConnections,
            String validationMethod, long validateAtMostOncePeriod) throws Exception {
        assertEquals(0, environment.asadmin("flush-connection-pool", POOL).getExitCode());
        configurePool(connectionValidation, failAllConnections, validationMethod, validateAtMostOncePeriod);

        final UserRestClient client = new UserRestClient(wsEndpoint);
        // initialize the pool if it's not already, otherwise flush in the next step fails
        createUsers(client, 1);
        // reset the pool to reset the last validation timestamps
        assertEquals(0, environment.asadmin("flush-connection-pool", POOL).getExitCode());

        // Break every pooled connection with a genuine socket-read failure by running a slow query while
        // socketTimeout is shorter than the query duration. The failed connections stay in the pool.
        LOG.log(INFO, () -> "Simulating a database outage (validation enabled: " + connectionValidation
            + ", method: " + validationMethod
            + ", validate-at-most-once: " + validateAtMostOncePeriod + "s"
            + ", fail-all-connections: " + failAllConnections + ")");
        breakPooledConnections();
        final int createdBefore = environment.asadminMonitor("server.resources." + POOL + ".numconncreated-count");

        // Send requests one per second until 2+ seconds past the validate-at-most-once window to observe when
        // connections are recreated. With validation enabled and period <= 0, all succeed (validation on every checkout).
        // With period > 0, requests before period seconds pass stale connections, requests after period seconds recreate.
        final long atMostOncePeriodSeconds = validateAtMostOncePeriod;
        final long minObservationDurationSeconds = Math.max(2L, atMostOncePeriodSeconds + 2L);
        final Map<Integer, Boolean> requestResults = sendStaggeredRequests(wsEndpoint, minObservationDurationSeconds);
        final int succeeded = (int) requestResults.values().stream().filter(v -> v).count();
        final long total = tryCount(client);

        final int createdAfter = environment.asadminMonitor("server.resources." + POOL + ".numconncreated-count");
        final int destroyed = environment.asadminMonitor("server.resources." + POOL + ".numconndestroyed-count");
        final int failedValidation = environment
                .asadminMonitor("server.resources." + POOL + ".numconnfailedvalidation-count");
        LOG.log(INFO, () -> "After outage: staggered requests total=" + requestResults.size()
            + ", successes=" + succeeded + "/" + requestResults.size()
            + ", total users=" + total
            + ", connections created=" + createdAfter + " (was " + createdBefore + ")"
            + ", destroyed=" + destroyed
            + ", failed validation=" + failedValidation);

        if (!connectionValidation) {
            // Validation disabled: run is purely observational
            LOG.log(INFO, "Validation disabled; behavior depends on connection reuse");
        } else if (atMostOncePeriodSeconds <= 0) {
            // Validation on every checkout: all requests must succeed
            assertThat("with validation-on-every-checkout all requests must succeed", succeeded,
                    equalTo(requestResults.size()));
            assertThat("the pool must destroy at least 1 connection after the outage (an then recreate it)", destroyed,
                    greaterThan(0));
        } else {
            // Validation with period > 0: early requests fail (stale), late requests succeed (recreated)
            assertThat("some early requests must fail (stale connections within validation window)", succeeded,
                    lessThan(requestResults.size()));
            // At least one late request must succeed (after the window)
            assertThat("late requests (after validation window) must start succeeding", succeeded, greaterThan(0));
        }
    }

    private static Stream<Arguments> poolValidationScenarios() {
        return ParameterSetup.poolValidationScenarios();
    }

    private static void configurePool(boolean connectionValidation, boolean failAllConnections,
            String validationMethod, long validateAtMostOncePeriod) {
        final String poolConfigPrefix = "resources.jdbc-connection-pool." + POOL;
        assertEquals(0, environment.asadmin("set",
                poolConfigPrefix + ".is-connection-validation-required=" + connectionValidation)
                .getExitCode());
        assertEquals(0, environment.asadmin("set",
                poolConfigPrefix + ".fail-all-connections=" + failAllConnections)
                .getExitCode());
        // Provide the method-specific attribute before switching the method.
        if ("custom-validation".equals(validationMethod)) {
            assertEquals(0, environment.asadmin("set",
                    poolConfigPrefix + ".validation-classname=" + POSTGRES_VALIDATION_CLASS)
                    .getExitCode());
        } else if ("table".equals(validationMethod)) {
            assertEquals(0, environment.asadmin("set",
                    poolConfigPrefix + ".validation-table-name=" + VALIDATION_TABLE)
                    .getExitCode());
        }
        assertEquals(0, environment.asadmin("set",
                poolConfigPrefix + ".connection-validation-method=" + validationMethod)
                .getExitCode());
        assertEquals(0, environment.asadmin("set",
                poolConfigPrefix + ".validate-atmost-once-period-in-seconds=" + validateAtMostOncePeriod)
                .getExitCode());
        // Fail a blocked socket read quickly so each scenario breaks pooled connections deterministically.
        assertEquals(0, environment.asadmin("set",
                poolConfigPrefix + ".property.socketTimeout=" + SOCKET_TIMEOUT_SECONDS)
                .getExitCode());
        assertEquals(0, environment.asadmin("set",
                poolConfigPrefix + ".max-pool-size=" + MAX_CONNECTIONS_IN_POOL)
                .getExitCode());
        environment.reinitializeDatabase();
    }

    /**
     * Sends one create-user request per second for the given duration,
     * recording which succeed. Returns a map of request index to success (true)
     * or failure (false), allowing verification of when broken connections are
     * detected and recreated based on validation and validate-at-most-once
     * settings.
     *
     * @param endpoint REST endpoint to send requests to
     * @param durationSeconds how long to send staggered requests (minimum 2)
     * @return map of request index to success flag
     */
    private static Map<Integer, Boolean> sendStaggeredRequests(WebTarget endpoint, long durationSeconds) throws InterruptedException {
        final Map<Integer, Boolean> results = new LinkedHashMap<>();
        final long startTimeNanos = System.nanoTime();
        final long endTimeNanos = startTimeNanos + Duration.ofSeconds(durationSeconds).toNanos();
        int requestIndex = 0;

        while (System.nanoTime() < endTimeNanos) {
            final User user = new User(RandomStringUtils.insecure().nextAlphabetic(32));
            final int idx = requestIndex++;
            try (Response response = endpoint.path("user").path("create").request().put(Entity.json(user))) {
                final boolean success = response.getStatusInfo().toEnum() == Status.NO_CONTENT;
                final long elapsedSeconds = (System.nanoTime() - startTimeNanos) / 1_000_000_000L;
                LOG.log(INFO, () -> "Request " + idx + " at " + elapsedSeconds + "s: "
                    + (success ? "success" : "failed"));
                results.put(idx, success);
            } catch (RuntimeException e) {
                final long elapsedSeconds = (System.nanoTime() - startTimeNanos) / 1_000_000_000L;
                LOG.log(INFO, () -> "Request " + idx + " at " + elapsedSeconds + "s: error " + e.getMessage());
                results.put(idx, false);
            }

            // Wait 1 second before next request
            final long nextRequestTimeNanos = startTimeNanos + Duration.ofSeconds(requestIndex).toNanos();
            final long waitNanos = nextRequestTimeNanos - System.nanoTime();
            if (waitNanos > 0) {
                Thread.sleep(waitNanos / 1_000_000L);
            }
        }

        return results;
    }

    /**
     * Breaks every pooled connection with a genuine socket-read failure instead
     * of terminating them on the server. Fires a slow query ({@code pg_sleep})
     * on each pooled connection while {@code socketTimeout} is shorter than the
     * sleep, so those reads fail with an 08-class connection error. The failed
     * connections stay in the pool, so the next batch must detect and replace
     * them.
     */
    private static void breakPooledConnections() throws InterruptedException {
        final WebTarget sleep = wsEndpoint.path("user").path("sleep").path(String.valueOf(SLEEP_QUERY_SECONDS));
        try (ExecutorService executor = Executors.newFixedThreadPool(MAX_CONNECTIONS_IN_POOL)) {
            IntStream.range(0, MAX_CONNECTIONS_IN_POOL)
                    .mapToObj(i -> executor.submit(() -> {
                Response response = sleep.request().get();
                LOG.log(INFO, () -> "Slow query returned status " + response.getStatus() + " (expected a connection failure)");
                if (response.getStatus() >= 500) {
                    throw new RuntimeException("Slow query returned status " + response.getStatus() + ": " + response.getEntity());
                }
            }))
                    .forEach(future -> {
                        final ExecutionException exc = Assertions.assertThrowsExactly(ExecutionException.class, future::get,
                                "Slow query completed successfully but expected a connection failure");
                        LOG.log(INFO, () -> "Slow query failed as expected: " + exc.getCause());
                    });
        }
    }

    private static long tryCount(UserRestClient client) {
        try {
            return client.count();
        } catch (RuntimeException e) {
            LOG.log(INFO, () -> "Count after outage failed: " + e.getMessage());
            return -1L;
        }
    }

    private static void createUsers(UserRestClient client, int count) {
        for (int i = 0; i < count; i++) {
            client.create(new User(RandomStringUtils.insecure().nextAlphabetic(32)));
        }
    }

    private static WebArchive getArchiveToDeploy() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(GlassFishUserRestEndpoint.class, User.class, RestAppConfig.class)
                .addAsWebInfResource("jdbc/pool/war/persistence.xml", "classes/META-INF/persistence.xml")
                .addAsWebInfResource("jdbc/pool/war/orm.xml", "classes/META-INF/orm.xml");
    }

    private static final class ParameterSetup {

        /**
         * Connection validation combinations; override with CSV, e.g.
         * {@code -Dit.connectionValidation=true,false}.
         */
        private static final List<Boolean> CONNECTION_VALIDATION_OPTIONS
                = parseBooleanProperty("it.connectionValidation", "true,false");

        /**
         * Fail-all-connections combinations; override with CSV, e.g.
         * {@code -Dit.failAllConnections=false,true}.
         */
        private static final List<Boolean> FAIL_ALL_CONNECTIONS_OPTIONS
                = parseBooleanProperty("it.failAllConnections", "false,true");

        /**
         * Validation method combinations; override with CSV, e.g.
         * {@code -Dit.validationMethod=custom-validation,auto-commit}.
         */
        private static final List<String> VALIDATION_METHOD_OPTIONS = parseStringProperty("it.validationMethod",
                "custom-validation,auto-commit,meta-data,table");

        /**
         * "Validate At Most Once" period combinations in seconds; override with
         * CSV, e.g. {@code -Dit.validateAtMostOncePeriod=0,3}.
         */
        private static final List<Long> VALIDATE_AT_MOST_ONCE_PERIOD_OPTIONS
                = parseLongProperty("it.validateAtMostOncePeriod", "0,10");

        private static Stream<Arguments> poolValidationScenarios() {
            final List<Arguments> scenarios = new ArrayList<>();
            for (boolean connectionValidation : CONNECTION_VALIDATION_OPTIONS) {
                for (boolean failAllConnections : FAIL_ALL_CONNECTIONS_OPTIONS) {
                    for (String validationMethod : VALIDATION_METHOD_OPTIONS) {
                        for (long validateAtMostOncePeriod : VALIDATE_AT_MOST_ONCE_PERIOD_OPTIONS) {
                            scenarios.add(Arguments.of(connectionValidation, failAllConnections, validationMethod,
                                    validateAtMostOncePeriod));
                        }
                    }
                }
            }
            return scenarios.stream();
        }

        private static List<String> parseCsv(String values) {
            final List<String> parsed = new ArrayList<>();
            for (String value : values.split(",")) {
                final String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    parsed.add(trimmed);
                }
            }
            if (parsed.isEmpty()) {
                throw new IllegalArgumentException("Property must contain at least one non-empty value");
            }
            return parsed;
        }

        private static List<String> parseStringProperty(String key, String defaultValue) {
            return parseCsv(System.getProperty(key, defaultValue));
        }

        private static List<Boolean> parseBooleanProperty(String key, String defaultValue) {
            final List<Boolean> values = new ArrayList<>();
            for (String value : parseCsv(System.getProperty(key, defaultValue))) {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("Invalid boolean for " + key + ": " + value);
                }
                values.add(Boolean.parseBoolean(value));
            }
            return values;
        }

        private static List<Long> parseLongProperty(String key, String defaultValue) {
            final List<Long> values = new ArrayList<>();
            for (String value : parseCsv(System.getProperty(key, defaultValue))) {
                values.add(Long.parseLong(value));
            }
            return values;
        }
    }

}
