/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.launcher;

import java.util.Map;

/**
 *
 * @author bnevins
 */
class RespawnInfo {

    private final String classname;
    private final String classpath;
    private final String modulepath;
    private final String[] args;

    private static final String PREFIX = "-asadmin-";
    private static final String SEPARATOR = ",,,";

    RespawnInfo(String cn, String modulepath, String cp, String[] args) {
        this.classname = cn;
        this.classpath = cp;
        this.modulepath = modulepath;

        if (args == null) {
            args = new String[0];
        }

        this.args = args;
    }

    void put(Map<String, String> map) throws GFLauncherException {
        validate();
        map.put(PREFIX + "classname", classname);
        map.put(PREFIX + "classpath", classpath);
        map.put(PREFIX + "modulepath", modulepath);
        putArgs(map);
    }

    private void validate() throws GFLauncherException {
        if (!ok(classname)) {
            throw new GFLauncherException("respawninfo.empty", "classname");
        }
        if (!ok(classpath)) {
            throw new GFLauncherException("respawninfo.empty", "classpath");
            // args are idiot-proof
        }
    }

    private void putArgs(Map<String, String> map) throws GFLauncherException {
        int numArgs = args.length;
        StringBuilder argLine = new StringBuilder();

        for (int i = 0; i < numArgs; i++) {
            String arg = args[i];

            if (i != 0) {
                argLine.append(SEPARATOR);
            }

            if (arg.indexOf(SEPARATOR) >= 0) {
                // this should not happen. Only the ultra-paranoid programmer would
                // bother checking for it. I guess that's me!
                throw new GFLauncherException("respawninfo.illegalToken", arg, SEPARATOR);
            }
            argLine.append(args[i]);
        }

        map.put(PREFIX + "args", argLine.toString());
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

}
