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
package org.glassfish.orb.http.glassfish;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.transaction.Transaction;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A transaction manager that keeps one transaction per thread of the test, as
 * the real one does, and records what it is asked.
 *
 * <p>Not covered by it: anything the real manager does with the transaction
 * (timeouts, synchronizations, resources) - only the association with the
 * thread and the calls the bridge makes.
 */
final class FakeTransactionManager {

    /** Every call on the manager, as {@code name} or {@code name(argument)}, in order. */
    final List<String> calls = new ArrayList<>();

    /** The transaction each call to {@code rollback} ended. */
    final List<Transaction> rolledBack = new ArrayList<>();

    /** Method names that throw, mapped to what they throw. */
    final Map<String, Exception> failures = new HashMap<>();

    final Set<Transaction> begun = new HashSet<>();

    /** The transaction on the thread, or null. */
    Transaction current;

    private int counter;

    final JavaEETransactionManager manager = (JavaEETransactionManager) Proxy.newProxyInstance(
            FakeTransactionManager.class.getClassLoader(), new Class<?>[] { JavaEETransactionManager.class },
            (proxy, method, args) -> {
                String name = method.getName();
                switch (name) {
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    case "toString":
                        return "fake transaction manager";
                    default:
                        break;
                }
                calls.add(args == null || args.length == 0 ? name : name + "(" + args[0] + ")");
                if (failures.containsKey(name)) {
                    throw failures.get(name);
                }
                switch (name) {
                    case "begin":
                        current = newTransaction();
                        begun.add(current);
                        return null;
                    case "getTransaction":
                        return current;
                    case "setRollbackOnly":
                        if (current == null) {
                            throw new IllegalStateException("no transaction");
                        }
                        return null;
                    case "suspend":
                        Transaction suspended = current;
                        current = null;
                        return suspended;
                    case "resume":
                        current = (Transaction) args[0];
                        return null;
                    case "rollback":
                        if (current == null) {
                            throw new IllegalStateException("no transaction");
                        }
                        rolledBack.add(current);
                        current = null;
                        return null;
                    default:
                        Class<?> type = method.getReturnType();
                        return type == boolean.class ? Boolean.FALSE
                                : type == int.class ? Integer.valueOf(0)
                                : type == long.class ? Long.valueOf(0) : null;
                }
            });

    /** @return a transaction that is only a name, which is all the bridge needs of it */
    Transaction newTransaction() {
        String name = "tx" + (++counter);
        return (Transaction) Proxy.newProxyInstance(FakeTransactionManager.class.getClassLoader(),
                new Class<?>[] { Transaction.class }, (proxy, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> name;
                    default -> null;
                });
    }
}
