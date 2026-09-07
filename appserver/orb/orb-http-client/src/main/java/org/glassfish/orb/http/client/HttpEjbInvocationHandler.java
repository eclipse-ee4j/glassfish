/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Dispatches a proxied business-interface call over HTTP.
 * <p>
 * {@code equals}, {@code hashCode} and {@code toString} are answered locally.
 * Sending them to the server would be a remote call per collection operation,
 * and identity for a remote reference is defined by the locator, not by the
 * bean instance's state.
 */
final class HttpEjbInvocationHandler implements InvocationHandler {

    private final HttpEjbClient client;
    private final Class<?> viewClass;
    private final EjbLocator locator;

    HttpEjbInvocationHandler(HttpEjbClient client, Class<?> viewClass, EjbLocator locator) {
        this.client = client;
        this.viewClass = viewClass;
        this.locator = locator;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        }
        // An EJB asynchronous method is declared to return Future<V>. Dispatch
        // it without blocking and let the caller decide when to join.
        if (Future.class.isAssignableFrom(method.getReturnType())) {
            CompletableFuture<Object> future = client.invokeAsync(locator, viewClass, method, args);
            return future;
        }
        return client.invoke(locator, viewClass, method, args);
    }

    private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "equals" -> {
                Object other = args[0];
                if (other == null || !java.lang.reflect.Proxy.isProxyClass(other.getClass())) {
                    yield false;
                }
                Object handler = java.lang.reflect.Proxy.getInvocationHandler(other);
                yield handler instanceof HttpEjbInvocationHandler h
                        && h.viewClass == viewClass
                        && h.locator.equals(locator);
            }
            case "hashCode" -> viewClass.hashCode() * 31 + locator.hashCode();
            case "toString" -> "proxy[" + viewClass.getName() + " -> " + locator + ']';
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }
}
