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

package org.glassfish.tests.utils.junit;

import java.lang.System.Logger;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import static java.lang.System.Logger.Level.INFO;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * Logs when test started and finished and how much time it took.
 * Useful when you need to find relevant logs.
 */
public class TestLoggingExtension
    implements BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {
    private static final Logger LOG = System.getLogger(TestLoggingExtension.class.getName());
    private static final String START_TIME_METHOD = "start time method";
    private static final String START_TIME_CLASS = "start time class";

    private Namespace namespaceClass;
    private Namespace namespaceMethod;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        this.namespaceClass = Namespace.create(context.getRequiredTestClass());
        context.getStore(this.namespaceClass).put(START_TIME_CLASS, LocalDateTime.now());
        LOG.log(INFO, "Starting test {0}", context.getTestClass().orElse(null));
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Method testMethod = context.getRequiredTestMethod();
        Class<?> testClass = context.getRequiredTestClass();
        this.namespaceMethod = Namespace.create(testClass, testMethod);
        context.getStore(this.namespaceMethod).put(START_TIME_METHOD, LocalDateTime.now());
        LOG.log(INFO, "Starting test method {0}.{1}", testClass.getSimpleName(), testMethod.getName());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Method testMethod = context.getRequiredTestMethod();
        Class<?> testClass = context.getRequiredTestClass();
        final LocalDateTime startTime = context.getStore(namespaceMethod).remove(START_TIME_METHOD,
            LocalDateTime.class);
        if (startTime == null) {
            // don't log if the beforeEach failed.
            return;
        }
        LOG.log(INFO, "Test method {0}.{1}, started at {2}, test time: {3} ms", testClass.getSimpleName(),
            testMethod.getName(), ISO_LOCAL_TIME.format(startTime), startTime.until(LocalDateTime.now(), MILLIS));

    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        final LocalDateTime startTime = context.getStore(namespaceClass).remove(START_TIME_CLASS, LocalDateTime.class);
        if (startTime == null) {
            // don't log if the beforeAll failed.
            return;
        }
        LOG.log(INFO, "Test {0}, started at {1}, test time: {2} ms", context.getTestClass(),
            ISO_LOCAL_TIME.format(startTime), startTime.until(LocalDateTime.now(), MILLIS));
    }
}
