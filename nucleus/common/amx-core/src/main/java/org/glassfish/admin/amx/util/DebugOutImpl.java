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
 */
public class DebugOutImpl implements DebugOut
{
    private final String mID;

    private boolean mDebug;

    private DebugSink mSink;

    public DebugOutImpl(
            final String id,
            final boolean debug,
            final DebugSink sink)
    {
        mID = id;
        mDebug = debug;

        mSink = sink == null ? new DebugSinkImpl(System.out) : sink;
    }

    public DebugOutImpl(
            final String id,
            final boolean debug)
    {
        this(id, debug, null);
    }

    public String getID()
    {
        return mID;
    }

    public boolean getDebug()
    {
        return mDebug;
    }

    public void print(final Object o)
    {
        mSink.print("" + o);
    }

    public void println(Object o)
    {
        mSink.println("" + o);
    }

    public String toString(final Object... args)
    {
        return StringUtil.toString(", ", args);
    }

    public void setDebug(final boolean debug)
    {
        mDebug = debug;
    }

    public void debug(final Object... args)
    {
        if (getDebug())
        {
            mSink.println(toString(args));
        }
    }

    public void debugMethod(
            final String methodName,
            final Object... args)
    {
        if (getDebug())
        {
            debug(methodString(methodName, args));
        }
    }

    public void debugMethod(
            final String msg,
            final String methodName,
            final Object... args)
    {
        if (getDebug())
        {
            debug(methodString(methodName, args) + ": " + msg);
        }
    }

    public static String methodString(
            final String name,
            final Object... args)
    {
        String result = null;

        if (args == null || args.length == 0)
        {
            result = name + "()";
        }
        else
        {
            final String argsString = StringUtil.toString(", ", args);
            result = StringUtil.toString("", name, "(", argsString, ")");
        }

        return result;
    }

}




























