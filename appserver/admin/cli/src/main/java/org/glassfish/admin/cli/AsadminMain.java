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

package org.glassfish.admin.cli;

import com.sun.enterprise.admin.cli.AdminMain;
import com.sun.enterprise.admin.cli.Environment;
import com.sun.enterprise.admin.remote.Metrix;

/**
 * The asadmin main program.
 */
public class AsadminMain extends AdminMain {


    public static void main(String[] args) {
//        Metrix.event("START");
        Environment.setPrefix("AS_ADMIN_");
        Environment.setShortPrefix("AS_");
        int code = new AsadminMain().doMain(args);
//        Metrix.event("DONE");
//        System.out.println("METRIX:");
//        System.out.println(Metrix.getInstance().toString());
        System.exit(code);
    }

    protected String getCommandName() {
        return "asadmin";
    }
}
