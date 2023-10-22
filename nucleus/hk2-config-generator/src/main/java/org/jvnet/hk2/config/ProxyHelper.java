/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.jvnet.hk2.config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class ProxyHelper {

    private static final Object[] NO_ARGS = new Object[0];

    private static final MethodHandle EXCEPTION_HANDLER;
    static {
        try {
            EXCEPTION_HANDLER = MethodHandles.lookup().findStatic(
                    ProxyHelper.class,
                    "exceptionHandler",
                    MethodType.methodType(Object.class, Throwable.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // Should never be thrown
            throw new IllegalStateException(e);
        }
    }

    private static final ClassValue<ConcurrentMap<Method, MethodHandle>>
            DEFAULT_METHODS_CACHE = new ClassValue<>() {
                @Override
                protected ConcurrentMap<Method, MethodHandle> computeValue(Class<?> type) {
                    return new ConcurrentHashMap<>();
                }
            };

    private ProxyHelper() {
        throw new AssertionError();
    }

    public static Object invokeDefault(Object proxy, Method method, Object... args) throws Throwable {
        Objects.requireNonNull(proxy);
        Objects.requireNonNull(method);

        @SuppressWarnings("unchecked")
        Class<? extends Proxy> proxyClass = (Class<? extends Proxy>) proxy.getClass();

        if (!Proxy.isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException(proxy + " is not a proxy instance");
        }

        if (!method.isDefault()) {
            throw new IllegalArgumentException(method + " is not a default method");
        }

        MethodHandle mh = defaultMethodHandle(proxyClass, method);

        try {
            Object[] params = args != null ? args : NO_ARGS;
            return mh.invokeExact(proxy, params);
        } catch (ClassCastException | NullPointerException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (ProxyInvocationException e) {
            // unwrap and throw the default method exception
            throw e.getCause();
        }
    }

    private static MethodHandle defaultMethodHandle(Class<? extends Proxy> proxyClass, Method method) {
        ConcurrentMap<Method, MethodHandle> defaultMethods = DEFAULT_METHODS_CACHE.get(proxyClass);

        MethodHandle defaultMethodHandle = defaultMethods.get(method);
        if (defaultMethodHandle == null) {
            MethodType mt = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> proxyIntf = findProxyInterface(proxyClass, method);

            MethodHandle methodHandle;
            try {
                methodHandle = lookup.findSpecial(proxyIntf, method.getName(), mt, proxyClass).asFixedArity();
            } catch (IllegalAccessException | NoSuchMethodException e) {
                // Should newer be thrown
                throw new IllegalStateException(e);
            }

            methodHandle = methodHandle.asType(methodHandle.type().changeReturnType(Object.class));
            // Wrap an exception thrown by the default method
            methodHandle = MethodHandles.catchException(methodHandle, Throwable.class, EXCEPTION_HANDLER);
            methodHandle = methodHandle.asSpreader(1, Object[].class, mt.parameterCount());
            methodHandle = methodHandle.asType(MethodType.methodType(Object.class, Object.class, Object[].class));

            MethodHandle cachedMethodHandle = defaultMethods.putIfAbsent(method, methodHandle);
            if (cachedMethodHandle != null) {
                defaultMethodHandle = cachedMethodHandle;
            } else {
                defaultMethodHandle = methodHandle;
            }
        }

        return defaultMethodHandle;
    }

    private static Class<?> findProxyInterface(Class<? extends Proxy> proxyClass, Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (!declaringClass.isInterface()) {
            throw new IllegalArgumentException(method + " is not declared in the proxy class " + proxyClass.getName());
        }
        // Because all of our configuration proxies directly implements
        // only one interface annotated @Configured, we simply return
        // this interface, without search
        return proxyClass.getInterfaces()[0];
    }

    private static Object exceptionHandler(Throwable cause) throws ProxyInvocationException {
        throw new ProxyInvocationException(cause);
    }

    private static class ProxyInvocationException extends ReflectiveOperationException {

        ProxyInvocationException(Throwable cause) {
            super(cause);
        }
    }
}
