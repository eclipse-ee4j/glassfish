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

package com.sun.s1asdev.deployment.ejb30.ear.xmloverride.client;

import java.io.*;
import java.net.*;
import java.util.*;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import com.sun.s1asdev.deployment.ejb30.ear.xmloverride.*;

public class Client {
    public static void main (String[] args) {
        System.out.println("deployment-ejb30-ear-xmloverride");
        Client client = new Client();
        client.doTest();
    }

    private static @EJB Sless sless;
    private static @EJB Sful sful;

    public void doTest() {

        try {

            System.out.println("invoking stateless");
            try {
                System.out.println(sless.hello());
                System.exit(-1);
            } catch(Exception ex) {
                System.out.println("Expected failure from sless.hello()");
            }

            sless.goodMorning();

            try {
                sless.goodBye();
                System.exit(-1);
            } catch(EJBException ex) {
                System.out.println("Expected failure from sless.goodBye()");
            }

            System.out.println("invoking stateful");
            System.out.println(sful.hello());
            System.out.println(sful.goodNight("everybody"));
            System.out.println(sful.goodNight("everybody", "see you tomorrow"));
            try {
                sful.bye();
                System.exit(-1);
            } catch(Exception ex) {
                System.out.println("Expected failure from sful.bye()");
            }


            System.out.println("test complete");

        } catch(Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

            return;
    }
}
