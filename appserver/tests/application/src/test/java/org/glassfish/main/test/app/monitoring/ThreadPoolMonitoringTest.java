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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

public class ThreadPoolMonitoringTest {

    private static final Logger LOG = Logger.getLogger(ThreadPoolMonitoringTest.class.getName());
    private static final String APP_NAME = "threadpool-test";
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @BeforeAll
    static void deployApp() throws IOException {
        File warFile = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", "--force", warFile.getAbsolutePath());
            assertThat(result, AsadminResultMatcher.asadminOK());

            // Enable monitoring
            ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH");
            ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.http-service=HIGH");
            ASADMIN.exec("set", "configs.config.server-config.monitoring-service.monitoring-enabled=true");
        } finally {
            warFile.delete();
        }
    }

    @AfterAll
    static void undeployApp() {
        ASADMIN.exec("undeploy", APP_NAME);
        // Disable monitoring
        ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=OFF");
        ASADMIN.exec("set", "configs.config.server-config.monitoring-service.module-monitoring-levels.http-service=OFF");
    }

    @Test
    void testThreadPoolMetricsUnderLoad() throws Exception {
        // Get baseline metrics
        ThreadPoolMetrics baseline = getThreadPoolMetrics();
        assertThat("baseline current thread count", baseline.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("baseline busy threads", baseline.currentThreadsBusy(), greaterThanOrEqualTo(0));
        assertThat("baseline busy threads", baseline.currentThreadsBusy(), lessThanOrEqualTo(baseline.currentThreadCount()));

        // Generate concurrent load
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<?>[] futures = new CompletableFuture[20];

        for (int i = 0; i < futures.length; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    HttpURLConnection conn = GlassFishTestEnvironment.openConnection(8080, "/threadpool-test/slow");
                    conn.setRequestMethod("GET");
                    conn.getResponseCode();
                    conn.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // Check metrics during load
        Thread.sleep(100); // Let requests start
        ThreadPoolMetrics duringLoad = getThreadPoolMetrics();

        assertThat("current threads during load", duringLoad.currentThreadCount(), greaterThanOrEqualTo(baseline.currentThreadCount()));
        assertThat("busy threads during load", duringLoad.currentThreadsBusy(), greaterThanOrEqualTo(1));
        assertThat("busy threads during load", duringLoad.currentThreadsBusy(), lessThanOrEqualTo(duringLoad.currentThreadCount()));
        assertThat("current threads during load", duringLoad.currentThreadCount(), lessThanOrEqualTo(duringLoad.maxThreads()));

        // Wait for completion
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Check metrics after load
        Thread.sleep(1000); // Let threads settle
        ThreadPoolMetrics afterLoad = getThreadPoolMetrics();

        assertThat("busy threads after load", afterLoad.currentThreadsBusy(), lessThanOrEqualTo(duringLoad.currentThreadsBusy()));
        assertThat("busy threads after load", afterLoad.currentThreadsBusy(), greaterThanOrEqualTo(0));
        assertThat("busy threads after load", afterLoad.currentThreadsBusy(), lessThanOrEqualTo(afterLoad.currentThreadCount()));
    }

    @Test
    void testThreadPoolMetricsBaseline() throws Exception {
        ThreadPoolMetrics metrics = getThreadPoolMetrics();

        // Basic sanity checks
        assertThat("current thread count", metrics.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("busy thread count", metrics.currentThreadsBusy(), greaterThanOrEqualTo(0));
        assertThat("max threads", metrics.maxThreads(), greaterThan(0));

        // Logical consistency
        assertThat("busy threads", metrics.currentThreadsBusy(), lessThanOrEqualTo(metrics.currentThreadCount()));
        assertThat("current threads", metrics.currentThreadCount(), lessThanOrEqualTo(metrics.maxThreads()));
    }

    @Test
    void testThreadPoolMetricsWithSequentialRequests() throws Exception {
        ThreadPoolMetrics baseline = getThreadPoolMetrics();

        // Make sequential requests to see if metrics respond
        for (int i = 0; i < 5; i++) {
            HttpURLConnection conn = GlassFishTestEnvironment.openConnection(8080, "/threadpool-test/test");
            conn.setRequestMethod("GET");
            conn.getResponseCode();
            conn.disconnect();
            Thread.sleep(100);
        }

        ThreadPoolMetrics afterSequential = getThreadPoolMetrics();

        // Metrics should remain consistent
        assertThat("current thread count after sequential", afterSequential.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("busy threads after sequential", afterSequential.currentThreadsBusy(), greaterThanOrEqualTo(0));
        assertThat("busy threads after sequential", afterSequential.currentThreadsBusy(), lessThanOrEqualTo(afterSequential.currentThreadCount()));
    }

    @Test
    void testThreadPoolMetricsWithBurstLoad() throws Exception {
        ThreadPoolMetrics baseline = getThreadPoolMetrics();

        // Create burst of quick requests
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CompletableFuture<?>[] futures = new CompletableFuture[100];

        for (int i = 0; i < futures.length; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    HttpURLConnection conn = GlassFishTestEnvironment.openConnection(8080, "/threadpool-test/test");
                    conn.setRequestMethod("GET");
                    conn.getResponseCode();
                    conn.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // Check metrics immediately during burst
        ThreadPoolMetrics duringBurst = getThreadPoolMetrics();

        // Wait for completion
        CompletableFuture.allOf(futures).get(15, TimeUnit.SECONDS);
        executor.shutdown();

        ThreadPoolMetrics afterBurst = getThreadPoolMetrics();

        // Validate all metrics remain within bounds
        assertThat("current threads during burst", duringBurst.currentThreadCount(), greaterThanOrEqualTo(baseline.currentThreadCount()));
        assertThat("busy threads during burst", duringBurst.currentThreadsBusy(), greaterThanOrEqualTo(0));
        assertThat("busy threads during burst", duringBurst.currentThreadsBusy(), lessThanOrEqualTo(duringBurst.currentThreadCount()));

        assertThat("current threads after burst", afterBurst.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("busy threads after burst", afterBurst.currentThreadsBusy(), greaterThanOrEqualTo(0));
        assertThat("busy threads after burst", afterBurst.currentThreadsBusy(), lessThanOrEqualTo(afterBurst.currentThreadCount()));
    }

    @Test
    void testThreadPoolMetricsConsistency() throws Exception {
        // Take multiple samples to check for consistency
        ThreadPoolMetrics[] samples = new ThreadPoolMetrics[5];

        for (int i = 0; i < samples.length; i++) {
            samples[i] = getThreadPoolMetrics();
            Thread.sleep(200);
        }

        // All samples should have consistent logical relationships
        for (ThreadPoolMetrics sample : samples) {
            assertThat("current threads", sample.currentThreadCount(), greaterThanOrEqualTo(0));
            assertThat("busy threads", sample.currentThreadsBusy(), greaterThanOrEqualTo(0));
            assertThat("max threads", sample.maxThreads(), greaterThan(0));
            assertThat("busy threads", sample.currentThreadsBusy(), lessThanOrEqualTo(sample.currentThreadCount()));
            assertThat("current threads", sample.currentThreadCount(), lessThanOrEqualTo(sample.maxThreads()));
        }
    }

    @Test
    void testThreadPoolMetricsUnderSustainedLoad() throws Exception {
        ThreadPoolMetrics baseline = getThreadPoolMetrics();

        // Create sustained load for longer period
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CompletableFuture<?>[] futures = new CompletableFuture[16];

        for (int i = 0; i < futures.length; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    // Multiple slow requests per thread
                    for (int j = 0; j < 3; j++) {
                        HttpURLConnection conn = GlassFishTestEnvironment.openConnection(8080, "/threadpool-test/slow");
                        conn.setRequestMethod("GET");
                        conn.getResponseCode();
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }

        // Sample metrics during sustained load
        Thread.sleep(500);
        ThreadPoolMetrics sample1 = getThreadPoolMetrics();

        Thread.sleep(1000);
        ThreadPoolMetrics sample2 = getThreadPoolMetrics();

        Thread.sleep(1000);
        ThreadPoolMetrics sample3 = getThreadPoolMetrics();

        // Wait for completion
        CompletableFuture.allOf(futures).get(60, TimeUnit.SECONDS);
        executor.shutdown();

        ThreadPoolMetrics afterSustained = getThreadPoolMetrics();

        // During sustained load, we should see consistent thread usage
        ThreadPoolMetrics[] samples = {sample1, sample2, sample3};
        for (int i = 0; i < samples.length; i++) {
            ThreadPoolMetrics sample = samples[i];
            assertThat("sample " + i + " current threads", sample.currentThreadCount(), greaterThanOrEqualTo(baseline.currentThreadCount()));
            assertThat("sample " + i + " busy threads", sample.currentThreadsBusy(), greaterThanOrEqualTo(0));
            assertThat("sample " + i + " busy threads", sample.currentThreadsBusy(), lessThanOrEqualTo(sample.currentThreadCount()));
        }

        // After load, busy threads should decrease
        assertThat("busy threads after sustained load", afterSustained.currentThreadsBusy(), lessThanOrEqualTo(sample3.currentThreadsBusy()));
    }

    private static File createDeployment() throws IOException {
        WebArchive war = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
            .addClass(TestServlet.class)
            .addClass(SlowServlet.class);

        File warFile = new File(System.getProperty("java.io.tmpdir"), APP_NAME + ".war");
        war.as(ZipExporter.class).exportTo(warFile, true);
        return warFile;
    }

    private ThreadPoolMetrics getThreadPoolMetrics() {
        return getThreadPoolMetrics("http-listener-1");
    }

    private ThreadPoolMetrics getThreadPoolMetrics(String listenerName) {
        // First, let's see what monitoring data is actually available
        AsadminResult listResult = ASADMIN.exec("list", "*thread*");
        System.out.println("Available thread monitoring paths: " + listResult.getStdOut());

        // Try to get metrics from the specified listener
        String basePath = "server.network." + listenerName + ".thread-pool";
        AsadminResult currentResult = ASADMIN.exec("get", "-m", basePath + ".currentthreadcount");
        AsadminResult busyResult = ASADMIN.exec("get", "-m", basePath + ".currentthreadsbusy");
        AsadminResult maxResult = ASADMIN.exec("get", "-m", basePath + ".maxthreads");
        AsadminResult minResult = ASADMIN.exec("get", "-m", basePath + ".minthreads");

        return new ThreadPoolMetrics(
            extractMetric(currentResult.getStdOut(), "currentthreadcount"),
            extractMetric(busyResult.getStdOut(), "currentthreadsbusy"),
            extractMetric(minResult.getStdOut(), "minthreads"),
            extractMetric(maxResult.getStdOut(), "maxthreads")
        );
    }

    private int extractMetric(String output, String metric) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.contains(metric + "-count")) {
                return Integer.parseInt(line.split("=")[1].trim());
            }
        }
        return -1000;
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
        return -1000;
    }

    private void makeRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            // Ignore connection errors for load testing
        }
    }

    private record ThreadPoolMetrics(int currentThreadCount, int currentThreadsBusy, int minThreads, int maxThreads) {
    }

    private record ThreadPoolConfig(int minThreads, int maxThreads) {
    }

    @Test
    void testThreadPoolSizeIncrease() throws Exception {
        ThreadPoolMetrics initial = getThreadPoolMetrics();
        int originalMaxThreads = initial.maxThreads();

        try {
            // Increase pool size significantly
            int newMaxThreads = originalMaxThreads + 100;
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + newMaxThreads);

            Thread.sleep(2000);

            ThreadPoolMetrics afterIncrease = getThreadPoolMetrics();
            assertThat("max threads after increase", afterIncrease.maxThreads(), equalTo(newMaxThreads));

            // Generate load to test the increased pool
            ExecutorService executor = Executors.newFixedThreadPool(50);
            for (int i = 0; i < 50; i++) {
                executor.submit(() -> {
                    try {
                        HttpURLConnection conn = GlassFishTestEnvironment.openConnection(8080, "/threadpool-test/slow?delay=2000");
                        conn.setRequestMethod("GET");
                        conn.getResponseCode();
                    } catch (Exception e) {
                        // Ignore for this test
                    }
                });
            }

            Thread.sleep(500); // Let load start
            ThreadPoolMetrics underLoad = getThreadPoolMetrics();

            // Critical assertions: monitoring should report valid values
            assertThat("current thread count under load", underLoad.currentThreadCount(), greaterThanOrEqualTo(0));
            assertThat("current thread count under load", underLoad.currentThreadCount(), greaterThan(initial.currentThreadCount()));
            assertThat("current thread count under load", underLoad.currentThreadCount(), lessThanOrEqualTo(newMaxThreads));

            // Should have approximately 50 threads active for 50 concurrent requests
            assertThat("current thread count under load", underLoad.currentThreadCount(), greaterThanOrEqualTo(30));

            executor.shutdown();
            Thread.sleep(3000); // Wait for completion

        } finally {
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + originalMaxThreads);
        }
    }

    @Test
    void testThreadPoolSizeDecrease() throws Exception {
        ThreadPoolMetrics initial = getThreadPoolMetrics();
        int originalMaxThreads = initial.maxThreads();

        try {
            // First increase pool size to 100
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=100");
            Thread.sleep(2000);

            ThreadPoolMetrics afterIncrease = getThreadPoolMetrics();
            assertThat("max threads after increase", afterIncrease.maxThreads(), equalTo(100));

            // Generate significant load to utilize the large pool
            ExecutorService executor = Executors.newFixedThreadPool(80);
            for (int i = 0; i < 80; i++) {
                executor.submit(() -> {
                    try {
                        HttpURLConnection conn = GlassFishTestEnvironment.openConnection(8080, "/threadpool-test/slow?delay=3000");
                        conn.setRequestMethod("GET");
                        conn.getResponseCode();
                    } catch (Exception e) {
                        // Ignore for this test
                    }
                });
            }

            Thread.sleep(1000); // Let load build up
            ThreadPoolMetrics underHeavyLoad = getThreadPoolMetrics();

            // Verify the pool scaled up to handle heavy load
            assertThat("current threads under heavy load", underHeavyLoad.currentThreadCount(), greaterThan(initial.currentThreadCount()));
            assertThat("current threads under heavy load", underHeavyLoad.currentThreadCount(), lessThanOrEqualTo(100));
            assertThat("current threads under heavy load", underHeavyLoad.currentThreadCount(), greaterThanOrEqualTo(0));

            // Should have approximately 80 threads active (or close to it)
            assertThat("current threads under heavy load", underHeavyLoad.currentThreadCount(), greaterThanOrEqualTo(50));

            // Now decrease pool size to 10 while under load
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=10");
            Thread.sleep(1000);

            ThreadPoolMetrics afterDecrease = getThreadPoolMetrics();
            assertThat("max threads after decrease", afterDecrease.maxThreads(), equalTo(10));

            // Critical assertions: thread count should remain valid
            assertThat("current threads after decrease", afterDecrease.currentThreadCount(), greaterThanOrEqualTo(0));
            assertThat("busy threads after decrease", afterDecrease.currentThreadsBusy(), greaterThanOrEqualTo(0));

            // Since we had 80 concurrent requests, current threads should still be high
            // (threads don't disappear instantly when pool size is reduced)
            assertThat("current threads after decrease", afterDecrease.currentThreadCount(), greaterThanOrEqualTo(10));

            executor.shutdown();
            Thread.sleep(4000); // Wait for requests to complete

            ThreadPoolMetrics afterCompletion = getThreadPoolMetrics();
            assertThat("current threads after completion", afterCompletion.currentThreadCount(), greaterThanOrEqualTo(0));
            assertThat("current threads after completion", afterCompletion.currentThreadCount(), lessThanOrEqualTo(10));

        } finally {
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + originalMaxThreads);
        }
    }

    @Test
    void testThreadPoolSizeCycling() throws Exception {
        ThreadPoolConfig initial = getThreadPoolConfig();
        int originalMinThreads = initial.minThreads();
        int originalMaxThreads = initial.maxThreads();

        try {
            int[] testSizes = {originalMaxThreads + 3, originalMaxThreads - 1, originalMaxThreads + 7, originalMaxThreads};

            int i = 0;
            for (int testSize : testSizes) {
                i++;
                LOG.info("Case " + i + ": testSize=" + testSize);

                testSize = Math.max(1, testSize);

                ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + testSize);
                Thread.sleep(1500);

                ThreadPoolMetrics metrics = getThreadPoolMetrics();
                assertThat("max threads (testSize=" + testSize + ")", metrics.maxThreads(), equalTo(testSize));
                int currentThreadsMaxSize = Math.max(testSize, originalMinThreads);
                assertThat("current threads (testSize=" + testSize + ")", metrics.currentThreadCount(), lessThanOrEqualTo(currentThreadsMaxSize));
                assertThat("busy threads (testSize=" + testSize + ")", metrics.currentThreadsBusy(), lessThanOrEqualTo(metrics.currentThreadCount()));
            }

        } finally {
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + originalMaxThreads);
        }
    }

    @Test
    void testAdminListenerThreadPoolMetrics() throws Exception {
        // Get baseline metrics for admin-listener
        ThreadPoolMetrics adminBaseline = getThreadPoolMetrics("admin-listener");

        // Verify admin listener has valid baseline metrics
        assertThat("admin listener current threads", adminBaseline.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("admin listener max threads", adminBaseline.maxThreads(), greaterThan(0));
        assertThat("admin listener busy threads", adminBaseline.currentThreadsBusy(), greaterThanOrEqualTo(0));

        // Create load on admin port (4848) using asadmin commands
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit multiple concurrent admin requests
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    ASADMIN.exec("list", "applications");
                    Thread.sleep(100);
                    ASADMIN.exec("get", "server.monitoring-service.*");
                } catch (Exception e) {
                    // Ignore for load testing
                }
            });
        }

        Thread.sleep(500); // Let requests start

        // Check metrics during admin load
        ThreadPoolMetrics adminUnderLoad = getThreadPoolMetrics("admin-listener");

        // Admin listener should maintain valid metrics under load
        assertThat("admin listener current threads under load", adminUnderLoad.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("admin listener busy threads under load", adminUnderLoad.currentThreadsBusy(), lessThanOrEqualTo(adminUnderLoad.currentThreadCount()));
        assertThat("admin listener max threads under load", adminUnderLoad.maxThreads(), greaterThan(0));

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Thread.sleep(1000); // Let threads settle

        // Final metrics check
        ThreadPoolMetrics adminFinal = getThreadPoolMetrics("admin-listener");
        assertThat("admin listener final current threads", adminFinal.currentThreadCount(), greaterThanOrEqualTo(0));
        assertThat("admin listener final busy threads", adminFinal.currentThreadsBusy(), greaterThanOrEqualTo(0));
    }

    @Test
    void testDualListenerThreadPoolMetrics() throws Exception {
        // First create http-listener-2 if it doesn't exist
        AsadminResult createResult = ASADMIN.exec("create-http-listener",
            "--listenerport=8081", "--listeneraddress=0.0.0.0",
            "--defaultvs=server", "http-listener-2");

        try {
            Thread.sleep(2000); // Allow listener to initialize

            // Get baseline metrics for both listeners
            ThreadPoolMetrics listener1Baseline = getThreadPoolMetrics("http-listener-1");
            ThreadPoolMetrics listener2Baseline = getThreadPoolMetrics("http-listener-2");

            // Verify both listeners have valid baseline metrics
            assertThat("listener 1 current threads", listener1Baseline.currentThreadCount(), greaterThanOrEqualTo(0));
            assertThat("listener 2 current threads", listener2Baseline.currentThreadCount(), greaterThanOrEqualTo(0));

            // Create load on both listeners simultaneously
            ExecutorService executor = Executors.newFixedThreadPool(10);

            // Submit requests to both ports
            for (int i = 0; i < 5; i++) {
                executor.submit(() -> makeRequest("http://localhost:4848/" + APP_NAME + "/hello"));
                executor.submit(() -> makeRequest("http://localhost:8081/" + APP_NAME + "/hello"));
            }

            Thread.sleep(500); // Let requests start

            // Check metrics during dual load
            ThreadPoolMetrics listener1UnderLoad = getThreadPoolMetrics("http-listener-1");
            ThreadPoolMetrics listener2UnderLoad = getThreadPoolMetrics("http-listener-2");

            // Both listeners should show activity
            assertThat("listener 1 current threads under load", listener1UnderLoad.currentThreadCount(), greaterThanOrEqualTo(0));
            assertThat("listener 2 current threads under load", listener2UnderLoad.currentThreadCount(), greaterThanOrEqualTo(0));

            // Thread counts should be consistent with busy counts
            assertThat("listener 1 busy threads under load", listener1UnderLoad.currentThreadsBusy(), lessThanOrEqualTo(listener1UnderLoad.currentThreadCount()));
            assertThat("listener 2 busy threads under load", listener2UnderLoad.currentThreadsBusy(), lessThanOrEqualTo(listener2UnderLoad.currentThreadCount()));

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            Thread.sleep(1000); // Let threads settle

            // Final metrics check
            ThreadPoolMetrics listener1Final = getThreadPoolMetrics("http-listener-1");
            ThreadPoolMetrics listener2Final = getThreadPoolMetrics("http-listener-2");

            // Both should have valid final states
            assertThat("listener 1 final current threads", listener1Final.currentThreadCount(), greaterThanOrEqualTo(0));
            assertThat("listener 2 final current threads", listener2Final.currentThreadCount(), greaterThanOrEqualTo(0));

        } finally {
            // Clean up http-listener-2
            ASADMIN.exec("delete-http-listener", "http-listener-2");
        }
    }

    @Test
    void testThreadPoolSizeUnderLoad() throws Exception {
        ThreadPoolMetrics initial = getThreadPoolMetrics();
        int originalMaxThreads = initial.maxThreads();

        try {
            // Start with increased pool size
            int largePoolSize = originalMaxThreads + 5;
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + largePoolSize);
            Thread.sleep(2000);

            // Generate load
            ExecutorService executor = Executors.newFixedThreadPool(8);
            for (int i = 0; i < 8; i++) {
                executor.submit(() -> {
                    try {
                        HttpURLConnection conn = GlassFishTestEnvironment.openConnection(8080, "/threadpool-test/slow?delay=3000");
                        conn.setRequestMethod("GET");
                        conn.getResponseCode();
                    } catch (Exception e) {
                        // Ignore for this test
                    }
                });
            }

            Thread.sleep(500);
            ThreadPoolMetrics duringLoad = getThreadPoolMetrics();

            // Decrease pool size while under load
            int smallPoolSize = Math.max(3, originalMaxThreads - 1);
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + smallPoolSize);
            Thread.sleep(1000);

            ThreadPoolMetrics afterResize = getThreadPoolMetrics();
            assertThat("max threads after resize", afterResize.maxThreads(), equalTo(smallPoolSize));

            executor.shutdown();
            Thread.sleep(4000);

        } finally {
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + originalMaxThreads);
        }
    }
}
