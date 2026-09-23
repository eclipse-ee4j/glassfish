/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable, deploy-time skeleton for a generated Fory service.
 *
 * <p>No reflection or method enumeration is performed on the invocation path.
 * The transport resolves the path once in the immutable map and then invokes
 * the cached method handle. Container target acquisition and release remain
 * outside this class so GlassFish can apply its normal EJB lifecycle rules.</p>
 */
public final class ForyGrpcSkeleton {

    private final Map<String, ForyGrpcMethod> methods;

    public ForyGrpcSkeleton(List<ForyGrpcMethod> methods) {
        Objects.requireNonNull(methods, "methods");
        HashMap<String, ForyGrpcMethod> index = new HashMap<>(methods.size() * 2);
        for (ForyGrpcMethod method : methods) {
            if (index.putIfAbsent(method.path(), method) != null) {
                throw new IllegalArgumentException("duplicate gRPC method path: " + method.path());
            }
        }
        this.methods = Map.copyOf(index);
    }

    public static ForyGrpcSkeleton of(String service, Class<?> view) {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(view, "view");
        List<ForyGrpcMethod> methods = java.util.Arrays.stream(view.getMethods())
                .filter(method -> method.getDeclaringClass() != Object.class)
                .map(method -> ForyGrpcMethod.of('/' + service + '/' + method.getName(), method))
                .toList();
        return new ForyGrpcSkeleton(methods);
    }

    public Object invoke(String path, Object target, Object request) throws Throwable {
        ForyGrpcMethod method = methods.get(path);
        if (method == null) {
            throw new NoSuchMethodException(path);
        }
        Method javaMethod = method.javaMethod();
        if (javaMethod.getParameterCount() == 1 && request != null
                && !javaMethod.getParameterTypes()[0].isInstance(request)) {
            throw new IllegalArgumentException("request type does not match " + javaMethod);
        }
        return method.invoke(target, request);
    }

    public boolean contains(String path) {
        return methods.containsKey(path);
    }
}
