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

package org.glassfish.main.jul.handler;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.formatter.OneLineFormatter;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

import static org.glassfish.main.jul.env.LoggingSystemEnvironment.getOriginalStdErr;
import static org.glassfish.main.jul.env.LoggingSystemEnvironment.getOriginalStdOut;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class GlassFishLogHandlerTest {

    private static final long MILLIS_FOR_PUMP = 20L;
    private static GlassFishLogHandler handler;

    @BeforeAll
    public static void initEnv() throws Exception {
        GlassFishLoggingTracer.setTracingEnabled(true);
        LogManager.getLogManager().reset();
        LoggingSystemEnvironment.initialize();
        final GlassFishLogHandlerConfiguration cfg = new GlassFishLogHandlerConfiguration();
        final File logFile = File.createTempFile(GlassFishLogHandlerTest.class.getCanonicalName(), ".log");
        logFile.delete();
        logFile.deleteOnExit();
        cfg.setLogFile(logFile);
        cfg.setFormatterConfiguration(new OneLineFormatter());
        handler = new GlassFishLogHandler(cfg);
        getRootLogger().addHandler(handler);
    }


    @AfterAll
    public static void resetEverything() {
        getRootLogger().removeHandler(handler);
        if (handler != null) {
            handler.close();
        }
        LogManager.getLogManager().reset();
        GlassFishLoggingTracer.setTracingEnabled(false);
    }


    private static Logger getRootLogger() {
        return LogManager.getLogManager().getLogger("");
    }


    @Test
    @Order(10)
    public void enableStandardStreamsLoggers(TestInfo testInfo) throws Exception {
        assertTrue(handler.isReady(), "handler.ready");
        final GlassFishLogHandlerConfiguration cfg = handler.getConfiguration();
        cfg.setRedirectStandardStreams(true);
        handler.reconfigure(cfg);
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertNotSame(System.out, getOriginalStdOut(), "System.out should be redirected"),
            () -> assertNotSame(System.err, getOriginalStdErr(), "System.err should be redirected"),
            () -> assertThat(System.out.toString(),
                stringContainsInOrder(LoggingPrintStream.class.getName(), LoggingOutputStream.class.getName(),
                    " redirecting messages to the logger jakarta.enterprise.logging.stdout")),
            () -> assertThat(System.err.toString(),
                stringContainsInOrder(LoggingPrintStream.class.getName(), LoggingOutputStream.class.getName(),
                    " redirecting messages to the logger jakarta.enterprise.logging.stderr"))
        );

        System.out.println("Tommy, can you hear me?");
        // output stream is pumped in parallel to the error stream, order is not guaranteed between streams
        Thread.sleep(MILLIS_FOR_PUMP);
        Logger.getLogger("some.usual.logger").info("Some info message");
        System.err.println("Can you feel me near you?");
        System.err.println("Příliš žluťoučký kůň úpěl ďábelské ódy");
        Thread.sleep(MILLIS_FOR_PUMP);
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertTrue(cfg.getLogFile().exists(), "file exists"),
            () -> assertThat("file content", Files.readAllLines(cfg.getLogFile().toPath()),
                contains(
                    stringContainsInOrder("INFO", "main", "Tommy, can you hear me?"),
                    stringContainsInOrder("INFO", "main",
                        GlassFishLogHandlerTest.class.getName() + "." + testInfo.getTestMethod().get().getName(),
                        "Some info message"),
                    stringContainsInOrder("SEVERE", "main", "Can you feel me near you?"),
                    stringContainsInOrder("SEVERE", "main", "Příliš žluťoučký kůň úpěl ďábelské ódy")
                )
            )
        );
    }


    @Test
    @Order(30)
    public void roll() throws Exception {
        assertTrue(handler.isReady(), "handler.ready");
        handler.publish(new GlassFishLogRecord(Level.SEVERE, "File one, record one", false));
        final File logFile = handler.getConfiguration().getLogFile();
        // pump is now to play
        Thread.sleep(MILLIS_FOR_PUMP);
        final List<String> fileLines1 = Files.readAllLines(logFile.toPath());
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertTrue(logFile.exists(), "file one exists"),
            () -> assertThat("file content, filename=" + logFile + ", content=" + fileLines1 , fileLines1,
                contains(
                    stringContainsInOrder("INFO", "main", "Tommy, can you hear me?"),
                    stringContainsInOrder("INFO", "main", "Some info message"),
                    stringContainsInOrder("SEVERE", "main", "Can you feel me near you?"),
                    stringContainsInOrder("SEVERE", "main", "Příliš žluťoučký kůň úpěl ďábelské ódy"),
                    stringContainsInOrder("SEVERE", "main", "File one, record one")
                )
            )
        );
        handler.roll();
        // There will be an asynchronous thread.
        Thread.sleep(10L);
        final List<String> fileLines2 = Files.readAllLines(logFile.toPath());
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertTrue(logFile.exists(), "file exists"),
            () -> assertThat("file content, filename=" + logFile + ", content=" + fileLines2 , fileLines2,
                contains(
                    stringContainsInOrder("INFO", "Rolling the file ", ".log", "output was originally enabled: true"),
                    stringContainsInOrder("INFO", "Archiving file ", ".log", " to ", ".log_")
                )
            )
        );
        handler.publish(new GlassFishLogRecord(Level.SEVERE, "File two, line two", false));
        Thread.sleep(MILLIS_FOR_PUMP);
        final List<String> fileLines3 = Files.readAllLines(logFile.toPath());
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertTrue(logFile.exists(), "file exists"),
            () -> assertThat("file content, filename=" + logFile + ", content=" + fileLines3 , fileLines3,
                contains(
                    stringContainsInOrder("INFO", "Rolling the file ", ".log", "output was originally enabled: true"),
                    stringContainsInOrder("INFO", "Archiving file ", ".log", " to ", ".log_"),
                    stringContainsInOrder("SEVERE", "main", "File two, line two")
                )
            )
        );
    }

    @Test
    @Order(50)
    public void disabledlogStandardStreams() throws Exception {
        assertTrue(handler.isReady(), "handler.ready");
        final GlassFishLogHandlerConfiguration cfg = handler.getConfiguration();
        cfg.setRedirectStandardStreams(false);
        handler.reconfigure(cfg);
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertSame(System.out, getOriginalStdOut(), "System.out should not be redirected"),
            () -> assertSame(System.err, getOriginalStdErr(), "System.err should not be redirected")
        );
    }


    @Test
    @Order(60)
    public void createConfiguration() throws Exception {
        final GlassFishLogHandlerConfiguration cfg = GlassFishLogHandler
            .createGlassFishLogHandlerConfiguration(GlassFishLogHandler.class);
        assertNotNull(cfg, "cfg");
    }
}
