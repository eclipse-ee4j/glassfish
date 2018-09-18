/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
Enforce singleton semantics: if an item already exists, an Error is thrown.
Otherwise it is recorded for future checks.  This facility exists because of 
ugly framework requirements for singletons to offer public constructors, which can allow
more than one instance of a class to be instantiated.
 */
public final class SingletonEnforcer {

    private static final ConcurrentMap<Class<?>, Object> mItems = new ConcurrentHashMap<Class<?>, Object>();

    private SingletonEnforcer() {
    }

    public static <T> T get(final Class<T> theClass) {
        return theClass.cast(mItems.get(theClass));
    }

    public static void register(final Class<?> theClass, final Object theInstance) {
        if (mItems.putIfAbsent(theClass, theInstance) != null) {
            throw new IllegalArgumentException("Illegal to register more than one instance of " + theClass.getName());
        }
    }

    public static void deregister(final Class<?> theClass, final Object theInstance) {
        if (!mItems.remove(theClass, theInstance)) {
            throw new IllegalArgumentException("Cannot deregister the instance of " + theClass.getName());
        }
    }
}

