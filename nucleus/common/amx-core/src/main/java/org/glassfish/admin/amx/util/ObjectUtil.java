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

/**
Provides a variety of useful utilities for computing hashCode().
 */
public final class ObjectUtil
{
    private ObjectUtil()
    {
        // disallow instantiation
    }

    public static int hashCode(final boolean value)
    {
        return value ? 1 : 0;
    }

    public static int hashCode(final Object... items)
    {
        int result = 0;

        for (final Object item : items)
        {
            result ^= hashCode(item);
        }
        return result;
    }

    public static int hashCode(final Object o)
    {
        return o == null ? 0 : o.hashCode();
    }

    public static int hashCode(final long value)
    {
        return (int) value ^ (int) (value >> 32);
    }

    public static int hashCode(final double value)
    {
        return Double.valueOf(value).hashCode();
    }

    public static boolean equals(final Object s1, final Object s2)
    {
        boolean equals;

        if (s1 == s2)
        {
            equals = true;
        }
        else if (s1 != null)
        {
            equals = s1.equals(s2);
        }
        else
        {
            // s1 is null and s2 isn't
            equals = false;
        }

        return equals;
    }

}

