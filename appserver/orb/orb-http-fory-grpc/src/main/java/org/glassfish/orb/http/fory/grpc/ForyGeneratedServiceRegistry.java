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
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Deploy-time index of Fory service methods and their generated runtimes.
 *
 * <p>The bindings are one immutable map behind one volatile reference. A
 * rebuild builds a new map beside the old one and swaps it in with
 * {@link #replaceAll}, so a lookup that runs during a rebuild sees the whole
 * old set or the whole new one, never a half-filled one.</p>
 */
public final class ForyGeneratedServiceRegistry {

    private volatile Map<String, Binding> bindings = Map.of();

    /**
     * Rebuilds the bindings from the applications deployed now. The bindings
     * are built when the endpoint starts, which is before anything is
     * deployed, so a path that is not here yet may simply belong to an
     * application that arrived later.
     */
    private volatile Runnable refresher = () -> { };

    /**
     * Tells the registry when what it holds is known to be out of date (an
     * application was started or replaced), so that a lookup that finds a
     * binding still asks the refresher before trusting it. A redeployment under
     * the same names would otherwise be a hit on the old binding, forever.
     */
    private volatile BooleanSupplier stale = () -> false;

    public void onStale(BooleanSupplier stale) {
        this.stale = Objects.requireNonNull(stale, "stale");
    }

    /**
     * What to run, instead of the miss refresher, when a lookup finds a binding
     * while {@link #onStale} says the registry is out of date. It is separate
     * because the two differ in what they may do: a caller that is looking for
     * something published can afford to wait for a rebuild, one that is looking
     * for something unknown should not.
     */
    private volatile Runnable staleHit;

    public void onStaleHit(Runnable staleHit) {
        this.staleHit = Objects.requireNonNull(staleHit, "staleHit");
    }

    public void onMiss(Runnable refresher) {
        this.refresher = Objects.requireNonNull(refresher, "refresher");
    }

    public void clear() {
        bindings = Map.of();
    }

    /** Replaces every binding at once with the given set. */
    public void replaceAll(Map<String, Binding> snapshot) {
        bindings = Map.copyOf(Objects.requireNonNull(snapshot, "snapshot"));
    }

    public void register(String wirePath, String skeletonPath, ForyGrpcSkeleton skeleton,
                         ForyRuntimeModelGenerator.GeneratedModels models,
                         ForyGeneratedRuntime runtime, ForyGrpcSchemaAdapter adapter) {
        Objects.requireNonNull(wirePath, "wirePath");
        Binding binding = new Binding(skeletonPath, skeleton, models, runtime, adapter);
        // Copy on write: registering is a deploy-time act, lookups are not.
        synchronized (this) {
            if (bindings.containsKey(wirePath)) {
                throw new IllegalArgumentException("duplicate Fory service method: " + wirePath);
            }
            Map<String, Binding> next = new HashMap<>(bindings);
            next.put(wirePath, binding);
            bindings = Map.copyOf(next);
        }
    }

    public Binding lookup(String wirePath) {
        Binding binding = bindings.get(wirePath);
        if (binding == null) {
            refresher.run();
            binding = bindings.get(wirePath);
        } else if (stale.getAsBoolean()) {
            Runnable hit = staleHit;
            (hit != null ? hit : refresher).run();
            binding = bindings.get(wirePath);
        }
        return binding;
    }

    public int size() {
        return bindings.size();
    }

    /**
     * @param attachment whatever the publisher needs to find again when the
     *        call arrives, carried here so that it is replaced together with
     *        the binding and cannot be looked up from a different generation
     */
    public record Binding(String skeletonPath, ForyGrpcSkeleton skeleton,
                          ForyRuntimeModelGenerator.GeneratedModels models,
                          ForyGeneratedRuntime runtime,
                          ForyGrpcSchemaAdapter adapter,
                          Object attachment) {

        public Binding(String skeletonPath, ForyGrpcSkeleton skeleton,
                       ForyRuntimeModelGenerator.GeneratedModels models,
                       ForyGeneratedRuntime runtime, ForyGrpcSchemaAdapter adapter) {
            this(skeletonPath, skeleton, models, runtime, adapter, null);
        }

        public Binding {
            Objects.requireNonNull(skeletonPath, "skeletonPath");
            Objects.requireNonNull(skeleton, "skeleton");
            Objects.requireNonNull(models, "models");
            Objects.requireNonNull(runtime, "runtime");
            Objects.requireNonNull(adapter, "adapter");
        }

        public Object invoke(Object target, Object request) throws Throwable {
            return skeleton.invoke(skeletonPath, target, request, adapter);
        }
    }
}
