/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.main.test.app.monitoring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@TestMethodOrder(MethodName.class)
public class ThreadPoolMonitoringTest {

    private static final Logger LOG = System.getLogger(ThreadPoolMonitoringTest.class.getName());
    private static final int HTTP_REQUEST_TIMEOUT = 5000;
    private static final String APP_NAME = "threadpool-test";
    private static final String APP_CONTEXT = "/threadpool-test";
    private static final String CONTEXT_FAST = APP_CONTEXT + "/test";
    private static final String CONTEXT_LOCK = APP_CONTEXT + "/lock?action=lock";
    private static final String CONTEXT_UNLOCK = APP_CONTEXT + "/lock?action=unlock";
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    private static final List<AutoCloseable> CLOSEABLES = new ArrayList<>();
    private static final List<String> LISTENER_NAMES = new ArrayList<>();
    private static final List<UUID> LOCKS = new ArrayList<>();

    private static boolean stop;

    @TempDir
    private static File tmpDir;

    @RegisterExtension
    AfterTestExecutionCallback afterTestExecutionCallback = context -> {
        if (context.getExecutionException().isPresent()) {
            stop = true;
        }
    };
    private ThreadPoolMetrics http1Baseline;

    @BeforeAll
    static void beforeAll() throws Exception {
        deploy();
        // Enable monitoring
        assertThat(
            ASADMIN.exec("set", "configs.config.server-config.monitoring-service.monitoring-enabled=true"),
            asadminOK());
        assertThat(
            ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH"),
            asadminOK());
        assertThat(
            ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.http-service=HIGH"),
            asadminOK());

        assertThat(httpGet(8080, CONTEXT_FAST), stringContainsInOrder("http-listener-1"));
        final ThreadPoolMetrics adminBaseline = getThreadPoolMetrics("admin-listener");
        assertThat("admin listener current threads", adminBaseline.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("admin listener max threads", adminBaseline.maxThreads(), greaterThan(0));
        assertThat("admin listener busy threads", adminBaseline.currentThreadsBusy(), greaterThanOrEqualTo(0));
    }

    @AfterAll
    static void afterAll() {
        ASADMIN.exec("undeploy", APP_NAME);
        // Disable monitoring
        ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=OFF");
        ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.http-service=OFF");
    }

    @BeforeEach
    void beforeEach() {
        assumeFalse(stop);
        http1Baseline = getThreadPoolMetrics();
    }

    @AfterEach
    void afterEach() throws Throwable {
        if (!LOCKS.isEmpty()) {
            HttpLoadGenerator.unlockAppThreads(8080, LOCKS).close();
        }
        for (AutoCloseable closeable : CLOSEABLES) {
            closeable.close();
        }
        CLOSEABLES.clear();
        for (String listenerName : LISTENER_NAMES) {
            ASADMIN.exec("delete-http-listener", listenerName);
        }
        if (stop) {
            return;
        }
        assertThat(
            ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH"),
            asadminOK());
        assertThat(ASADMIN.exec("set",
            "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size="
                + http1Baseline.maxThreads()),
            asadminOK());
    }

    /**
     * Create load on admin port (4848) using asadmin commands
     */
    @Test
    void testAdminListenerThreadPoolMetrics() throws Exception {
        AsadminLoadGenerator.startGenerator();
        ThreadPoolMetrics metrics = getThreadPoolMetrics("admin-listener");
        waitFor(Duration.ofSeconds(1L), () -> {
            assertAll(
                () -> assertThat("admin listener current threads under load", metrics.currentThreadCount(),
                    greaterThanOrEqualTo(0)),
                () -> assertThat("admin listener busy threads under load", metrics.currentThreadsBusy(),
                    greaterThan(0)),
                () -> assertThat("admin listener core threads under load", metrics.coreThreads(),
                    greaterThan(0)),
                () -> assertThat("admin listener max threads under load", metrics.maxThreads(),
                    greaterThan(0)),
                () -> assertThat("admin listener tasks", metrics.totalTasks(),
                    greaterThan(0))
            );
            return null;
        });
    }

    @Test
    void testDualListenerHugeAmountOfFastRequests() throws Exception {
        final String listenerTestName = "http-listener-test";
        final int listenerTestPort = 8081;
        LISTENER_NAMES.add(listenerTestName);
        assertThat(ASADMIN.exec("create-http-listener", "--listenerport=" + listenerTestPort,
            "--listeneraddress=0.0.0.0", "--defaultvs=server", listenerTestName), asadminOK());

        final String listener1Name = "http-listener-1";
        AsadminResult listResult = ASADMIN.exec("list", "-m", "*thread-pool*");
        assertThat(listResult, asadminOK());
        assertThat(listResult.getStdOut(), stringContainsInOrder(listenerTestName, listener1Name));

        final ThreadPoolMetrics metrics1Baseline = getThreadPoolMetrics(listener1Name);
        final ThreadPoolMetrics metricsTestBaseline = getThreadPoolMetrics(listenerTestName);

        assertThat("listener 1 current threads", metrics1Baseline.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("listener Test current threads", metricsTestBaseline.currentThreadCount(), equalTo(5));

        assertThat(httpGet(listenerTestPort, CONTEXT_FAST), stringContainsInOrder("http-listener-test"));

        // Create load on both listeners simultaneously
        final HttpLoadGenerator generator1 = HttpLoadGenerator.startGenerator(8080, CONTEXT_FAST, 5, 1_000_000);
        final HttpLoadGenerator generatorTest = HttpLoadGenerator.startGenerator(listenerTestPort, CONTEXT_FAST, 5, 1_000_000);

        waitFor(Duration.ofSeconds(60L), () -> {
            ThreadPoolMetrics metrics1 = getThreadPoolMetrics(listener1Name);
            assertThat("listener 1 tasks", metrics1.totalTasks(), greaterThan(metrics1Baseline.totalTasks()));
            ThreadPoolMetrics metricsTest = getThreadPoolMetrics(listenerTestName);
            assertThat("listener Test tasks", metricsTest.totalTasks(), greaterThan(metricsTestBaseline.totalTasks()));
            return null;
        });

        generator1.close();
        generatorTest.close();
        final ThreadPoolMetrics metrics1 = waitFor(Duration.ofSeconds(10L), () -> {
            ThreadPoolMetrics metrics = getThreadPoolMetrics(listener1Name);
            assertThat("listener 1 busy", metrics.currentThreadsBusy(), equalTo(0));
            return metrics;
        });
        final ThreadPoolMetrics metricsTest = waitFor(Duration.ofSeconds(10L), () -> {
            ThreadPoolMetrics metrics = getThreadPoolMetrics(listenerTestName);
            assertThat("listener Test busy", metrics.currentThreadsBusy(), equalTo(0));
            return metrics;
        });

        assertAll(
            // 1 could be already on the limit
            () -> assertThat("listener 1 current threads", metrics1.currentThreadCount(),
                greaterThanOrEqualTo(metrics1Baseline.currentThreadCount())),
            () -> assertThat("listener Test current threads", metricsTest.currentThreadCount(),
                greaterThanOrEqualTo(metricsTestBaseline.currentThreadCount())),

            // Thread counts should be consistent with busy counts
            () -> assertThat("listener 1 busy threads", metrics1.currentThreadsBusy(),
                lessThanOrEqualTo(metrics1.currentThreadCount())),
            () -> assertThat("listener Test busy threads", metricsTest.currentThreadsBusy(),
                lessThanOrEqualTo(metricsTest.currentThreadCount()))
        );


        final ThreadPoolMetrics metrics1Final = getThreadPoolMetrics(listener1Name);
        final ThreadPoolMetrics metricsTestFinal = getThreadPoolMetrics(listenerTestName);
        assertAll("Client closed, metrics should not change",
            () -> assertEquals(metrics1, metrics1Final),
            () -> assertEquals(metricsTest, metricsTestFinal)
        );
    }

    /** Basic sanity checks */
    @Test
    void testThreadPoolMetricsBaseline() throws Exception {
        ThreadPoolMetrics metrics = getThreadPoolMetrics();
        assertAll(
            () -> assertThat("current thread count", metrics.currentThreadCount(), greaterThanOrEqualTo(0)),
            () -> assertThat("busy thread count", metrics.currentThreadsBusy(), greaterThanOrEqualTo(0)),
            () -> assertThat("max threads", metrics.maxThreads(), greaterThan(0)),
            () -> assertThat("total tasks", metrics.totalTasks(), greaterThanOrEqualTo(metrics.totalTasks())),
            () -> assertThat("busy threads", metrics.currentThreadsBusy(), lessThanOrEqualTo(metrics.currentThreadCount())),
            () -> assertThat("current threads", metrics.currentThreadCount(), lessThanOrEqualTo(metrics.maxThreads()))
        );
    }

    @Test
    void testThreadPoolMetricsReadTimeout() throws Exception {
        final int remainingThreadCount = 1;
        final List<UUID> locks = generateLocks(http1Baseline.maxThreads() - remainingThreadCount);
        final HttpLoadGenerator lockGenerator = HttpLoadGenerator.lockAppThreads(8080, locks);
        final ThreadPoolMetrics duringLoad = waitFor(Duration.ofSeconds(10L), () -> {
            ThreadPoolMetrics metrics = getThreadPoolMetrics();
            assertThat("listener 1 tasks", metrics.currentThreadsBusy(), equalTo(locks.size()));
            return metrics;
        });

        // All client threads are already running, now we let them wait for the server response.
        // They will time out.
        Thread.sleep(HTTP_REQUEST_TIMEOUT + 100L);

        assertAll("metrics under load - no response yet",
            () -> assertThat("current threads", duringLoad.currentThreadCount(), equalTo(http1Baseline.maxThreads())),
            () -> assertThat("busy threads", duringLoad.currentThreadsBusy(), equalTo(locks.size())),
            () -> assertThat("total tasks", duringLoad.totalTasks(), equalTo(http1Baseline.totalTasks()))
        );

        final HttpLoadGenerator unlockGenerator = HttpLoadGenerator.unlockAppThreads(8080, locks);
        unlockGenerator.close();
        lockGenerator.close();

        final ThreadPoolMetrics afterLoad = getThreadPoolMetrics();
        assertAll("metrics after client stopped",
            () -> assertThat("current threads", afterLoad.currentThreadCount(), equalTo(afterLoad.maxThreads())),
            () -> assertThat("busy threads", afterLoad.currentThreadsBusy(), equalTo(0)),
            () -> assertThat("total tasks", afterLoad.totalTasks(), greaterThan(http1Baseline.totalTasks()))
        );
    }

    @Test
    void testThreadPoolMetricsWithBurstLoad() throws Exception {
        final HttpLoadGenerator generator = HttpLoadGenerator.startGenerator(8080, CONTEXT_FAST, 50, 100);
        // Check metrics immediately during burst
        ThreadPoolMetrics duringBurst = getThreadPoolMetrics();
        generator.close();
        ThreadPoolMetrics afterBurst = getThreadPoolMetrics();

        // Validate all metrics remain within bounds
        assertAll("after client stopped",
            () -> assertThat("current threads during burst", duringBurst.currentThreadCount(),
                greaterThanOrEqualTo(http1Baseline.currentThreadCount())),
            () -> assertThat("busy threads during burst", duringBurst.currentThreadsBusy(),
                greaterThanOrEqualTo(0)),
            () -> assertThat("busy threads during burst", duringBurst.currentThreadsBusy(),
                lessThanOrEqualTo(duringBurst.currentThreadCount())),
            () -> assertThat("total tasks during burst", duringBurst.totalTasks(),
                greaterThan(http1Baseline.totalTasks())),

            () -> assertThat("current threads after burst", afterBurst.currentThreadCount(),
                greaterThanOrEqualTo(0)),
            () -> assertThat("busy threads after burst", afterBurst.currentThreadsBusy(),
                greaterThanOrEqualTo(0)),
            () -> assertThat("busy threads after burst", afterBurst.currentThreadsBusy(),
                lessThanOrEqualTo(afterBurst.currentThreadCount())),
            () -> assertThat("total tasks after burst", afterBurst.totalTasks(),
                greaterThanOrEqualTo(duringBurst.totalTasks()))
        );
    }


    @Test
    void testThreadPoolMonitoringEnableDisable() throws Exception {
        // 1. Disable thread-pool monitoring
        assertThat(
            ASADMIN.exec("set",
                "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=OFF"),
            asadminOK());

        // 2. Schedule 3 long-running requests
        final int count = 3;
        final List<UUID> locks = generateLocks(count);
        final HttpLoadGenerator lockGenerator = HttpLoadGenerator.lockAppThreads(8080, locks);

        waitFor(Duration.ofSeconds(10L), () -> {
            assertThat(getThreadPoolMetrics().currentThreadsBusy(), equalTo(count));
            return null;
        });

        // 3. Enable thread-pool monitoring while requests are running
        assertThat(
            ASADMIN.exec("set",
                "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH"),
            asadminOK());

        // 4. Verify thread metrics show running requests
        final ThreadPoolMetrics metrics = getThreadPoolMetrics();
        assertAll("Under load",
            () -> assertThat("current thread count", metrics.currentThreadCount(), equalTo(http1Baseline.currentThreadCount())),
            () -> assertThat("busy threads", metrics.currentThreadsBusy(), equalTo(count))
        );

        // Wait for requests to complete
        final HttpLoadGenerator unlockGenerator = HttpLoadGenerator.unlockAppThreads(8080, locks);
        unlockGenerator.close();
        lockGenerator.close();

        assertThat("busy threads after completion", getThreadPoolMetrics().currentThreadsBusy(), equalTo(0));
    }

    @Test
    void testThreadPoolSizeCycling() throws Exception {
        final ThreadPoolConfig initialConfig = getThreadPoolConfig();
        final int originalMinThreads = initialConfig.minThreads();
        final int originalMaxThreads = initialConfig.maxThreads();
        final ThreadPoolMetrics origMetrics = getThreadPoolMetrics();

        assertThat("max threads in pool matches config", origMetrics.maxThreads(), equalTo(initialConfig.maxThreads()));
        final int[] testSizes = {originalMaxThreads + 3, originalMaxThreads - 1, originalMaxThreads + 7, originalMaxThreads};
        for (int i = 0; i < testSizes.length; i++) {
            final int size = testSizes[i];
            final int index = i;
            LOG.log(INFO, "Case " + index + ": testSize=" + size);

            final int testSize = Math.max(1, size);

            assertThat(ASADMIN.exec("set",
                "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + testSize),
                asadminOK());

            int currentThreadsMaxSize = Math.max(testSize, originalMinThreads);
            final ThreadPoolMetrics metrics = getThreadPoolMetrics();
            assertAll(
                () -> assertThat("max threads (case " + index + ", testSize=" + testSize + ")",
                    metrics.maxThreads(), equalTo(testSize)),
                () -> assertThat("current threads (case " + index + ", testSize=" + testSize + ")",
                    metrics.currentThreadCount(), lessThanOrEqualTo(currentThreadsMaxSize)),
                () -> assertThat("busy threads (case " + index + ", testSize=" + testSize + ")",
                    metrics.currentThreadsBusy(), lessThanOrEqualTo(metrics.currentThreadCount())));
        }
    }

    @Test
    void testThreadPoolSizeDecrease() throws Exception {
        // First increase pool size to 100
        assertThat(
            ASADMIN.exec("set",
                "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=100"),
            asadminOK());

        waitFor(Duration.ofSeconds(10L), () -> {
            assertThat("max threads after increase", getThreadPoolMetrics().maxThreads(), equalTo(100));
            return null;
        });

        final int count = 80;
        // Generate significant load to utilize the large pool
        final List<UUID> locks = generateLocks(count);
        final HttpLoadGenerator lockGenerator = HttpLoadGenerator.lockAppThreads(8080, locks);
        waitFor(Duration.ofSeconds(10L), () -> {
            ThreadPoolMetrics metrics = getThreadPoolMetrics();
            assertThat("busy threads under heavy load", metrics.currentThreadsBusy(), equalTo(count));
            assertThat("current thread count under heavy load", metrics.currentThreadCount(), equalTo(count));
            return metrics;
        });

        // Now decrease pool size to 10 while under load
        final int count2 = 10;
        assertThat(ASADMIN.exec("set",
            "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + count2),
            asadminOK());

        ThreadPoolMetrics afterDecrease = getThreadPoolMetrics();
        assertAll("afterDecrease",
            () -> assertThat("max threads", afterDecrease.maxThreads(), equalTo(count2)),
            () -> assertThat("current threads", afterDecrease.currentThreadCount(), equalTo(afterDecrease.coreThreads())),
            () -> assertThat("busy threads", afterDecrease.currentThreadsBusy(), equalTo(0))
        );

        final HttpLoadGenerator unlockGenerator = HttpLoadGenerator.unlockAppThreads(8080, locks);
        unlockGenerator.close();
        lockGenerator.close();

        ThreadPoolMetrics afterCompletion = getThreadPoolMetrics();
        assertAll("afterCompletition",
            () -> assertThat("busy threads", afterCompletion.currentThreadsBusy(), equalTo(0)),
            () -> assertThat("current threads", afterCompletion.currentThreadCount(), equalTo(afterCompletion.maxThreads()))
        );
    }

    @Test
    void testThreadPoolSizeIncrease() throws Exception {
        // Increase pool size significantly
        final int newMaxThreads = http1Baseline.maxThreads() + 100;
        assertThat(ASADMIN.exec("set",
            "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size="
                + newMaxThreads),
            asadminOK());

        final ThreadPoolMetrics afterIncrease = getThreadPoolMetrics();
        assertThat("max threads after increase", afterIncrease.maxThreads(), equalTo(newMaxThreads));

        List<UUID> locks = generateLocks(http1Baseline.maxThreads() + 10);
        HttpLoadGenerator lockGenerator = HttpLoadGenerator.lockAppThreads(8080,  locks);
        ThreadPoolMetrics loaded = getThreadPoolMetrics();
        HttpLoadGenerator unlockGenerator = HttpLoadGenerator.unlockAppThreads(8080,  locks);
        unlockGenerator.close();
        lockGenerator.close();

        assertThat("current thread count under load", loaded.currentThreadCount(), equalTo(locks.size()));
    }

    private static List<UUID> generateLocks(int count) {
        List<UUID> ids = Stream.generate(UUID::randomUUID).limit(count).collect(Collectors.toList());
        LOCKS.addAll(ids);
        return ids;
    }

    /**
     * @return "http-listener-1"
     */
    private static ThreadPoolMetrics getThreadPoolMetrics() {
        return getThreadPoolMetrics("http-listener-1");
    }

    private static ThreadPoolMetrics getThreadPoolMetrics(String listenerName) {
        String basePath = "server.network." + listenerName + ".thread-pool";
        AsadminResult result = ASADMIN.exec("get", "-m", basePath + ".*");
        assertThat(result, asadminOK());
        return new ThreadPoolMetrics(
                extractMetric(result.getStdOut(), "currentthreadcount", null),
                extractMetric(result.getStdOut(), "currentthreadsbusy", null),
                extractMetric(result.getStdOut(), "corethreads", null),
                extractMetric(result.getStdOut(), "maxthreads", null),
                extractMetric(result.getStdOut(), "totalexecutedtasks", null)
        );
    }

    private static int extractMetric(String output, String metric, Integer defaultValue) {
        String[] lines = output.split("\n");
        assertThat(lines, arrayWithSize(greaterThan(10)));
        final String metricId = metric + "-count";
        for (String line : lines) {
            if (line.contains(metricId)) {
                return Integer.parseInt(line.split("=")[1].trim());
            }
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        throw new IllegalStateException("Metric not found: " + metricId);
    }

    private static String httpGet(int port, String context) {
        final HttpURLConnection conn = openHttpGetConnection(port, context);
        try {
            assertEquals(200, conn.getResponseCode(), "HTTP Response Code");
            try (InputStream output = conn.getInputStream()) {
                return new String(output.readAllBytes(), UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            conn.disconnect();
        }
    }

    private static HttpURLConnection openHttpGetConnection(int port, String context) {
        try {
            HttpURLConnection connection = GlassFishTestEnvironment.openConnection(port, context);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(100);
            connection.setReadTimeout(HTTP_REQUEST_TIMEOUT);
            return connection;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open connection to " + context, e);
        }
    }


    private static <T> T waitFor(Duration maxTime, Action<T> action) throws InterruptedException {
        final long start = System.currentTimeMillis();
        final long timeout = start + maxTime.toMillis();
        while (true) {
            try {
                T result = action.doAction();
                LOG.log(INFO, "Action passed after {0} ms", System.currentTimeMillis() - start);
                return result;
            } catch (AssertionError e) {
                if (timeout < System.currentTimeMillis()) {
                    throw e;
                }
                Thread.sleep(100L);
            }
        }
    }



    private static void deploy() {
        File warFile = createDeployment();
        try {
            assertThat(ASADMIN.exec("deploy", "--force", warFile.getAbsolutePath()), asadminOK());
        } finally {
            warFile.delete();
        }
    }

    private static File createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .addClass(TestServlet.class)
                .addClass(LockServlet.class);

        File warFile = new File(tmpDir, APP_NAME + ".war");
        war.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }

    private ThreadPoolConfig getThreadPoolConfig() {
        return getThreadPoolConfig("http-thread-pool");
    }

    private ThreadPoolConfig getThreadPoolConfig(String threadPoolName) {
        String basePath = "configs.config.server-config.thread-pools.thread-pool.." + threadPoolName;
        AsadminResult minResult = ASADMIN.exec("get", basePath + ".min-thread-pool-size");
        AsadminResult maxResult = ASADMIN.exec("get", basePath + ".max-thread-pool-size");

        return new ThreadPoolConfig(
                extractConfig(minResult.getStdOut(), "min-thread-pool-size"),
                extractConfig(maxResult.getStdOut(), "max-thread-pool-size")
        );
    }

    private int extractConfig(String output, String key) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.contains(key)) {
                return Integer.parseInt(line.split("=")[1].trim());
            }
        }
        throw new IllegalStateException("Config key not found: " + key);
    }

    private record ThreadPoolMetrics(int currentThreadCount, int currentThreadsBusy, int coreThreads, int maxThreads, int totalTasks) {
    }

    private record ThreadPoolConfig(int minThreads, int maxThreads) {
    }

    private static class HttpLoadGenerator extends Thread implements AutoCloseable {

        private final AtomicInteger threadCounter = new AtomicInteger();
        private final AtomicInteger requestCounter = new AtomicInteger();
        private final int maxParallel;
        private final ExecutorService executor;
        private final Action<?> action;
        private final int maxRequests;

        /**
         * Executes a thread for every lock, which will need another server thread which will
         * be blocked by the request.
         * Then automatically terminates itself and returns itself.
         * You then have to call close which will wait for the termination of all client threads.
         */
        static HttpLoadGenerator lockAppThreads(int port, List<UUID> locks) throws InterruptedException {
            HttpLoadGenerator generator = new HttpLoadGenerator(port, locks, true);
            generator.start();
            generator.join();
            return generator;
        }

        /**
         * WARNING: The server must have enough free threads to unlock those which were locked!
         */
        static HttpLoadGenerator unlockAppThreads(int port, List<UUID> locks) throws InterruptedException {
            List<UUID> locksCopy = List.copyOf(locks);
            HttpLoadGenerator generator = new HttpLoadGenerator(port, locksCopy, false);
            LOCKS.removeAll(locksCopy);
            generator.start();
            generator.join();
            return generator;
        }

        static HttpLoadGenerator startGenerator(int port, String context, int maxParallel, int maxRequests) {
            HttpLoadGenerator generator = new HttpLoadGenerator(port, context, maxParallel, maxRequests);
            generator.start();
            return generator;
        }

        HttpLoadGenerator(int port, List<UUID> locks, boolean lock) {
            CLOSEABLES.add(this);
            setName("HttpLoadGenerator");
            this.maxParallel = locks.size();
            this.maxRequests = locks.size();
            this.executor = Executors.newFixedThreadPool(maxParallel,
                t -> new Thread(t, "HttpClient-" + threadCounter.incrementAndGet()));
            final ConcurrentLinkedDeque<UUID> lockIds = new ConcurrentLinkedDeque<>(locks);
            final String actionContext = lock ? CONTEXT_LOCK : CONTEXT_UNLOCK;
            this.action = () -> httpGet(port, actionContext + "&idLock=" + lockIds.remove());
        }

        HttpLoadGenerator(int port, String context, int maxParallel, int maxRequests) {
            CLOSEABLES.add(this);
            setName("HttpLoadGenerator");
            this.maxParallel = maxParallel;
            this.maxRequests = maxRequests;
            this.executor = Executors.newFixedThreadPool(maxParallel,
                t -> new Thread(t, "HttpClient-" + threadCounter.incrementAndGet()));
            this.action = () -> httpGet(port, context);
        }

        @Override
        public void run() {
            final AtomicInteger countRunning = new AtomicInteger(0);
            while (!isInterrupted()) {
                if (requestCounter.getAndIncrement() > maxRequests) {
                    LOG.log(INFO, "Already produced {0} requests, stopping.", maxRequests);
                    executor.shutdown();
                    return;
                }
                if (countRunning.get() == maxParallel) {
                    LOG.log(TRACE, "Waiting...");
                    Thread.onSpinWait();
                    continue;
                }
                LOG.log(DEBUG, "Starting another...");
                countRunning.incrementAndGet();
                executor.submit(() -> {
                    action.doAction();
                    countRunning.decrementAndGet();
                });
            }
        }

        /**
         * Interrupts the thread is it is alive
         * Then waits until it terminates.
         * Then shuts down the executor pool.
         * Then waits until all threads finish.
         */
        @Override
        public void close() throws InterruptedException {
            // First interrupt the thread
            if (isAlive()) {
                synchronized(this) {
                    interrupt();
                }
            }
            // ... then wait until it really terminates
            join();
            // ... then shutdown executor (but not its threads)
            executor.shutdown();
            // ... then wait until all threads finish.
            assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS),
                "Timeout when terminating HTTP load generator!");
        }
    }

    private static class AsadminLoadGenerator extends Thread implements AutoCloseable {

        private final AtomicInteger threadCounter = new AtomicInteger();
        private final int maxParallel = 5;
        private final ExecutorService executor;

        @SuppressWarnings("resource")
        static void startGenerator() {
            new AsadminLoadGenerator().start();
        }

        AsadminLoadGenerator() {
            CLOSEABLES.add(this);
            setName("AsadminLoadGenerator");
            executor = Executors.newFixedThreadPool(maxParallel,
                t -> new Thread(t, "AsadminClient-" + threadCounter.incrementAndGet()));
        }

        @Override
        public void run() {
            final AtomicInteger countRunning = new AtomicInteger(0);
            while (!isInterrupted()) {
                if (countRunning.get() > maxParallel) {
                    Thread.onSpinWait();
                    continue;
                }
                LOG.log(DEBUG, "Starting another...");
                countRunning.incrementAndGet();
                executor.submit(() -> {
                    ASADMIN.exec("list", "applications");
                    ASADMIN.exec("get", "server.monitoring-service.*");
                    countRunning.decrementAndGet();
                });
            }
        }

        @Override
        public void close() throws InterruptedException {
            if (isAlive()) {
                synchronized(this) {
                    this.interrupt();
                }
            }
            join();
            executor.shutdown();
            assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS),
                "Timeout when terminating asadmin load generator!");
        }
    }


    @FunctionalInterface
    interface Action<T> {
        T doAction();
    }
}
