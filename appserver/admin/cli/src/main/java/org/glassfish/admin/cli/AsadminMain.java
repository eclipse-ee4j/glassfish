/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import java.io.File;
import java.util.Set;

/**
 * The asadmin main program.
 */
public class AsadminMain extends AdminMain {


    public static void main(String[] args) {
//        Metrix.event("START");
        Environment.setPrefix("AS_ADMIN_");
        Environment.setShortPrefix("AS_");

        AsadminMain main = new AsadminMain();
        int code = main.doMain(args);
//        Metrix.event("DONE");
//        System.out.println("METRIX:");
//        System.out.println(Metrix.getInstance().toString());
        System.exit(code);
    }


    @Override
    protected String getCommandName() {
        return "asadmin";
    }


    @Override
    protected Set<File> getExtensions() {
        final Set<File> locations = super.getExtensions();
        // FIXME: Identify just modules containing admin commands and their dependencies.
        //        Then split those jar files to server and cli part.
        //        Then cli parts should be under lib/admin
        //        And server parts under modules.
        //        They should not depend on each other.
        final File modules = getInstallRoot().resolve("modules").toFile();
        locations.add(modules);
        return locations;
    }
}
