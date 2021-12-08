/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.gf.ejb.internal;

import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A helper utility to work with EJB classes.
 *
 * @author David Matejcek
 */
class EjbClassUtilities {

    /**
     * @param resourceClass
     * @return values of {@link Local} and {@link Remote} annotations of the class or it's interfaces
     */
    static List<Class<?>> getRemoteAndLocalIfaces(final Class<?> resourceClass) {
        final List<Class<?>> allLocalOrRemoteIfaces = new LinkedList<>();
        if (resourceClass.isAnnotationPresent(Remote.class)) {
            List<Class<?>> asList = Arrays.asList(resourceClass.getAnnotation(Remote.class).value());
            allLocalOrRemoteIfaces.addAll(asList);
        }
        if (resourceClass.isAnnotationPresent(Local.class)) {
            List<Class<?>> list = Arrays.asList(resourceClass.getAnnotation(Local.class).value());
            allLocalOrRemoteIfaces.addAll(list);
        }
        for (Class<?> i : resourceClass.getInterfaces()) {
            if (i.isAnnotationPresent(Remote.class) || i.isAnnotationPresent(Local.class)) {
                allLocalOrRemoteIfaces.add(i);
            }
        }
        if (allLocalOrRemoteIfaces.isEmpty()) {
            for (Class<?> i : resourceClass.getInterfaces()) {
                if (isAcceptableLocalInterface(i)) {
                    allLocalOrRemoteIfaces.add(i);
                }
            }
        }
        return allLocalOrRemoteIfaces;
    }


    /**
     * @param iface
     * @return true if the interface is NOT of the jakarta.ejb package and is neither equal to
     *         {@link Serializable} nor {@link Externalizable}.
     */
    private static boolean isAcceptableLocalInterface(final Class<?> iface) {
        if (Stateless.class.getPackage().getName().equals(iface.getPackage().getName())) {
            return false;
        }
        return !Serializable.class.equals(iface) && !Externalizable.class.equals(iface);
    }
}
