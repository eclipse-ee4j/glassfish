/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.diagnostics.context.impl;

import java.util.LinkedList;
import java.util.List;

import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.diagnostics.context.Context;
import org.glassfish.diagnostics.context.ContextManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test between diagnostics context implementation and
 * context propagation.
 *
 * This test explicitly initializes the context propagation module and
 * the ContextManagerImpl under test.
 */
public class ContextImplContextPropagationIntegrationTest {

    private ContextManager mContextManager;

    @BeforeAll
    public static void setUpOncePerTestClass() throws Exception {
        // Natural place to add initialization of context propagation but
        // the ContextBootstrap therein initializes as part of class
        // load. We could wait for lazy initialization per test but
        // clearer to force it to happen here
        Class.forName(ContextBootstrap.class.getName());
    }


    @BeforeEach
    public void setUpOncePerTestMethod() {
        mContextManager = new ContextManagerImpl();
    }


    /**
     * Verify that multiple calls to get the current diagnostics context
     * return the same instance.
     */
    @Test
    public void testThreadLocalBehaviour() {
        Context diagnosticsContextStart = mContextManager.getContext();
        assertEquals(ContextImpl.class.getName(), diagnosticsContextStart.getClass().getName());
        for (int i = 0; i < 13; i++) {
            Context diagnosticsContext = mContextManager.getContext();
            assertSame(diagnosticsContextStart, diagnosticsContext,
                "The diagnostics context instance returned in iteration " + i
                    + " is not the same instance as fetched at the start of the test.");
        }
    }


    /**
     * Verify that values set on the incumbent diagnostics context remain
     * accessible on subsequent fetches of the diagnostics context.
     */
    @Test
    public void testValuePersistence() {
        final String propagatingKey = "propagatingKey";
        final String propagatingValue = "propagatingValue";
        final String nonPropagatingKey = "nonPropagatingKey";
        final String nonPropagatingValue = "nonPropagatingValue";

        {
            Context diagnosticsContextStart = mContextManager.getContext();
            diagnosticsContextStart.put(propagatingKey, propagatingValue, true);
            diagnosticsContextStart.put(nonPropagatingKey, nonPropagatingValue, false);
        }

        for (int i = 0; i < 17; i++) {
            Context diagnosticsContext = mContextManager.getContext();
            assertEquals(propagatingValue, diagnosticsContext.get(propagatingKey),
                "The value associated with key " + propagatingKey + " is not as expected.");
            assertEquals(nonPropagatingValue, diagnosticsContext.get(nonPropagatingKey),
                "The value associated with key " + nonPropagatingKey + " is not as expected.");
        }
    }


    @Test
    @Disabled("this test fails, decide if it is a feature or a bug and fix this discrepancy.")
    public void testValuePropagationAndNonPropagation() throws Exception {
        final String propagatingKey = "propagatingKey";
        final String propagatingValue = "propagatingValue";
        final String nonPropagatingKey = "nonPropagatingKey";
        final String nonPropagatingValue = "nonPropagatingValue";
        final ContextManager contextManager = mContextManager;
        final List<Throwable> exceptionList = new LinkedList();
        final List<Thread> threadList = new LinkedList();

        {
            Context diagnosticsContextStart = mContextManager.getContext();
            diagnosticsContextStart.put(propagatingKey, propagatingValue, true);
            diagnosticsContextStart.put(nonPropagatingKey, nonPropagatingValue, false);
        }

        for (int i = 0; i < 17; i++) {
            Thread t = new Thread(() -> {
                try {
                    String threadName = Thread.currentThread().getName();
                    Context diagnosticsContext = contextManager.getContext();
                    assertEquals(propagatingValue, diagnosticsContext.get(propagatingKey),
                        "The value associated with key " + propagatingKey + " on thread " + threadName
                            + " is not as expected.");
                    assertNull(diagnosticsContext.get(nonPropagatingKey),
                        "The null value should be associated with key " + nonPropagatingKey + " on thread "
                            + threadName);
                } catch (Throwable e) {
                    synchronized (exceptionList) {
                        exceptionList.add(e);
                    }
                }
            });
            t.setName("Child_" + i + "_of_parent_'" + Thread.currentThread().getName() + "'");
            t.start();
            threadList.add(t);
        }

        for (Thread t : threadList) {
            t.join();
        }

        if (!exceptionList.isEmpty()) {
            fail(() -> {
                StringBuilder sb = new StringBuilder();
                for (Throwable e : exceptionList) {
                    sb.append("\n  ").append(e.getMessage());
                }
                sb.append("\n");
                return sb.toString();
            });
        }
    }
}
