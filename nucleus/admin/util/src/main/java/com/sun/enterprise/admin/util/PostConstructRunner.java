/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import jakarta.annotation.PostConstruct;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.util.LinkedList;

/**
 *
 * @author tjquinn
 */
public class PostConstructRunner {

    public static void runPostConstructs(final Object obj)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, PrivilegedActionException {
        /*
         * As we ascend the hierarchy, record the @PostConstruct methods we find
         * at each level at the beginning of the list.  After we have processed
         * the whole hierarchy, the highest-level @PostConstruct methods will be
         * first in the list. Processing them from first to last will execute them
         * from the top of the hierarchy down.
         */
        final LinkedList<Method> postConstructMethods = new LinkedList<Method>();
        for (ClassLineageIterator cIT = new ClassLineageIterator(obj.getClass()); cIT.hasNext();) {
            final Class<?> c = cIT.next();
            for (Method m : c.getDeclaredMethods()) {
                /*
                 * The injection manager will already have run a postConstruct
                 * method if the class implements the hk2 PostConstruct interface,
                 * so don't invoke it again if the developer also annotated it
                 * with @PostConstruct.  Ideally this will eventually migrate into
                 * the injection manager implementation.
                 */
                if (m.getAnnotation(PostConstruct.class) != null) {
                    if ((!PostConstruct.class.isAssignableFrom(c)) || !m.getName().equals("postConstruct")) {
                        postConstructMethods.addFirst(m);
                    }
                }
            }
        }
        for (final Method m : postConstructMethods) {
            java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                public java.lang.Object run() throws Exception {
                    if (!m.trySetAccessible()) {
                        throw new InaccessibleObjectException("Unable to make accessible: " + m);
                    }
                    m.invoke(obj);
                    return null;
                }
            });
        }
    }

}
