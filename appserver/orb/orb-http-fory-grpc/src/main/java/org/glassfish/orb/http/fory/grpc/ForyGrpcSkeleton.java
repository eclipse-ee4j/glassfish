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
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable, deploy-time skeleton for a generated Fory service.
 *
 * <p>No method enumeration is performed on the invocation path, and the
 * signature of each method (parameter count and type, return type) was read
 * when the skeleton was built. The transport resolves the path once in the
 * immutable map and then invokes the cached method handle. Container target acquisition and release remain
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
        List<ForyGrpcMethod> methods = ForyViewMethods.of(view).stream()
                .map(method -> ForyGrpcMethod.of('/' + service + '/' + method.getName(), method))
                .toList();
        return new ForyGrpcSkeleton(methods);
    }

    public Object invoke(String path, Object target, Object request) throws Throwable {
        return invoke(path, target, request, ForyGrpcSchemaAdapter.IDENTITY);
    }

    /** Invokes a decoded generated request through an application schema adapter. */
    public Object invoke(String path, Object target, Object request,
                         ForyGrpcSchemaAdapter adapter) throws Throwable {
        ForyGrpcMethod method = methods.get(path);
        if (method == null) {
            throw new NoSuchMethodException(path);
        }
        Object argument = request;
        if (method.parameterCount() == 1) {
            try {
                argument = adapter.request(path, request, method.parameterType());
            } catch (RuntimeException e) {
                throw new ForyGrpcCallException("cannot adapt the request of " + path, e);
            }
            if (!method.accepts(argument)) {
                throw new ForyGrpcCallException("adapted request type does not match " + method.javaMethod());
            }
        }
        // What the bean throws comes out of here as it is.
        Object result = method.invoke(target, argument);
        try {
            return adapter.response(path, result, method.returnType());
        } catch (RuntimeException e) {
            throw new ForyGrpcCallException("cannot adapt the response of " + path, e);
        }
    }

    /**
     * @return whether the failure is one the method declares, which makes it
     *         part of its contract and so something its caller may be told
     */
    public boolean declares(String path, Throwable failure) {
        ForyGrpcMethod method = methods.get(path);
        if (method == null) {
            return false;
        }
        for (Class<?> declared : method.javaMethod().getExceptionTypes()) {
            if (declared.isInstance(failure)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(String path) {
        return methods.containsKey(path);
    }
}
