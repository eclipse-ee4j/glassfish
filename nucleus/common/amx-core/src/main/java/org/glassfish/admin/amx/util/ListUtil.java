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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public final class ListUtil
{
    private ListUtil()
    {
        // disallow instantiation
    }

    /**
    Add all items in an array to a list.
     */
    public static <T> void addArray(
            final List<T> list,
            final T[] array)
    {
        for (int i = 0; i < array.length; ++i)
        {
            list.add(array[i]);
        }
    }

    public static <T> List<T> newList()
    {
        return new ArrayList<T>();
    }

    public static List<String> asStringList(final Object value)
    {
        List<String> values = null;

        if (value instanceof String)
        {
            values = Collections.singletonList((String) value);
        }
        else if (value instanceof String[])
        {
            values = ListUtil.newListFromArray((String[]) value);
        }
        else if (value instanceof List)
        {
            final List<String> checkedList = TypeCast.checkList(TypeCast.asList(value), String.class);
            values = new ArrayList<String>(checkedList);
        }
        else
        {
            throw new IllegalArgumentException("" + value);
        }

        return values;
    }

    /**
    Convert a List to a String[]
     */
    public static String[] toStringArray(final List<?> list)
    {
        final String[] names = new String[list.size()];

        int i = 0;
        for (final Object o : list)
        {
            names[i] = "" + o;
            ++i;
        }

        return (names);
    }

    /**
    Create a new List from a Collection
     */
    public static <T> List<T> newListFromCollection(final Collection<T> c)
    {
        final List<T> list = new ArrayList<T>();

        list.addAll(c);

        return (list);
    }

    public static <T> List<T> newList(final Enumeration<T> e)
    {
        final List<T> items = new ArrayList<T>();
        while (e.hasMoreElements())
        {
            items.add(e.nextElement());
        }
        return items;
    }

    /**
    Create a new List from a Collection
     */
    public static <T> List<? extends T> newListFromIterator(final Iterator<? extends T> iter)
    {
        final List<T> list = new ArrayList<T>();

        while (iter.hasNext())
        {
            list.add(iter.next());
        }

        return (list);
    }

    /**
    Create a new List with one member.
     */
    public static <T> List<T> newList(T m1)
    {
        final List<T> list = new ArrayList<T>();

        list.add(m1);

        return (list);
    }

    /**
    Create a new List with two members.
     */
    public static <T> List<T> newList(
            final T m1,
            final T m2)
    {
        final List<T> list = new ArrayList<T>();

        list.add(m1);
        list.add(m2);

        return (list);
    }

    /**
    Create a new List with three members.
     */
    public static <T> List<T> newList(
            final T m1,
            final T m2,
            final T m3)
    {
        final List<T> list = new ArrayList<T>();

        list.add(m1);
        list.add(m2);
        list.add(m3);

        return (list);
    }

    /**
    Create a new List with four members.
     */
    public static <T> List<T> newList(
            final T m1,
            final T m2,
            final T m3,
            final T m4)
    {
        final List<T> list = new ArrayList<T>();

        list.add(m1);
        list.add(m2);
        list.add(m3);
        list.add(m4);

        return (list);
    }

    /**
    Create a new List with four members.
     */
    public static <T> List<T> newList(
            final T m1,
            final T m2,
            final T m3,
            final T m4,
            final T m5)
    {
        final List<T> list = new ArrayList<T>();

        list.add(m1);
        list.add(m2);
        list.add(m3);
        list.add(m4);
        list.add(m5);

        return (list);
    }

    public static <T> List<T> newListFromArray(final T[] items)
    {
        final List<T> list = new ArrayList<T>();

        for (int i = 0; i < items.length; ++i)
        {
            list.add(items[i]);
        }

        return (list);
    }


    /**
    Return a new List in reverse order. Because the List is new,
    it works on any list, modifiable or not.
     */
    public static <T> List<T> reverse(final List<T> list)
    {
        final int numItems = list.size();
        final List<T> result = new ArrayList<T>(numItems);

        for (int i = 0; i < numItems; ++i)
        {
            result.add(list.get(numItems - i - 1));
        }

        return (result);
    }

}

