/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.deployment.noappxml.client;

import jakarta.ejb.EJB;
import com.sun.s1asdev.deployment.noappxml.ejb.Sful;
import com.sun.s1asdev.deployment.noappxml.ejb.Sless;

public class Client {

    public static void main (String[] args) {
        Client client = new Client(args);
        client.doTest();
    }

    public Client (String[] args) {}

    @EJB
    private static Sful sful;

    @EJB
    private static Sless sless;

    public void doTest() {
        try {

            System.err.println("invoking stateful");
            sful.hello();

            System.err.println("invoking stateless");
            sless.hello();

            pass();
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }

            return;
    }

    private void pass() {
        System.err.println("PASSED: descriptor_free_zone/ear/no_appxml");
        System.exit(0);
    }

    private void fail() {
        System.err.println("FAILED: descriptor_free_zone/ear/no_appxml");
        System.exit(-1);
    }
}
