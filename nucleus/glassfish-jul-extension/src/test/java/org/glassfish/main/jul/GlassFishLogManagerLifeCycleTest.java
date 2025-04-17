/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.jul;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.glassfish.main.jul.GlassFishLogManager.Action;
import org.glassfish.main.jul.cfg.GlassFishLogManagerConfiguration;
import org.glassfish.main.jul.cfg.GlassFishLogManagerProperty;
import org.glassfish.main.jul.cfg.LoggingProperties;
import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.handler.BlockingExternallyManagedLogHandler;
import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This test is executed as a sequence going through some lifecycle usage.
 *
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
public class GlassFishLogManagerLifeCycleTest {

    private static final GlassFishLogManager MANAGER = GlassFishLogManager.getLogManager();
    private static final Logger LOG = Logger.getLogger(GlassFishLogManagerLifeCycleTest.class.getName());

    private static final LogCollectorHandler COLLECTOR = new LogCollectorHandler(LOG);
    private static final BlockingAction ACTION_RECONFIG = new BlockingAction();
    private static final BlockingAction ACTION_FLUSH = new BlockingAction();

    private static GlassFishLogManagerConfiguration originalCfg;
    private static boolean originalLevelResolution;
    private static CompletableFuture<Void> reconfiguration;
    private static CompletableFuture<Void> logWhenFlushing;

    @BeforeAll
    public static void backupConfiguration() {
        assertNotNull(MANAGER, () -> "Unsupported log manager used: " + LogManager.getLogManager());
        originalCfg = MANAGER.getConfiguration();
        originalLevelResolution = LoggingSystemEnvironment.isResolveLevelWithIncompleteConfiguration();

        COLLECTOR.setLevel(Level.ALL);
        LOG.setUseParentHandlers(false);
    }


    @AfterAll
    public static void resetConfiguration() throws Exception {
        ACTION_FLUSH.unblock();
        ACTION_RECONFIG.unblock();
        MANAGER.reconfigure(originalCfg, null, null);
        LoggingSystemEnvironment.setResolveLevelWithIncompleteConfiguration(originalLevelResolution);
        COLLECTOR.close();
    }


    /**
     * This uses GlassFish Server - because it starts without a configuration, uses
     * {@link BlockingExternallyManagedLogHandler} to block logging system in
     * {@link GlassFishLoggingStatus#CONFIGURING} state, so it is just collecting
     * log records to the StartupQueue.
     */
    @Test
    @Order(1)
    public void startReconfigurationAndBlock() {
        reconfigureToBlockingHandler();
        assertEquals(GlassFishLoggingStatus.CONFIGURING, MANAGER.getLoggingStatus());
        LOG.log(Level.INFO, "Hello StartupQueue, my old friend");
    }


    /**
     * Then GlassFish Server finds domain directory and logging properties and initiates
     * reconfiguration.
     * <p>
     * Here we simulate additional actions by two fake blocking implementations, controlled
     * by the test.
     * <p>
     * The logging should stay in {@link GlassFishLoggingStatus#CONFIGURING}.
     */
    @Test
    @Order(2)
    @Timeout(5)
    public void startReconfiguration() throws Exception {
        LoggingSystemEnvironment.setResolveLevelWithIncompleteConfiguration(false);
        assertEquals(GlassFishLoggingStatus.CONFIGURING, MANAGER.getLoggingStatus(),
            "status after startReconfigurationAndBlock test");

        LOG.log(Level.FINE, "Log before reconfiguration");
        LOG.log(Level.FINEST, "This log should be dropped, logger's level is FINE");
        reconfiguration = runAsync(() -> MANAGER.reconfigure(originalCfg, ACTION_RECONFIG, ACTION_FLUSH));
        waitForStatus(GlassFishLoggingStatus.CONFIGURING);
        assertNull(COLLECTOR.pop(), "COLLECTOR should be empty after reconfiguration started");
        LOG.log(Level.INFO, "Log after reconfiguration started");
        assertNull(COLLECTOR.pop(), "COLLECTOR should be empty after reconfiguration started even if we log again");
    }


    /**
     * Another step. We finish the custom reconfiguration action.
     * <p>
     * The logging should step into {@link GlassFishLoggingStatus#FLUSHING_BUFFERS}.
     */
    @Test
    @Order(3)
    @Timeout(5)
    public void finishReconfigurationAndStartFlushing() throws Exception {
        ACTION_RECONFIG.unblock();
        waitForStatus(GlassFishLoggingStatus.FLUSHING_BUFFERS);
        assertNull(COLLECTOR.pop(), "COLLECTOR should be empty after reconfiguration finished");
        logWhenFlushing = runAsync(() -> LOG.log(Level.SEVERE, "Log while flushing is still executed"));
        // The previous line will be stuck in onSpinWait cycle waiting for the log manager's full service state.
        // We will let it enough time to process if the waiting would not work to fail the test.
        Thread.sleep(100L);
        assertNull(COLLECTOR.pop(), "COLLECTOR should be empty when we log again while manager is still flushing");
    }


    /**
     * Last step, we flush waiting logs from startup queue.
     * <p>
     * The logging shall step into {@link GlassFishLoggingStatus#FULL_SERVICE} and all records shall
     * end in our collector handler.
     */
    @Test
    @Order(4)
    @Timeout(10)
    public void finishFlushing() throws Exception {
        ACTION_FLUSH.unblock();
        waitForStatus(GlassFishLoggingStatus.FULL_SERVICE);
        logWhenFlushing.join();
        LOG.log(Level.INFO, "Log after flushing finished");
        final List<GlassFishLogRecord> logRecords = COLLECTOR.getAll();
        assertAll(
            () -> assertThat(logRecords, hasSize(5)),
            () -> assertThat("record 0", logRecords.get(0).getLevel(), equalTo(Level.INFO)),
            () -> assertThat("record 0", logRecords.get(0).getMessage(), equalTo("Hello StartupQueue, my old friend")),
            () -> assertThat("record 1", logRecords.get(1).getLevel(), equalTo(Level.FINE)),
            () -> assertThat("record 1", logRecords.get(1).getMessage(), equalTo("Log before reconfiguration")),
            () -> assertThat("record 2", logRecords.get(2).getLevel(), equalTo(Level.INFO)),
            () -> assertThat("record 2", logRecords.get(2).getMessage(), equalTo("Log after reconfiguration started")),
            () -> assertThat("record 3", logRecords.get(3).getLevel(), equalTo(Level.SEVERE)),
            () -> assertThat("record 3", logRecords.get(3).getMessage(), equalTo("Log while flushing is still executed")),
            () -> assertThat("record 4", logRecords.get(4).getLevel(), equalTo(Level.INFO)),
            () -> assertThat("record 4", logRecords.get(4).getMessage(), equalTo("Log after flushing finished"))
        );

        reconfiguration.get(5, TimeUnit.SECONDS);
    }


    /**
     * Yet another case, here we get into blocked state, and then we DISABLE resolving if the record
     * is loggable or not when we simply don't know. All records then go to the StartupQueue, which
     * means also some memory requirements.
     */
    @Test
    @Order(100)
    @Timeout(5)
    public void reconfigureWithoutResolvingLevelsWithincompleteconfiguration() throws Exception {
        reconfigureToBlockingHandler();
        LoggingSystemEnvironment.setResolveLevelWithIncompleteConfiguration(false);
        assertEquals(GlassFishLoggingStatus.CONFIGURING, MANAGER.getLoggingStatus(), "after reconfigureToBlockingHandler");

        LOG.log(Level.FINEST, "message0");
        LOG.log(Level.INFO, "message1");
        MANAGER.reconfigure(originalCfg, () -> LOG.setLevel(Level.FINEST), null);
        assertFalse(LoggingSystemEnvironment.isResolveLevelWithIncompleteConfiguration(), "isResolveLevelWithIncompleteConfiguration");

        final List<GlassFishLogRecord> logRecords = COLLECTOR.getAll();
        assertAll("Both records must pass via startup queue",
            () -> assertEquals(GlassFishLoggingStatus.FULL_SERVICE, MANAGER.getLoggingStatus(),
                "status after all reconfiguration and flushing finished"),
            () -> assertThat(logRecords, hasSize(2)),
            () -> assertThat("record 0", logRecords.get(0).getLevel(), equalTo(Level.FINEST)),
            () -> assertThat("record 0", logRecords.get(0).getMessage(), equalTo("message0")),
            () -> assertThat("record 1", logRecords.get(1).getLevel(), equalTo(Level.INFO)),
            () -> assertThat("record 1", logRecords.get(1).getMessage(), equalTo("message1")));
    }


    /**
     * ... and the opposite, here we get into blocked state, and then we ENABLE resolving if the record
     * is loggable or not even when we simply don't know. Only records accepted by loggers with default
     * configuration then go to the StartupQueue, usually it means records with level {@link Level#INFO}
     * and higher.
     * <p>
     * Which variant is better depend on what we need to see in logs.
     */
    @Test
    @Order(101)
    @Timeout(5)
    public void reconfigureWithResolvingLevelsWithincompleteconfiguration() throws Exception {
        reconfigureToBlockingHandler();
        LoggingSystemEnvironment.setResolveLevelWithIncompleteConfiguration(true);
        assertEquals(GlassFishLoggingStatus.CONFIGURING, MANAGER.getLoggingStatus(), "after reconfigureToBlockingHandler");

        LOG.log(Level.FINEST, "message0");
        LOG.log(Level.INFO, "message1");
        MANAGER.reconfigure(originalCfg, () -> LOG.setLevel(Level.FINEST), null);
        assertTrue(LoggingSystemEnvironment.isResolveLevelWithIncompleteConfiguration(), "isResolveLevelWithIncompleteConfiguration");

        final List<GlassFishLogRecord> logRecords = COLLECTOR.getAll();
        assertAll("Both records must pass via startup queue",
            () -> assertEquals(GlassFishLoggingStatus.FULL_SERVICE, MANAGER.getLoggingStatus(),
                "status after all reconfiguration and flushing finished"),
            () -> assertThat(logRecords, hasSize(1)),
            () -> assertThat("record 0", logRecords.get(0).getLevel(), equalTo(Level.INFO)),
            () -> assertThat("record 0", logRecords.get(0).getMessage(), equalTo("message1"))
        );
    }


    private void reconfigureToBlockingHandler() {
        final LoggingProperties properties = new LoggingProperties();
        properties.setProperty(GlassFishLogManagerProperty.KEY_ROOT_HANDLERS.getPropertyName(),
            BlockingExternallyManagedLogHandler.class.getName());
        final GlassFishLogManagerConfiguration cfg = new GlassFishLogManagerConfiguration(properties);
        MANAGER.reconfigure(cfg);
    }


    private void waitForStatus(GlassFishLoggingStatus status) {
        final long timeout = 1000L;
        final long start = System.currentTimeMillis();
        while (MANAGER.getLoggingStatus() != status) {
            Thread.onSpinWait();
            if (System.currentTimeMillis() > start + timeout) {
                fail("The log manager status wasn't reached in a " + timeout + " ms timeout");
            }
        }
    }


    private static final class BlockingAction implements Action {

        private final AtomicBoolean blocker = new AtomicBoolean(true);

        public void unblock() {
            blocker.setRelease(false);
        }


        @Override
        public void run() {
            while (blocker.get()) {
                Thread.yield();
            }
        }
    }
}
