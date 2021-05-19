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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
Utilities for working with sets using JDK 1.5 generics.
 */
public final class SetUtil
{
    private SetUtil()
    {
        // disallow instantiation
    }

    public static <T> T getSingleton(final Set<T> s)
    {
        if (s.size() != 1)
        {
            throw new IllegalArgumentException(s.toString());
        }
        return (s.iterator().next());
    }

    public static <T> void addArray(
            final Set<T> set,
            final T[] array)
    {
        for (final T item : array)
        {
            set.add(item);
        }
    }


    public static <T> Set<T> newSet()
    {
        return new HashSet<T>();
    }


    public static <T> Set<T> newSet(final Collection<T> c)
    {
        final HashSet<T> set = new HashSet<T>();

        set.addAll(c);

        return (set);
    }

    /**
    Create a new Set with one member.
     */
    public static <T> Set<T> newSet(final T item)
    {
        final Set<T> set = new HashSet<T>();
        set.add(item);

        return (set);
    }

    /**
    Create a new Set containing all members of another.
    The returned Set is always a HashSet.
     */
    public static <T> HashSet<T> copySet(final Set<? extends T> s1)
    {
        final HashSet<T> set = new HashSet<T>();

        set.addAll(s1);

        return (set);
    }

    public static <T> Set<? extends T> newSet(
            final T m1,
            final T m2)
    {
        final HashSet<T> set = new HashSet<T>();

        set.add(m1);
        set.add(m2);

        return (set);
    }

    /*
    public static <T> Set<T>
    newSet(
    final T m1,
    final T m2,
    final T m3 )
    {
    final HashSet<T>        set        = new HashSet<T>();

    set.add( m1 );
    set.add( m2 );
    set.add( m3 );

    return( set );
    }
     */
    public static <T> Set<T> newSet(
            final T m1,
            final T m2,
            final T m3,
            final T m4)
    {
        final HashSet<T> set = new HashSet<T>();

        set.add(m1);
        set.add(m2);
        set.add(m3);
        set.add(m4);

        return (set);
    }

    /**
    Create a new Set containing all array elements.
     */
    public static <T> Set<T> newSet(final T[] objects)
    {
        return (newSet(objects, 0, objects.length));
    }

    public static <T, TT extends T> Set<T> newSet(final Set<T> s1, final Set<TT> s2)
    {
        final Set<T> both = new HashSet<T>();
        both.addAll(s1);
        both.addAll(s2);

        return both;
    }

    /**
    Create a new Set containing all array elements.
     */
    public static <T> Set<T> newSet(
            final T[] objects,
            final int startIndex,
            final int numItems)
    {
        final Set<T> set = new HashSet<T>();

        for (int i = 0; i < numItems; ++i)
        {
            set.add(objects[startIndex + i]);
        }

        return (set);
    }

    /**
    Convert a Set to a String[]
     */
    public static String[] toStringArray(final Set<?> s)
    {
        final String[] strings = new String[s.size()];

        int i = 0;
        for (final Object o : s)
        {
            strings[i] = "" + o;
            ++i;
        }

        return (strings);
    }

    public static String[] toSortedStringArray(final Set<?> s)
    {
        final String[] strings = toStringArray(s);

        Arrays.sort(strings);

        return (strings);
    }

    public static Set<String> newStringSet(final String... args)
    {
        return newUnmodifiableSet(args);
    }

    public static <T> Set<T> newUnmodifiableSet(final T... args)
    {
        final Set<T> set = new HashSet<T>();

        for (final T s : args)
        {
            set.add(s);
        }
        return set;
    }

    public static Set<String> newUnmodifiableStringSet(final String... args)
    {
        return Collections.unmodifiableSet(newStringSet(args));
    }

    /*
    public static Set<String>
    newStringSet( final Object... args)
    {
    final Set<String>   set   = new HashSet<String>();

    for( final Object o : args )
    {
    set.add( o == null ? null : "" + o );
    }
    return set;
    }
     */
    public static <T> Set<T> newTypedSet(final T... args)
    {
        final Set<T> set = new HashSet<T>();

        for (final T o : args)
        {
            set.add(o);
        }
        return set;
    }

    /**
    Create a new Set with one member.  Additional items
    may be added.
     */
    public static <T> Set<T> newSingletonSet(final T m1)
    {
        final Set<T> set = new HashSet<T>();

        set.add(m1);

        return (set);
    }

    /**
    Return a new Set of all items in both set1 and set2.
     */
    public static <T> Set<T> intersectSets(
            final Set<T> set1,
            final Set<T> set2)
    {
        final Set<T> result = newSet(set1);
        result.retainAll(set2);

        return (result);
    }

    /**
    Return a new Set of all items in set1 not in set2.
     */
    public static <T> Set<T> removeSet(
            final Set<T> set1,
            final Set<T> set2)
    {
        final Set<T> result = new HashSet<T>();
        result.addAll(set1);
        result.removeAll(set2);

        return (result);
    }

    /**
    Return a new Set of all items not common to both sets.
     */
    public static <T> Set<T> newNotCommonSet(
            final Set<T> set1,
            final Set<T> set2)
    {
        final Set<T> result = newSet(set1, set2);
        final Set<T> common = intersectSets(set1, set2);

        result.removeAll(common);

        return (result);
    }

    public static String findIgnoreCase(final Set<String> candidates, final String target)
    {
        String match = null;
        // case-insensitive search
        for (final String candidate : candidates)
        {
            if (candidate.equalsIgnoreCase(target))
            {
                match = candidate;
                break;
            }
        }
        return match;
    }

}























