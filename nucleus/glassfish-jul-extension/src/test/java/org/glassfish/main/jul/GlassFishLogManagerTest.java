/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.main.jul.GlassFishLogManager.Action;
import org.glassfish.main.jul.cfg.GlassFishLogManagerConfiguration;
import org.glassfish.main.jul.cfg.GlassFishLoggingConstants;
import org.glassfish.main.jul.cfg.LoggingProperties;
import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.formatter.OneLineFormatter;
import org.glassfish.main.jul.handler.ExternallyManagedLogHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
@EnabledIfSystemProperty(
    named = GlassFishLoggingConstants.JVM_OPT_LOGGING_MANAGER,
    matches = GlassFishLoggingConstants.CLASS_LOG_MANAGER_GLASSFISH)
public class GlassFishLogManagerTest {

    private static GlassFishLogManager logManager;
    private static GlassFishLogManagerConfiguration originalCfg;


    @BeforeAll
    public static void backup() {
        logManager = GlassFishLogManager.getLogManager();
        originalCfg = logManager.getConfiguration();
        System.out.println("Original configuration: " + originalCfg);
    }


    @AfterEach
    public void reset() {
        logManager.reconfigure(originalCfg);
    }


    @Test
    public void getRootLogger() {
        final Logger rootLogger = logManager.getLogger(GlassFishLogManager.ROOT_LOGGER_NAME);
        assertNotNull(rootLogger, "root logger");
        assertAll(
            () -> assertThat("root logger", rootLogger.getClass().getName(), equalTo(GlassFishLogger.class.getName())),
            () -> assertThat("logManager.getRootLogger()", logManager.getRootLogger(), sameInstance(rootLogger))
        );
    }


    @Test
    public void getGlobalLogger() {
        final Logger globalLogger = logManager.getLogger(Logger.GLOBAL_LOGGER_NAME);
        assertAll(
            () -> assertNotNull(globalLogger, "global logger"),
            () -> assertNotSame(globalLogger, Logger.getGlobal(), "global logger")
        );
        assertAll(
            () -> assertThat("global logger via GlassFishLogManager", globalLogger.getClass().getName(),
                equalTo("org.glassfish.main.jul.GlassFishLoggerWrapper")),
            () -> assertThat("global logger name", Logger.getGlobal().getName(), equalTo(globalLogger.getName()))
        );
    }


    @Test
    public void addLoggers() {
        final String loggerName = "com.acme.MyCustomLogger";
        final CustomLogger customLogger = new CustomLogger(loggerName);
        final boolean result = logManager.addLogger(customLogger);
        assertAll(
            () -> assertTrue(result, "addLogger result"),
            () -> assertSame(customLogger, logManager.getLogger(loggerName), "LogManager.getLogger"),
            () -> assertSame(logManager.getLogger(loggerName), Logger.getLogger(loggerName), "getLogger results"),
            () -> assertFalse(logManager.addLogger(new CustomLogger(Logger.GLOBAL_LOGGER_NAME)), "add as global"),
            () -> assertFalse(logManager.addLogger(Logger.getLogger(logManager.getRootLogger().getName())), "add as root"),
            () -> assertFalse(logManager.addLogger(customLogger), "added for the second time"),
            () -> assertThrows(NullPointerException.class,
                () -> logManager.addLogger(Logger.getAnonymousLogger()), "add anonymous")
        );
    }


    @Test
    public void externalHandlers() {
        assertEquals(GlassFishLoggingStatus.FULL_SERVICE, logManager.getLoggingStatus());
        final AtomicBoolean reconfigActionCalled = new AtomicBoolean();
        final AtomicBoolean flushActionCalled = new AtomicBoolean();
        final LoggingProperties properties = new LoggingProperties();
        properties.setProperty(GlassFishLoggingConstants.KEY_TRACING_ENABLED, "true");
        final GlassFishLogManagerConfiguration cfg1 = new GlassFishLogManagerConfiguration(properties);
        logManager.reconfigure(cfg1, () -> reconfigActionCalled.set(true), () -> flushActionCalled.set(true));
        assertAll(
            () -> assertTrue(reconfigActionCalled.get(), "reconfig action was executed"),
            () -> assertTrue(flushActionCalled.get(), "flush action was executed"),
            () -> assertEquals(GlassFishLoggingStatus.FULL_SERVICE, logManager.getLoggingStatus()),
            () -> assertFalse(LoggingSystemEnvironment.getOriginalStdOut().checkError(),
                "something probably closed STDOUT"),
            () -> assertFalse(LoggingSystemEnvironment.getOriginalStdErr().checkError(),
                "something probably closed STDERR")
        );

        // we will reuse both actions
        reconfigActionCalled.set(false);
        flushActionCalled.set(false);
        final String handlerName = GlassFishLogManagerTest.class.getName() + "$TestHandler";
        properties.setProperty("handlers", handlerName);
        final GlassFishLogManagerConfiguration cfg2 = new GlassFishLogManagerConfiguration(properties);
        logManager.reconfigure(cfg2, () -> reconfigActionCalled.set(true), () -> flushActionCalled.set(true));
        assertAll(
            () -> assertTrue(reconfigActionCalled.get(), "reconfig action was executed"),
            () -> assertFalse(flushActionCalled.get(), "flush action was executed"),
            () -> assertNotNull(logManager.getRootLogger().getHandler(TestHandler.class), "test handler"),
            () -> assertFalse(logManager.getRootLogger().getHandler(TestHandler.class).isReady(), "test handler ready"),
            () -> assertEquals(GlassFishLoggingStatus.CONFIGURING, logManager.getLoggingStatus())
        );

        Logger.getAnonymousLogger().info("Tick tock!");
        // why: CONFIGURING already sends records to handlers and it is on the handler how it will manage it.
        assertTrue(logManager.getRootLogger().getHandler(TestHandler.class).published, "publish called");
        final Action reconfigAction3 = () -> logManager.getRootLogger().getHandler(TestHandler.class).ready = true;
        logManager.reconfigure(cfg2, reconfigAction3, () -> flushActionCalled.set(true));
        assertAll(
            () -> assertTrue(flushActionCalled.get(), "flush action was executed"),
            () -> assertEquals(GlassFishLoggingStatus.FULL_SERVICE, logManager.getLoggingStatus()),
            () -> assertTrue(logManager.getRootLogger().getHandler(TestHandler.class).published,
                "publish called - maybe another instance!")
        );

        logManager.closeAllExternallyManagedLogHandlers();
        assertNull(logManager.getRootLogger().getHandler(TestHandler.class));
    }


    @Test
    public void reconfigure() {
        final GlassFishLogManagerConfiguration configuration0 = logManager.getConfiguration();
        logManager.reconfigure(configuration0);
        final GlassFishLogManagerConfiguration configuration1 = logManager.getConfiguration();
        assertAll(
            () -> assertNotSame(configuration0.getPropertyNames(), configuration1.getPropertyNames()),
            () -> assertNotSame(configuration0, configuration1),
            () -> assertThat(configuration1.getPropertyNames(), contains(configuration0.getPropertyNames().toArray())),
            () -> assertThat(configuration1.getPropertyNames(), hasSize(configuration0.getPropertyNames().size()))
        );
        for (String name : configuration0.getPropertyNames()) {
            assertEquals(configuration0.getProperty(name), configuration1.getProperty(name));
        }
    }


    private static class CustomLogger extends GlassFishLogger {

        protected CustomLogger(String name) {
            super(name);
        }
    }


    public static class TestHandler extends Handler implements ExternallyManagedLogHandler {
        private boolean ready;
        private boolean published;

        public TestHandler() {
            setFormatter(new OneLineFormatter());
        }

        @Override
        public boolean isReady() {
            return ready;
        }


        @Override
        public void close() {
            ready = false;
        }


        @Override
        public void publish(final LogRecord record) {
            LoggingSystemEnvironment.getOriginalStdOut().println(getFormatter().format(record));
            published = true;
        }


        @Override
        public void flush() {
            // nothing
        }
    }
}
