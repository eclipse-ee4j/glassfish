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

package com.sun.enterprise.universal.process;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;


///////////////////////////////////////////////////////////////////////////

class ProcessStreamDrainerWorker implements Runnable
{
    ProcessStreamDrainerWorker(InputStream in, PrintStream Redirect, boolean save)
    {
        if(in == null)
            throw new NullPointerException("InputStream argument was null.");

        reader = new BufferedInputStream(in);
        redirect = Redirect;

        if(save) {
            sb = new StringBuilder();
        }
    }

    public void run()
    {
        if(reader == null)
            return;

        try
        {
            int count = 0;
            byte[] buffer = new byte[4096];

            while ((count = reader.read(buffer)) != -1)
            {
                if(redirect != null)
                    redirect.write(buffer, 0, count);

               if(sb != null)
                   sb.append(new String(buffer, 0, count));
            }
        }
        catch (IOException e)
        {
        }
    }

    String getString() {
        if(sb != null)
            return sb.toString();
        else
            return "";
    }

    private final   BufferedInputStream reader;
    private final   PrintStream         redirect;
    private         StringBuilder       sb;
}
