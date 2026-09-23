/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.orb.http.fory.grpc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;

/** A deploy-time compiled mapping from one gRPC method to one EJB method. */
public final class ForyGrpcMethod {

    private final String path;
    private final Method method;
    private final MethodHandle handle;

    private ForyGrpcMethod(String path, Method method, MethodHandle handle) {
        this.path = path;
        this.method = method;
        this.handle = handle;
    }

    /**
     * Resolves the Java method once, while the application is deployed. The
     * request path is then used as a direct map key on every call.
     */
    public static ForyGrpcMethod of(String path, Method method) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(method, "method");
        try {
            MethodHandle handle = MethodHandles.publicLookup().unreflect(method);
            MethodType erased = method.getParameterCount() == 0
                    ? MethodType.methodType(Object.class, Object.class)
                    : MethodType.methodType(Object.class, Object.class, Object.class);
            return new ForyGrpcMethod(path, method, handle.asType(erased));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("EJB method is not publicly callable: " + method, e);
        }
    }

    public String path() {
        return path;
    }

    public Method javaMethod() {
        return method;
    }

    Object invoke(Object target, Object request) throws Throwable {
        MethodType type = handle.type();
        return switch (type.parameterCount()) {
            case 1 -> handle.invokeExact(target);
            case 2 -> handle.invokeExact(target, request);
            default -> throw new IllegalStateException(
                    "Fory gRPC skeleton supports zero or one request parameter: " + method);
        };
    }
}
