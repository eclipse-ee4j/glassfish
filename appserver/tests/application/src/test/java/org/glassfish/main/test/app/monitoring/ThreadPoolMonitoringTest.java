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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadPoolMonitoringTest {

    private static final String APP_NAME = "threadpool-test";
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @BeforeAll
    static void deployApp() throws IOException {
        File warFile = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", warFile.getAbsolutePath());
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
        assertTrue(baseline.currentThreadCount >= 0);
        assertTrue(baseline.currentThreadsBusy >= 0);
        assertTrue(baseline.currentThreadsBusy <= baseline.currentThreadCount);

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
        
        assertThat("Current threads should be >= baseline", 
                   duringLoad.currentThreadCount, greaterThanOrEqualTo(baseline.currentThreadCount));
        assertThat("Busy threads should be > 0 during load", 
                   duringLoad.currentThreadsBusy, greaterThanOrEqualTo(1));
        assertThat("Busy threads <= current threads", 
                   duringLoad.currentThreadsBusy, lessThanOrEqualTo(duringLoad.currentThreadCount));
        assertThat("Current threads <= max threads", 
                   duringLoad.currentThreadCount, lessThanOrEqualTo(duringLoad.maxThreads));

        // Wait for completion
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Check metrics after load
        Thread.sleep(1000); // Let threads settle
        ThreadPoolMetrics afterLoad = getThreadPoolMetrics();
        
        assertThat("Busy threads should decrease after load", 
                   afterLoad.currentThreadsBusy, lessThanOrEqualTo(duringLoad.currentThreadsBusy));
        assertTrue(afterLoad.currentThreadsBusy >= 0);
        assertTrue(afterLoad.currentThreadsBusy <= afterLoad.currentThreadCount);
    }

    @Test
    void testThreadPoolMetricsBaseline() throws Exception {
        ThreadPoolMetrics metrics = getThreadPoolMetrics();
        
        // Basic sanity checks
        assertTrue(metrics.currentThreadCount >= 0, "Current thread count should be non-negative");
        assertTrue(metrics.currentThreadsBusy >= 0, "Busy thread count should be non-negative");
        assertTrue(metrics.maxThreads > 0, "Max threads should be positive");
        
        // Logical consistency
        assertTrue(metrics.currentThreadsBusy <= metrics.currentThreadCount, 
                  "Busy threads should not exceed current threads");
        assertTrue(metrics.currentThreadCount <= metrics.maxThreads, 
                  "Current threads should not exceed max threads");
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
        assertTrue(afterSequential.currentThreadCount >= 0);
        assertTrue(afterSequential.currentThreadsBusy >= 0);
        assertTrue(afterSequential.currentThreadsBusy <= afterSequential.currentThreadCount);
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
        assertTrue(duringBurst.currentThreadCount >= baseline.currentThreadCount);
        assertTrue(duringBurst.currentThreadsBusy >= 0);
        assertTrue(duringBurst.currentThreadsBusy <= duringBurst.currentThreadCount);
        
        assertTrue(afterBurst.currentThreadCount >= 0);
        assertTrue(afterBurst.currentThreadsBusy >= 0);
        assertTrue(afterBurst.currentThreadsBusy <= afterBurst.currentThreadCount);
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
            assertTrue(sample.currentThreadCount >= 0, "Current threads >= 0");
            assertTrue(sample.currentThreadsBusy >= 0, "Busy threads >= 0");
            assertTrue(sample.maxThreads > 0, "Max threads > 0");
            assertTrue(sample.currentThreadsBusy <= sample.currentThreadCount, 
                      "Busy <= Current");
            assertTrue(sample.currentThreadCount <= sample.maxThreads, 
                      "Current <= Max");
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
            assertTrue(sample.currentThreadCount >= baseline.currentThreadCount, 
                      "Sample " + i + ": threads should increase under load");
            assertTrue(sample.currentThreadsBusy >= 0, 
                      "Sample " + i + ": busy threads should be non-negative");
            assertTrue(sample.currentThreadsBusy <= sample.currentThreadCount, 
                      "Sample " + i + ": busy <= current");
        }
        
        // After load, busy threads should decrease
        assertTrue(afterSustained.currentThreadsBusy <= sample3.currentThreadsBusy, 
                  "Busy threads should decrease after sustained load");
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
        
        return new ThreadPoolMetrics(
            extractValue(currentResult.getStdOut(), "currentthreadcount"),
            extractValue(busyResult.getStdOut(), "currentthreadsbusy"),
            extractValue(maxResult.getStdOut(), "maxthreads")
        );
    }

    private int extractValue(String output, String metric) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.contains(metric + "-count")) {
                return Integer.parseInt(line.split("=")[1].trim());
            }
        }
        return 0;
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

    private static class ThreadPoolMetrics {
        final int currentThreadCount;
        final int currentThreadsBusy;
        final int maxThreads;

        ThreadPoolMetrics(int currentThreadCount, int currentThreadsBusy, int maxThreads) {
            this.currentThreadCount = currentThreadCount;
            this.currentThreadsBusy = currentThreadsBusy;
            this.maxThreads = maxThreads;
        }
    }

    @Test
    void testThreadPoolSizeIncrease() throws Exception {
        ThreadPoolMetrics initial = getThreadPoolMetrics();
        int originalMaxThreads = initial.maxThreads;
        
        try {
            // Increase pool size significantly
            int newMaxThreads = originalMaxThreads + 100;
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + newMaxThreads);
            
            Thread.sleep(2000);
            
            ThreadPoolMetrics afterIncrease = getThreadPoolMetrics();
            assertEquals(newMaxThreads, afterIncrease.maxThreads, "Max threads should reflect new configuration");
            
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
            assertTrue(underLoad.currentThreadCount >= 0, "Current thread count should never be negative");
            assertTrue(underLoad.currentThreadCount > initial.currentThreadCount, "Thread count should increase under load");
            assertTrue(underLoad.currentThreadCount <= newMaxThreads, "Current <= max");
            
            // Should have approximately 50 threads active for 50 concurrent requests
            assertTrue(underLoad.currentThreadCount >= 30, "Should have many threads active for 50 concurrent requests");
            
            executor.shutdown();
            Thread.sleep(3000); // Wait for completion
            
        } finally {
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + originalMaxThreads);
        }
    }

    @Test
    void testThreadPoolSizeDecrease() throws Exception {
        ThreadPoolMetrics initial = getThreadPoolMetrics();
        int originalMaxThreads = initial.maxThreads;
        
        try {
            // First increase pool size to 100
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=100");
            Thread.sleep(2000);
            
            ThreadPoolMetrics afterIncrease = getThreadPoolMetrics();
            assertEquals(100, afterIncrease.maxThreads, "Max threads should be 100");
            
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
            assertTrue(underHeavyLoad.currentThreadCount > initial.currentThreadCount, "Thread count should increase under heavy load");
            assertTrue(underHeavyLoad.currentThreadCount <= 100, "Current threads should not exceed 100");
            assertTrue(underHeavyLoad.currentThreadCount >= 0, "Current thread count should never be negative");
            
            // Should have approximately 80 threads active (or close to it)
            assertTrue(underHeavyLoad.currentThreadCount >= 50, "Should have many threads active for 80 concurrent requests");
            
            // Now decrease pool size to 10 while under load
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=10");
            Thread.sleep(1000);
            
            ThreadPoolMetrics afterDecrease = getThreadPoolMetrics();
            assertEquals(10, afterDecrease.maxThreads, "Max threads should be 10");
            
            // Critical assertions: thread count should remain valid
            assertTrue(afterDecrease.currentThreadCount >= 0, "Current thread count should never be negative");
            assertTrue(afterDecrease.currentThreadsBusy >= 0, "Busy threads should be non-negative");
            
            // Since we had 80 concurrent requests, current threads should still be high
            // (threads don't disappear instantly when pool size is reduced)
            assertTrue(afterDecrease.currentThreadCount >= 10, "Should still have many active threads from previous load");
            
            executor.shutdown();
            Thread.sleep(4000); // Wait for requests to complete
            
            ThreadPoolMetrics afterCompletion = getThreadPoolMetrics();
            assertTrue(afterCompletion.currentThreadCount >= 0, "Thread count should never be negative");
            assertTrue(afterCompletion.currentThreadCount <= 10, "Eventually current threads should respect new max");
            
        } finally {
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + originalMaxThreads);
        }
    }

    @Test
    void testThreadPoolSizeCycling() throws Exception {
        ThreadPoolMetrics initial = getThreadPoolMetrics();
        int originalMaxThreads = initial.maxThreads;
        
        try {
            int[] testSizes = {originalMaxThreads + 3, originalMaxThreads - 1, originalMaxThreads + 7, originalMaxThreads};
            
            for (int testSize : testSizes) {
                testSize = Math.max(1, testSize);
                
                ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + testSize);
                Thread.sleep(1500);
                
                ThreadPoolMetrics metrics = getThreadPoolMetrics();
                assertEquals(testSize, metrics.maxThreads, "Max threads should match configured size: " + testSize);
                assertTrue(metrics.currentThreadCount <= testSize, "Current threads should not exceed max: " + testSize);
                assertTrue(metrics.currentThreadsBusy <= metrics.currentThreadCount, "Busy <= current for size: " + testSize);
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
        assertTrue(adminBaseline.currentThreadCount >= 0, "Admin listener should have valid thread count");
        assertTrue(adminBaseline.maxThreads > 0, "Admin listener should have positive max threads");
        assertTrue(adminBaseline.currentThreadsBusy >= 0, "Admin listener should have valid busy count");
        
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
        assertTrue(adminUnderLoad.currentThreadCount >= 0, "Admin listener should maintain valid thread count under load");
        assertTrue(adminUnderLoad.currentThreadsBusy <= adminUnderLoad.currentThreadCount, 
            "Admin listener: Busy threads should not exceed current threads");
        assertTrue(adminUnderLoad.maxThreads > 0, "Admin listener should maintain positive max threads");
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        Thread.sleep(1000); // Let threads settle
        
        // Final metrics check
        ThreadPoolMetrics adminFinal = getThreadPoolMetrics("admin-listener");
        assertTrue(adminFinal.currentThreadCount >= 0, "Admin listener final thread count should be valid");
        assertTrue(adminFinal.currentThreadsBusy >= 0, "Admin listener final busy count should be valid");
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
            assertTrue(listener1Baseline.currentThreadCount >= 0, "Listener 1 should have valid thread count");
            assertTrue(listener2Baseline.currentThreadCount >= 0, "Listener 2 should have valid thread count");
            
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
            assertTrue(listener1UnderLoad.currentThreadCount >= 0, "Listener 1 should maintain valid thread count under load");
            assertTrue(listener2UnderLoad.currentThreadCount >= 0, "Listener 2 should maintain valid thread count under load");
            
            // Thread counts should be consistent with busy counts
            assertTrue(listener1UnderLoad.currentThreadsBusy <= listener1UnderLoad.currentThreadCount, 
                "Listener 1: Busy threads should not exceed current threads");
            assertTrue(listener2UnderLoad.currentThreadsBusy <= listener2UnderLoad.currentThreadCount,
                "Listener 2: Busy threads should not exceed current threads");
            
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            
            Thread.sleep(1000); // Let threads settle
            
            // Final metrics check
            ThreadPoolMetrics listener1Final = getThreadPoolMetrics("http-listener-1");
            ThreadPoolMetrics listener2Final = getThreadPoolMetrics("http-listener-2");
            
            // Both should have valid final states
            assertTrue(listener1Final.currentThreadCount >= 0, "Listener 1 final thread count should be valid");
            assertTrue(listener2Final.currentThreadCount >= 0, "Listener 2 final thread count should be valid");
            
        } finally {
            // Clean up http-listener-2
            ASADMIN.exec("delete-http-listener", "http-listener-2");
        }
    }

    @Test
    void testThreadPoolSizeUnderLoad() throws Exception {
        ThreadPoolMetrics initial = getThreadPoolMetrics();
        int originalMaxThreads = initial.maxThreads;
        
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
            assertEquals(smallPoolSize, afterResize.maxThreads, "Max threads should reflect new size even under load");
            
            executor.shutdown();
            Thread.sleep(4000);
            
        } finally {
            ASADMIN.exec("set", "configs.config.server-config.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=" + originalMaxThreads);
        }
    }
}
