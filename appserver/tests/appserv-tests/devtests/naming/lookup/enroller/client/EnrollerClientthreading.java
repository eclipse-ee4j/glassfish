/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.loadbalancing.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class EnrollerClientthreading extends Thread{

    private static  SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static int MAXTHREADS = 100;
    public static int count = 0;
    public static String ctxFactory;

    public static void main(String[] args) {

        ctxFactory = args[0];
        System.out.println("Using " + ctxFactory);
        for (int i = 0; i < 300; i++) {
            new EnrollerClientthreading().start();
        }
    }

    public void run() {
        try {
            Properties env = new Properties();
            env.put("java.naming.factory.initial", ctxFactory);
            InitialContext ctx = new InitialContext(env);

            Object objref = ctx.lookup("ejb/MyStudent");
            System.out.println("Thread #" + ++count + " looked up...ejb/MyStudent");

            StudentHome sHome =
              (StudentHome) PortableRemoteObject.narrow(objref,
                                                        StudentHome.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
