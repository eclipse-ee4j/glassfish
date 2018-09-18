/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package admin;

import admin.AdminBaseDevTest;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author wnevins
 */
public class Reminders extends AdminBaseDevTest {
    public Reminders() {
    }

    public static void main(String[] args) {
        System.out.println("###################################################");
        System.out.println("###################################################");
        System.out.println("###################################################");
        System.out.println("##############   REMINDERS   ######################");
        System.out.println("HUGE Speedup ==>  use 'asadmin multimode -f cmds.txt' for the set/get commands");
        System.out.println("WHy does delete-domain print 'null' for non-existent domain?");
        System.out.println("###################################################");
        System.out.println("###################################################");
        System.out.println("###################################################");
        System.out.println("###################################################");

        new Reminders().report("Reminded Developer...", true);
    }

    @Override
    public String getTestName() {
        return "Reminders for Test Developers";
    }

    @Override
    protected String getTestDescription() {
        return "Reminders for Test Developers";
    }

    @Override
    public void subrun() {
        stat.printSummary();
    }
}
