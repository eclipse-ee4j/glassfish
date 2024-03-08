/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util;

import java.util.ArrayList;

/**
 * Useful utilities for Exceptions
 * Subset of methods copied from org.glassfish.admin.amx.util
 */
public final class ExceptionUtil
{
    private ExceptionUtil()
    {
        // disallow instantiation
    }

    /**
    Get the chain of exceptions via getCause(). The first element is the
    Exception passed.

    @param start        the Exception to traverse
    @return                a Throwable[] or an Exception[] as appropriate
     */
    public static Throwable[] getCauses(final Throwable start)
    {
        final ArrayList<Throwable> list = new ArrayList<Throwable>();

        boolean haveNonException = false;

        Throwable t = start;
        while (t != null)
        {
            list.add(t);

            if (!(t instanceof Exception))
            {
                haveNonException = true;
            }

            final Throwable temp = t.getCause();
            if (temp == null)
            {
                break;
            }
            t = temp;
        }

        final Throwable[] results = haveNonException ? new Throwable[list.size()] : new Exception[list.size()];

        list.toArray(results);

        return (results);
    }

    /**
    Get the original troublemaker.

    @param e        the Exception to dig into
    @return                the original Throwable that started the problem
     */
    public static Throwable getRootCause(final Throwable e)
    {
        final Throwable[] causes = getCauses(e);

        return (causes[causes.length - 1]);
    }

    /**
    Get the stack trace as a String.

    @param t        the Throwabe whose stack trace should be gotten
    @return                a String containing the stack trace
     */
    public static String getStackTrace(Throwable t)
    {
        final StringBuffer buf = new StringBuffer();
        final StackTraceElement[] elems = t.getStackTrace();

        for (int i = 0; i < elems.length; ++i)
        {
            buf.append(elems[i]);
            buf.append("\n");
        }


        return (buf.toString());
    }

}

