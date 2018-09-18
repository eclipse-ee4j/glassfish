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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
The API that should be used to output from a Cmd running within the framework.
 */
public final class FileUtils
{
    private FileUtils()
    {
    }

    public static String fileToString(final File src)
            throws FileNotFoundException, IOException
    {
        return fileToString(src, 32 * 1024);
    }

    public static String fileToString(final File src, final int readBufferSize)
            throws FileNotFoundException, IOException
    {
        final long length = src.length();
        if (length > 1024 * 1024 * 1024)
        {
            throw new IllegalArgumentException();
        }

        final char[] readBuffer = new char[readBufferSize];

        final StringBuilder result = new StringBuilder((int) length);
        FileReader in = null;

        try
        {
            in = new FileReader(src);
            while (true)
            {
                final int numRead = in.read(readBuffer, 0, readBufferSize);
                if (numRead < 0)
                {
                    break;
                }

                result.append(readBuffer, 0, numRead);
            }
        }
        finally
        {
            if (in != null) {
                in.close();
            }
        }

        return (result.toString());
    }

    public static void stringToFile(final String s, final File dest)
            throws IOException
    {
        final FileWriter out = new FileWriter(dest);

        try
        {
            out.write(s);
        }
        finally
        {
            out.close();
        }
    }

};


