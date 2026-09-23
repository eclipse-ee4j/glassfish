/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Deploy-time index of Fory service methods and their generated runtimes. */
public final class ForyGeneratedServiceRegistry {

    private final Map<String, Binding> bindings = new ConcurrentHashMap<>();

    public void register(String wirePath, String skeletonPath, ForyGrpcSkeleton skeleton,
                         ForyRuntimeModelGenerator.GeneratedModels models,
                         ForyGeneratedRuntime runtime, ForyGrpcSchemaAdapter adapter) {
        Objects.requireNonNull(wirePath, "wirePath");
        if (bindings.putIfAbsent(wirePath, new Binding(skeletonPath, skeleton, models, runtime, adapter)) != null) {
            throw new IllegalArgumentException("duplicate Fory service method: " + wirePath);
        }
    }

    public Binding lookup(String wirePath) {
        return bindings.get(wirePath);
    }

    public int size() {
        return bindings.size();
    }

    public record Binding(String skeletonPath, ForyGrpcSkeleton skeleton,
                          ForyRuntimeModelGenerator.GeneratedModels models,
                          ForyGeneratedRuntime runtime,
                          ForyGrpcSchemaAdapter adapter) {
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
