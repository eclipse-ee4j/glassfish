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

package com.sun.s1asdev.ejb.ejb30.hello.session.client;

import java.io.*;
import java.util.*;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session.*;

public class Client {
    private static boolean afterInj = false;

    public static void main (String[] args) {

        System.out.println("ejb-ejb30-hello-session");
        Client client = new Client(args);
        client.doTest();
    }

    public Client (String[] args) {
    }

    private static @EJB Sful sful;
    private static @EJB Sless sless;

    public void doTest() {

        try {

            System.out.println("invoking stateful");
            sful.hello();

            System.out.println("invoking stateless");
            sless.hello();

            if (!afterInj) {
                System.exit(-1);
            } else {
                System.out.println("after injection check");
            }

            System.out.println("test complete");

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

            return;
    }

    @PostConstruct
    private static void afterInjection() {
        afterInj = true;
    }

}

