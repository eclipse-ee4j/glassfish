package org.glassfish.orb.http.client;


import jakarta.ejb.EJBObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Dispatches a proxied call over HTTP.
 *
 * <p>{@code equals}, {@code hashCode} and {@code toString} are answered
 * locally. Sending them would be a remote call per collection operation, and
 * identity for a remote reference is defined by the locator rather than by the
 * bean instance's state.
 *
 * <p>The same handler serves an EJB 3 business view and an EJB 2.x component
 * view. The difference is that the component view also inherits
 * {@link EJBObject}, whose methods are container operations rather than
 * business ones and are handled here rather than dispatched as invocations.
 */
final class HttpEjbInvocationHandler implements InvocationHandler {

    private final HttpEjbClient client;
    private final Class<?> viewClass;
    private final EjbLocator locator;
    private final Object home;

    HttpEjbInvocationHandler(HttpEjbClient client, Class<?> viewClass, EjbLocator locator) {
        this(client, viewClass, locator, null);
    }

    HttpEjbInvocationHandler(HttpEjbClient client, Class<?> viewClass, EjbLocator locator, Object home) {
        this.client = client;
        this.viewClass = viewClass;
        this.locator = locator;
        this.home = home;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(method, args);
        }
        if (method.getDeclaringClass() == EJBObject.class) {
            return invokeComponentMethod(method, args);
        }
        // An EJB asynchronous method is declared to return Future<V>. Dispatch
        // it without blocking and let the caller decide when to join.
        if (Future.class.isAssignableFrom(method.getReturnType())) {
            CompletableFuture<Object> future = client.invokeAsync(locator, viewClass, method, args);
            return future;
        }
        return client.invoke(locator, viewClass, method, args);
    }

    /** The {@link EJBObject} operations, which the container owns. */
    private Object invokeComponentMethod(Method method, Object[] args) throws Exception {
        switch (method.getName()) {
            case "remove":
                client.removeSession(locator);
                return null;
            case "getEJBHome":
                if (home == null) {
                    throw new RemoteException("this reference was not obtained from a home");
                }
                return home;
            case "isIdentical":
                return args[0] != null
                        && Proxy.isProxyClass(args[0].getClass())
                        && Proxy.getInvocationHandler(args[0]) instanceof HttpEjbInvocationHandler other
                        && other.locator.equals(locator);
            case "getPrimaryKey":
                // Session beans have no primary key; the specification requires
                // this to fail rather than invent one.
                throw new RemoteException("a session bean has no primary key");
            case "getHandle":
                throw new RemoteException("handles are not supported by this transport");
            default:
                throw new RemoteException("unsupported EJBObject method: " + method.getName());
        }
    }

    private Object invokeObjectMethod(Method method, Object[] args) {
        return switch (method.getName()) {
            case "equals" -> {
                Object other = args[0];
                if (other == null || !Proxy.isProxyClass(other.getClass())) {
                    yield false;
                }
                Object handler = Proxy.getInvocationHandler(other);
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
