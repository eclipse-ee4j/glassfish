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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** A deploy-time compiled mapping from one gRPC method to one EJB method. */
public final class ForyGrpcMethod {

    private final String path;
    private final Method method;
    private final MethodHandle handle;

    /**
     * The handle to use when the container hands back something that is not an
     * instance of the business view.
     *
     * <p>It does: a remote view is reached through a proxy that implements the
     * <em>generated</em> remote interface - {@code demo._Greeter_Remote} - and
     * not {@code demo.Greeter}, so the cast that {@link #handle} carries fails
     * with "Cannot cast jdk.proxy42.$Proxy242 to demo.Greeter". The EJB path
     * has always re-resolved the method on what it was given; this is the same
     * thing, kept off the invocation path. The container returns one proxy
     * class per view, so after the first call this is a lookup, and the class
     * value releases the handle when the application's classes are unloaded.
     */
    private final ClassValue<MethodHandle> forTargetClass = new ClassValue<>() {
        @Override
        protected MethodHandle computeValue(Class<?> targetClass) {
            return compileFor(targetClass);
        }
    };

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
            return new ForyGrpcMethod(path, method, compile(method));
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
        MethodHandle callable = method.getDeclaringClass().isInstance(target)
                ? handle
                : forTargetClass.get(target.getClass());
        return switch (callable.type().parameterCount()) {
            case 1 -> callable.invokeExact(target);
            case 2 -> callable.invokeExact(target, request);
            default -> throw new IllegalStateException(
                    "Fory gRPC skeleton supports zero or one request parameter: " + method);
        };
    }

    /** Erases the signature so the invocation path needs no per-method call site. */
    private static MethodHandle compile(Method resolved) throws IllegalAccessException {
        MethodHandle handle = MethodHandles.publicLookup().unreflect(resolved);
        MethodType erased = resolved.getParameterCount() == 0
                ? MethodType.methodType(Object.class, Object.class)
                : MethodType.methodType(Object.class, Object.class, Object.class);
        return handle.asType(erased);
    }

    private MethodHandle compileFor(Class<?> targetClass) {
        IllegalAccessException refused = null;
        for (Class<?> declaring : callableTypesOf(targetClass)) {
            Method onTarget;
            try {
                onTarget = declaring.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                continue;
            }
            try {
                return compile(onTarget);
            } catch (IllegalAccessException e) {
                // A generated proxy class lives in a module of its own; the
                // interface it implements is the accessible way in.
                refused = e;
            }
        }
        throw new IllegalArgumentException(targetClass.getName()
                + " does not offer a callable " + method.getName(), refused);
    }

    /**
     * The interfaces the target implements, nearest first, and then the target
     * class itself: an interface method dispatches on the proxy and is public
     * where the proxy class need not be.
     */
    private static List<Class<?>> callableTypesOf(Class<?> targetClass) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        for (Class<?> type = targetClass; type != null && type != Object.class; type = type.getSuperclass()) {
            collectInterfaces(type, interfaces);
        }
        List<Class<?>> types = new ArrayList<>(interfaces);
        types.add(targetClass);
        return types;
    }

    private static void collectInterfaces(Class<?> type, Set<Class<?>> into) {
        for (Class<?> implemented : type.getInterfaces()) {
            if (into.add(implemented)) {
                collectInterfaces(implemented, into);
            }
        }
    }
}
