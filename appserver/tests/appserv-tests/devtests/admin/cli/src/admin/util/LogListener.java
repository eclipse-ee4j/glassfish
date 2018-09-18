/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple class designed to check on new logged messages. When you create it
 * it automatically sets the file pointer to the end of the log-file. Then you
 * can call the NON-BLOCKING method, getLatest() to see the latest stuff written
 * since your last check. Created May 27, 2011
 *
 * @author Byron Nevins
 */
public class LogListener {

    public static void main(String[] args) {
        LogListener ll = new LogListener("domain1");
        System.out.println("LogListener Main");
        System.out.println("length() returned: " + ll.length());

        while (true) {
            String s = ll.getLatest();

            if (s.length() > 0)
                System.out.println("\nLATEST:  [[[" + s + "]]]");
            else
                System.out.print(".");
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex) {
                System.exit(0);
            }
        }
    }

    public LogListener() {
        this("domain1");
    }

    public LogListener(String domainName) {
        if (domainName == null || domainName.isEmpty())
            domainName = "domain1";

        RandomAccessFile di = null; // so reader can be final
        File f = null;

        try {
            // for filelayout change
            f = new File(new File(System.getenv("S1AS_HOME")),
                    "domains/" + domainName + "/server/logs/server.log");
            if(!f.exists())
                f = new File(new File(System.getenv("S1AS_HOME")),
                    "domains/" + domainName + "/logs/server.log");

            di = new RandomAccessFile(f, "rws");
            di.seek(f.length());
        }
        catch (Exception ex) {
            di = null;
            f = null;
        }
        reader = di;
        logfile = f;
    }

    public String getLatest(int secondsToWait) {
        try {
            Thread.sleep(1000 * secondsToWait);
            return getLatest();
        }
        catch (InterruptedException ex) {
            return "";
        }
    }

    public void close() {
        try {
            if (reader != null)
                reader.close();
        }
        catch (IOException ex) {
            // nothing to do
        }
    }

    public String getLatest() {
        try {
            long cur = length();
            long ptr = reader.getFilePointer();
            long numNew = cur - reader.getFilePointer() - 1;

            if (numNew == 0)
                return "";

            if (numNew < 0) {
                if (cur <= 1)
                    reader.seek(0);
                else
                    reader.seek(cur - 1);

                return "";
            }

            byte[] bytes = new byte[(int) numNew];
            reader.read(bytes);
            return new String(bytes);
        }
        catch (IOException ex) {
            return "";
        }
    }

    public final File getFile() {
        return logfile;
    }

    private long length() {
        return logfile.length();
    }
    private final File logfile;
    private final RandomAccessFile reader;
}
