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

import org.glassfish.admin.amx.util.Output;
import org.glassfish.admin.amx.util.StringUtil;

/**
Convenient wrapper around {@link AMXDebug}.
Can be made non-final if necessary; declared as 'final' until needed.
Note that the "-DAMX-DEBUG=true" must be set in order to see any output.
 */
public final class AMXDebugHelper
{
    private final Output mOutput;

    private final String mName;

    volatile boolean mEchoToStdOut;

    public AMXDebugHelper(final String name)
    {
        mOutput = AMXDebug.getInstance().getOutput(name);
        mName = name;

        mEchoToStdOut = false;
    }

    public AMXDebugHelper()
    {
        this("debug");
    }

    public boolean getEchoToStdOut(final boolean echo)
    {
        return mEchoToStdOut;
    }

    public void setEchoToStdOut(final boolean echo)
    {
        mEchoToStdOut = echo;
    }

    public boolean getDebug()
    {
        return AMXDebug.getInstance().getDebug(mName);
    }

    public void setDebug(final boolean debug)
    {
        AMXDebug.getInstance().setDebug(mName, debug);
    }

    private void printlnWithTime(final String s)
    {
        final long now = System.currentTimeMillis();
        final String msg = now + ": " + s;

        mOutput.println(msg);
        if (mEchoToStdOut)
        {
            System.out.println(msg);
        }
    }

    public void println(final Object o)
    {
        if (getDebug())
        {
            printlnWithTime("" + StringUtil.toString(o));
        }
    }

    public void println()
    {
        println("");
    }

    /**
    This form is preferred for multiple arguments so that String concatenation
    can be avoided when no message will actually be output. For example, use:
    <pre>println( a, b, c)</pre>
    instead of:
    <pre>println( a + b + c )</pre>
     */
    public void println(final Object... items)
    {
        if (getDebug() && items != null)
        {
            String msg = null;

            if (items.length == 1)
            {
                msg = StringUtil.toString(items[0]);
            }
            else
            {
                msg = StringUtil.toString("", items);
            }
            printlnWithTime(msg);
        }
    }

    public void dumpStack(final String msg)
    {
        if (getDebug())
        {
            println();
            println("STACK DUMP FOLLOWS: " + msg);
            println(StringUtil.toString(new Exception("not a real exception")));
            println();
        }
    }

}




