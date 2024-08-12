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
import java.util.Collection;
import java.util.List;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

/**
Various helper utilities for Collections.
 */
public final class CollectionUtil
{
    private CollectionUtil()
    {
        // disallow instantiation
    }

    public static <T> void addAll(Collection<T> c, T[] items)
    {
        for (final T item : items)
        {
            c.add(item);
        }
    }

    /**
    @return a String
     */
    public static <T> String toString(
            final Collection<T> c,
            final String delim)
    {
        final String[] strings = toStringArray(c);
        //Arrays.sort( strings );

        return StringUtil.toString(delim, (Object[]) strings);
    }

    /**
    @return String[]
     */
    public static <T> String[] toStringArray(final Collection<T> c)
    {
        final String[] strings = new String[c.size()];

        int i = 0;
        for (final Object o : c)
        {
            strings[i] = SmartStringifier.toString(o);
            ++i;
        }

        return (strings);
    }

    public static <T> List<String> toStringList(final Collection<T> c)
    {
        final String[] strings = toStringArray(c);

        final List<String> list = new ArrayList<String>();
        for (final String s : strings)
        {
            list.add(s);
        }
        return list;
    }

    public static <T> T getSingleton(final Collection<T> s)
    {
        if (s.size() != 1)
        {
            throw new IllegalArgumentException();
        }
        return (s.iterator().next());
    }

    /**
    Add all items in an array to a set.
     */
    public static <T> void addArray(
            final Collection<T> c,
            final T[] array)
    {
        for (int i = 0; i < array.length; ++i)
        {
            c.add(array[i]);
        }
    }

    /**
    @param c        the Collection
    @param elementClass         the type of the element, must be non-primitive
    @return array of <elementClass>[] elements
     */
    public static <T> T[] toArray(
            final Collection<? extends T> c,
            final Class<T> elementClass)
    {
        final T[] items = ArrayUtil.newArray(elementClass, c.size());

        c.toArray(items);

        return items;
    }

    /**
    @return true if all elements are String, and there is at least one element
     */
    public static boolean isAllStrings(final Collection<?> c)
    {
        return IteratorUtil.getUniformClass(c.iterator()) == String.class;
    }

}













