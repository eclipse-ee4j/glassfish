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

import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author David Matejcek
 */
public class GlassFishLoggerTest {

    private static final String BUNDLE_WITHOUT_PARAMETER = "bundle.without.parameter";
    private static final String BUNDLE_WITH_PARAMETER = "bundle.with.parameter";

    private static GlassFishLogger logger;
    private static LogCollectorHandler handler;


    @BeforeAll
    public static void initEnv() throws Exception {
        final Logger originalLogger = Logger.getLogger(GlassFishLoggerTest.class.getName());
        originalLogger.setResourceBundle(new TestResourceBundle());
        handler = new LogCollectorHandler(originalLogger);
        logger = (GlassFishLogger) Logger.getLogger(originalLogger.getName());
        logger.setLevel(Level.FINEST);
    }

    @BeforeEach
    public void reinit() {
        logger.setLevel(Level.FINEST);
        handler.setLevel(Level.ALL);
    }


    @AfterAll
    public static void resetEverything() {
        if (handler != null) {
            handler.close();
        }
        assertThat("Nothing should remain after close", handler.getAll(), hasSize(0));
    }


    @Test
    public void filteredLevelAndSupplier() {
        logger.setLevel(Level.INFO);
        logger.finest(() -> "supplied text");
        assertAll(
            () -> assertNull(handler.pop(), "more records"),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }


    @Test
    public void logParameter() {
        logger.log(Level.FINE, "bundle.with.parameter", 42L);
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertEquals("bundle.with.parameter", record.getMessageKey(), "messageKey"),
            () -> assertEquals("resourceBundleValue with this parameter: 42", record.getMessage(), "message"),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logSupplier() {
        logger.log(Level.SEVERE, () -> "It is not broken!");
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("It is not broken!", record.getMessage(), "message"),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logException() {
        final RuntimeException exception = new RuntimeException("Kaboom!");
        logger.log(Level.SEVERE, "It is not broken!", exception);
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("It is not broken!", record.getMessage(), "message"),
            () -> assertSame(exception, record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty()));
    }

    @Test
    public void logExceptionAndSupplier() {
        final RuntimeException exception = new RuntimeException("Kaboom!");
        logger.log(Level.SEVERE, exception, () -> "It is not broken!");
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("It is not broken!", record.getMessage(), "message"),
            () -> assertSame(exception, record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }


    @Test
    public void logp() {
        logger.logp(Level.SEVERE, "FakeClass", "fakeMethod", "It is not broken!");
        final GlassFishLogRecord record = handler.pop();
        assertAll(() -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("It is not broken!", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertNull(record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logpSupplier() {
        logger.logp(Level.SEVERE, "FakeClass", "fakeMethod", () -> "It is not broken!");
        final GlassFishLogRecord record = handler.pop();
        assertAll(() -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("It is not broken!", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertNull(record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logpException() {
        final RuntimeException exception = new RuntimeException("Kaboom!");
        logger.logp(Level.SEVERE, "FakeClass", "fakeMethod", "It is not broken!", exception);
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("It is not broken!", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertSame(exception, record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logpExceptionWithSupplier() {
        final RuntimeException exception = new RuntimeException("Kaboom!");
        logger.logp(Level.SEVERE, "FakeClass", "fakeMethod", exception, () -> "It is not broken!");
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("It is not broken!", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertSame(exception, record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logpParameter() {
        logger.logp(Level.SEVERE, "FakeClass", "fakeMethod", "Message {0}", "is here.");
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("Message is here.", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertNull(record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logpParameters() {
        logger.logp(Level.SEVERE, "FakeClass", "fakeMethod", "Hello {0}, the number is {1}.",
            new Object[] {"my friend", 9});
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("Hello my friend, the number is 9.", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertNull(record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void entering0Params() {
        logger.entering("FakeClass", "fakeMethod", new Object[0]);
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("ENTRY", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertNull(record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void entering3Params() {
        logger.entering("FakeClass", "fakeMethod", new Object[] {"paramVal1", 2, "paramVal3"});
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("ENTRY paramVal1 2 paramVal3", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertNull(record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void throwing() {
        final RuntimeException exception = new RuntimeException("Kaboom!");
        logger.throwing("FakeClass", "fakeMethod", exception);
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertNull(record.getMessageKey(), "messageKey"),
            () -> assertEquals("THROW", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertSame(exception, record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logrbThrowable() {
        final RuntimeException exception = new RuntimeException("Kaboom!");
        final ResourceBundle bundle = new AnotherTestResourceBundle();
        logger.logrb(Level.SEVERE, "FakeClass", "fakeMethod", bundle, BUNDLE_WITHOUT_PARAMETER, exception);
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertEquals(BUNDLE_WITHOUT_PARAMETER, record.getMessageKey(), "messageKey"),
            () -> assertEquals("another one.", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertSame(exception, record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    @Test
    public void logrbParameters() {
        final ResourceBundle bundle = new AnotherTestResourceBundle();
        logger.logrb(Level.SEVERE, "FakeClass", "fakeMethod", bundle, BUNDLE_WITH_PARAMETER, new Object[] {"X"});
        final GlassFishLogRecord record = handler.pop();
        assertAll(
            () -> assertEquals(BUNDLE_WITH_PARAMETER, record.getMessageKey(), "messageKey"),
            () -> assertEquals("another X.", record.getMessage(), "message"),
            () -> assertEquals("FakeClass", record.getSourceClassName()),
            () -> assertEquals("fakeMethod", record.getSourceMethodName()),
            () -> assertNull(record.getThrown()),
            () -> assertThat(handler.getAll(), IsEmptyCollection.empty())
        );
    }

    private static class TestResourceBundle extends ListResourceBundle {

        @Override
        public String getBaseBundleName() {
            return getClass().getSimpleName();
        }


        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                {BUNDLE_WITH_PARAMETER, "resourceBundleValue with this parameter: {0}"},
                {BUNDLE_WITHOUT_PARAMETER, "resourceBundleValue without parameter"}
            };
        }
    }


    private static final class AnotherTestResourceBundle extends TestResourceBundle {

        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                {BUNDLE_WITH_PARAMETER, "another {0}."},
                {BUNDLE_WITHOUT_PARAMETER, "another one."}
            };
        }

    }
}
