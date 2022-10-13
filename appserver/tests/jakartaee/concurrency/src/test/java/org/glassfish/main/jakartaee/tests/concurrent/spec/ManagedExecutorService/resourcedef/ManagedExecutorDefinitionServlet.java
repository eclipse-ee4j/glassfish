/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jakartaee.tests.concurrent.spec.ManagedExecutorService.resourcedef;

import ee.jakarta.tck.concurrent.spec.ContextService.contextPropagate.ContextServiceDefinitionServlet;

import ee.jakarta.tck.concurrent.common.context.IntContext;
import ee.jakarta.tck.concurrent.common.context.StringContext;
import ee.jakarta.tck.concurrent.framework.TestServlet;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.annotation.WebServlet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.naming.InitialContext;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @ContextServiceDefinitions are defined under {@link ContextServiceDefinitionServlet}
 */
@ManagedExecutorDefinition(name = "java:module/concurrent/ExecutorB",
                           context = "java:module/concurrent/ContextB",
                           maxAsync = 1)
@WebServlet("/ManagedExecutorDefinitionServlet")
public class ManagedExecutorDefinitionServlet extends TestServlet {
    private static final long serialVersionUID = 1L;
    private static final long MAX_WAIT_SECONDS = TimeUnit.MINUTES.toSeconds(2);

    /**
     * ManagedExecutorService can create a contextualized copy of an unmanaged CompletableFuture (twice in a row).
     */
    public void testCopyCompletableFutureTwice() throws Throwable {
        copyCompletableFuture(273);
        copyCompletableFuture(373);
    }

    private void copyCompletableFuture(int iContextValue) throws Throwable {
        ManagedExecutorService executor = InitialContext.doLookup("java:module/concurrent/ExecutorB");

        IntContext.set(iContextValue);
        StringContext.set("testCopyCompletableFuture-1");
        try {
            CompletableFuture<Character> stage1unmanaged = new CompletableFuture<Character>();
            CompletableFuture<Character> stage1copy = executor.copy(stage1unmanaged);
            CompletableFuture<Character> permanentlyIncompleteStage = new CompletableFuture<Character>();

            StringContext.set("testCopyCompletableFuture-2");

            CompletableFuture<String> stage2 = stage1copy.applyToEitherAsync(permanentlyIncompleteStage, sep -> {
                String s = StringContext.get();
                return "StringContext " + ("testCopyCompletableFuture-2".equals(s) ? "propagated" : "incorrect:" + s)
                                + sep;
            });

            StringContext.set("testCopyCompletableFuture-3");

            CompletableFuture<String> stage3 = stage2.handleAsync((result, failure) -> {
                if (failure == null) {
                    int i = IntContext.get();
                    return result + "IntContext " + (i == 0 ? "unchanged" : "incorrect:" + i);
                } else {
                    throw (AssertionError) new AssertionError().initCause(failure);
                }
            });

            assertTrue(stage1unmanaged.complete(';'),
                       "Completation stage that is supplied to copy must not be modified by the " +
                       "ManagedExecutorService.");

            String result = stage3.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            assertEquals(result, "StringContext propagated;IntContext unchanged",
                         "StringContext must be propagated and Application context and IntContext must be left " +
                         "unchanged per ManagedExecutorDefinition and ContextServiceDefinition config.");
        } finally {
            IntContext.set(0);;
            StringContext.set(null);
        }
    }

}
