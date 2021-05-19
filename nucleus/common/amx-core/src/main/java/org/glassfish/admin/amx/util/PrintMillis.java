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
Useful for printing elapsed timings out. Example:

<code>
final PrintMillis        timer        = new PrintMillis();
...
timer.println( "start" );
...
timer.println( "middle" );
...
...
timer.println( "end" );
</code>
 */
public class PrintMillis
{
    private long mLast = System.currentTimeMillis();

    public PrintMillis()
    {
        mLast = System.currentTimeMillis();
    }

    /**
    Print out the milliseconds that have elapsed since the last call.
     */
    public void println(String msg)
    {
        final long elapsed = System.currentTimeMillis() - mLast;

        // this printing to System.out is BY DESIGN, so leave it.
        System.out.println((msg == null ? "" : msg) + ": " + elapsed);

        mLast = System.currentTimeMillis();
    }

}
