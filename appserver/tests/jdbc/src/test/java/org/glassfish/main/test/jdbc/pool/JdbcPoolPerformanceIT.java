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
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.glassfish.main.test.jdbc.pool.war.GlassFishUserRestEndpoint;
import org.glassfish.main.test.jdbc.pool.war.RestAppConfig;
import org.glassfish.main.test.jdbc.pool.war.User;
import org.glassfish.main.test.perf.util.DockerTestEnvironment;
import org.glassfish.main.test.perf.util.UserRestClient;
import org.glassfish.tests.utils.junit.TestLoggingExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.testcontainers.DockerClientFactory;

import static java.lang.Math.min;
import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.test.jdbc.pool.JdbcPoolPerformanceIT.RestClientProvider.SYS_PROPERTY_ENDPOINT;
import static org.glassfish.main.test.perf.util.DockerTestEnvironment.LIMIT_JDBC;
import static org.glassfish.main.test.perf.util.DockerTestEnvironment.asadmin;
import static org.glassfish.main.test.perf.util.DockerTestEnvironment.asadminMonitor;
import static org.glassfish.main.test.perf.util.DockerTestEnvironment.deploy;
import static org.glassfish.main.test.perf.util.DockerTestEnvironment.undeploy;
import static org.glassfish.main.test.perf.util.GlassFishContainer.LIMIT_HTTP_THREADS;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.openjdk.jmh.runner.options.TimeValue.seconds;

@EnabledIfSystemProperty(
    named = "enableHWDependentTests",
    matches = "true",
    disabledReason = "Test depends on hardware performance")
@ExtendWith(TestLoggingExtension.class)
public class JdbcPoolPerformanceIT {
    private static final Logger LOG = System.getLogger(JdbcPoolPerformanceIT.class.getName());
    private static final String APPNAME = "perf";
    private static final int LIMIT_JMH_THREADS = 500;
    private static boolean dockerAvailable;
    private static WebTarget wsEndpoint;

    @BeforeAll
    public static void init() throws Exception {
        dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        assumeTrue(dockerAvailable, "Docker is not available on this environment");
        wsEndpoint = deploy(APPNAME, getArchiveToDeploy());
    }

    @AfterAll
    public static void cleanup() throws Exception {
        if (!dockerAvailable) {
            return;
        }
        undeploy(APPNAME);
        DockerTestEnvironment.reinitializeDatabase();
    }

    @Test
    public void testHeavyLoad() throws Exception {
        Options options = createOptions();
//        Thread networkIssue = new Thread(() -> {
//            try {
//                Thread.sleep(10);
//                DockerTestEnvironment.disconnectDatabase(2);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        });
//        networkIssue.start();

        Collection<RunResult> results = new Runner(options).run();
        // Print all results - for tuning.
        asadmin("get", "--monitor", "*");
        assertThat(results, hasSize(1));
        Result<?> primaryResult = results.iterator().next().getPrimaryResult();
        long usersCreated = new UserRestClient(wsEndpoint).count();
        int jdbcConnAcquired = asadminMonitor("server.resources.domain-pool-A.numconnacquired-count");
        int jdbcConnReleased = asadminMonitor("server.resources.domain-pool-A.numconnreleased-count");
        int jdbcConnCreated = asadminMonitor("server.resources.domain-pool-A.numconncreated-count");
        int jdbcConnCurrent = asadminMonitor("server.resources.domain-pool-A.perf.numconnused-current");
        int jdbcConnFree = asadminMonitor("server.resources.domain-pool-A.numconnfree-current");
        int jdbcMaxUsed = asadminMonitor("server.resources.domain-pool-A.perf.numconnused-highwatermark");
        int limit = min(LIMIT_JMH_THREADS, min(LIMIT_HTTP_THREADS, LIMIT_JDBC));
        double jmhThroughput = primaryResult.getScore();
        LOG.log(INFO, () -> "Results:"
            + "\nJMH throughput: " + jmhThroughput
            + "\nusersCreated: " + usersCreated
            + "\njdbcConnAcquired: " + jdbcConnAcquired
            + "\njdbcConnReleased: " + jdbcConnReleased
            + "\njdbcConnCreated: " + jdbcConnCreated
            + "\njdbcConnCurrent: " + jdbcConnCurrent
            + "\njdbcConnFree: " + jdbcConnFree
            + "\njdbcMaxUsed: " + jdbcMaxUsed
        );
        assertAll(
            () -> assertThat("JMH throughput", jmhThroughput, greaterThan(1_000_000d)),
            () -> assertThat("Records created", usersCreated, greaterThan(170_000L)),
            () -> assertThat("conn released==acquired", jdbcConnAcquired, equalTo(jdbcConnReleased)),
            () -> assertThat("conn acquired", jdbcConnAcquired, greaterThan(limit)),
            () -> assertThat("conn created", jdbcConnCreated, equalTo(limit)),
            () -> assertThat("conn used now", jdbcConnCurrent, equalTo(0)),
            () -> assertThat("conn usable now", jdbcConnFree, equalTo(limit)),
            () -> assertThat("conn highwatermark", jdbcMaxUsed, equalTo(limit))
        );
    }

    private Options createOptions() {
        ChainedOptionsBuilder builder = new OptionsBuilder().include(getClass().getName() + ".*");
        builder.shouldFailOnError(true);
        builder.warmupIterations(0);
        builder.timeUnit(TimeUnit.MILLISECONDS).mode(Mode.Throughput);
        builder.detectJvmArgs().jvmArgsAppend("-D" + SYS_PROPERTY_ENDPOINT + "=" + wsEndpoint.getUri());
        builder.forks(1).threads(LIMIT_JMH_THREADS);
        builder.operationsPerInvocation(1_000_000).measurementTime(seconds(5)).timeout(seconds(30));
        return builder.build();
    }

    @Benchmark
    public void meanResponseTimeBenchmark(RestClientProvider clientProvider) throws Exception {
        User user = new User(RandomStringUtils.insecure().nextAlphabetic(32));
        UserRestClient client = clientProvider.getClient();
        client.create(user);
        List<User> users = client.list();
        assertThat(users, hasSize(allOf(greaterThan(0), lessThanOrEqualTo(100))));
    }


    private static WebArchive getArchiveToDeploy() throws Exception {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(GlassFishUserRestEndpoint.class, User.class, RestAppConfig.class)
            .addAsWebInfResource("jdbc/pool/war/persistence.xml", "classes/META-INF/persistence.xml")
        ;
    }

    @State(Scope.Benchmark)
    public static class RestClientProvider {
        public static final String SYS_PROPERTY_ENDPOINT = "endpoint";
        private static final UserRestClient CLIENT = new UserRestClient(
            URI.create(System.getProperty(SYS_PROPERTY_ENDPOINT)), false);

        public UserRestClient getClient() {
            return CLIENT;
        }
    }
}
