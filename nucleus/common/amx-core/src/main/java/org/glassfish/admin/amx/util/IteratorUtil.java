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

package org.glassfish.admin.amx.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class IteratorUtil
{
    private IteratorUtil()
    {
        // disallow instantiation
    }

    public static <T> Object[] toArray(final Iterator<T> iter)
    {
        final List<T> list = new ArrayList<T>();

        while (iter.hasNext())
        {
            final T elem = iter.next();
            list.add(elem);
        }

        final Object[] result = new Object[list.size()];
        list.toArray(result);

        return (ArrayConversion.specializeArray(result));
    }

    /**
    @param iter
    @return the Class of the elements, or null if all null or different
     */
    public static Class getUniformClass(final Iterator<?> iter)
    {
        Class theClass = null;

        Object next;
        if (iter.hasNext())
        {
            next = iter.next();
            theClass = (next == null) ? null : next.getClass();
        }

        while (iter.hasNext())
        {
            next = iter.next();

            if (next != null && next.getClass() != theClass)
            {
                theClass = null;
                break;
            }
        }

        return (theClass);
    }

    /**
    @param iter
    @param theClass
    @param exactMatch  if true, then subclasses are considered to be different
    @return true if all items are of the same class
     */
    public static <T> boolean isUniformClass(
            final Iterator<?> iter,
            final Class<T> theClass,
            final boolean exactMatch)
    {
        boolean isUniform = true;

        while (iter.hasNext())
        {
            Object next = iter.next();
            final Class nextClass = (next == null) ? null : next.getClass();

            if (nextClass != theClass && nextClass != null)
            {
                if (exactMatch)
                {
                    isUniform = false;
                    break;
                }

                if (!theClass.isAssignableFrom(nextClass))
                {
                    isUniform = false;
                    break;
                }
            }
        }

        return (isUniform);
    }

}

