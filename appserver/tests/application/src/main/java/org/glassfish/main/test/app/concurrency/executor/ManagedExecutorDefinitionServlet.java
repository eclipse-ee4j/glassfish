/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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
package org.glassfish.main.test.app.concurrency.executor;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.main.test.app.concurrency.executor.IntContextProvider.IntContext;
import org.glassfish.main.test.app.concurrency.executor.StringContextProvider.StringContext;

import static jakarta.enterprise.concurrent.ContextServiceDefinition.ALL_REMAINING;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.APPLICATION;
import static jakarta.enterprise.concurrent.ContextServiceDefinition.TRANSACTION;

@ContextServiceDefinition(
    name = "java:app/concurrent/ContextB",
    cleared = TRANSACTION,
    propagated = ALL_REMAINING,
    unchanged = {APPLICATION, "IntContext"}
)
@ManagedExecutorDefinition(
    name = "java:app/concurrent/ExecutorB",
    context = "java:app/concurrent/ContextB",
    maxAsync = 1
)
@WebServlet("/")
public class ManagedExecutorDefinitionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;


    /**
     * ManagedExecutorService can create a contextualized copy of an unmanaged CompletableFuture (twice in a row).
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        copyCompletableFuture(273);
        copyCompletableFuture(373);
    }

    private void copyCompletableFuture(int iContextValue) {
        IntContext.set(iContextValue);
        StringContext.set("STAGE1");
        try {
            ManagedExecutorService executor = InitialContext.doLookup("java:app/concurrent/ExecutorB");
            final CompletableFuture<Character> stage1unmanaged = new CompletableFuture<>();
            final CompletableFuture<Character> stage1copy = executor.copy(stage1unmanaged);
            final CompletableFuture<Character> permanentlyIncompleteStage = new CompletableFuture<>();

            StringContext.set("STAGE2");
            final CompletableFuture<String> stage2 = stage1copy.applyToEitherAsync(permanentlyIncompleteStage,
                separator -> {
                    final String string = StringContext.get();
                    final String message = "STAGE2".equals(string) ? "propagated" : "incorrect: " + string;
                    return "StringContext " + message + separator;
                });

            StringContext.set("STAGE3");
            final CompletableFuture<String> stage3 = stage2.handleAsync((stage2result, failure) -> {
                if (failure == null) {
                    final int i = IntContext.get();
                    return stage2result + " IntContext " + (i == 0 ? "unchanged" : "incorrect: " + i);
                }
                throw new IllegalStateException(failure);
            });

            if (!stage1unmanaged.complete(';')) {
                throw new IllegalStateException(
                    "Completation stage that is supplied to copy must not be modified by the ManagedExecutorService.");
            }

            final String result = stage3.get(10, TimeUnit.SECONDS);
            if ("StringContext propagated; IntContext unchanged".equals(result)) {
                return;
            }
            throw new IllegalStateException("Result: " + result + "\n");
        } catch (InterruptedException | ExecutionException | TimeoutException | NamingException e) {
            throw new IllegalStateException("Unexpected failure.", e);
        } finally {
            IntContext.set(0);
            StringContext.set(null);
        }
    }
}
