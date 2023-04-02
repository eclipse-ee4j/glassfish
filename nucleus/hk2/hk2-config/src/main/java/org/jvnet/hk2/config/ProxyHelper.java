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

final class ProxyHelper {

    private static final Object[] NO_ARGS = new Object[0];

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

        Object[] params = args != null ? args : NO_ARGS;
        return mh.invokeExact(proxy, params);
    }

    private static MethodHandle defaultMethodHandle(Class<? extends Proxy> proxyClass, Method method) {
        MethodType mt = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> proxyIntf = findProxyInterface(proxyClass, method);

        MethodHandle mh;
        try {
            mh = lookup.findSpecial(proxyIntf, method.getName(), mt, proxyClass).asFixedArity();
        } catch (IllegalAccessException | NoSuchMethodException e) {
            // Should newer be thrown
            throw new IllegalStateException(e);
        }

        mh = mh.asType(mh.type().changeReturnType(Object.class));
        mh = mh.asSpreader(1, Object[].class, mt.parameterCount());
        mh = mh.asType(MethodType.methodType(Object.class, Object.class, Object[].class));

        return mh;
    }

    private static Class<?> findProxyInterface(Class<? extends Proxy> proxyClass, Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (!declaringClass.isInterface()) {
            throw new IllegalArgumentException(method + " is not declared in the proxy class " + proxyClass.getName());
        }
        // Because all of our configuration proxies directly implements
        // only one interface annotated @Configured, we simply return
        // this interface
        return proxyClass.getInterfaces()[0];
    }
}
