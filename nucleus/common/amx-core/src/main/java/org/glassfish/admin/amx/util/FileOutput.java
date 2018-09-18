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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
Directs output to a file. Lazy initialization; the file
is not actually opened until output is sent.
 */
public final class FileOutput implements Output
{
    private volatile PrintStream mOut;

    private final File mFile;

    private final boolean mAppend;

    public FileOutput(final File f)
    {
        this(f, false);
    }

    public FileOutput(final File f, boolean append)
    {
        mOut = null;
        mFile = f;
        mAppend = append;
    }

    private void lazyInit()
    {
        if (mOut == null)
        {
            synchronized (this)
            {
                if (mOut == null)
                {
                    try
                    {
                        if ((!mAppend) && mFile.exists())
                        {
                            if (!mFile.delete()) {
                                throw new Exception("cannot delete file: " + mFile);
                            }
                        }

                        mOut = new PrintStream(new FileOutputStream(mFile, mAppend));
                    }
                    catch (Exception e)
                    {
                        // don't use System.out/err; possible infinite recursion
                        throw new RuntimeException("Can't create file: " + mFile +
                                                   ", exception = " + e);
                    }
                }
            }
        }
    }

    public void print(final Object o)
    {
        lazyInit();
        mOut.print(o.toString());
    }

    public void println(Object o)
    {
        lazyInit();
        mOut.println(o.toString());
    }

    public void printError(final Object o)
    {
        lazyInit();
        println("ERROR: " + o);
    }

    public boolean getDebug()
    {
        lazyInit();
        return (false);
    }

    public void printDebug(final Object o)
    {
        lazyInit();
        println("DEBUG: " + o);
    }

    public void close()
    {
        if (mOut != null)
        {
            try
            {
                mOut.close();
            }
            finally
            {
                mOut = null;
            }
        }
    }

};


