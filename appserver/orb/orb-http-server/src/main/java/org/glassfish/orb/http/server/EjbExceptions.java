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

import jakarta.ejb.ApplicationException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.TransactionRolledbackLocalException;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
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
 * <p>One system exception is not flattened to {@link EJBException}: a call
 * refused because the caller's transaction can only roll back. The container
 * raises {@link TransactionRolledbackLocalException} for that, and the
 * specification gives a business client {@link EJBTransactionRolledbackException},
 * which is the type a caller catches to tell "your transaction is doomed"
 * from "the bean failed". Wrapping it in a plain {@code EJBException} would
 * hide the one fact the caller needs.
 *
 * <p>The container does not always raise that exception as itself. For a call
 * arriving on a remote view GlassFish passes what the container threw through
 * its IIOP exception mapping, which turns it into a CORBA
 * {@code TRANSACTION_ROLLEDBACK} carrying the original as its cause, or a
 * {@code RuntimeException} wrapping either. So the whole cause chain is
 * searched, by class name for the types this module cannot depend on.
 *
 * <p>A thin client may not be able to load CORBA classes, so none is shipped to
 * it: where a CORBA type would travel as a cause, a plain copy of the chain
 * (class names, messages and stack traces) travels instead.
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
        if (thrown instanceof EJBTransactionRolledbackException) {
            return thrown;
        }
        Throwable rollback = findRollback(thrown);
        if (rollback != null) {
            EJBTransactionRolledbackException rolledBack = new EJBTransactionRolledbackException(describe(rollback));
            Throwable cause = isCorba(rollback) ? safe(rollback.getCause()) : safe(rollback);
            if (cause != null) {
                rolledBack.initCause(cause);
            }
            return rolledBack;
        }
        // EJBException carries the original as its "caused by", which is the
        // shape a client already expects to unwrap.
        Throwable cause = safe(thrown);
        return new EJBException(describe(thrown), cause instanceof Exception e ? e : new RuntimeException(cause));
    }

    /** How deep a cause chain is followed before it is taken to be a cycle. */
    private static final int MAX_CAUSE_DEPTH = 32;

    /** The classes, by name, that say a transaction was rolled back, none of which this module can depend on. */
    private static final Set<String> ROLLBACK_TYPES = Set.of(
            "org.omg.CORBA.TRANSACTION_ROLLEDBACK",
            "jakarta.transaction.TransactionRolledbackException",
            "javax.transaction.TransactionRolledbackException");

    private static String describe(Throwable t) {
        return t.getClass().getName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
    }

    /** @return the first throwable in the cause chain that reports a rollback, or null */
    private static Throwable findRollback(Throwable thrown) {
        int depth = 0;
        for (Throwable t = thrown; t != null && depth < MAX_CAUSE_DEPTH; t = t.getCause(), depth++) {
            if (t instanceof TransactionRolledbackLocalException) {
                return t;
            }
            for (Class<?> c = t.getClass(); c != null; c = c.getSuperclass()) {
                if (ROLLBACK_TYPES.contains(c.getName())) {
                    return t;
                }
            }
        }
        return null;
    }

    private static boolean isCorba(Throwable t) {
        for (Class<?> c = t.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getName().startsWith("org.omg.CORBA.")) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCorba(Throwable thrown) {
        int depth = 0;
        for (Throwable t = thrown; t != null && depth < MAX_CAUSE_DEPTH; t = t.getCause(), depth++) {
            if (isCorba(t)) {
                return true;
            }
        }
        return false;
    }

    /** @return {@code t} if nothing in its chain is a CORBA type, else a plain copy of it */
    private static Throwable safe(Throwable t) {
        return t == null || !hasCorba(t) ? t : plainCopy(t);
    }

    /** A chain of plain exceptions that describes {@code t} and its causes without their classes. */
    private static Throwable plainCopy(Throwable t) {
        return plainCopy(t, 0);
    }

    private static Throwable plainCopy(Throwable t, int depth) {
        if (t == null || depth >= MAX_CAUSE_DEPTH) {
            return null;
        }
        RuntimeException copy = new RuntimeException(describe(t));
        copy.setStackTrace(t.getStackTrace());
        Throwable cause = plainCopy(t.getCause(), depth + 1);
        if (cause != null) {
            copy.initCause(cause);
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
