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

import java.lang.reflect.Constructor;
import java.util.Set;

/**
Maps a Throwable to another one in order to avoid the transfer
of non-standard (proprietary) Exception types, which could result in
ClassNotFoundException on remote clients.
<p>
Any Throwable which either is, or contains,
a Throwable which is not in the allowed packages is converted.
 */
public final class ThrowableMapper
{
    final Throwable mOriginal;

    /**
    By default, any Throwable whose package does not start with one
    of these packages must be mapped to something standard.
     */
    private final static Set<String> OK_PACKAGES =
            SetUtil.newUnmodifiableStringSet("java.", "javax.");

    public ThrowableMapper(final Throwable t)
    {
        mOriginal = t;
    }

    private static boolean shouldMap(final Throwable t)
    {
        final String tClass = t.getClass().getName();

        boolean shouldMap = true;

        for (final String prefix : OK_PACKAGES)
        {
            if (tClass.startsWith(prefix))
            {
                shouldMap = false;
                break;
            }
        }

        return (shouldMap);
    }

    public static Throwable map(final Throwable t)
    {
        Throwable result = t;

        if (t != null)
        {
            final Throwable tCause = t.getCause();
            final Throwable tCauseMapped = map(tCause);

            // if either this Exception or its cause needs/was mapped,
            // then we must form a new Exception

            if (shouldMap(t))
            {
                // the Throwable itself needs to be mapped
                final String msg = t.getMessage();

                if (t instanceof Error)
                {
                    result = new Error(msg, tCauseMapped);
                }
                else if (t instanceof RuntimeException)
                {
                    result = new RuntimeException(msg, tCauseMapped);
                }
                else if (t instanceof Exception)
                {
                    result = new Exception(msg, tCauseMapped);
                }
                else
                {
                    result = new Throwable(msg, tCauseMapped);
                }

                result.setStackTrace(t.getStackTrace());
            }
            else if (tCauseMapped != tCause)
            {
                // the Throwable doesn't need mapping, but its Cause does
                // create a Throwable of the same class, and insert its
                // cause and stack trace.
                try
                {
                    final Constructor<? extends Throwable> c =
                            t.getClass().getConstructor(String.class, Throwable.class);
                    result = c.newInstance(t.getMessage(), tCauseMapped);
                }
                catch (final Throwable t1)
                {
                    try
                    {
                        final Constructor<? extends Throwable> c =
                                t.getClass().getConstructor(String.class);
                        result = c.newInstance(t.getMessage());
                        result.initCause(tCauseMapped);
                    }
                    catch (final Throwable t2)
                    {
                        result = new Throwable(t.getMessage(), tCauseMapped);
                    }
                }

                result.setStackTrace(tCause.getStackTrace());
            }
            else
            {
                result = t;
            }
        }

        return (result);
    }

    /**
    Map the original Throwable to one that is non-proprietary (standard).
    Possible results include java.lang.Exception, java.lang.RuntimeException,
    java.lang.Error.  The original stack trace and exception chain is
    preserved, each element in that chain being mapped if necessary.

    @return a Throwable which uses only standard classes
     */
    public Throwable map()
    {
        return (map(mOriginal));
    }

}








