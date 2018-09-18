/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.OutputStream;

import java.util.Map;
import java.util.Set;
import static com.sun.enterprise.universal.process.ProcessUtils.jstackExe;

/**
 * Provide a Jstack getDump of a given PID -- or a concatenated getDump of ALL
 * running JVMs.
 *
 * @author Byron Nevins
 */
public class Jstack {

    private static final Set<Map.Entry<Integer, String>> set;

    public static void main(String[] args) {

        System.out.println("** Got " + set.size() + " process entries");
        if (args.length > 0) {
            int pid = Integer.parseInt(args[0]);
            boolean legal = Jps.isPid(Integer.parseInt(args[0]));
            System.out.printf("Jps.isPid(%s) ==> %b%n", args[0], legal);
            getDump(pid);
        }
        else {
            for (Map.Entry<Integer, String> e : set) {
                System.out.printf("%d %s%n", e.getKey(), e.getValue());
            }
            System.out.println(getDump());
            System.out.println("XXXXX");
        }
    }

    public static String getDump() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Integer, String> e : set) {
            int pid = e.getKey();
            sb.append(prepend(pid, e.getValue()));
            sb.append(getDump(pid));
        }
        return sb.toString();
    }

    public static String getDump(int pid) {
        try {
            if (jstackExe == null) {
                return "";
            }
            ProcessManager pm = new ProcessManager(jstackExe.getPath(), "-l", "" + pid);
            pm.setEcho(false);
            pm.execute();
            return pm.getStdout();
        }
        catch (Exception e) {
            return "";
        }
    }

    private static String prepend(int pid, String name) {
        return "-------    DUMPING JSTACK FOR PID= " + pid + ", name = " + name + " -------\n";
    }

    static {
        set = Jps.getProcessTable().entrySet();
    }
}
