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

package org.glassfish.orb.http.server;

import jakarta.ejb.AccessLocalException;
import jakarta.ejb.ApplicationException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBTransactionRequiredException;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.TransactionRequiredLocalException;
import jakarta.ejb.TransactionRolledbackLocalException;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

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
 *
 * <p>Four system exceptions are not flattened to {@link EJBException}. They are
 * the ones GlassFish's IIOP business-view wrapper keeps apart, and each says
 * something about the call rather than about the bean:
 * <ul>
 *   <li>a call refused because the caller's transaction can only roll back:
 *       {@link EJBTransactionRolledbackException}</li>
 *   <li>a call the caller may not make: {@link EJBAccessException}</li>
 *   <li>a call that needs a transaction and has none:
 *       {@link EJBTransactionRequiredException}</li>
 *   <li>a call on a bean instance that no longer exists:
 *       {@link NoSuchEJBException}</li>
 * </ul>
 * Wrapping them in a plain {@code EJBException} would hide the one fact the
 * caller needs: "your transaction is doomed" and "you are not allowed" are not
 * "the bean failed".
 *
 * <p>On a remote view the container does not hand these over as themselves.
 * Its IIOP exception mapping turns them into CORBA system exceptions
 * ({@code TRANSACTION_ROLLEDBACK}, {@code NO_PERMISSION},
 * {@code TRANSACTION_REQUIRED}, {@code OBJECT_NOT_EXIST}) that carry the
 * original as their cause, and wraps a bean's system exception in the
 * container-internal {@code InternalEJBContainerException}. The business-view
 * wrapper undoes both, and so does this class: the internal wrapper is removed,
 * and only the exception that is then at the top is looked at, as the IIOP
 * mapping does. A bean's own system exception that merely wraps one of the four
 * is therefore still an {@code EJBException}. Types this module cannot depend
 * on are recognised by class name.
 *
 * <p>A thin client cannot be expected to load container or CORBA classes, so
 * none is shipped to it: where such a type would travel in a cause chain, or as
 * a suppressed exception, a plain copy (class names, messages and stack traces)
 * travels instead.
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
        // Not the container's own wrapper: it is checked, and a method that
        // declares Exception would otherwise make it an application exception.
        if (!isContainerWrapper(thrown) && isApplicationException(thrown, method)) {
            return thrown;
        }
        Throwable top = unwrap(thrown);
        if (top instanceof EJBTransactionRolledbackException || top instanceof EJBAccessException
                || top instanceof EJBTransactionRequiredException || top instanceof NoSuchEJBException) {
            return containsInternal(top) ? rebuilt((EJBException) top) : top;
        }
        if (isRollback(top)) {
            return typed(new EJBTransactionRolledbackException(describe(top)), top);
        }
        if (isAccessDenied(top)) {
            return typed(new EJBAccessException(describe(top)), top);
        }
        if (isTransactionRequired(top)) {
            return typed(new EJBTransactionRequiredException(describe(top)), top);
        }
        if (isNoSuchObject(top)) {
            return typed(new NoSuchEJBException(describe(top)), top);
        }
        if (top != thrown && top instanceof EJBException ejb) {
            // The bean's own system exception, which is what the container
            // wrapped for the transport and what the IIOP wrapper hands over.
            return containsInternal(ejb) ? rebuilt(ejb) : ejb;
        }
        // EJBException carries the original as its "caused by", which is the
        // shape a client already expects to unwrap.
        Throwable cause = safe(top);
        return new EJBException(describe(top), cause instanceof Exception e ? e : new RuntimeException(cause));
    }

    /**
     * The same exception without anything a thin client cannot load: the type,
     * message and stack trace are kept, the causes and suppressed exceptions are
     * plain copies.
     */
    private static EJBException rebuilt(EJBException original) {
        String message = original.getMessage();
        EJBException clean;
        if (original instanceof EJBTransactionRolledbackException) {
            clean = new EJBTransactionRolledbackException(message);
        } else if (original instanceof EJBAccessException) {
            clean = new EJBAccessException(message);
        } else if (original instanceof EJBTransactionRequiredException) {
            clean = new EJBTransactionRequiredException(message);
        } else if (original instanceof NoSuchEJBException) {
            clean = new NoSuchEJBException(message);
        } else {
            clean = new EJBException(message);
        }
        clean.setStackTrace(original.getStackTrace());
        Throwable cause = safe(original.getCause());
        if (cause != null) {
            clean.initCause(cause);
        }
        for (Throwable suppressed : original.getSuppressed()) {
            Throwable plain = plainCopy(suppressed, new int[1], 0);
            if (plain != null) {
                clean.addSuppressed(plain);
            }
        }
        return clean;
    }

    private static boolean isContainerWrapper(Throwable t) {
        return CONTAINER_WRAPPER.equals(t.getClass().getName());
    }

    private static final String CONTAINER_WRAPPER = "com.sun.ejb.containers.InternalEJBContainerException";

    /** How deep a cause chain is followed before it is taken to be a cycle. */
    private static final int MAX_CAUSE_DEPTH = 32;

    /** How many throwables are examined or copied for one exception, cause chains and suppressed ones together. */
    private static final int MAX_NODES = 256;

    /** The classes, by exact name, that only carry another exception across the container or the transport. */
    private static final Set<String> TRANSPORT_WRAPPERS = Set.of(
            CONTAINER_WRAPPER,
            "java.lang.reflect.UndeclaredThrowableException",
            "java.lang.reflect.InvocationTargetException");

    private static final Set<String> ROLLBACK_TYPES = Set.of(
            "org.omg.CORBA.TRANSACTION_ROLLEDBACK",
            "jakarta.transaction.TransactionRolledbackException",
            "javax.transaction.TransactionRolledbackException");

    private static final Set<String> ACCESS_TYPES = Set.of("org.omg.CORBA.NO_PERMISSION");

    private static final Set<String> TRANSACTION_REQUIRED_TYPES = Set.of(
            "org.omg.CORBA.TRANSACTION_REQUIRED",
            "jakarta.transaction.TransactionRequiredException",
            "javax.transaction.TransactionRequiredException");

    private static final Set<String> NO_SUCH_OBJECT_TYPES = Set.of("org.omg.CORBA.OBJECT_NOT_EXIST");

    /** Packages whose classes belong to the container or the ORB and mean nothing to a thin client. */
    private static final List<String> INTERNAL_PREFIXES = List.of(
            "org.omg.", "com.sun.ejb.", "com.sun.corba.", "com.sun.enterprise.",
            "org.glassfish.enterprise.", "org.glassfish.ejb.");

    private static boolean isRollback(Throwable t) {
        return t instanceof TransactionRolledbackLocalException || named(t, ROLLBACK_TYPES);
    }

    private static boolean isAccessDenied(Throwable t) {
        return t instanceof java.rmi.AccessException || t instanceof AccessLocalException || named(t, ACCESS_TYPES);
    }

    private static boolean isTransactionRequired(Throwable t) {
        return t instanceof TransactionRequiredLocalException || named(t, TRANSACTION_REQUIRED_TYPES);
    }

    private static boolean isNoSuchObject(Throwable t) {
        return t instanceof java.rmi.NoSuchObjectException || t instanceof NoSuchObjectLocalException
                || named(t, NO_SUCH_OBJECT_TYPES);
    }

    /** @return whether the class of {@code t}, or one of its superclasses, is one of these */
    private static boolean named(Throwable t, Set<String> names) {
        for (Class<?> c = t.getClass(); c != null; c = c.getSuperclass()) {
            if (names.contains(c.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInternal(Throwable t) {
        for (Class<?> c = t.getClass(); c != null; c = c.getSuperclass()) {
            for (String prefix : INTERNAL_PREFIXES) {
                if (c.getName().startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @return {@code thrown} with the wrappers that only carry another exception taken off */
    private static Throwable unwrap(Throwable thrown) {
        Throwable top = thrown;
        for (int i = 0; i < MAX_CAUSE_DEPTH && top.getCause() != null
                && TRANSPORT_WRAPPERS.contains(top.getClass().getName()); i++) {
            top = top.getCause();
        }
        return top;
    }

    private static String describe(Throwable t) {
        String message = t.getMessage();
        return t.getClass().getName() + (message == null || message.isEmpty() ? "" : ": " + message);
    }

    /**
     * Gives the client exception the cause the IIOP wrapper gives it: the
     * original. When the exception at the top is itself a container type, as the
     * CORBA ones are, that is what it carried.
     */
    private static <T extends EJBException> T typed(T client, Throwable top) {
        Throwable cause = isInternal(top) ? safe(top.getCause()) : safe(top);
        if (cause != null) {
            client.initCause(cause);
        }
        return client;
    }

    /**
     * @return whether {@code root}, its causes or its suppressed exceptions
     *         include a container or CORBA type; what cannot be examined within
     *         the limits counts as including one
     */
    private static boolean containsInternal(Throwable root) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Throwable> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty() && seen.size() < MAX_NODES) {
            Throwable t = pending.poll();
            if (!seen.add(t)) {
                continue;
            }
            if (isInternal(t)) {
                return true;
            }
            if (t.getCause() != null) {
                pending.add(t.getCause());
            }
            pending.addAll(Arrays.asList(t.getSuppressed()));
        }
        return !pending.isEmpty();
    }

    /** @return {@code t} if nothing it carries is a container or CORBA type, else a plain copy of it */
    private static Throwable safe(Throwable t) {
        return t == null || !containsInternal(t) ? t : plainCopy(t, new int[1], 0);
    }

    /** A tree of plain exceptions describing {@code t}, its causes and suppressed ones, without their classes. */
    private static Throwable plainCopy(Throwable t, int[] copied, int depth) {
        if (t == null || depth >= MAX_CAUSE_DEPTH || copied[0] >= MAX_NODES) {
            return null;
        }
        copied[0]++;
        RuntimeException copy = new RuntimeException(describe(t));
        copy.setStackTrace(t.getStackTrace());
        Throwable cause = plainCopy(t.getCause(), copied, depth + 1);
        if (cause != null) {
            copy.initCause(cause);
        }
        for (Throwable suppressed : t.getSuppressed()) {
            Throwable plain = plainCopy(suppressed, copied, depth + 1);
            if (plain != null) {
                copy.addSuppressed(plain);
            }
        }
        return copy;
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
