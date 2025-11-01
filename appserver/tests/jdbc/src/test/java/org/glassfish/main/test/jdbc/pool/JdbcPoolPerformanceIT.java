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

import jakarta.ws.rs.client.WebTarget;

import java.lang.System.Logger;
import java.time.Duration;
import java.util.Collection;

import org.glassfish.main.test.jdbc.pool.war.GlassFishUserRestEndpoint;
import org.glassfish.main.test.jdbc.pool.war.RestAppConfig;
import org.glassfish.main.test.jdbc.pool.war.User;
import org.glassfish.main.test.perf.benchmark.Environment;
import org.glassfish.main.test.perf.benchmark.RestBenchmark;
import org.glassfish.main.test.perf.embedded.DockerTestEnvironmentWithEmbedded;
import org.glassfish.main.test.perf.rest.UserRestClient;
import org.glassfish.main.test.perf.server.DockerTestEnvironment;
import org.glassfish.tests.utils.junit.TestLoggingExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.testcontainers.DockerClientFactory;

import static java.lang.Math.min;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_JMH_THREADS;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_POOL_HTTP_THREADS;
import static org.glassfish.main.test.perf.benchmark.BenchmarkLimits.LIMIT_POOL_JDBC;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * <code>
 * mvn clean install -pl :jdbc-tests -Dit.test=JdbcPoolPerformanceIT -DenableHWDependentTests=true -Dit.embedded=true -Dit.disconnectDatabase=false
 * </code>
 */
@EnabledIfSystemProperty(
    named = "enableHWDependentTests",
    matches = "true",
    disabledReason = "Test depends on hardware performance")
@ExtendWith(TestLoggingExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class JdbcPoolPerformanceIT {
    private static final Logger LOG = System.getLogger(JdbcPoolPerformanceIT.class.getName());
    private static final String APPNAME = "perf";
    private static final boolean DOCKER_AVAILABLE = DockerClientFactory.instance().isDockerAvailable();
    private static final boolean EMBEDDED = Boolean.getBoolean("it.embedded");
    private static final boolean DISCONNECT_DB = Boolean.getBoolean("it.disconnectDatabase");
    private static final int CONN_MAX = min(LIMIT_JMH_THREADS, min(LIMIT_POOL_HTTP_THREADS, LIMIT_POOL_JDBC));

    private static Environment env;
    private static WebTarget wsEndpoint;

    @BeforeAll
    public static void init() throws Exception {
        assumeTrue(DOCKER_AVAILABLE, "Docker is not available on this environment");
        WebArchive war = getArchiveToDeploy();
        env = EMBEDDED ? new DockerTestEnvironmentWithEmbedded() : new DockerTestEnvironment();
        wsEndpoint = env.start(APPNAME, war);
    }

    @AfterAll
    public static void cleanup() throws Exception {
        if (!DOCKER_AVAILABLE) {
            return;
        }
        env.stop();
    }

    @Test
    public void createUser() throws Exception {
        Options options = RestBenchmark.createOptions(wsEndpoint.getUri(), "createUser");
        if (DISCONNECT_DB) {
            env.disconnectDatabase(Duration.ofSeconds(2L), Duration.ofSeconds(5));
        }

        Collection<RunResult> results = new Runner(options).run();
        assertThat(results, hasSize(1));
        PerformanceTestResult resultCreate = new PerformanceTestResult(results.iterator().next().getPrimaryResult());
        LOG.log(INFO, () -> "Results(create): " + resultCreate);
        if (!EMBEDDED) {
            assertAll(
                () -> assertThat("conn released==acquired", resultCreate.jdbcConnAcquired, equalTo(resultCreate.jdbcConnReleased)),
                () -> assertThat("conn acquired", resultCreate.jdbcConnAcquired, greaterThan(CONN_MAX)),
                () -> assertThat("conn created", resultCreate.jdbcConnCreated, equalTo(CONN_MAX)),
                () -> assertThat("conn used now", resultCreate.jdbcConnCurrent, equalTo(0)),
                () -> assertThat("conn usable now", resultCreate.jdbcConnFree, equalTo(CONN_MAX)),
                () -> assertThat("conn highwatermark", resultCreate.jdbcMaxUsed, equalTo(CONN_MAX))
            );
        }
        assertAll(
            () -> assertThat("Average Time (ms)", resultCreate.avgTime, lessThan(100d)),
            () -> assertThat("Records created", resultCreate.usersCreated, greaterThan(150_000L))
        );
    }

    @Test
    public void listUsers() throws Exception {
        Options options = RestBenchmark.createOptions(wsEndpoint.getUri(), "listUsers");
        if (DISCONNECT_DB) {
            env.disconnectDatabase(Duration.ofSeconds(2L), Duration.ofSeconds(5));
        }

        Collection<RunResult> results = new Runner(options).run();
        assertThat(results, hasSize(1));
        PerformanceTestResult resultList = new PerformanceTestResult(results.iterator().next().getPrimaryResult());
        LOG.log(INFO, () -> "Results(list): " + resultList);
        if (!EMBEDDED) {
            assertAll(
                () -> assertThat("conn released==acquired", resultList.jdbcConnAcquired, equalTo(resultList.jdbcConnReleased)),
                () -> assertThat("conn acquired", resultList.jdbcConnAcquired, greaterThan(CONN_MAX)),
                () -> assertThat("conn created", resultList.jdbcConnCreated, equalTo(CONN_MAX)),
                () -> assertThat("conn used now", resultList.jdbcConnCurrent, equalTo(0)),
                () -> assertThat("conn usable now", resultList.jdbcConnFree, equalTo(CONN_MAX)),
                () -> assertThat("conn highwatermark", resultList.jdbcMaxUsed, equalTo(CONN_MAX))
            );
        }
        assertAll(
            () -> assertThat("Average Time (ms)", resultList.avgTime, lessThan(2000d)),
            () -> assertThat("Records created", resultList.usersCreated, equalTo(resultList.usersCreated))
        );
    }


    private static WebArchive getArchiveToDeploy() throws Exception {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(GlassFishUserRestEndpoint.class, User.class, RestAppConfig.class)
            .addAsWebInfResource("jdbc/pool/war/persistence.xml", "classes/META-INF/persistence.xml")
        ;
    }

    private static class PerformanceTestResult {
        long usersCreated;
        int jdbcConnAcquired;
        int jdbcConnReleased;
        int jdbcConnCreated;
        int jdbcConnCurrent;
        int jdbcConnFree;
        int jdbcMaxUsed;
        double avgTime;

        PerformanceTestResult(Result<?> result) {
            usersCreated = new UserRestClient(wsEndpoint).count();
            jdbcConnAcquired = asadminMonitor("server.resources.domain-pool-A.numconnacquired-count");
            jdbcConnReleased = asadminMonitor("server.resources.domain-pool-A.numconnreleased-count");
            jdbcConnCreated = asadminMonitor("server.resources.domain-pool-A.numconncreated-count");
            jdbcConnCurrent = asadminMonitor("server.resources.domain-pool-A.perf.numconnused-current");
            jdbcConnFree = asadminMonitor("server.resources.domain-pool-A.numconnfree-current");
            jdbcMaxUsed = asadminMonitor("server.resources.domain-pool-A.perf.numconnused-highwatermark");
            avgTime = result.getScore();
        }


        @Override
        public String toString() {
            return "\nAverage Time (ms): " + avgTime
            + "\nusersCreated: " + usersCreated
            + "\njdbcConnAcquired: " + jdbcConnAcquired
            + "\njdbcConnReleased: " + jdbcConnReleased
            + "\njdbcConnCreated: " + jdbcConnCreated
            + "\njdbcConnCurrent: " + jdbcConnCurrent
            + "\njdbcConnFree: " + jdbcConnFree
            + "\njdbcMaxUsed: " + jdbcMaxUsed;
        }

        private int asadminMonitor(String string) {
            if (env instanceof DockerTestEnvironmentWithEmbedded) {
                return 0;
            }
            DockerTestEnvironment gfEnv = (DockerTestEnvironment) env;
            return gfEnv.asadminMonitor(string);
        }
    }
}
