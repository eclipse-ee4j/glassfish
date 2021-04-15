/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
* Remove this class
*/
package com.sun.enterprise.admin.remote;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mmares
 */
public class Metrix {

    public static class Stat {
        public final long timestamp;
        public final String message;
        public final String param;

        public Stat(String message, String param) {
            this.timestamp = System.currentTimeMillis();
            this.message = message;
            this.param = param;
        }

    }

    private static final Metrix instance = new Metrix();
    private static final long timestamp = System.currentTimeMillis();

    private List<Stat> list = new ArrayList<Metrix.Stat>(64);

    private Metrix() {
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("duration, delta, event\n");
        long lastTS = timestamp;
        for (Stat stat : list) {
            res.append(stat.timestamp - timestamp).append(", ");
            res.append(stat.timestamp - lastTS).append(", ");
            res.append(stat.message);
            if (stat.param != null) {
                res.append(" - ").append(stat.param);
            }
            res.append('\n');
            lastTS = stat.timestamp;
        }
        return res.toString();
    }

    // ---------- Static API

    public static void event(String message) {
        instance.list.add(new Stat(message, null));
    }

    public static void event(String message, String param) {
        instance.list.add(new Stat(message, null));
    }

    public static Metrix getInstance() {
        return instance;
    }

}
