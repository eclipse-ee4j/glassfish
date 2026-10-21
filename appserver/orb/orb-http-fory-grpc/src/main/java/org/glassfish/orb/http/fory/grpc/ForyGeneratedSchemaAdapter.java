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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Deploy-time adapter for the unary message shapes emitted by {@code foryc}.
 * Method discovery happens once; calls use only cached method handles.
 */
public final class ForyGeneratedSchemaAdapter implements ForyGrpcSchemaAdapter {

    private final MethodHandle requestValue;
    private final MethodHandle responseValue;
    private final boolean emptyRequest;
    private final boolean emptyResponse;

    private ForyGeneratedSchemaAdapter(MethodHandle requestValue, MethodHandle responseValue,
                                       boolean emptyRequest, boolean emptyResponse) {
        this.requestValue = requestValue;
        this.responseValue = responseValue;
        this.emptyRequest = emptyRequest;
        this.emptyResponse = emptyResponse;
    }

    /** Creates an adapter for one generated request/response message pair. */
    public static ForyGeneratedSchemaAdapter unary(Class<?> requestModel,
                                                   Class<?> responseModel) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodHandle request = valueAccessor(lookup, requestModel);
            MethodHandle response = valueConstructor(lookup, responseModel);
            return new ForyGeneratedSchemaAdapter(request, response,
                    request == null, response == null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("unsupported Fory generated model pair", e);
        }
    }

    @Override
    public Object request(String methodPath, Object generatedRequest, Class<?> javaType) {
        if (emptyRequest) {
            return null;
        }
        try {
            return requestValue.invoke(generatedRequest);
        } catch (Throwable e) {
            throw new IllegalArgumentException("cannot extract request value for " + methodPath, e);
        }
    }

    @Override
    public Object response(String methodPath, Object javaValue, Class<?> javaType) {
        if (emptyResponse) {
            return null;
        }
        try {
            return responseValue.invoke(javaValue);
        } catch (Throwable e) {
            throw new IllegalArgumentException("cannot construct response value for " + methodPath, e);
        }
    }

    private static MethodHandle valueAccessor(MethodHandles.Lookup lookup, Class<?> model)
            throws ReflectiveOperationException {
        Method method = find(model, "getValue");
        if (method == null) {
            method = find(model, "value");
        }
        return method == null ? null : lookup.unreflect(method);
    }

    private static MethodHandle valueConstructor(MethodHandles.Lookup lookup, Class<?> model)
            throws ReflectiveOperationException {
        for (Constructor<?> constructor : model.getConstructors()) {
            if (constructor.getParameterCount() == 1) {
                return lookup.unreflectConstructor(constructor);
            }
        }
        return null;
    }

    private static Method find(Class<?> type, String name) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 0) {
                return method;
            }
        }
        return null;
    }
}
