/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The registry and the catalog are replaced as a whole: a request that runs
 * while they are rebuilt has to see the old set or the new one.
 */
class ForyGeneratedServiceRegistryTest {

    private static final String CONTEXT = "/glassfish-services";
    private static final int SET_SIZE = 40;

    public interface Greeter {
        String greet(String name);
    }

    private static ForyGeneratedServiceRegistry.Binding binding(Object attachment) {
        ForyRuntimeModelGenerator.GeneratedModels models = ForyRuntimeModelGenerator.unary(
                "generated.registry", "greet", String.class, String.class);
        return new ForyGeneratedServiceRegistry.Binding("/demo.Greeter/greet",
                ForyGrpcSkeleton.of("demo.Greeter", Greeter.class), models,
                new ForyGeneratedRuntime(models, 4000, 4001), ForyGrpcSchemaAdapter.IDENTITY, attachment);
    }

    private static Map<String, ForyGeneratedServiceRegistry.Binding> set(String prefix,
            ForyGeneratedServiceRegistry.Binding binding) {
        Map<String, ForyGeneratedServiceRegistry.Binding> all = new HashMap<>();
        for (int i = 0; i < SET_SIZE; i++) {
            all.put(prefix + i, binding);
        }
        return all;
    }

    @Test
    void replaceAllSwapsTheWholeSetAtOnce() throws Exception {
        ForyGeneratedServiceRegistry registry = new ForyGeneratedServiceRegistry();
        ForyGeneratedServiceRegistry.Binding binding = binding(null);
        Map<String, ForyGeneratedServiceRegistry.Binding> a = set("/a/", binding);
        Map<String, ForyGeneratedServiceRegistry.Binding> b = set("/b/", binding);
        registry.replaceAll(a);

        AtomicBoolean done = new AtomicBoolean();
        AtomicReference<String> failure = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            started.countDown();
            while (!done.get()) {
                int size = registry.size();
                if (size != SET_SIZE) {
                    failure.compareAndSet(null, "a reader saw " + size + " bindings");
                }
            }
        });
        reader.start();
        started.await();
        for (int i = 0; i < 3000 && failure.get() == null; i++) {
            registry.replaceAll(i % 2 == 0 ? b : a);
        }
        done.set(true);
        reader.join(10_000);

        assertNull(failure.get());
    }

    @Test
    void theAttachmentTravelsWithTheBinding() {
        ForyGeneratedServiceRegistry registry = new ForyGeneratedServiceRegistry();
        Object route = new Object();
        registry.replaceAll(Map.of("/x", binding(route)));

        assertSame(route, registry.lookup("/x").attachment());
    }

    @Test
    void registerStillRejectsADuplicate() {
        ForyGeneratedServiceRegistry registry = new ForyGeneratedServiceRegistry();
        ForyGeneratedServiceRegistry.Binding binding = binding(null);
        registry.register("/x", binding.skeletonPath(), binding.skeleton(), binding.models(),
                binding.runtime(), binding.adapter());

        assertThrows(IllegalArgumentException.class, () -> registry.register("/x", binding.skeletonPath(),
                binding.skeleton(), binding.models(), binding.runtime(), binding.adapter()));
        assertEquals(1, registry.size());
    }

    @Test
    void theCatalogIsReplacedAsAWholeToo() throws Exception {
        ForyGrpcCatalog catalog = new ForyGrpcCatalog();
        Map<String, String> a = new HashMap<>();
        Map<String, String> b = new HashMap<>();
        for (int i = 0; i < SET_SIZE; i++) {
            a.put(CONTEXT + ForyGrpcCatalog.PREFIX + "a/" + i + ".fdl", "package a;\n");
            b.put(CONTEXT + ForyGrpcCatalog.PREFIX + "b/" + i + ".fdl", "package b;\n");
        }
        catalog.replaceAll(a);
        AtomicBoolean done = new AtomicBoolean();
        AtomicReference<String> failure = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            started.countDown();
            while (!done.get()) {
                int size = catalog.size();
                if (size != SET_SIZE) {
                    failure.compareAndSet(null, "a reader saw " + size + " documents");
                }
            }
        });
        reader.start();
        started.await();
        for (int i = 0; i < 3000 && failure.get() == null; i++) {
            catalog.replaceAll(i % 2 == 0 ? b : a);
        }
        done.set(true);
        reader.join(10_000);

        assertNull(failure.get());
    }

    @Test
    void anInvalidDocumentLeavesTheCatalogAsItWas() {
        ForyGrpcCatalog catalog = new ForyGrpcCatalog();
        catalog.register(CONTEXT + ForyGrpcCatalog.PREFIX + "app/Bean/demo.fdl", "package demo;\n");
        // The invalid entry first, so that a replacement that empties the
        // catalog before it checks has done so when it fails.
        Map<String, String> next = new java.util.LinkedHashMap<>();
        next.put("/not/under/the/prefix.fdl", "package bad;\n");
        next.put(CONTEXT + ForyGrpcCatalog.PREFIX + "other/Bean/demo.fdl", "package other;\n");

        assertThrows(IllegalArgumentException.class, () -> catalog.replaceAll(next));

        assertEquals(1, catalog.size());
    }
}
