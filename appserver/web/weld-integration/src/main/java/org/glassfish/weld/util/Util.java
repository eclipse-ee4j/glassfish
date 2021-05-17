/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.util;

import java.lang.reflect.InvocationTargetException;

import org.glassfish.weld.ACLSingletonProvider;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider;

public class Util {

    public static <T> T newInstance(String className) {
        try {
            return Util.<T>classForName(className).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("Cannot instantiate instance of " + className + " with no-argument constructor", e);
        }
    }

    public static <T> Class<T> classForName(String name) {
        try {
            if (Thread.currentThread().getContextClassLoader() != null) {
                Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(name);
                @SuppressWarnings("unchecked")
                Class<T> clazz = (Class<T>) c;
                return clazz;
            }
            Class<?> c = Class.forName(name);
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) c;
            return clazz;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new IllegalArgumentException("Cannot load class for " + name, e);
        }
    }

    public static void initializeWeldSingletonProvider() {
        boolean earSupport = false;
        try {
            Class.forName("org.glassfish.javaee.full.deployment.EarClassLoader");
            earSupport = true;
        } catch (ClassNotFoundException ignore) {
        }

        SingletonProvider.initialize(earSupport ? new ACLSingletonProvider() : new TCCLSingletonProvider());
    }

}
