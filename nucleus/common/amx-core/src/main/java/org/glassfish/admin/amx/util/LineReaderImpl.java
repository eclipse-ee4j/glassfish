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

import java.io.InputStream;
import java.io.InputStreamReader;

/**
Reads a line from the specified input stream, outputs
the prompt to System.out.
 */
public class LineReaderImpl implements LineReader
{
    final InputStreamReader mInputStreamReader;

    public LineReaderImpl(InputStream inputStream)
    {
        mInputStreamReader = new InputStreamReader(inputStream);
    }

    public String readLine(String prompt)
            throws java.io.IOException
    {
        final StringBuffer line = new StringBuffer();

        if (prompt != null)
        {
            System.out.print(prompt);
        }

        while (true)
        {
            final int value = mInputStreamReader.read();
            if (value < 0)
            {
                if (line.length() != 0)
                {
                    // read a line but saw EOF before a newline
                    break;
                }
                return (null);
            }

            final char theChar = (char) value;
            if (theChar == '\n')
            {
                break;
            }

            line.append(theChar);
        }

        return (line.toString().trim());
    }

}




