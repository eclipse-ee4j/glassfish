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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author David Matejcek
 */
@TestMethodOrder(OrderAnnotation.class)
public class GlassFishLogHandlerTest {

    private static GlassFishLogHandler handler;
    private static File logFile;

    @BeforeAll
    public static void initEnv() throws Exception {
        GlassFishLoggingTracer.setTracingEnabled(true);
        LogManager.getLogManager().reset();
        LoggingSystemEnvironment.initialize();
        final GlassFishLogHandlerConfiguration cfg = new GlassFishLogHandlerConfiguration();
        logFile = File.createTempFile(GlassFishLogHandlerTest.class.getCanonicalName(), ".log");
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
        waitForSize(logFile, 1);
        Logger.getLogger("some.usual.logger").info("Some info message");
        waitForSize(logFile, 2);
        System.err.println("Can you feel me near you?");
        System.err.println("Příliš žluťoučký kůň úpěl ďábelské ódy");
        waitForSize(logFile, 4);
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
        // pump is now to play
        waitForSize(logFile, 5);
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertTrue(handler.getConfiguration().getLogFile().exists(), "file one exists"),
            () -> assertThat("file content", Files.readAllLines(handler.getConfiguration().getLogFile().toPath()),
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
        // There will be an asynchronous thread archiving the file.
        waitForSize(logFile, 2);
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertTrue(handler.getConfiguration().getLogFile().exists(), "file exists"),
            () -> assertThat("file content", Files.readAllLines(handler.getConfiguration().getLogFile().toPath()),
                contains(
                    stringContainsInOrder("INFO", "org.glassfish.main.jul.rotation.LogFileManager.roll",
                        "Rolling the file ", ".log", "output was originally enabled: true"),
                    stringContainsInOrder("INFO", "org.glassfish.main.jul.rotation.LogFileManager.moveFile",
                        "Archiving file ", ".log", " to ", ".log_")
                    )
            )
        );
        handler.publish(new GlassFishLogRecord(Level.SEVERE, "File two, line two", false));
        waitForSize(logFile, 3);
        assertAll(
            () -> assertTrue(handler.isReady(), "handler.ready"),
            () -> assertTrue(handler.getConfiguration().getLogFile().exists(), "file exists"),
            () -> assertThat("file content", Files.readAllLines(handler.getConfiguration().getLogFile().toPath()),
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


    private static void waitForSize(File file, long minLineCount) throws IOException {
        long start = System.currentTimeMillis();
        long lastLength = 0;
        do {
            Thread.onSpinWait();
            long newLength = file.length();
            if (newLength == lastLength) {
                continue;
            }
            lastLength = newLength;
            final long lineCount;
            try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
                lineCount = stream.count();
            }
            if (lineCount >= minLineCount) {
                return;
            }
        } while (System.currentTimeMillis() - start < 10000L);
        fail("Timeout waiting until the file " + file + " grows to " + minLineCount + " lines. File content: \n"
            + Files.readString(file.toPath(), handler.getConfiguration().getEncoding()));
    }
}
