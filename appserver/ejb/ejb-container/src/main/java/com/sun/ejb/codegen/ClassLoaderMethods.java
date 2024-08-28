/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package com.sun.ejb.codegen;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

/**
 * Lazily loaded classloader methods.
 *
 * They are loaded only if needed. If they are not needed, loading them would cause {@link InaccessibleObjectException}
 * due to the module system encapsulation. In that case, {@code --add-opens=java.base/java.lang=ALL-UNNAMED} is needed
 * in the JVM arguments.
 *
 * @author Ondro Mihalyi
 */
class ClassLoaderMethods {

    private static final Logger LOG = Logger.getLogger(ClassGenerator.class.getName());

    private ClassLoaderMethods() {
        // hidden
    }

    static Method defineClassMethod;
    static Method defineClassMethodSM;

    static {
        try {
            final PrivilegedExceptionAction<Void> action = () -> {
                final Class<?> cl = Class.forName("java.lang.ClassLoader");
                final String name = "defineClass";
                defineClassMethod = cl.getDeclaredMethod(name, String.class, byte[].class, int.class, int.class);
                defineClassMethod.setAccessible(true);
                defineClassMethodSM = cl.getDeclaredMethod(
                        name, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
                defineClassMethodSM.setAccessible(true);
                return null;
            };
            AccessController.doPrivileged(action);
            LOG.config("ClassLoader methods capable of generating classes were successfully detected.");
        } catch (final InaccessibleObjectException e) {
            throw new Error("Could not access ClassLoader.defineClass method. Try adding --add-opens=java.base/java.lang=ALL-UNNAMED on the JVM command line.", e);
        } catch (final Exception e) {
            throw new Error("Could not initialize access to ClassLoader.defineClass method.", e);
        }
    }

}
