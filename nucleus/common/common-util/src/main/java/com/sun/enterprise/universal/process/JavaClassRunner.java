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

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.OS;
import java.io.*;
import java.util.*;

/**
 * Very simple initial implementation
 * If it is useful there are plenty of improvements that can be made...
 * @author bnevins
 */
public class JavaClassRunner {
    public JavaClassRunner(String classpath, String[] sysprops, String classname, String[] args) throws IOException{
        if(javaExe == null)
            throw new IOException("Can not find a jvm");

        if(!ok(classname))
            throw new IllegalArgumentException("classname was null");

        List<String> cmdline = new LinkedList<String>();
        cmdline.add(javaExe.getPath());

        if(ok(classpath)) {
            cmdline.add("-cp");
            cmdline.add(classpath);
        }

        if(sysprops != null)
            for(String sysprop : sysprops)
                cmdline.add(sysprop);

        cmdline.add(classname);

        if(args != null)
            for(String arg : args)
                cmdline.add(arg);

        ProcessBuilder pb = new ProcessBuilder(cmdline);
        Process p = pb.start();
        ProcessStreamDrainer.drain(classname, p);
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private static final File javaExe;

    static{
        String javaName = "java";

        if(OS.isWindows())
            javaName = "java.exe";

        final String    javaroot    = System.getProperty("java.home");
        final String    relpath     = "/bin/" + javaName;
        final File      fhere       = new File(javaroot + relpath);
        File            fthere      = new File(javaroot + "/.." + relpath);

        if(fhere.isFile())
            javaExe = SmartFile.sanitize(fhere);
        else if(fthere.isFile())
            javaExe = SmartFile.sanitize(fthere);
        else
            javaExe = null;
    }
}
