/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * @version 1.00 April 1, 2000
 * @author Byron Nevins
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.io.*;


public class Tee extends PrintStream
{

    /////////////////////////////////////////////////////////////////

    private Tee(PrintStream ps)
    {
        super(ps);
    }


    /////////////////////////////////////////////////////////////////

    // Starts copying stdout and
    //stderr to the file f.
    public static void start(String f) throws IOException
    {
        // Save old settings.
        oldStdout = System.out;
        //oldStderr = System.err;

        // Create/Open logfile.
        logfile = new PrintStream(new BufferedOutputStream(new FileOutputStream(f)));

        // Start redirecting the output.
        System.setOut(new Tee(System.out));
        //System.setErr(new Tee(System.err));
    }


    /////////////////////////////////////////////////////////////////

    // Restores the original settings.
    public static void stop()
    {
        System.setOut(oldStdout);
        //System.setErr(oldStderr);

        try
        {
            logfile.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /////////////////////////////////////////////////////////////////

    // PrintStream override.
    public void write(int b)
    {
        try
        {
            logfile.write(b);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            setError();
        }
        super.write(b);
    }

    /////////////////////////////////////////////////////////////////

    // PrintStream override.
    public void write(byte buf[], int off, int len)
    {
        try
        {
            logfile.write(buf, off, len);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            setError();
        }
        super.write(buf, off, len);
    }

    /////////////////////////////////////////////////////////////////

    static OutputStream logfile;
    static PrintStream oldStdout;
    //static PrintStream oldStderr;

    /////////////////////////////////////////////////////////////////

    public static void main(String[] args)
    {
        try
        {
            // Start capturing characters
            //into the log file.
            Tee.start("log.txt");

            // Test it.
            System.out.println(
            "Here's is some stuff to stdout.");
            System.err.println(
            "Here's is some stuff to stderr.");
            System.out.println(
            "Let's throw an exception...");
            new Exception().printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Stop capturing characters
            //into the log file
            // and restore old setup.
            Tee.stop();
        }
    }
}
