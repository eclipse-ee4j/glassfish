package org.glassfish.orb.http.server;


import jakarta.ejb.ApplicationException;
import jakarta.ejb.EJBException;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

/**
 * Decides whether what a bean threw belongs to the caller or to the container.
 *
 * <p>This is not a detail of presentation. An application exception is part of
 * the business contract: it reaches the caller as itself, carrying whatever
 * state it carries, and the bean instance survives. A system exception is a
 * failure of the bean: the container discards the instance and the caller sees
 * {@link EJBException} rather than the type that was thrown.
 *
 * <p>The transport got this wrong until an IIOP baseline running against a
 * real server was compared against it. Both cases were being propagated
 * unchanged, which meant an application moved from IIOP to HTTP would start
 * catching exception types its {@code catch} blocks had never been able to see
 * before - a change of behaviour in code nobody had touched, and exactly the
 * kind a migration must not make.
 *
 * <p>The rule, from the Enterprise Beans specification:
 * <ul>
 *   <li>anything annotated {@link ApplicationException}, checked or not, is an
 *       application exception</li>
 *   <li>a checked exception the method declares is an application exception,
 *       except {@link RemoteException}</li>
 *   <li>everything else - unchecked, undeclared, or an {@link Error} - is a
 *       system exception</li>
 * </ul>
 */
public final class EjbExceptions {

    private EjbExceptions() {
    }

    /**
     * @param thrown what the business method threw
     * @param method the method it was thrown from
     * @return the exception the caller should receive
     */
    public static Throwable toClientException(Throwable thrown, Method method) {
        if (isApplicationException(thrown, method)) {
            return thrown;
        }
        // EJBException carries the original as its "caused by", which is the
        // shape a client already expects to unwrap.
        return new EJBException(thrown.getClass().getName()
                + (thrown.getMessage() == null ? "" : ": " + thrown.getMessage()),
                thrown instanceof Exception cause ? cause : new RuntimeException(thrown));
    }

    /**
     * @param thrown what the business method threw
     * @param method the method it was thrown from
     * @return whether it is part of the business contract
     */
    public static boolean isApplicationException(Throwable thrown, Method method) {
        if (findAnnotation(thrown.getClass()) != null) {
            return true;
        }
        if (thrown instanceof RuntimeException || thrown instanceof Error) {
            return false;
        }
        for (Class<?> declared : method.getExceptionTypes()) {
            if (declared.isInstance(thrown) && !RemoteException.class.isAssignableFrom(declared)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Walks up for an {@link ApplicationException}, honouring
     * {@code inherited}.
     *
     * <p>The flag is not decoration: an exception hierarchy may declare its
     * root an application exception and mean it only for the root. Finding the
     * annotation on a superclass and stopping there would make every subclass
     * an application exception whether or not that was intended.
     *
     * @param type the thrown type
     * @return the annotation that governs it, or null
     */
    static ApplicationException findAnnotation(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            ApplicationException annotation = current.getDeclaredAnnotation(ApplicationException.class);
            if (annotation != null) {
                return current == type || annotation.inherited() ? annotation : null;
            }
        }
        return null;
    }
}
