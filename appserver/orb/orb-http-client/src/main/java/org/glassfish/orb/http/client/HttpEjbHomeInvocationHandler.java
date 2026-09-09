package org.glassfish.orb.http.client;



import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBObject;
import jakarta.ejb.RemoveException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * The EJB 2.x home view: {@code create()} and {@code remove()}.
 *
 * <p>This is the shape a legacy client uses and cannot be talked out of:
 *
 * <pre>
 * Object o = ctx.lookup("java:global/myapp/CartBean");
 * CartHome home = (CartHome) PortableRemoteObject.narrow(o, CartHome.class);
 * Cart cart = home.create();
 * cart.addItem("book");
 * cart.remove();
 * </pre>
 *
 * <p>Supporting it matters because applications written that way are exactly
 * the ones with the strongest reason to move off IIOP and the least appetite
 * for being rewritten first. The business view of EJB 3 is a single interface;
 * this is two, and the client needs a proxy for each.
 *
 * <p><strong>Session beans only.</strong> Entity beans - finders, primary
 * keys, {@code Handle}, {@code EJBMetaData} - are not supported, and were
 * pruned from the platform. {@code create()} maps onto the same session
 * creation the business view uses, so a stateless home hands back a reference
 * without a session and a stateful one hands back a session of its own.
 */
final class HttpEjbHomeInvocationHandler implements InvocationHandler {

    private final HttpEjbClient client;
    private final Class<?> homeClass;
    private final Class<?> componentClass;
    private final EjbLocator locator;

    HttpEjbHomeInvocationHandler(HttpEjbClient client,
                                 Class<?> homeClass,
                                 Class<?> componentClass,
                                 EjbLocator locator) {
        this.client = client;
        this.homeClass = homeClass;
        this.componentClass = componentClass;
        this.locator = locator;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaring = method.getDeclaringClass();

        if (declaring == Object.class) {
            return invokeObjectMethod(method);
        }

        if (declaring == EJBHome.class) {
            return invokeHomeMethod(method, args);
        }

        // Anything else declared on the home interface itself is a create
        // method. The 2.x specification allows create(...) with arguments and
        // several overloads; all of them mean "give me a reference".
        if (method.getName().startsWith("create")) {
            return create();
        }

        throw new RemoteException("unsupported home method: " + method.getName()
                + ". Finders and entity bean homes are not supported by this transport.");
    }

    private Object create() throws Exception {
        byte[] session = client.openSession(locator);
        return client.createComponentProxy(componentClass, locator.withSession(session), proxyHome());
    }

    private Object proxyHome() {
        return client.createHomeProxy(homeClass, componentClass, locator);
    }

    private Object invokeHomeMethod(Method method, Object[] args) throws Exception {
        switch (method.getName()) {
            case "remove":
                // EJBHome.remove takes a Handle or a primary key. Both belong to
                // machinery this transport does not carry, and quietly doing
                // nothing would look like a successful removal.
                throw new RemoveException("remove(Handle) and remove(primaryKey) are not supported;"
                        + " call remove() on the component interface instead");
            case "getEJBMetaData":
            case "getHomeHandle":
                throw new RemoteException(method.getName()
                        + " is not supported by this transport (session beans only)");
            default:
                throw new RemoteException("unsupported EJBHome method: " + method.getName());
        }
    }

    private Object invokeObjectMethod(Method method) {
        return switch (method.getName()) {
            case "equals" -> false;
            case "hashCode" -> homeClass.hashCode() * 31 + locator.hashCode();
            case "toString" -> "home[" + homeClass.getName() + " -> " + locator + ']';
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }

    /** @return the component interface this home creates references to */
    Class<?> componentClass() {
        return componentClass;
    }

    /** Marker so a component proxy can answer getEJBHome without a round trip. */
    static boolean isHomeMethod(Method method) {
        return method.getDeclaringClass() == EJBHome.class
                || method.getDeclaringClass() == EJBObject.class;
    }
}
